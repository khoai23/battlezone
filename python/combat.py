import json, collections, io
from python.items import FrozenDict
import python.utils

class CombatManager:
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
		self.tacticList = [Tactic(block) for block in json_data["tactics"]]

	def getSquadTactics(self, squad, bare=False):
		squad_tactics = self.getAvailableTactics(squad.command_level)
		if(bare):
			return [tactic.name for tactic in squad_tactics]
		else:
			return squad_tactics

	def getAvailableTactics(self, level):
		return [t for t in self.tacticList if t.requirement <= level]

	def getCounteredTactic(self, target, listTactics):
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
				second_tactic = self.selectWorseTactic(first_tactic, self.getAvailableTactics(secondLevel))
			else:
				# second got good one, first got bad
				second_tactic = utils.select_with_preference(self.getAvailableTactics(secondLevel), second_preference_fn)
				first_tactic = self.selectWorseTactic(second_tactic, self.getAvailableTactics(firstLevel))
			return first_tactic, second_tactic

class CombatSession:
	def __init__(self, friendly, enemy, dimensions=(800, 400)):
		self._friendly = friendly
		self._enemy = enemy
		self._board_dimension = dimensions
	
	def _initializePositioning(self):
		# drop units randomly on the sides of the board
		pass
	
	def calculatePositioning(self):
		pass

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
		return Tactic.TACTIC_TYPES[self._idx+1 % len(Tactic.TACTIC_TYPES)]

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

def executeSquadAttackAction(attackerSquad, defenderSquad, attackRange, additionalAttackerTraits=None, additionalDefenderTraits=None):
	# each individual engage one/several target independently, attacker get an initiative bonus, and those died don't get their turn
	individual_full_list = [(True, ind) for ind in attackerSquad.members] + [(False, ind) for ind in defenderSquad.members]
	# sort list by initiative with bonus; from largest to smallest
	initiative_list = sorted(individual_full_list, key=lambda item: int(item[0])*DEFAULT_INITIATIVE_BONUS+item[1].init, reverse=True)
	messages = []
	for opponent_bool, ind in initiative_list:
		target_list = defenderSquad.members if opponent_bool else attackerSquad.members
		if(len(target_list) < 0):
			break
		if(attackRange == 0):
			indWeaponHands, indActions = ind.getAllPossibleMeleeAttacks()
		else:
			indWeaponHands, indActions = ind.getAllPossibleRangedAttack(attackRange)
		# TODO factor in the weapon hand effect on accuracy
		for damage, speed, accuracy, traits in indActions:
			target_list = [ind for ind in target_list if ind.current_hp > 0]
			if(len(target_list) == 0):
				utils.Debug.printDebug("Shortcut exit! one side completely destroyed!")
				return messages
			else:
				target = utils.roll_list(target_list)
			# process the value increase/decrease here
			for attempt in range(speed):
				hit = utils.roll_percentage(accuracy)
				if(hit):
					messages.append("Attempt {:d} missed on weapon stat ({}-{}-{})".format(attempt, damage, speed, accuracy))
				else:
					# reduce hp base on the attack
					hp_reduction = max(damage - defender.armor, 0.0)
					messages.append("Attempt {:d} scored {} damage on weapon stat ({}-{}-{})".format(attempt, hp_reduction, damage, speed, accuracy))
					target.set_hp(defender.current_hp - hp_reduction)
	return messages
