import abc, math
import python.utils as utils

class Movement:
	name = None
	MOVEMENT_SCALING = 20.0
	BADGE_MARGIN = 20.0
	@staticmethod
	def updateConstants(movement_scaling=None, badge_margin=None):
		if(movement_scaling is not None and isinstance(movement_scaling, float)):
			Movement.MOVEMENT_SCALING = movement_scaling
		if(badge_margin is not None and isinstance(badge_margin, float)):
			Movement.BADGE_MARGIN = badge_margin

	@abc.abstractstaticmethod
	def moveUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, **kwargs):
		"""Move the unit within the field. Can be influenced by friendly units, enemy units, etc.
			Returns the new friendly_coord if can move, or None if not
		"""
		raise NotImplementedError()

	@abc.abstractstaticmethod
	def tryMoveUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, **kwargs):
		"""Unlike moveUnit, this function evaluate if the coordinate from moveUnit should be done or not.
		Returns:
			tuple of (moved, position)
			"""
		raise NotImplementedError()

	def chargeUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, **kwargs):
		"""Attempt to find an eligible unit to charge.
		Returns:
			a target to charge in form of (unit, pos), or None if not available
			"""
		raise NotImplementedError()

	@staticmethod
	def distance(p1, p2):
		return distance(p1, p2)

class RandomMovement(Movement):
	name = "Random"
	@staticmethod
	def moveUnit(field, unit, unit_coord, *args, **kwargs):
		"""Override: Completely random movement"""
		battle_height, battle_width = field
		speed = unit.speed * Movement.MOVEMENT_SCALING
		current_x, current_y = unit_coord
			# choose a point that stay within confines of the speed and the bound of the map
		x_movement_raw = utils.roll_between(-speed, speed)
		y_movement_raw = utils.roll_random() * math.sqrt(speed * speed - x_movement_raw * x_movement_raw) * (-1 if utils.roll_random() < 0.5 else 1)
		x_movement = min( max(x_movement_raw, -current_x + Movement.BADGE_MARGIN), battle_height - current_x - Movement.BADGE_MARGIN)
		y_movement = min( max(y_movement_raw, -current_y + Movement.BADGE_MARGIN), battle_width - current_y - Movement.BADGE_MARGIN)
		utils.Debug.printDebug("RandomMovement: Unit {} moved by ({:.2f},{:.2f}), originally ({:.2f},{:.2f})".format(unit.name, x_movement, y_movement, current_x, current_y))
		assert 0.0 < current_x + x_movement < battle_height, "Height error at {} move {}".format(current_x, x_movement)
		assert 0.0 < current_y + y_movement < battle_width, "Width error at {} move {}".format(current_y, y_movement)
		return (current_x + x_movement, current_y + y_movement)

	@staticmethod
	def tryMoveUnit(field, unit, unit_coord, *args, percentage=50.0, **kwargs):
		will_move = utils.roll_percentage(percentage)
		return will_move, RandomMovement.moveUnit(field, unit, unit_coord, *args, **kwargs) if will_move else unit_coord

	@staticmethod
	def chargeUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, **kwargs):
		# only select available targets from deployed units (coord is not none)
		available_targets = sorted( ((e, e_pos) for e, e_pos in zip(enemy, enemy_coord) if e_pos is not None and distance(unit_coord, e_pos) <= unit.speed), key=lambda x: distance(unit_coord, x[1]))
		if(len(available_targets) == 0):
			return None
		else:
			return available_targets[0]

class EnemyMassMovement(Movement):
	name = "Target Enemy Mass"
	NOISE = None
	@staticmethod
	def moveUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, movement_noise=True, **kwargs):
		"""Override: Move unit toward the enemy center mass. There is no chance for outside-the-board calculation
			movement_noise: used by default, add noise to movement so units don't just came into one place
		"""
		current_x, current_y = unit_coord
		speed = unit.speed * Movement.MOVEMENT_SCALING
		battle_height, battle_width = field
		
		if(len(enemy_coord) == 0):
			return None
		enemy_count = float(len(enemy_coord))
		enemy_x, enemy_y = zip(*enemy_coord)
		enemy_mass_x, enemy_mass_y = sum(enemy_x) / enemy_count, sum(enemy_y) / enemy_count
		if(movement_noise):
			noise = EnemyMassMovement.NOISE or speed / 4
			enemy_mass_x += utils.roll_between(-noise, noise)
			enemy_mass_y = min(battle_height - Movement.BADGE_MARGIN, max(enemy_mass_y, Movement.BADGE_MARGIN))
			enemy_mass_y += utils.roll_between(-noise, noise)
			enemy_mass_y = min(battle_width - Movement.BADGE_MARGIN, max(enemy_mass_y, Movement.BADGE_MARGIN))
		# calculate the movement needed to reach that center mass
		target_vector = (enemy_mass_x - current_x, enemy_mass_y - current_y)
		normalized_vector = normalize_vector(target_vector)
		maximum_speed = min(target_vector[0] / normalized_vector[0], speed)
		movement_vector = (normalize_vector[0] * maximum_speed, normalize_vector[1] * maximum_speed)
		# add it for the new movement
		utils.Debug.printDebug("Unit {} moved ({:.2f}-{:.2f}) from ({:.2f}-{:.2f})".format(unit, *unit_coord, *movement_vector))
		move_x, move_y = movement_vector
		return (current_x + move_x, current_y + move_y)

	@staticmethod
	def tryMoveUnit(*args, **kwargs):
		"""Always move if possible, which is always
		TODO: Only move if it matter (e.g bringing unit closer to gun range, put more unit into range, etc.)"""
		return True, EnemyMassMovement.moveUnit(*args, **kwargs)

class EnemyNearestMovement(Movement):
	name = "Target Nearest Enemy"
	NOISE = Movement.MOVEMENT_SCALING
	@staticmethod
	def moveUnit(field, unit, unit_coord, ally, ally_coord, enemy, enemy_coord, movement_noise=True, **kwargs):
		"""Override: Move unit toward the nearest enemy unit. There is no chance for outside-the-board calculation
			movement_noise: used by default, add noise to movement so units don't just came into one place
		"""
		current_x, current_y = unit_coord
		speed = unit.speed * Movement.MOVEMENT_SCALING
		battle_height, battle_width = field
		
		if(len(enemy_coord) == 0):
			return None
		enemy_x, enemy_y = zip(*enemy_coord)
		# don't actually need the lambda since the distance is before
		_, (enemy_nearest_x, enemy_nearest_y) = next(sorted( [(distance(unit_coord, ecoord), ecoord) for ecoord in enemy_coord] ))
		if(movement_noise):
			noise = EnemyMassMovement.NOISE or speed / 4
			enemy_nearest_x += utils.roll_between(-noise, noise)
			enemy_nearest_y = min(battle_height - Movement.BADGE_MARGIN, max(enemy_nearest_y, Movement.BADGE_MARGIN))
			enemy_nearest_y += utils.roll_between(-noise, noise)
			enemy_nearest_y = min(battle_width - Movement.BADGE_MARGIN, max(enemy_nearest_y, Movement.BADGE_MARGIN))
		# calculate the movement needed to reach that center mass
		target_vector = (enemy_nearest_x - current_x, enemy_nearest_y - current_y)
		normalized_vector = normalize_vector(target_vector)
		maximum_speed = min(target_vector[0] / normalized_vector[0], speed)
		movement_vector = (normalize_vector[0] * maximum_speed, normalize_vector[1] * maximum_speed)
		# add it for the new movement
		utils.Debug.printDebug("Unit {} moved ({:.2f}-{:.2f}) from ({:.2f}-{:.2f})".format(unit, *unit_coord, *movement_vector))
		move_x, move_y = movement_vector
		return (current_x + move_x, current_y + move_y)

	@staticmethod
	def tryMoveUnit(*args, **kwargs):
		"""Always move if possible, which is always
		TODO: Only move if it matter (e.g bringing unit closer to gun range, put more unit into range, etc.)"""
		return True, EnemyNearestMovement.moveUnit(*args, **kwargs)

ALL_MOVEMENTS = [RandomMovement, EnemyMassMovement, EnemyNearestMovement]
def selectModeByName(modeName, default=RandomMovement):
	for movement in ALL_MOVEMENTS:
		if(movement.name == modeName or type(movement).__name__ == modeName):
			return movement
	utils.Debug.printError("Movement name {:s} not found, returning the default {:s} instead".format(modeName, default.name))
	return default

def normalize_vector(vector):
	x, y = vector
	v_length = math.sqrt(x * x + y * y)
	return (x / v_length, y / v_length)

def distance(pos1, pos2):
	x, y = pos1[0] - pos2[0], pos1[1] - pos2[1]
	return math.sqrt(x * x + y * y)
