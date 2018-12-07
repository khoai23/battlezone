import json, io, random, os
import collections
import python.items as itemLib

def variance(val, fluctuation=0.5):
	return float(val) * (1.0 + random.uniform(-fluctuation, fluctuation))

def random_choice(sequence, num_choices=1, probs=None):
	# select one/many random element from the sequence
	result = []
	if(probs is not None):
		expanded_probs = [ (sum(probs[:idx]) if idx>0 else 0.0, idx) for idx in reversed(range(len(probs))) ]
	for _ in range(num_choices):
		selector = random.uniform(0.0, 1.0)
		if(probs is None):
			# python do floor when convert float to int. Nice
			select_idx = int( selector * float(len(sequence)) )
		else:
			# compare with the reversed and expanded probs
			_, select_idx = next((x for x in expanded_probs if x[0] < selector))
		result.append(sequence[select_idx])
	# return the array or item
	if(num_choices == 1):
		return result[0]
	else:
		return result

def vehicleVariantSplit(statDict):
	# vehicles have different variants, which should be splitted to different statDict
	weaponSets = statDict["weaponsName"]
	pintleChoices = set(statDict["pintleName"])
	# pintle can be
	if(len(weaponSets) <= 1):
		# only one variant / no weapon (transport), return as-is
		statDict["weaponsName"] = weaponSets[0] if len(weaponSets == 1) else []
		return [statDict]
	else:
		listVariants = []
		for wSet in weaponSets:
			variantDict = dict(statDict)
			variantDict["weaponsName"] = wset
			listVariants.append(variantDict)
		return listVariants

class ItemManager:
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
		self.vehicleList = [itemLib.Vehicle(data) for data in self.vehicleList]
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
		if(isinstance(itemOrItemName, str)):
			itemIdx = next( (i for i,x in enumerate(self.armoryItems) if x["name"] == itemOrItemName) )
		else:
			itemIdx = next( (i for i,x in enumerate(self.armoryItems) if x == itemOrItemName) )
		if(safety and self.armoryCounts[itemIdx] + amount < 0):
			raise ValueError("Error! item {} with count {:d} cannot accept modify value {:d}".format(itemOrItemName, self.armoryCounts[itemIdx], amount))
		self.armoryCounts[itemIdx] += amount
	
	def searchItemByName(self, name, itemType, searchImgName=True):
#		print("Searching for {} with type {}, useImgName {}".format(name, itemType, searchImgName))
		return next( (x for x in self.armoryItems if x.checkProperty(name, itemType, useImgName=searchImgName)) )

class IndividualStatManager:
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
#			print("JSON data read @IndividualStatManager: {}".format(json_data))
		self.statList = json_data["stat"]
		self.specialTraitList = json_data["special_progression"]
		self.namesList = json_data["names"]
		self.numberingTypes = json_data["numbering"]
		# components to craft random name
		namesComponents = json_data["name_components"]
		# first is an even distribution; second can be empty more, third can occasionally be empty
		firstProbs = [1.0 / len(namesComponents[0])] * len(namesComponents[0])
		secondProbs = [0.5] + [0.5 / (len(namesComponents[1]) - 1)] * (len(namesComponents[1]) - 1)
		thirdProbs = [0.2] + [0.8 / (len(namesComponents[2]) - 1)] * (len(namesComponents[2]) - 1)
		self.namesComponents = list(zip(namesComponents, [firstProbs, secondProbs, thirdProbs]))
		print("IndividualStatManager initialized to file {:s}".format(jsonFilePath))
	
	def setNumbering(comNumType, sqdNumType):
		assert comNumType in self.numberingTypes, "Invalid comNumType {:s}, must be one of {}".format(comNumType, self.numberingTypes.keys())
		self.companyNumberFn = lambda num: self.numberingTypes[comNumType][num]
		assert sqdNumType in self.numberingTypes, "Invalid sqdNumType {:s}, must be one of {}".format(sqdNumType, self.numberingTypes.keys())
		self.squadNumberFn = lambda num: self.numberingTypes[sqdNumType][num]

	def createName(self, safe=False):
		# if safe, take a random name from the namelist; if not, craft it using namesComponent
		if(safe):
			return random_choice(self.namesList, 1)
		else:
			nameSeg = [random_choice(comp_list, 1, probs) for comp_list, probs in self.namesComponents]
			return "".join(nameSeg)

	def createAstartes(self, level, progression_line=None):
		name = self.createName()
		current_progression = self.statList[0] if not progression_line else progression_line[0]
		hp = ws = bs = i = 0.0
		new_unit = Astartes(name, hp, ws, bs, i, lvl=0, current_progression=current_progression)
		for i in range(level):
			self.addLevel(new_unit, progression_line=None)
		return new_unit
	
	def addLevel(self, character, lvl=None, progression_line=None):
		lvl = lvl if lvl else character.lvl
		current_progression = character.current_progression
		if(lvl >= current_progression["endPoint"]):
			# switch progression
			next_progression_choices = current_progression["to"]
			if(progression_line):
				# have set progression, set the choice to the one specified
				next_progression_choice = next((choice for choice in next_progression_choices if choice in progression_line))
			else:
				next_progression_choice = random_choice(next_progression_choices, 1)
			print("Changing progression for character {}: from {} to {}".format(character.name, current_progression["name"], next_progression_choice))
			# find the progression in the stat list
			current_progression = next((statSet for statSet in self.statList if statSet["name"] == next_progression_choice))
			character.current_progression = current_progression
		hp_increase = variance(current_progression["baseHP"], 0.2)
		character.base_hp = character.base_hp + hp_increase
		character.current_hp = min(character.base_hp, character.current_hp + hp_increase)
		character.ws = character.ws + variance(current_progression["baseWS"], 0.1)
		character.bs = character.bs + variance(current_progression["baseBS"], 0.1)
		character.i = character.i + variance(current_progression["baseI"], 0.1)
		character.lvl = lvl + 1
		return character

class EnemyManager:
	def __init__(self, jsonFilePath):
		with io.open(jsonFilePath, "r", encoding="utf8") as json_file:
			json_data = json.load(json_file)
		invalidBlock = next( (block for block in json_data["individual"] + json_data["unit"] if "refname" not in block), None)
		assert invalidBlock is None, "Invalid block: {}".format(invalidBlock)
		self.enemyIndividualTemplate = { block["refname"]: EnemyIndividual(block) for block in json_data["individual"] }
		self.enemySquadTemplate = { block["refname"]: EnemySquad(block) for block in json_data["unit"] }
	
	def createSquad(self, squadTemplate):
		# create a new squad using the template, having empty composition
		newSquad = squadTemplate.clone()
		# populate it with the concerning individual object
		for indName, indNum in squadTemplate.composition:
			individual = self.getIndividualTemplate(indName)
			newSquad.extend( (self.createIndividual(indName) for _ in range(indNum)) )
		return newSquad

	def createIndividual(self, refnameOrTemplate):
			if(isinstance(refnameOrTemplate, EnemyIndividual)):
				# input is object, clone it
				return refnameOrTemplate.clone()
			else:
				# input is name, search and clone it
				return self.getIndividualTemplate(refnameOrTemplate).clone()
	
	def getIndividualTemplate(self, refname):
		return self.enemyIndividualTemplate[refname]

class Astartes:
	def __init__(self, name, hp, ws, bs, i, lvl=0, current_progression=None):
		self.name = name
		self.base_hp = self.current_hp = hp
		self.ws = ws
		self.bs = bs
		self.i = i
		self.lvl = 0
		self.exp = 0.0
		self.equipments = {"armor": None, "main": None, "secondary": None, "accessory": None}
		if(current_progression is None):
			raise ValueError("progression must exist to have character leveling up")
		self.current_progression = current_progression

	def getAllPossibleMeleeAttacks(self):
		all_weapons = [weapon for weapon in (self.equipments["main"], self.equipments["secondary"]) if weapon is not None]
		if(len(all_weapons) == 0):
			return 0, all_weapons
		hand_count = sum((weapon.weapon_hand for weapon in all_weapons))
		# return the hand count alongside the formatted weapon
		# tuple is damage/num_of_attack/accuracy
		return hand_count, [(weapon.attack_damage, weapon.attack_speed, self.ws, weapon.weapon_traits) for weapon in all_weapons if weapon.isMelee]

	def getAllPossibleRangedAttack(self, attack_range):
		all_weapons = [weapon for weapon in (self.equipments["main"], self.equipments["secondary"]) if weapon is not None]
		if(len(all_weapons) == 0):
			return 0, all_weapons
		hand_count = sum((weapon.weapon_hand for weapon in all_weapons))
		# return the hand count alongside the formatted weapon
		# tuple is damage/num_of_attack/accuracy
		return hand_count, [(weapon.attack_damage, weapon.attack_speed, self.ws, weapon.weapon_traits) for weapon in all_weapons if weapon.range >= attack_range]
	
	@property
	def armor(self):
		armor_item = self.equipments["armor"]
		if(armor_item == None):
			return 0
		else:
			return armor.armor_rating

	def equip(self, item, slot):
		self.equipments[slot] = item

	def getDisplayData(self):
		# return the necessary data to display the Astartes on a canvas
		listDir = {}
		armor = self.equipments["armor"]
		if(armor is not None):
			listDir["armor_dir"], _ = armor.getDisplayAndConstraints()
			listDir["armor_omit"] = []
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
			_, _, _ = listDir.pop("accessory_dir", None), listDir.pop("main_dir", None), listDir.pop("secondary_dir", None)
		else:
			# if not, remove the scarring (outline_scar)
			listDir["armor_omit"].append("outline_scar")
		return listDir
	

class EnemyIndividual( collections.namedtuple("EnemyIndividual", ["name", "hp", "armor", "init", "melee", "ranged", "isTemplate"]) ):
	def __new__(_cls, jsonData):
		jsonData = dict(jsonData)
		jsonData["melee"] = tuple(jsonData["melee"])
		jsonData["ranged"] = tuple(jsonData["ranged"])
		jsonData["isTemplate"] = True
		jsonData.pop("refname")
		return super(_cls, EnemyIndividual).__new__(_cls, **jsonData)
	
	def clone(self):
		assert self.isTemplate, "must use a template to initiate an enemy individual"
		# the clone function will use a current_hp property so it can be modifiable
		cloneObj = super(EnemyIndividual, self).__new__(EnemyIndividual, name=self.name, hp=self.hp, armor=self.armor, init=self.init, melee=self.melee, ranged=self.ranged, isTemplate=False)
		cloneObj._current_hp = self.hp
		return cloneObj
	
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
		return [(self.melee_damage, self.melee_attack_speed, self.melee_accuracy, "")]
	def getAllPossibleRangedAttack(self, attack_range):
		if(attack_range <= self.ranged_range):
			return [(self.ranged_damage, self.ranged_attack_speed, self.ranged_accuracy, "")]
		else:
			return []

class EnemySquad( collections.namedtuple("EnemySquad", ["name", "composition", "description", "speed", "badge", "isTemplate"]) ):
	def __new__(_cls, jsonData):
		jsonData = dict(jsonData)
#		print(jsonData)
		# ignore the refName and rename composition/badge name
		jsonData.pop("refname")
		jsonData["composition"] = jsonData.pop("refComposition")
		jsonData["badge"] = jsonData.pop("unitBadge")
		# only those made by json data are considered template
		jsonData["isTemplate"] = True
		return super(EnemySquad, _cls).__new__(_cls, **jsonData)

	def clone(self):
		assert self.isTemplate, "must use a template to initiate an enemy squad"
		# the clone function will create a empty compostion list and isTemplate switch to false
		return super(EnemySquad, self).__new__(EnemySquad, self.name, [], self.description, self.speed, self.badge, False)
