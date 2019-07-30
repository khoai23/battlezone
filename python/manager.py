import json, io, os, collections, math
import python.items as itemLib
import python.utils as utils
from python.combat import CombatManager
import python.units as units

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
		self.itemPrices = json_data.get("commission")
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

	def getItemsByGroup(self):
		return {
			"armor": self.armorList,
			"weapon": self.weaponList,
			"accessory": self.accessoryList,
			"vehicle": self.vehicleList,
			"vehicle_modules:": self.chassisList + self.vehicleWeaponList
		}

	def getItemsByType(self, group, available=True):
		all_items_by_type = [ (item, self.getItemCount(item)) for item in self.getItemsByGroup()[group] ]
		if(available):
			return [data for data in all_items_by_type if data[-1] > 0]
		else:
			return all_items_by_type

	def _retrieveItemId(self, item):
		return next( (idx for idx, it in enumerate(self.armoryItems) if item == it) )

	def initializeArmory(self):
		self.armoryItems = self.allItemList
		self.availableItem = [0 for _ in range(len(self.armoryItems))]
		self.damagedItem = [0 for _ in range(len(self.armoryItems))]

	def changeItemCount(self, item, amount, safety=True, damagedItem=False):
		# extract the name and select the type of the item (normal or damaged)
		item_id = self._retrieveItemId(item)
		item_counter = self.availableItem if not damagedItem else self.damagedItem
		# check if the amount changed will cause negative stock
		if(safety and item_counter[item_id] + amount < 0):
			raise ValueError("Error! item {} with count {:d} cannot accept modify value {:d}".format(item, item_counter[item_id], amount))
		# execute the change
		item_counter[item_id] += amount
	
	def searchItemByName(self, name, itemType, searchImgName=True):
		utils.Debug.printDebug("Searching for {} with type {}, useImgName(refname) {}".format(name, itemType, searchImgName))
		return next( (x for x in self.armoryItems if x.checkProperty(name, itemType, useImgName=searchImgName)), None)

	def getItemCount(self, item, damagedItem=False):
		# extract the name and select the type of the item (normal or damaged)
		item_id = self._retrieveItemId(item)
		item_counter = self.availableItem if not damagedItem else self.damagedItem
		return item_counter[item_id]

	def fetchItemPrices(self, item):
		item_name = item if isinstance(item, str) else item.refname
		return self.itemPrices[item_name]

class IndividualStatManager:
	"""Manage the processing of levels of individual"""
	def __init__(self, jsonFilePath, numbering="roman_num"):
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
		self.setNumbering(numbering, numbering)
		utils.Debug.printDebug("IndividualStatManager initialized to file {:s}, numbering used {:s}".format(jsonFilePath, numbering))
	
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
		new_unit = units.Astartes(name, hp, ws, bs, i, lvl=0, current_progression=current_progression)
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
		character.current_hp = character.current_hp + hp_increase
		character.ws = character.ws + utils.variance(current_progression["baseWS"], 0.1)
		character.bs = character.bs + utils.variance(current_progression["baseBS"], 0.1)
		character.init = character.init + utils.variance(current_progression["baseI"], 0.1)
		character.lvl = lvl + 1
		if("special" in character.current_progression):
			special_type = character.current_progression["special"]
			character.addSpecialistLevel(special_type)
		return character

class ConversationManager:
	"""A manager to create random banter for flavored vox, both in and out of combat"""
	DUPLICATE_PACING = 4
	TACTIC_DIFF_THRESHOLDS = [4.0, 2.0, 0.5, 0.25]
	TACTIC_DIFF_KEY = ["advantage_high", "advantage_low", "balance", "disadvantage_low", "disadvantage_high"]
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
		# TODO load the conversation during normal 
		# load the conversation during normal/battle differently
		self._battleBanters = json_data["group_banters"]
		self._battleReports = json_data["report_tactic"]
		enemy_raw_filler = json_data["race_data"]
		# for each race entry, convert the keys to @ format (enemy->@enemy) for formatting purpose
		self._enemyStrings = {"@"+key:value for key, value in enemy_raw_filler.items()}
		
		# implement a query system to make sure that lines will not directly be repeated due to random choices
		self._pastNormalLines = []
		self._pastBattleLines = []

	@staticmethod
	def _getKeyFromDiff(diff):
		key_idx = next((idx for idx, threshold in enumerate(ConversationManager.TACTIC_DIFF_THRESHOLDS) if diff >= threshold), -1)
		return ConversationManager.TACTIC_DIFF_KEY[key_idx]

	@staticmethod
	def _randomizeWords(enemyStringDict):
		return {k:v if isinstance(v, str) else utils.select_random(v) for k, v in enemyStringDict}

	def getRandomBattleReportLine(self, tactic_differences, enemy_key, lineReplacementDict=None):
		"""Get a random line and replace it using the combined lineReplacementDict and enemy preset strings
			Args:
				tactic_differences: the difference between tactics between your squad and enemy. >4.0 is very good, 4.0 > val > 2.0 is good, 2.0 > val > 0.5 is normal, 0.5 > val > 0.25 is bad, and > 0.25 is very fucking bad. float
				enemy_key: the name of enemy in this mission. must be a key in self._enemyStrings. str
				lineReplacementDict: the name of your squad/squad leader/various other things that might be used
			Returns:
				a string that is a report which is guaranteed to not be duplicated within DUPLICATE_PACING values
		"""
		# select an eligible line
		line_key = ConversationManager._getKeyFromDiff(tactic_differences)
		list_sentences = [line for line in self._battleReports[line_key] if line not in self._pastBattleLines]
		selected_line = utils.select_random(list_sentences)
		# convert the replacement dict to a valid replacer, and select a random word in the strings
		replacer = {k if "@" in k else "@"+k:v for k, v in lineReplacementDict.items()}
		replacer.update(ConversationManager._randomizeWord(self._enemyStrings[enemy_key]))
		# update the self._pastBattleLines in FIFO style
		if(len(self._pastBattleLines) >= ConversationManager.DUPLICATE_PACING):
			self._pastBattleLines.pop()
		self._pastBattleLines.insert(0, selected_line)
		# replace everything in the selected_line dictated by the replacer 
		# do sorted to make sure overlaps will not cause trouble (e.g @enemy_slur replaced by @enemy key)
		for key, value in sorted(replacer.items(), key=lambda item: len(item[0])):
			selected_line = selected_line.replace(key, value)
		return selected_line

class EnemyManager:
	"""Manage the enemy, being mission(participable combat), EnemyUnits, EnemyIndividual"""
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
#			utils.Debug.printMsg(1, "Enemy JSONDATA: {}".format(json_data))
		invalid_block = next( (block for block in json_data["individual"] + json_data["unit"] if "refname" not in block), None)
		assert invalid_block is None, "Invalid block: {}".format(invalid_block)
		#self._enemyIndividualTemplate = { block["refname"]: EnemyIndividual(block) for block in json_data["individual"] }
		self._enemyIndividualTemplate = utils.convertJSONToDictObject(json_data["individual"], "refname", units.EnemyIndividual)
		self._enemySquadTemplate = utils.convertJSONToDictObject(json_data["unit"], "refname", units.EnemySquad)
		self._missionTemplate = utils.convertJSONToDictObject(json_data["mission"], "refname", Mission)
#		utils.Debug.printDebug("Mission templates: ".format(self._missionTemplate))
	
	def createSquad(self, squadNameOrTemplate):
		if(isinstance(squadNameOrTemplate, str)):
			utils.Debug.printDebug("Searching for template: {:s}".format(squadNameOrTemplate))
			squadTemplate = self._enemySquadTemplate[squadNameOrTemplate]
		elif(squadNameOrTemplate, units.EnemySquad):
			squadTemplate = squadNameOrTemplate
		else:
			raise ValueError("Value {} (type {}) invalid @createIndividual".format(squadNameOrTemplate, type(squadNameOrTemplate)))
		# create a new squad using the template, having empty composition
		new_squad = squadTemplate.clone()
		# populate it with the concerning individual object
		for ind_name, ind_num in squadTemplate.composition:
#			individual = self.getIndividualTemplate(ind_name)
			new_squad.composition.extend( (self.createIndividual(ind_name) for _ in range(ind_num)) )
		return new_squad

	def createIndividual(self, refnameOrTemplate):
			if(isinstance(refnameOrTemplate, units.EnemyIndividual)):
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
		if(isinstance(missionNameOrTemplate, units.EnemyIndividual)):
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

class TimeManager:
	def __init__(self):
		self._currentTime = 0
		self._scheduledEvents = []

	def advanceCounter(self):
		self._currentTime += 1
		self._executeDueEvents()
	
	def _executeDueEvents(self):
		due_events = [event for due_time, event in self._scheduledEvents if due_time <= self._currentTime]
		self._scheduledEvents = [e for e in self._scheduledEvents if e[0] > self._currentTime]
		for event_fn in due_events:
			event_fn()

	def addEvent(self, due_time, event_fn):
		self._scheduledEvents.append( (due_time, event_fn) )

	@property
	def counter(self):
		return self._currentTime

class DiplomacyManager:
	FACTION = [
		# Proper name, race, reference name, faction
		("Imperial Guard", "human", "guard", "Imperium"),
		("Inquisitor", "human", "inq", "Imperium"),
		("Adeptus Mechanicus", "human", "mech", "Imperium"),
		("Adepta Sororitas", "human", "nun", "Imperium"),
		("Craftword Eldar", "eldar", "eldar", "Eldar"),
		("Ork", "ork", "ork", "Ork"),
		("Necron", "necron", "necron", "Necron"),
		("Chaos", "human", "chaos", "Chaos"),
	]
	def __init__(self, useDefaultDipScore=True):
		self.factions = {refname:(proper_name, side, race) for proper_name, race, refname, side in DiplomacyManager.FACTION}
		self._faction_affinity = {refname:50 for refname in self.factions.keys()}
		self._faction_requisition = {refname:0 for refname in self.factions.keys()}
		self._setDefaultChapterDipScore()
	
	def _setDefaultChapterDipScore(self):
		self._faction_affinity.update({
			"self": 100,
			"inq": 40, "guard": 70, "mech": 50, "nun": 55,
			"ork": -20, "necron": -15, "chaos": -50, "eldar": 25, 
		})
		self._faction_requisition.update({"self": 4000, "guard": 100, "mech": 50})
	
	@property
	def relations(self):
		"""Relation in form of (proper name, relation value) for each factions"""
		return [(self.factions[k][0], self._faction_affinity.get(k, 50)) for k in self.factions.keys()]
	
	def getAffinityAndReq(self, keys=None, cheat=False):
		if(keys is None): # make sure keys belong to self or other factions
			keys = ["self"] + list(self.factions.keys())
		else:
			keys = [k for k in keys if k in ["self"] + list(self.factions.keys())]
		return [(k, self.factions.get(k, ["Chapter"])[0], self._faction_affinity[k] if not cheat else 99, self._faction_requisition[k] if not cheat else 99999999) for k in keys]

"""Default values"""

DEFAULT_JSON_PATH = {"stat":"./res/data/AstartesStats.json", "enemy": "./res/data/EnemyData.json",
"item": "./res/data/ItemData.json", "texture_location": "./res/texture", "icon_location": "./res/texture/unit_icon",
"combat": "./res/data/CombatTactics.json", "conversation": "./res/data/ConversationData.json"}

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
		self.combatManager = CombatManager(allJSONPath["combat"], allJSONPath["icon_location"])
		self.timeManager = TimeManager()
		self.diplomacyManager = DiplomacyManager()
		self.conversationManager = ConversationManager(allJSONPath["conversation"])
		self._company = units.Company(chapterName, companyName, None, self.statManager)
		self.colorScheme = colorScheme
		self.map = None
		self._mainPanelText = None

	def hookMainPanelText(self, command):
		self._mainPanelText = command

	def mainPanelText(self, text):
		self._mainPanelText(text)
	
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
				new_squad = units.Squad(self._company, (self.createAstartes(squad_member_config) for _ in range(num_members)), self.createAstartes(squad_leader_config))
				self._company.squads.append(new_squad)

	def moveToSystem(self, systemIdx=None):
		assert self.map is not None, "map is not initialized in this function"
		if(systemIdx is None):
			if(len(self.map._movement_stack) > 0):
				utils.Debug.printDebug("Moving, refrain from random, stack is {}".format(self.map._movement_stack))
				return
			else:
				utils.Debug.printDebug("Idle, expected stack: {}".format(self.map._movement_stack))
			# roll for random not-duplicate value
			new_target = utils.roll_random_int(0, self.map.systems_count-2)
			if(new_target >= self.map._orbit_system):
				new_target += 1
			# estimate the turns needed to arrive
			distance, route_str = self.map.moveToSystemEstimate(new_target)
			eta = self.calculateFleetETA(distance, route_str)
			utils.Debug.printDebug("Idle, rolling random target, result: {:d}:{:d}-turn".format(new_target, eta))
			# put it on the map
			self.map.moveToSystemConfirmed(new_target, eta)
		else:
			raise NotImplementedError("Not ready")
	
	def calculateFleetETA(self, distance, routeStrength):
		"""Calculate an integer representing how many turn needed to cross this distance"""
		speed = 50.0
		distance *= (100.0 - routeStrength) / 100.0
		eta = distance / speed
		return int(math.ceil(eta))

	def createCommissionEvent(self, item, origin, itemAmount, itemTime):
		utils.Debug.printDebug("{} item(s) {} is commisssioned, from {}".format(itemAmount, item.name, origin))
		current_turn = self.timeManager.counter
		# add the commission to the timeManager
		def commission_func():
			self.itemManager.changeItemCount(item, itemAmount)
			self.mainPanelText("<ally>@advisor: <\\ally>Chapter Master @your_name, we have received the shipment from {:s}, containing {:d} {:s}(s). You commissioned this at turn {:d}.".format(self.diplomacyManager.factions.get(origin, ["@chapter_home"])[0], itemAmount, item.name, current_turn))
		self.timeManager.addEvent(itemTime, commission_func)

	def endTurn(self):
		utils.Debug.printDebug("Running random movement across the map")
		self.map.endTurnTrigger()
		self.timeManager.advanceCounter()
		self.moveToSystem()

	@property
	def company(self):
		return self._company

class Mission( collections.namedtuple("Mission", ["name", "composition", "description", "missionType", "weight", "isTemplate"]) ):
	def __new__(_cls, jsonData):
		# this will create a template for further usage
		# turn the composition to nested tuple
		jsonData["composition"] = tuple([tuple(squad_config) for squad_config in jsonData["composition"]])
		# add isTemplate
		jsonData["isTemplate"] = True
		# initialize
		return super(Mission, _cls).__new__(_cls, **jsonData)
	
	def clone(self):
		# the composition will be a list to be updated by the EnemyManager
		# first, only allow cloning for template
		assert self.isTemplate, "must use a template to initiate mission"
		# create the clone
		return super(Mission, self).__new__(Mission, self.name, [], self.description, self.missionType, self.weight, False)
