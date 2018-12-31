import json, io, os
import python.items as itemLib
import python.utils as utils
from python.combat import CombatManager
from python.units import *

def vehicleVariantSplit(statDict):
	# vehicles have different variants, which should be splitted to different statDict
	weapon_sets = statDict["weaponsName"]
#	pintleChoices = set(statDict["pintleName"])
	# pintle can be
	if(len(weapon_sets) <= 1):
		# only one variant / no weapon (transport), return as-is
		statDict["weaponsName"] = weapon_sets[0] if len(weapon_sets) == 1 else []
		return [statDict]
	else:
		list_variants = []
		for w_set in weapon_sets:
			variant_dict = dict(statDict)
			variant_dict["weaponsName"] = w_set
			list_variants.append(variant_dict)
		return list_variants

class ItemManager:
	"""Manage the game instance's items"""
	def __init__(self, jsonFilePath, textureDirectory):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
		self.armorList = json_data["armour"]
		self.weaponList = json_data["weapon"]
		self.accessoryList = json_data["accessory"]
		self.chassisList = json_data["chassis"]
		self.vehicleWeaponList = json_data["vehicle_weapon"]
		self.vehicleList = json_data["vehicle_type"]
		# add the necessary image_dir
		for item in self.weaponList:
			# weapons is ./dir/view_weapon/weapon_{name}_{id}.png
			item["image_dir"] = os.path.join(textureDirectory, "view_weapon", "weapon_" + item["imgName"] + "_{:d}.png")
		for item in self.armorList:
			# armor is ./dir/view_unit/{name}_{id}.png
			item["image_dir"] = os.path.join(textureDirectory, "view_unit", item["imgName"] + "_{:d}.png")
		for item in self.accessoryList:
			# accessory is ./dir/view_unit/acc_{name}_{id}.png
			item["image_dir"] = os.path.join(textureDirectory, "view_unit", "acc_" + item["imgName"] + "_{:d}.png")
			# also convert the `underlay` param if found; default is overlay (accessory_is_overlay=True)
			item["accessory_is_overlay"] = not item.get("underlay", False)
		# TODO add a decoration for specialist 
		for item in self.chassisList + self.vehicleWeaponList:
			# for elements of a vehicle, it is ./dir/view_vehicle/{name}_{id}.png
			item["image_dir"] = os.path.join(textureDirectory, "view_vehicle", item["imgName"] + "_{:d}.png")
		# initialize all items
		self.weaponList = [itemLib.Weapon(data) for data in self.weaponList]
		self.armorList = [itemLib.Armor(data) for data in self.armorList]
		self.accessoryList = [itemLib.Accessory(data) for data in self.accessoryList]
		self.chassisList = [itemLib.Chassis(data) for data in self.chassisList]
		self.vehicleWeaponList = [itemLib.VehicleWeapon(data) for data in self.vehicleWeaponList]
		# vehicles are split into variants
		self.vehicleList = [itemLib.Vehicle(variantData) for data in self.vehicleList for variantData in vehicleVariantSplit(data)]
		self.initializeArmory()
		# update the vehicleList with this object
		for veh in self.vehicleList:
			veh.updateStat(self)
	
	@property
	def allItemList(self):
		return self.armorList + self.weaponList + self.accessoryList + self.chassisList + self.vehicleWeaponList + self.vehicleList

	def initializeArmory(self):
		self.armoryItems, self.armoryCounts = ( list(item) for item in zip(*[(item, 0) for item in self.allItemList]) )

	def changeItemCounts(self, itemOrItemName, amount, safety=True):
		if(itemOrItemName == None):
			utils.Debug.printDebug("Item None caught, assume to be empty item value")
			return
		if(isinstance(itemOrItemName, str)):
			item_idx = next( (i for i,x in enumerate(self.armoryItems) if x["name"] == itemOrItemName) )
		else:
			item_idx = next( (i for i,x in enumerate(self.armoryItems) if x == itemOrItemName) )
		if(safety and self.armoryCounts[item_idx] + amount < 0):
			raise ValueError("Error! item {} with count {:d} cannot accept modify value {:d}".format(itemOrItemName, self.armoryCounts[item_idx], amount))
		self.armoryCounts[item_idx] += amount
	
	def searchItemByName(self, name, itemType, searchImgName=True):
		utils.Debug.printDebug("Searching for {} with type {}, useImgName {}".format(name, itemType, searchImgName))
		return next( (x for x in self.armoryItems if x.checkProperty(name, itemType, useImgName=searchImgName)), None)

	def getItemsByType(self, itemType, available=True):
		return [(item, count) for item, count in zip(self.armoryItems, self.armoryCounts) if (item.type == itemType and (not available or count <= 0))]

class IndividualStatManager:
	"""Manage the processing of levels of individual"""
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
#			utils.Debug.printDebug("JSON data read @IndividualStatManager: {}".format(json_data))
		self.statList = json_data["stat"]
		self.specialTraitList = json_data["special_progression"]
		self.namesList = json_data["names"]
		self.numberingTypes = json_data["numbering"]
		# components to craft random name
		names_components = json_data["name_components"]
		# first is an even distribution; second can be empty more, third can occasionally be empty
		first_probs = [1.0 / len(names_components[0])] * len(names_components[0])
		second_probs = [0.5] + [0.5 / (len(names_components[1]) - 1)] * (len(names_components[1]) - 1)
		third_probs = [0.2] + [0.8 / (len(names_components[2]) - 1)] * (len(names_components[2]) - 1)
		self.namesComponents = list(zip(names_components, [first_probs, second_probs, third_probs]))
		utils.Debug.printDebug("IndividualStatManager initialized to file {:s}".format(jsonFilePath))
	
	def setNumbering(self, comNumType, sqdNumType):
		assert comNumType in self.numberingTypes, "Invalid comNumType {:s}, must be one of {}".format(comNumType, self.numberingTypes.keys())
		self.companyNumberFn = lambda num: self.numberingTypes[comNumType][num]
		assert sqdNumType in self.numberingTypes, "Invalid sqdNumType {:s}, must be one of {}".format(sqdNumType, self.numberingTypes.keys())
		self.squadNumberFn = lambda num: self.numberingTypes[sqdNumType][num]

	def createName(self, safe=False):
		# if safe, take a random name from the namelist; if not, craft it using namesComponent
		if(safe):
			return utils.random_choice(self.namesList, 1)
		else:
			name_seg = [utils.random_choice(comp_list, 1, probs) for comp_list, probs in self.namesComponents]
			return "".join(name_seg)

	def createAstartes(self, level, progressionLine=None):
		name = self.createName()
		current_progression = self.statList[0] if not progressionLine else self.searchProgressionByName(progressionLine[0])
		hp = ws = bs = i = 0.0
		new_unit = Astartes(name, hp, ws, bs, i, lvl=0, current_progression=current_progression)
		for i in range(level):
			self.addLevel(new_unit, progressionLine=None)
		return new_unit
	
	def searchProgressionByName(self, progressionName):
		return next( (item for item in self.statList if item["name"].lower() == progressionName.lower()) )
	
	def addLevel(self, character, lvl=None, progressionLine=None):
		lvl = lvl if lvl else character.lvl
		current_progression = character.current_progression
		if(lvl > current_progression["endPoint"]):
			# switch progression
			next_progression_choices = current_progression["to"]
			if(progressionLine):
				# have set progression, set the choice to the one specified
				next_progression_choice = next((choice for choice in next_progression_choices if choice in progressionLine))
			else:
				next_progression_choice = utils.random_choice(next_progression_choices, 1)
			utils.Debug.printDebug("Changing progression for character {}: from {} to {}".format(character.name, current_progression["name"], next_progression_choice))
			# find the progression in the stat list
			current_progression = self.searchProgressionByName(next_progression_choice)
			character.current_progression = current_progression
		hp_increase = utils.variance(current_progression["baseHP"], 0.2)
		character.base_hp = character.base_hp + hp_increase
		character.current_hp = min(character.base_hp, character.current_hp + hp_increase)
		character.ws = character.ws + utils.variance(current_progression["baseWS"], 0.1)
		character.bs = character.bs + utils.variance(current_progression["baseBS"], 0.1)
		character.i = character.i + utils.variance(current_progression["baseI"], 0.1)
		character.lvl = lvl + 1
		if("special" in character.current_progression):
			special_type = character.current_progression["special"]
			character.addSpecialistLevel(special_type)
		return character

DEFAULT_JSON_PATH = {"stat":"./res/data/AstartesStats.json", "enemy": "./res/data/EnemyData.json",
"item": "./res/data/Item.json", "texture_location": "./res/texture",
"combat": "./res/data/CombatTactics.json"}

DEFAULT_LEADER = {"lvl": 80, "progression": ["Initiate", "Neophyte", "Tactical", "Command", "Ancient"], "gears": [["paxe", "plasma", "artificer", "halo"]]}
DEFAULT_VETERAN = {"lvl": (70, 90), "progression": None, "gears": [["psword", "ppistol", "corvus", None], ["chainsword", "bolter", "errant", None], ["pcannon", "__empty__", "aquila", "devpack"], ["pfist", "bpistol", "aquila", "jumppack"]]}
DEFAULT_SQUADMATE = {"lvl": (20, 39), "progression": None, "gears": [["bolter", "__empty__", "aquila", None], ["chainsword", "bpistol", "aquila", "jumppack"], ["hbolt", "__empty__", "aquila", "devpack"]]}
DEFAULT_SQUADLEADER = {"lvl": (40, 50), "progression": ["Initiate", "Neophyte", "Tactical", "Command", "Ancient"], "gears": [["bolter", "chainsword", "aquila", None]]}
DEFAULT_COMPANY = {"commander": DEFAULT_LEADER, "squads": [(DEFAULT_VETERAN, DEFAULT_VETERAN, (5, 8), 2), (DEFAULT_SQUADLEADER, DEFAULT_SQUADMATE, (9, 10), 8)]}
DEFAULT_COLORSCHEME = ("half", "red", "blue", "gold", "brown", "cyan")

class OverallManager:
	"""Manage the roster of the company and all respective managers"""
	def __init__(self, chapterName, companyName, colorScheme=DEFAULT_COLORSCHEME, allJSONPath=DEFAULT_JSON_PATH):
		self.statManager = IndividualStatManager(allJSONPath["stat"])
		self.enemyManager = EnemyManager(allJSONPath["enemy"])
		self.itemManager = ItemManager(allJSONPath["item"], allJSONPath["texture_location"])
		self.combatManager = CombatManager(allJSONPath["combat"])
		self._company = Company(chapterName, companyName, self.statManager)
		self.colorScheme = colorScheme
	
	def createAstartes(self, astartesConfig=None):
		assert astartesConfig is not None, "Config must not be None to initialize"
		# create
		lvl = astartesConfig["lvl"]
		lvl = lvl if isinstance(lvl, int) else utils.select_within_range(lvl)
		astartes = self.statManager.createAstartes(lvl, progressionLine=astartesConfig["progression"])
		# equip
		gears = zip(utils.select_random(astartesConfig["gears"]), ["main", "secondary", "armor", "accessory"], ["weapon", "weapon", "armor", "accessory"])
		for gear_name, gear_slot, gear_type in gears:
			item = self.itemManager.searchItemByName(gear_name, gear_type)
			astartes.equip(item, gear_slot)
		utils.Debug.printDebug("Astartes {} created with equipments: {}".format(astartes.name, astartes.equipments))
		return astartes

	def createDefaultCompany(self, companyConfig=DEFAULT_COMPANY):
		self._company.commander = self.createAstartes(companyConfig["commander"])
		for squad_config in companyConfig["squads"]:
			squad_leader_config, squad_member_config, num_members_tuple, num_squad = squad_config
			for _ in range(num_squad):
				# members is -1, since leader is initiated separately
				num_members = utils.roll_random_int(num_members_tuple[0]-1, num_members_tuple[1])
				new_squad = Squad(self._company, (self.createAstartes(squad_member_config) for _ in range(num_members)), self.createAstartes(squad_leader_config))
				self._company.squads.append(new_squad)

	@property
	def company(self):
		return self._company

class EnemyManager:
	"""Manage the enemy, being mission(participable combat), EnemyUnits, EnemyIndividual"""
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
#			utils.Debug.printMsg(1, "Enemy JSONDATA: ".format(json_data))
		invalid_block = next( (block for block in json_data["individual"] + json_data["unit"] if "refname" not in block), None)
		assert invalid_block is None, "Invalid block: {}".format(invalid_block)
		#self._enemyIndividualTemplate = { block["refname"]: EnemyIndividual(block) for block in json_data["individual"] }
		self._enemyIndividualTemplate = utils.convertJSONToDictObject(json_data["individual"], "refname", EnemyIndividual)
		self._enemySquadTemplate = utils.convertJSONToDictObject(json_data["unit"], "refname", EnemySquad)
		self._missionTemplate = utils.convertJSONToDictObject(json_data["mission"], "refname", Mission)
#		utils.Debug.printDebug("Mission templates: ".format(self._missionTemplate))
	
	def createSquad(self, squadNameOrTemplate):
		if(isinstance(squadNameOrTemplate, str)):
			utils.Debug.printDebug("Searching for template: {:s}".format(squadTemplate))
			squadTemplate = self._enemySquadTemplate[squadTemplate]
		elif(squadNameOrTemplate, EnemySquad):
			squadTemplate = squadNameOrTemplate
		else:
			raise ValueError("Value {} (type {}) invalid @createIndividual".format(squadnameOrTemplate, type(squadnameOrTemplate)))
		# create a new squad using the template, having empty composition
		new_squad = squadTemplate.clone()
		# populate it with the concerning individual object
		for ind_name, ind_num in squadTemplate.composition:
			individual = self.getIndividualTemplate(ind_name)
			new_squad.composition.extend( (self.createIndividual(ind_name) for _ in range(ind_num)) )
		return new_squad

	def createIndividual(self, refnameOrTemplate):
			if(isinstance(refnameOrTemplate, EnemyIndividual)):
				# input is object, clone it
				return refnameOrTemplate.clone()
			elif(isinstance(refnameOrTemplate, str)):
				# input is name, search and clone it
				return self._enemyIndividualTemplate[refnameOrTemplate].clone()
			else:
				raise ValueError("Value {} (type {}) invalid @createIndividual".format(refnameOrTemplate, type(refnameOrTemplate)))
	
	def getIndividualTemplate(self, refname):
		return self._enemyIndividualTemplate[refname]

	def createMission(self, missionNameOrTemplate):
		# search mission if needed
		if(isinstance(missionNameOrTemplate, EnemyIndividual)):
			# input is object, clone it
			mission = missionNameOrTemplate
		elif(isinstance(missionNameOrTemplate, str)):
			# input is name, search and clone it
			mission = self._missionTemplate[missionNameOrTemplate]
		else:
			raise ValueError("Value {} (type {}) invalid @createIndividual".format(missionNameOrTemplate, type(missionNameOrTemplate)))
		instance_composition = []
		# generate the units randomly
		for unit_name, bound_lower, bound_upper in mission.composition:
			for _ in range(utils.roll_random_int(bound_lower, bound_upper)):
				instance_composition.append(self.createSquad(unit_name))
		# clone to a working mission object
		mission = mission.clone()
		mission.composition.extend(instance_composition)
		return mission

class Mission( collections.namedtuple("Mission", ["name", "composition", "description", "missionType", "weight", "isTemplate"]) ):
	def __new__(_cls, jsonData):
		# this will create a template for further usage
		# turn the composition to nested tuple
		jsonData["composition"] = tuple([tuple(squad_config) for squad_config in jsonData["composition"]])
		# add isTemplate
		jsonData["isTemplate"] = True
		# initialize
		super(Mission, _cls).__new__(_cls, **jsonData)
	
	def clone(self):
		# the composition will be a list to be updated by the EnemyManager
		# first, only allow cloning for template
		assert self.isTemplate, "must use a template to initiate mission"
		# create the clone
		return super(Mission, self).__new__(Mission, self.name, [], self.description, self.missionType, self.weight, False)
