import json, collections, io, os
from python.items import FrozenDict
import python.utils as utils
import python.combat_movement as movement_modes

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

def executeSquadAttackAction(attackerSquad, defenderSquad, attackRange, tacticBonus=lambda x, y:(1.0, 1.0), additionalAttackerTraits=None, additionalDefenderTraits=None, moved=False):
	"""Each individual engage one/several target independently, attacker get an initiative bonus, and those died don't get their turn"""
	individual_full_list = [(True, ind) for ind in attackerSquad.members] + [(False, ind) for ind in defenderSquad.members]
	# sort list by initiative with bonus; from largest to smallest
	initiative_list = sorted(individual_full_list, key=lambda item: int(item[0])*DEFAULT_INITIATIVE_BONUS+item[1].init, reverse=True)
	messages = []
	bonus_atk, bonus_enemy_def = tacticBonus(attackerSquad, defenderSquad)
	utils.Debug.printDebug("Tactics from attacker {} and defender {} giving percentage multiplier: * {:.2f} / {:.2f}".format(attackerSquad, defenderSquad, bonus_atk, bonus_enemy_def))
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
					# the tactic bonus is after reduction. After all, a better tactic should not make bolter penetrate plasteel any better, but it should make shots more damaging once penetrated.
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
		self.moved_last_turn = [False] * len(self._all_units)
		# movement modes
		self._friendly_movement = self._enemy_movement = movement_modes.RandomMovement

	def combatEnded(self):
		return all( (not u.alive for u in self._friendly_units) ) or all( (not u.alive for u in self._enemy_units) )
	
	def _debugListUnits(self):
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
		return int(movement_modes.distance(self.coordinates[idx1], self.coordinates[idx2]) / Battle.MOVEMENT_SCALING)

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
			new_pos = self.coordinates[idx] = movement_modes.RandomMovement.moveUnit(self.dimensions, unit, pos)
			if(voxStreamFn is not None and callable(voxStreamFn)):
				voxStreamFn("Unit {} moved: ({:.2f},{:.2f}) -> ({:.2f},{:.2f})".format(unit.name, *pos, *new_pos))
			if(drawFn is not None and callable(drawFn)):
				# draw using imageLib.drawArrow, use same format
				color = "green" if idx < len(self._your_units) else "yellow" if idx < len(self._friendly_units) else "red"
				drawFn(pos, new_pos, color=color)

	def unitMoveAndAttack(self, unit, movementFn=None, voxStreamFn=None, drawFn=None):
		"""Move all units according to the movement modes instead
			This function will run along the course of self._all_units, so do the sorting of initiative there instead
		"""
		idx = self._all_units.index(unit)
		pos = self.coordinates[idx]
		# do not consider if dead or not deployed
		if(not unit.alive):
			utils.Debug.printDebug("Unit {}({}, idx {}) is not alive, cannot move.".format(unit.name, type(unit), idx))
			return
		elif(pos is None):
			utils.Debug.printDebug("Unit {}({}, idx {}) have not yet been deployed.".format(unit.name, type(unit), idx))
			return
		# try to attack/charge from this position
		if(not self.tryAttack(unit, idx, pos, moved=False)):
			# if not attack or charged, can move and try ranged attack
			moved, new_pos = self.tryMoveUnit(unit, idx, pos)
			if(moved):
				# only try to attack again when moved
				self.tryAttack(unit, idx, new_pos, moved=True)
		self.coordinates[idx] = new_pos
		# all hooked external function
		if(movementFn is not None and callable(movementFn)):
			movementFn(pos, new_pos)
		if(voxStreamFn is not None and callable(voxStreamFn)):
			tag = "ally" if self._isUnitFriendly(unit) else "enemy"
			voxStreamFn("Unit <{:s}>{:s}<\\{:s}> moved: ({:.2f},{:.2f}) -> ({:.2f},{:.2f})".format(tag, unit.name, tag, *pos, *new_pos))
		if(drawFn is not None and callable(drawFn)):
			# draw using imageLib.drawArrow, use same format
			color = "green" if idx < len(self._your_units) else "yellow" if idx < len(self._friendly_units) else "red"
			drawFn(pos, new_pos, color=color)

	def _isUnitFriendly(self, unit, verify=True):
		"""Check if unit is on friendly side or enemy side"""
		if(verify):
			assert unit in self._friendly_units or unit in self._enemy_units
		return unit in self._friendly_units

	def tryMoveUnit(self, unit, idx, pos):
		"""Move unit, as dictated by respective movement modes @movement_modes"""
		if(self._isUnitFriendly(unit)):
			unit_ally, unit_enemy = self._friendly_units, self._enemy_units
			movement_mode = self._friendly_movement
		else:
			unit_ally, unit_enemy = self._enemy_units, self._friendly_units
			movement_mode = self._enemy_movement
		unit_ally_coord = [self.coordinates[self._all_units.index(funit)] for funit in unit_ally]
		unit_enemy_coord = [self.coordinates[self._all_units.index(eunit)] for eunit in unit_enemy]
		unit_ally.remove(unit)
		new_pos = movement_mode.moveUnit(self.dimensions, unit, pos, unit_ally, unit_ally_coord, unit_enemy, unit_enemy_coord)
		if(new_pos is not None):
			return True, new_pos
		else:
			return False, pos

	def tryAttack(self, unit, idx, pos, moved=None, voxStreamFn=None, movementFn=None, drawFn=None, tacticBonusFn=None):
		"""Try attacking from selected position. If moved, receive a penalty for rapid and stop heavy weapon from firing. If not, allow charging into melee toward all units """
		# search for targets
		unit_is_friendly = self._isUnitFriendly(unit)
		# create and sort targets by distances
		all_targets = self._enemy_units if unit_is_friendly else self._friendly_units
		target_ids = [self._all_units.index(t) for t in all_targets]
		target_distance = [self._getSquadsDistance(idx, t_idx) for t_idx in target_ids]
		all_target_sorted = list(sorted(zip(target_distance, target_ids, all_targets)))
		utils.Debug.printDebug("By default: Selecting closest as target for unit {}, prefer melee to ranged".format(unit))
		# select the closest
		target_range, target_idx, target = filter(lambda it: it[0] <= unit.maximum_range, all_target_sorted)[0]
		target_pos = self.coordinates[target_idx]
		if(moved or unit.speed < target_range):
			# target outside charging range, shoot
			if(target_range > unit.maximum_range):
				# target outside shooting and chargeable range
				return False
			else:
				# target can be shot at but cannot be charged at
				attacks_made, combat_messages = executeSquadAttackAction(unit, target, target_range, tacticBonus=tacticBonusFn, moved=moved)
				if(voxStreamFn is not None and callable(voxStreamFn)):
					tag_unit, tag_target = ("ally", "enemy") if unit_is_friendly else ("enemy", "ally")
					voxStreamFn("Unit <{:s}>{:s}<\\{:s}> shot {:d} times at Unit <{:s}>{:s}<\\{:s}> ".format(tag_unit, unit.name, tag_unit, attacks_made, tag_target, target.name, tag_target))
				if(drawFn is not None and callable(drawFn)):
					# range attack made, draw the arrows
					attacker_color = "green" if unit_is_friendly else "red"
					drawFn(pos, target_pos, color=attacker_color, have_dash=True, arrow_end=True)
		else:
			# inside charging range or already in contact, move toward them and initiate melee
			# TODO adding range/melee preference
			self._coordinates[idx] = target_pos
			if(movementFn is not None and callable(movementFn)):
				movementFn(pos, target_pos)
			attacks_made, combat_messages = executeSquadAttackAction(unit, target, 0, tacticBonus=tacticBonusFn, moved=moved)
			if(voxStreamFn is not None and callable(voxStreamFn)):
				tag_unit, tag_target = ("ally", "enemy") if unit_is_friendly else ("enemy", "ally")
				voxStreamFn("Unit <{:s}>{:s}<\\{:s}> assaulted Unit <{:s}>{:s}<\\{:s}>, dealing {:d} blows".format(tag_unit, unit.name, tag_unit, tag_target, target.name, tag_target, attacks_made))
		return True


	def changeMovementModes(self, modeName, friendly=False, enemy=False):
		assert friendly != enemy, "You can only set friendly OR enemy @changeMovementModes!"
		if(friendly):
			self._friendly_movement = movement_modes.selectModeByName(modeName)
		if(enemy):
			self._enemy_movement = movement_modes.selectModeByName(modeName)

	def initiateCombat(self, tacticBonusFn=None, voxStreamFn=None, drawFn=None):
		"""Run combat for everyone. Warning: this function have a screwed friendly check"""
		assert callable(tacticBonusFn), "tacticBonusFn must be a function accepting fn(attacker, defender), but instead is {}".format(type(tacticBonusFn))
		# sort by unit initiative, and retrieve the index
		all_alive_units = [idx for idx, unit in enumerate(self._all_units) if unit.alive and self.coordinates[idx] is not None]
		all_deployed_units = [idx for idx in sorted(all_alive_units, key=lambda i: self._all_units[i].initiative)]
		# execute one by one
		for idx in all_deployed_units:
			initiator = self._all_units[idx]
			# check if still alive, because during combat one might already get fucked
			if(initiator.alive):
				initiator_is_friendly = self._isUnitFriendly(initiator)
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
