import json, collections, io, math, os
from python.items import FrozenDict
import python.utils as utils

class CombatManager:
	def __init__(self, jsonFilePath, iconLocation):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
		self.tacticList = [Tactic(block) for block in json_data["tactics"]]
		self.iconLocation = iconLocation

	def getSquadTactics(self, squad, bare=False):
		squad_tactics = self.getAvailableTactics(squad.command_level)
		if(bare):
			return [tactic.name for tactic in squad_tactics]
		else:
			return squad_tactics

	def getTacticByName(self, tacticName):
		return next((t for t in self.tacticList if t.name == tacticName), None)

	def getAvailableTactics(self, level):
		return [t for t in self.tacticList if t.requirement <= level]

	def getCounterTactic(self, target, listTactics):
		# get a random tactic that counter the target
		return utils.select_random([t for t in listTactics if t.type == target.counter_type])
	
	def getInferiorTactic(self, target, listTactics):
		# get a random tactic that is countered by the target
		return utils.select_random([t for t in listTactics if t.type == target.inferior_type])

	def selectRoundTactic(self, firstLevel, secondLevel, firstPreference=None, secondPreference=None):
		# the round will have attack/counterattack, so there is no distinguish between first and second
		# 10% is random, rest is scaled by the difference between level.
		# create the pref function
		first_preference_fn = lambda tactic: tactic.type == firstPreference if firstPreference else False
		second_preference_fn = lambda tactic: tactic.type == secondPreference if secondPreference else False
		if(utils.roll_percentage(10.0)):
			# randomize both
			return utils.select_with_preference(self.getAvailableTactics(firstLevel), first_preference_fn), utils.select_with_preference(self.getAvailableTactics(secondLevel), second_preference_fn)
		else:
			# scale so minimum(0) is 50%(1.0), 10 is ~83%(5.0), maximum(20) is 90% (9.0)
			# select good/bad tactic depending on the difference in level
			level_diff = float(abs(firstLevel - secondLevel))
			scaled_level_diff = 1.0 + level_diff / 2.5
			better_win = utils.roll_percentage(100.0 / (scaled_level_diff+1.0) * scaled_level_diff)
			if(better_win == (firstLevel > secondLevel)):
				# first got good one, second got bad
				first_tactic = utils.select_with_preference(self.getAvailableTactics(firstLevel), first_preference_fn)
				second_tactic = self.getInferiorTactic(first_tactic, self.getAvailableTactics(secondLevel))
			else:
				# second got good one, first got bad
				second_tactic = utils.select_with_preference(self.getAvailableTactics(secondLevel), second_preference_fn)
				first_tactic = self.getInferiorTactic(second_tactic, self.getAvailableTactics(firstLevel))
			return first_tactic, second_tactic
	
	def getOpposingSquadTacticBonus(self, attacker, defender, forceTacticDict=None, voxStreamFn=None):
		atk_com = attacker.command_level
		def_com = defender.command_level
		# find the auto_generated
		atk_tactic, def_tactic = self.selectRoundTactic(atk_com, def_com)
		# replace with forced
		if(forceTacticDict):
			atk_tactic = forceTacticDict.get(attacker, atk_tactic)
			def_tactic = forceTacticDict.get(attacker, def_tactic)
		return self.calculateTacticBonus(atk_tactic, def_tactic)

	def calculateTacticBonus(self, attackerTactic, defenderTactic):
		atk_bonus = attackerTactic.coefficients[defenderTactic.type][0]
		def_bonus = defenderTactic.coefficients[attackerTactic.type][1]
		return atk_bonus, def_bonus

	def getBadgePath(self, badge):
		return os.path.join(self.iconLocation, "{:s}.png".format(badge))

class Tactic(collections.namedtuple("Tactic", ["name", "type", "coefficients", "requirement"])):
	TACTIC_TYPES = ("heavy", "light", "feint")
	
	def __new__(_cls, jsonData):
		# convert the atk-def field to the coefficients
		tacticAtkDict = jsonData.pop("atk")
		tacticDefDict = jsonData.pop("def")
		jsonData["coefficients"] = FrozenDict({k:(tacticAtkDict[k], tacticDefDict[k]) for k in Tactic.TACTIC_TYPES})
		# initiate and add the _idx
		self = super(Tactic, _cls).__new__(_cls, **jsonData)
		self._idx = Tactic.TACTIC_TYPES.index(self.type)
		return self
	
	@property
	def rating(self):
		return sum( (attack*defense for attack, defense in self.coefficients.values()) )
	
	@property
	def counter_type(self):
		return Tactic.TACTIC_TYPES[self._idx-1]

	@property
	def inferior_type(self):
		return Tactic.TACTIC_TYPES[(self._idx+1) % len(Tactic.TACTIC_TYPES)]

def executeSingleAttackAction(attacker, defender, attackRange, additionalAttackerTraits=None, additionalDefenderTraits=None):
	if(attackRange == 0):
		attackerWeaponHands, attackerActions = attacker.getAllPossibleMeleeAttacks()
	else:
		attackerWeaponHands, attackerActions = attacker.getAllPossibleRangedAttack(attackRange)
	if(len(attackerActions) == 0):
		return "Attack from {} to {} not available(no at range {})".format(attacker, defender, attackRange)
	else:
		message = "Initiate attack from {} to {}".format(attacker, defender)
		for damage, speed, accuracy, traits in attackerActions:
			# process the value increase/decrease here
			for attempt in range(speed):
				hit = utils.roll_percentage(accuracy)
				if(hit):
					message += "\n\tAttempt {:d} missed on weapon stat ({}-{}-{})".format(attempt, damage, speed, accuracy)
				else:
					# reduce hp base on the attack
					hp_reduction = max(damage - defender.armor, 0.0)
					message += "\n\tAttempt {:d} scored {} damage on weapon stat ({}-{}-{})".format(attempt, hp_reduction, damage, speed, accuracy)
					defender.set_hp(defender.current_hp - hp_reduction)
		return message

DEFAULT_INITIATIVE_BONUS = 2

def executeSquadAttackAction(attackerSquad, defenderSquad, attackRange, tacticBonus=lambda x, y:(1.0, 1.0), additionalAttackerTraits=None, additionalDefenderTraits=None):
	# each individual engage one/several target independently, attacker get an initiative bonus, and those died don't get their turn
	individual_full_list = [(True, ind) for ind in attackerSquad.members] + [(False, ind) for ind in defenderSquad.members]
	# sort list by initiative with bonus; from largest to smallest
	initiative_list = sorted(individual_full_list, key=lambda item: int(item[0])*DEFAULT_INITIATIVE_BONUS+item[1].init, reverse=True)
	messages = []
	bonus_atk, bonus_enemy_def = tacticBonus(attackerSquad, defenderSquad)
	total_attack_count = 0
	for opponent_bool, ind in initiative_list:
		target_list = defenderSquad.members if opponent_bool else attackerSquad.members
		if(len(target_list) < 0):
			break
		if(attackRange == 0):
			indWeaponHands, indActions = ind.getAllPossibleMeleeAttacks()
		else:
			indWeaponHands, indActions = ind.getAllPossibleRangedAttack(attackRange)
		total_attack_count += len(indActions)
		# TODO factor in the weapon hand effect on accuracy
		for damage, speed, accuracy, traits in indActions:
			target_list = [ind for ind in target_list if ind.current_hp > 0]
			if(len(target_list) == 0):
				utils.Debug.printDebug("Shortcut exit! one side completely destroyed!")
				return total_attack_count, messages
			else:
				target = utils.select_random(target_list)
			# process the value increase/decrease here
			for attempt in range(speed):
				hit = utils.roll_percentage(accuracy)
				if(hit):
					messages.append("Attempt {:d} missed on weapon stat ({}-{}-{})".format(attempt, damage, speed, accuracy))
				else:
					# reduce hp base on the attack
					hp_reduction = max(damage - target.armor, 0.0)
					# the tactic bonus is after reduction. After all, a better tactic should not make bolter penetrate plasteel any better
					hp_reduction = hp_reduction * bonus_atk / bonus_enemy_def
					messages.append("Attempt {:d} scored {} damage on weapon stat ({}-{}-{})".format(attempt, hp_reduction, damage, speed, accuracy))
					target.set_hp(target.current_hp - hp_reduction)
	return total_attack_count, messages

class Battle:
	MOVEMENT_SCALING = 50.0
	BADGE_MARGIN = 20.0
	def __init__(self, yourUnits, enemyUnits, friendlyUnits=[], battleDimensions=(400.0, 800.0)):
		# save
#		utils.Debug.printDebug("Battle init: ", [type(u) for u in yourUnits], [type(u) for u in enemyUnits])
		self._your_units = yourUnits
		self._enemy_units = enemyUnits
		self._friendly_units = friendlyUnits + yourUnits
		self._all_units = self._friendly_units + self._enemy_units
		# map coordinate
		self.dimensions = battleDimensions
		# apparently dict can't handle a namedtuple with list within. fuck.
		self.coordinates = [None] * len(self._all_units)

	def combatEnded(self):
		return all( (not u.alive for u in self._friendly_units) ) or all( (not u.alive for u in self._enemy_units) )
	
	def _debug_ListUnits(self):
		for idx, unit in enumerate(self._all_units):
			if(self.coordinates[idx] != None):
				x, y = self.coordinates[idx]
				utils.Debug.printDebug("Unit {}({}) deployed and standing at {:.2f}-{:.2f}".format(unit.name, type(unit), x, y))
			else:
				utils.Debug.printDebug("Unit {}({}) not deployed".format(unit.name, type(unit)))

	@property
	def battle_height(self):
		return self.dimensions[0]
	@property
	def battle_width(self):
		return self.dimensions[1]

	def _getSquadsDistance(self, idx1, idx2):
		(x1, y1), (x2, y2) = self.coordinates[idx1], self.coordinates[idx2]
		return int(math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)) / Battle.MOVEMENT_SCALING)

	def isDeployable(self, unit_idx):
		unit = self._all_units[unit_idx]
		return unit.alive and self.coordinates[unit_idx] == None

	def deployUnitsRandomly(self, deployChance=1.0, voxStreamFn=None):
		# load all deployable
		friendly_deployment = [unit_idx for unit_idx in range(len(self._friendly_units)) if self.isDeployable(unit_idx)]
		enemy_deployment = [unit_idx for unit_idx in range(len(self._friendly_units), len(self._all_units)) if self.isDeployable(unit_idx)]
		# only roll if necessary
		if(deployChance < 1.0):
			# deploy at least one whenever available
			friendly_deployment_chc = utils.sublist_by_chance(friendly_deployment, deployChance)
			friendly_deployment = friendly_deployment_chc if len(friendly_deployment_chc) > 0 else friendly_deployment[:1]
			enemy_deployment_chc = utils.sublist_by_chance(enemy_deployment, deployChance)
			enemy_deployment = enemy_deployment_chc if len(enemy_deployment_chc) > 0 else enemy_deployment[:1]
		utils.Debug.printDebug("Deploying {:d} friendly unit, {:d} enemy unit".format(len(friendly_deployment), len(enemy_deployment)))
		# deploy to a randomly generated coordinate, friendly on the left (0.0), enemy on the right (battle_width)
		# TODO deploy on traits confered by vehicle/squad type
		for friendly_idx in friendly_deployment:
			self.coordinates[friendly_idx] = (utils.roll_between(0.0, self.battle_height), utils.roll_between(0.0, 40.0))
		for enemy_idx in enemy_deployment:
			self.coordinates[enemy_idx] = (utils.roll_between(0.0, self.battle_height), utils.roll_between(self.battle_width, self.battle_width-40.0))

	def moveUnitsRandomly(self, voxStreamFn=None, drawFn=None):
		for idx, pos in enumerate(self.coordinates):
			unit = self._all_units[idx]
			if(not unit.alive):
				utils.Debug.printDebug("Unit {}({}, idx {}) is not alive, cannot move.".format(unit.name, type(unit), idx))
				continue
			elif(pos is None):
				utils.Debug.printDebug("Unit {}({}, idx {}) have not yet been deployed.".format(unit.name, type(unit), idx))
				continue
			current_x, current_y = pos
			speed = unit.speed * Battle.MOVEMENT_SCALING
			# choose a point that stay within confines of the speed and the bound of the map
			x_movement_raw = utils.roll_between(-speed, speed)
			y_movement_raw = utils.roll_random() * math.sqrt(speed * speed - x_movement_raw * x_movement_raw) * (-1 if utils.roll_random() < 0.5 else 1)
			x_movement = min( max(x_movement_raw, -current_x + Battle.BADGE_MARGIN), self.battle_height - current_x - Battle.BADGE_MARGIN)
			y_movement = min( max(y_movement_raw, -current_y + Battle.BADGE_MARGIN), self.battle_width - current_y - Battle.BADGE_MARGIN)
			utils.Debug.printDebug("Unit {} moved by ({:.2f},{:.2f}), originally ({:.2f},{:.2f})".format(unit.name, x_movement, y_movement, current_x, current_y))
			assert 0.0 < current_x + x_movement < self.battle_height, "Height error at {} move {}".format(current_x, x_movement)
			assert 0.0 < current_y + y_movement < self.battle_width, "Width error at {} move {}".format(current_y, y_movement)
			new_pos = self.coordinates[idx] = (current_x + x_movement, current_y + y_movement)
			if(voxStreamFn is not None and callable(voxStreamFn)):
				voxStreamFn("Unit {}({}) moved: ({:.2f},{:.2f}) -> ({:.2f},{:.2f})".format(unit.name, type(unit), *pos, *new_pos))
			if(drawFn is not None and callable(drawFn)):
				# draw using imageLib.drawArrow, use same format
				color = "green" if idx < len(self._your_units) else "yellow" if idx < len(self._friendly_units) else "red"
				drawFn(pos, new_pos, color=color)

	def initiateCombat(self, tacticBonusFn=None, voxStreamFn=None, drawFn=None):
		assert callable(tacticBonusFn), "tacticBonusFn must be a function accepting fn(attacker, defender), but instead is {}".format(type(tacticBonusFn))
		# sort by unit initiative, and retrieve the index
		all_alive_units = [idx for idx, unit in enumerate(self._all_units) if unit.alive and self.coordinates[idx] is not None]
		all_deployed_units = [idx for idx in sorted(all_alive_units, key=lambda i: self._all_units[i].initiative)]
		# execute one by one
		for idx in all_deployed_units:
			initiator = self._all_units[idx]
			# check if still alive, because during combat one might already get fucked
			if(initiator.alive):
				initiator_is_friendly = idx < len(self._friendly_units)
				unit_all_targets = range(len(self._friendly_units)) if not initiator_is_friendly else range(len(self._friendly_units), len(self._all_units))
				unit_viable_targets = [tar_idx for tar_idx in unit_all_targets if self._all_units[tar_idx].alive and self.coordinates[tar_idx] is not None]
				if(len(unit_viable_targets) == 0):
					# one side loss
					utils.Debug.printDebug("No target is alive/deployed for unit {}({}) - is_friendly {}, exiting the combat process".format(initiator.name, type(initiator), initiator_is_friendly))
					return
				# select closest target
				unit_viable_targets = sorted(unit_viable_targets, key=lambda tar: self._getSquadsDistance(idx, tar))
				utils.Debug.printDebug("Viable targets ids of {}({}): {}".format(initiator.name, type(initiator), unit_viable_targets))
				best_target_idx = unit_viable_targets[0]
				target = self._all_units[best_target_idx]
				attacks_made, combat_messages = executeSquadAttackAction(initiator, target, self._getSquadsDistance(idx, best_target_idx), tacticBonus=tacticBonusFn)
				initiator_pos, target_pos = self.coordinates[idx], self.coordinates[best_target_idx]
				if(drawFn is not None and callable(drawFn) and attacks_made > 0):
					# in-range attack received, draw the arrows
					attacker_color = "green" if initiator_is_friendly else "red"
					drawFn(initiator_pos, target_pos, color=attacker_color, have_dash=True, arrow_end=True)
#				if(voxStreamFn):
#					voxStreamFn(combat_messages)
