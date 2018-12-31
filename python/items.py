import python.utils as utils
# implement a sort of immutable frozen dict
# TODO why is __hash__ overriden?
class FrozenDict(dict):
		def __init__(self, *args, **kwargs):
				self._hash = None
				super(FrozenDict, self).__init__(*args, **kwargs)

		def __hash__(self):
				if self._hash is None:
						self._hash = hash(tuple(sorted(self.items())))	# iteritems() on py2
				return self._hash

		def _immutable(self, *args, **kwargs):
				raise TypeError("cannot change object - object is immutable")

		__setitem__ = _immutable
		__delitem__ = _immutable
		pop = _immutable
		popitem = _immutable
		clear = _immutable
		update = _immutable
		setdefault = _immutable

class ArmoryItem:
	@property
	def name(self):
		return self.stat["name"]
	
	def __repr__(self):
		"""The string denoting name and classname of objects children of ArmoryItem"""
		return "{:s}({})".format(self.name, type(self))

class Item(ArmoryItem):
	itemTypes = ["armor", "weapon", "accessory", "decoration", "vehicle_chassis", "vehicle_weapon"]
	def __init__(self, itemType, stat):
		assert itemType in Item.itemTypes, "itemType must belong to category Item.itemTypes"
		self.type = itemType
		# self.stat is immutable
		self.stat = FrozenDict(**stat)
#		self.stat = stat
	
	def getDisplayAndConstraints(self, main_hand=True):
		# image constraints is the list of armor items that will be hidden with the item on display
		image_omit = []
		if(isinstance(self, Weapon)):
			if(self.hide_aligned_arm):
				image_omit.append("outline_right_arm" if main_hand else "outline_left_arm")
			if(self.hide_unaligned_arm):
				image_omit.append("outline_left_arm" if main_hand else "outline_right_arm")
		elif(isinstance(self, Accessory)):
			if(self.replace_backpack):
				image_omit.extend(["fill_left_backpack", "fill_right_backpack"])
		return self.stat["image_dir"], image_omit

	def checkProperty(self, name, itemType, useImgName=True):
		return ( self.stat["name"] == name or (useImgName and self.stat["imgName"] == name)) and self.type == itemType 

class Vehicle(ArmoryItem):
	def __init__(self, stat):
		self.type = "vehicle"
		self.stat = FrozenDict(**stat)
		# the vehicle MUST be updated with the item manager to function properly
		self.chassis = self.weapons = self.pintle = None
		self._updated = False
		# convert the chassisName/weaponsName field to concerning item list
	
	def updateStat(self, itemManagerObj):
		self._updated = True
		self.chassis = itemManagerObj.searchItemByName(self.stat["chassisName"], 'vehicle_chassis')
		self.weapons = [itemManagerObj.searchItemByName(item, 'vehicle_weapon') for item in self.stat["weaponsName"]]
		pintle_name = self.stat["pintleName"]
		self.pintle = itemManagerObj.searchItemByName(pintle_name, 'vehicle_weapon')
	
	def checkProperty(self, name, itemType, useImgName=True):
	#	assert self._updated, "Vehicle had not been initialized properly. Update it with updateStat!"
#		if(useImgName):
#			utils.Debug.printDebug("Warning: Vehicle do not have image name")
		return self.stat["name"] == name and self.type == itemType

class Armor(Item):
	def __init__(self, stat):
		super(Armor, self).__init__("armor", stat)
	
	@property
	def armor_rating(self):
		return self.stat["def"]

class Weapon(Item):
	RANGED_TYPE_STRS = ("short", "medium", "long", "extreme")
	def __init__(self, stat):
		# check if melee/ranged/inactive and trim the "type" value base on that
		if("melee" in stat["type"]):
			self.is_melee = True
			self._range = 0
			stat["type"] = stat["type"].replace("melee", "").strip("|,")
		elif( any((rstr in stat["type"] for rstr in Weapon.RANGED_TYPE_STRS)) ):
			self.is_melee = False
			rstr = next( (r for r in Weapon.RANGED_TYPE_STRS if r in stat["type"]) )
			self._range = Weapon.RANGED_TYPE_STRS.index(rstr)
			stat["type"] = stat["type"].replace(rstr, "").strip("|,")
		else:
			# inactive object (e.g shields), modify nothing
			self.is_melee = False
			self._range = 0
#			raise ValueError("Object data do not specify the range of the weapon! String {} must contain melee|{:s}".format(stat["type"], "|".join(Weapon.RANGED_TYPE_STRS)))
		super(Weapon, self).__init__("weapon", stat)
	
	@property
	def range(self):
		assert not self.is_melee, "Melee weapons do not have range"
		return self._range
	@property
	def attack_damage(self):
		return self.stat["str"]
	@property
	def attack_speed(self):
		return self.stat["spd"]
	@property
	def weapon_hand(self):
		return self.stat["hand"]
	@property
	def weapon_traits(self):
		return self.stat["type"]
	@property
	def hide_aligned_arm(self):
		return self.stat.get("hide_primary_arm", False)
	@property
	def hide_unaligned_arm(self):
		return self.stat.get("hide_secondary_arm", False)

class Accessory(Item):
	def __init__(self, stat):
		super(Accessory, self).__init__("accessory", stat)
	@property
	def overlay(self):
		return self.stat.get("accessory_is_overlay", False) and not self.stat.get("replaceBackpack", False)
	@property
	def replace_backpack(self):
		return self.stat.get("replaceBackpack", False)

class Chassis(Item):
	def __init__(self, stat):
		super(Chassis, self).__init__("vehicle_chassis", stat)

class VehicleWeapon(Item):
	def __init__(self, stat):
		super(VehicleWeapon, self).__init__("vehicle_weapon", stat)
