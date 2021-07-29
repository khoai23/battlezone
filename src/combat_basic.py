from src.combat import CombatManager, Tactic, executeSquadAttackAction
import random


class BasicBattle:
	"""Very simple class, both rosters engaged on a randomized fashion. Also decided by initiative"""
	def __init__(self, yourUnits, enemyUnits, friendlyUnits=[]):
		self._your_units = yourUnits
		self._enemy_units = enemyUnits
		self._friendly_units = friendlyUnits + yourUnits
		self._all_units = self._friendly_units + self._enemy_units
		self.moved_last_turn = [False] * len(self._all_units)
		
		# roll this dice for overall chance to initiate combat
		self.combat_chance = lambda *args, **kwargs: random.random() > 0.8
		# roll this dice to calculate which side is attacking
		# 0.3 flat for each side, 0.4 decided by difference between i1, i2; true mean i1 get the attack
		self.combat_starter = lambda i1, i2, **kwargs: random.random() < (0.3 + i1 / (i1+i2) * 0.4 )
		# combat range decision
		self.combat_range = lambda u1, u2, **kwargs: random.randint(0, max(u1.maximum_range, u2.maximum_range))

	def select_random_enemy(self, unit):
		if(unit in self._friendly_units):
			return random.sample(self._enemy_units, 1)[0]
		elif(unit in self._enemy_units):
			return random.sample(self._friendly_units, 1)[0]
		else:
			raise ValueError

	def initiateCombat(self, voxStreamFn=None, attackFn=None):
		all_deployed_units = list(sorted([u for u in self._all_units if u.alive], key=lambda u: u.initiative))
		# roll to decide engagements
		initiate_attacks = [self.combat_chance(u) for u in all_deployed_units]
		attackers = [u for u, i in zip(all_deployed_units, initiate_attacks) if i]
		# select a random opposing squad as target for each attackers
		pairs = [(u, self.select_random_enemy(u)) for u in attackers]
		# for each pair, roll to check which side get the initiative
		# TODO maybe limit the amount of combat an unit can be in
		for s1, s2 in pairs:
			if(not s1.alive or not s2.alive):
				# either side already dead, ignore
				continue
			s1_is_attack = self.combat_starter(s1.initiative, s2.initiative)
			a, d = (s1, s2) if s1_is_attack else (s2, s1)
			combat_range = self.combat_range(s1, s2)
			# TODO proper movement fn (roll another?)
			attacks_made, combat_messages = executeSquadAttackAction(a, d, combat_range, moved=combat_range < 2) 
			tag_unit, tag_target = ("ally", "enemy") if a in self._friendly_units else ("enemy", "ally")
			voxStreamFn("Unit <{:s}>{:s}<\\{:s}> attacked Unit <{:s}>{:s}<\\{:s}> at range {:d}, dealing {:d} blows".format(tag_unit, a.name, tag_unit, tag_target, d.name, tag_target, combat_range, attacks_made))
	
	def combatEnded(self):
		return all( (not u.alive for u in self._friendly_units) ) or all( (not u.alive for u in self._enemy_units) )
