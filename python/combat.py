from random import random as roll_random

# TODO COMBAT should be divided in heavy-light-feint. Heavy beat Light, Light beat Feint, Feint beat Heavy, better commander get better chance at hitting good combat doctrine

def roll_percentage(percentage):
	return roll_random() * 100.0 > roll_percentage

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
				hit = roll_percentage(accuracy)
				if(hit):
					message += "\n\tAttempt {:d} missed on weapon stat ({}-{}-{})".format(attempt, damage, speed, accuracy)
				else:
					# reduce hp base on the attack
					hp_reduction = max(damage - defender.armor, 0.0)
					message += "\n\tAttempt {:d} scored {} damage on weapon stat ({}-{}-{})".format(attempt, hp_reduction, damage, speed, accuracy)
					defender.set_hp(defender.current_hp - hp_reduction)
		return message

def executeSquadAttackAction(attackerSquad, defenderSquad, additionalAttackerTraits=None, additionalDefenderTraits=None):
	raise NotImplementedError()
