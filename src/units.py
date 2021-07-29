import collections, abc

class Individual:
	@abc.abstractmethod
	def getAllPossibleMeleeAttacks(self):
		"""Return the attacks which could be made in melee
			Returns:
				a list of (attack_damage, attack_speed, attack_accuracy, traits)
		"""
		raise NotImplementedError("Base abstract class Individual")

	@abc.abstractmethod
	def getAllPossibleRangedAttack(self, attackRange):
		"""Return the attacks which could be made at range
			Args:
				attackRange: int, the range to attack
			Returns:
				a list of (attack_damage, attack_speed, attack_accuracy, traits)
		"""
		raise NotImplementedError("Base abstract class Individual")

class Astartes(Individual):
	BASE_SPEED = 3.0
	def __init__(self, name, hp, ws, bs, init, lvl=0, current_progression=None):
		self.name = name
		self.base_hp = self.current_hp = hp
		self.ws = ws
		self.bs = bs
		self.init = init
		self.lvl = 0
		self.exp = 0.0
		self._speed = Astartes.BASE_SPEED
		self._specialist_level = {}
		self.equipments = {"armor": None, "main": None, "secondary": None, "accessory": None}
		if(current_progression is None):
			raise ValueError("progression must exist to have character leveling up")
		self.current_progression = current_progression

	def set_hp(self, hpValue):
		self.current_hp = hpValue

	@property
	def command_level(self):
		return self._specialist_level.get("command", 0)

	def getAllPossibleMeleeAttacks(self):
		all_weapons = [weapon for weapon in (self.equipments["main"], self.equipments["secondary"]) if weapon is not None]
		if(len(all_weapons) == 0):
			return 0, all_weapons
		hand_count = sum((weapon.weapon_hand for weapon in all_weapons))
		# return the hand count alongside the formatted weapon
		# tuple is damage/num_of_attack/accuracy
		return hand_count, [(weapon.attack_damage, weapon.attack_speed, self.ws, weapon.weapon_traits) for weapon in all_weapons if weapon.is_melee]

	def getAllPossibleRangedAttack(self, attackRange):
		all_weapons = [weapon for weapon in (self.equipments["main"], self.equipments["secondary"]) if weapon is not None]
		if(len(all_weapons) == 0):
			return 0, all_weapons
		hand_count = sum((weapon.weapon_hand for weapon in all_weapons))
		# return the hand count alongside the formatted weapon
		# tuple is damage/num_of_attack/accuracy
		return hand_count, [(weapon.attack_damage, weapon.attack_speed, self.ws, weapon.weapon_traits) for weapon in all_weapons if not weapon.is_melee and weapon.range >= attackRange]
	
	@property
	def maximum_range(self):
		all_weapons_range = [weapon._range for weapon in (self.equipments["main"], self.equipments["secondary"]) if weapon is not None]
		return max(0, *all_weapons_range)

	@property
	def armor(self):
		armor_item = self.equipments["armor"]
		if(armor_item == None):
			return 0
		else:
			return armor_item.armor_rating

	def equip(self, item, slot):
		self.equipments[slot] = item

	def getDisplayData(self):
		# return the necessary data to display the Astartes on a canvas
		listDir = {}
		armor = self.equipments["armor"]
		if(armor is not None):
			listDir["armor_dir"], _ = armor.getDisplayAndConstraints()
			listDir["armor_omit"] = []
		else:
			return listDir
		if(self.equipments["main"]):
			main_dir, main_omit = self.equipments["main"].getDisplayAndConstraints(main_hand=True)
			listDir["main_dir"] = main_dir
			if("armor_omit" in listDir):
				listDir["armor_omit"].extend(main_omit)
		if(self.equipments["secondary"]):
			secondary_dir, secondary_omit = self.equipments["secondary"].getDisplayAndConstraints(main_hand=False)
			listDir["secondary_dir"] = secondary_dir
			if("armor_omit" in listDir):
				listDir["armor_omit"].extend(secondary_omit)
		if(self.equipments["accessory"]):
			accessory_dir, accessory_omit = self.equipments["accessory"].getDisplayAndConstraints()
			listDir["accessory_dir"] = accessory_dir
			listDir["accessory_is_overlay"] = self.equipments["accessory"].overlay
			if("armor_omit" in listDir):
				listDir["armor_omit"].extend(accessory_omit)
		if("armor_omit" in listDir and self.current_hp <= 0 ):
			# if injured/no armor equipped, remove all weapons/accessory
			_ = listDir.pop("accessory_dir", None), listDir.pop("main_dir", None), listDir.pop("secondary_dir", None)
			_ = _
		else:
			# if not, remove the scarring (outline_scar)
			listDir["armor_omit"].append("outline_scar")
		return listDir
	
	def isCombatReady(self, damage_threshold):
		return self.current_hp / self.base_hp >= damage_threshold

	def addSpecialistLevel(self, type_str):
		self._specialist_level[type_str] = self._specialist_level.get(type_str, 0) + 1

	@property
	def speed(self):
		# TODO increase/decrease speed by gears
		return self._speed

	def rosterRowData(self):
		"""Create the tuple to be displayed in the roster management
		Format is (path, level, stat, equipment)
		"""

class EnemyIndividual( collections.namedtuple("EnemyIndividual", ["name", "hp", "armor", "init", "melee", "ranged", "isTemplate"]), Individual ):
	def __new__(_cls, jsonData):
		jsonData = dict(jsonData)
		jsonData["melee"] = tuple(jsonData["melee"])
		jsonData["ranged"] = tuple(jsonData["ranged"])
		jsonData["isTemplate"] = True
		return super(_cls, EnemyIndividual).__new__(_cls, **jsonData)
	
	def clone(self):
		assert self.isTemplate, "must use a template to initiate an enemy individual"
		# the clone function will use a current_hp property so it can be modifiable
		clone_obj = super(EnemyIndividual, self).__new__(EnemyIndividual, name=self.name, hp=self.hp, armor=self.armor, init=self.init, melee=self.melee, ranged=self.ranged, isTemplate=False)
		clone_obj._current_hp = self.hp
		return clone_obj
	
	def set_hp(self, hpValue):
		assert not self.isTemplate, "Is a template, cannot have hp value"
		self._current_hp = hpValue

	@property
	def current_hp(self):
		return self._current_hp
	@property
	def base_hp(self):
		return self.hp
	@property
	def melee_damage(self):
		return self.melee[0]
	@property
	def melee_attack_speed(self):
		return self.melee[1]
	@property
	def melee_accuracy(self):
		return self.melee[2]
	@property
	def ranged_damage(self):
		return self.ranged[0]
	@property
	def ranged_attack_speed(self):
		return self.ranged[1]
	@property
	def ranged_accuracy(self):
		return self.ranged[2]
	@property
	def ranged_range(self):
		return self.ranged[3]
	def getAllPossibleMeleeAttacks(self):
		return 2, [(self.melee_damage, self.melee_attack_speed, self.melee_accuracy, "")]
	def getAllPossibleRangedAttack(self, attackRange):
		if(attackRange <= self.ranged_range):
			return 2, [(self.ranged_damage, self.ranged_attack_speed, self.ranged_accuracy, "")]
		else:
			return 2, []

class Company:
	COMPANY_FULL_STR = "{chapter_name} Chapter, {company_name} Company"
	COMPANY_PARTIAL_STR = "{company_name} Company"
	SQUAD_FULL_STR = "{squad_name} Squad of {company_name} Company, {chapter_name} Chapter"
	SQUAD_PARTIAL_STR = "{squad_name} Squad"
	def __init__(self, chapterName=None, name=None, commander=None, statManagerObj=None):
		assert name is not None and chapterName is not None
		self.squads = []
		self.vehicles = []
		self.chapterName = chapterName
		self.name = name
		self.commander = commander
		self._statManagerObj = statManagerObj
	
	def getSquadByIdx(self, squadIdx, safety=False, fullName=False):
		"""Return tuple of squadObj, name"""
		assert not safety or len(self.squads) > squadIdx >= 0
		squad_str = Company.SQUAD_FULL_STR if fullName else Company.SQUAD_PARTIAL_STR
		return self.squads[squadIdx], squad_str.format(squad_name=self._statManagerObj.squadNumberFn(squadIdx), chapter_name=self.chapterName, company_name=self.name)
	
	def getSquadNameByObj(self, squadObj, fullName=False):
		squadObj, squadName = self.getSquadByIdx(self.squads.index(squadObj), safety=True, fullName=fullName)
		return squadName

	def getCompanyName(self, fullName=False):
		squad_str = Company.SQUAD_FULL_STR if fullName else Company.SQUAD_PARTIAL_STR
		return squad_str.format(chapter_name=self.chapterName, company_name=self.name)

class Unit:
	@property
	@abc.abstractmethod
	def members(self):
		"""List the members of the units to be called
			Returns:
				list of Individual that are still combat-available
		"""
		raise NotImplementedError("Base abstract class Unit")

	@property
	def speed(self):
		"""List the speed of the unit
			Returns:
				a float value equal to the speed of that unit
		"""
		return min(ind.speed for ind in self.members)

	@property
	def initiative(self):
		"""List the initiative of the unit
			Returns:
				a float value equal to the initiative of that unit
		"""
		return min(ind.init for ind in self.members)

	@property
	def alive(self):
		"""Check if the unit is alive
			Returns:
				a bool specify if the unit is combat available
		"""
		return any( (ind for ind in self.members if ind.current_hp > 0) )
	
	@property
	@abc.abstractmethod
	def command_level(self):
		"""Get the command expertise of the squad
			Returns:
				an int in the range of 1-20
		"""
		raise NotImplementedError("Base abstract class Unit")
	
	@property
	@abc.abstractmethod
	def is_vehicle(self):
		raise NotImplementedError("Base abstract class Unit")

	@property
	@abc.abstractmethod
	def maximum_range(self):
		raise NotImplementedError("Base abstract class Unit")
		
class Squad(Unit):
	"""Despite the name, this is an Astartes squad"""
	def __init__(self, parentCompany, members=[], leader=None):
		self._company = parentCompany
		self._members = list(members)
		self._leader = leader
		if(self._leader not in self._members and self._leader is not None):
			self._members.append(self._leader)
		self.badge = "default_friendly"
	
	@property
	def name(self, fullName=False):
		return self._company.getSquadNameByObj(self, fullName=fullName)
	
	@property
	def command_level(self):
		return self._leader.command_level if self._leader else 0

	@property
	def members(self):
		return [member for member in self._members if member.current_hp > 0.0]
	
	@property
	def deployable(self):
		# TODO count those over a specific threshold instead of 0.0
		return any( (member for member in self._members if member.current_hp > 0.0) )
	
	@property
	def maximum_range(self):
		return max((member.maximum_range for member in self._members))

	def is_vehicle(self):
		return False

	def getSquadHp(self):
		return sum( (max(member.current_hp, 0.0) for member in self._members) ), sum( (member.base_hp for member in self._members) ), len([member for member in self._members if member.current_hp > 0]), len(self._members)

class EnemyVehicle(collections.namedtuple("EnemyVehicle", ["name", "hp", "melee", "ranged", "description", "speed", "badge", "isTemplate"]), Unit, EnemyIndividual):
	def  __new__(_cls, jsonData):
		# ignore the refName and rename composition/badge name
		jsonData["badge"] = jsonData.pop("unitBadge")
		# convert the stat block into necessary hp/melee/ranged
		stat_block = jsonData.pop("stat")
		jsonData["hp"] = stat_block["hp"]
		jsonData["melee"] = tuple([tuple(item) for item in stat_block["melee"]])
		jsonData["ranged"] = tuple([tuple(item) for item in stat_block["ranged"]])
		# only those made by json data are considered template
		jsonData["isTemplate"] = True
		return super(EnemySquad, _cls).__new__(_cls, **jsonData)
	
	@property
	def members(self):
		return [self] if self.current_hp > 0.0 else []
	
	def is_vehicle(self):
		return True

	@property
	def command_level(self):
		# TODO fix
		return 0

	def clone(self):
		assert self.isTemplate, "must use a template to initiate an enemy squad"
		# the clone function will create an object with current_hp and not isTemplate, allowing it to be damaged
		newObj = super(EnemySquad, self).__new__(EnemySquad, self.name, self.hp, self.melee, self.ranged, self.description, self.speed, self.badge, False)
		newObj.current_hp = newObj.hp
		return newObj

	def getAllPossibleMeleeAttacks(self):
		assert not self.isTemplate, "template cannot have attack"
		return self.melee
	
	def getAllPossibleRangedAttack(self, attackRange):
		assert not self.isTemplate, "template cannot have attack"
		return [atk for atk in self.ranged if atk[-1] >= attackRange ]
	
	def maximum_range(self):
		return max((atk[-1] for atk in self.ranged))

class EnemySquad( collections.namedtuple("EnemySquad", ["name", "composition", "description", "speed", "badge", "isTemplate"]), Unit ):
	def __new__(_cls, jsonData):
		# ignore the refName and rename composition/badge name
		jsonData["composition"] = jsonData.pop("refComposition")
		jsonData["badge"] = jsonData.pop("unitBadge")
		# only those made by json data are considered template
		jsonData["isTemplate"] = True
		return super(EnemySquad, _cls).__new__(_cls, **jsonData)

	def getSquadHp(self):
		assert not self.isTemplate, "a template cannot be used for combat"
		return sum( (max(member.current_hp, 0.0) for member in self.composition) ), sum( (member.base_hp for member in self.composition) ), len([member for member in self.composition if member.current_hp > 0.0]), len(self.composition)

	def clone(self):
		assert self.isTemplate, "must use a template to initiate an enemy squad"
		# the clone function will create a empty compostion list and isTemplate switch to false
		return super(EnemySquad, self).__new__(EnemySquad, self.name, [], self.description, self.speed, self.badge, False)
	
	@property
	def maximum_range(self):
		assert not self.isTemplate, "A template should not have this property accessed (maximum_range)"
		return max((m.ranged_range for m in self.members))
	
	@property
	def members(self):
		assert not self.isTemplate, "A template should not have this property accessed (members)"
		return [member for member in self.composition if member.current_hp > 0.0]
	
	def is_vehicle(self):
		return False

	@property
	def command_level(self):
		# TODO fix
		return 0
