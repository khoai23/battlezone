# here will handle all images processing
import tkinter as tk
import PIL.Image as Image
import PIL.ImageOps as ImageOps
import PIL.ImageTk as ImageTk
import os, math
from python.utils import Debug

DEFAULT_CANVAS_WIDTH = 167.0
DEFAULT_CANVAS_HEIGHT = 232.0

class IconManager():
	def __init__(self, location, default_images=("default_friendly, default_hostile")):
		self._icon_location = location
		self._default_friendly = self._icon_location.format(default_images[0])
		self._default_hostile = self._icon_location.format(default_images[1])
	
	def getBadgeImage(self, badge_name, is_friendly):
		if(os.path.isfile(self._icon_location.format(badge_name))):
			image_dir = self._icon_location.format(badge_name)
		else:
			Debug.printDebug("Cannot find badge name {:s}, using default.")
			if(is_friendly):
				image_dir = self._default_friendly
			else:
				image_dir = self._default_hostile
		return image_dir

def drawOnCanvas(canvas, image, location=(0,0), scale=1.0, anchor="nw"):
	if(image is None):
		return -1
	#	Debug.printDebug(type(image), vars(image))
	# due to a tkinter bug, canvas must keep track of images that was drawn into it.
	if not hasattr(canvas, "image_list") or not isinstance(getattr(canvas, "image_list"), list):
		# may bite me in the ass later, but oh well
		canvas.image_list = []
	canvas.image_list.append(image)
	x, y = location
	image_id = canvas.create_image(x, y, image=image, anchor=anchor)
	return image_id

def clearCanvas(canvas):
	if(hasattr(canvas, "image_list")):
		del canvas.image_list 
	canvas.delete('all')
	return canvas

def createImage(imageOrDir):
	if isinstance(imageOrDir, Image.Image):
		return ImageTk.PhotoImage(imageOrDir)
	elif (imageOrDir is not None and os.path.isfile(imageOrDir)):
		return ImageTk.PhotoImage(file=imageOrDir)
	else:
		return None

def tryOpenImage(imageDir):
	if(imageDir is None or not os.path.isfile(imageDir)):
		return None
	else:
		return Image.open(imageDir)

'''
	This section is for the drawing and embelishment of the main campaign map
'''

def drawItem(canvas, itemDir, location=(0, 0), scale=1.0, anchor="nw", half_transparent=False, return_canvas_id=False, **kwargs):
	"""Draw an item on a specified canvas
		half_transparent: if specified, the image will have half transparency
		return_canvas_id: if specified, instead of returning the PhotoImage object, return the id of the object. Used for reusable items
	"""
	pil_image = tryOpenImage(itemDir)
	if(half_transparent):
		pil_image = img_half_transparent(pil_image)
	image = createImage(pil_image)
	item_id = drawOnCanvas(canvas, image, location=location, scale=scale, anchor=anchor)
	if(return_canvas_id):
		return item_id
	else:
		return image

def drawArrow(canvas, start_pos, end_pos, color="black", have_dash=False, arrow_end=False):
	item_id = canvas.create_line(*start_pos, *end_pos, fill=color, dash=(5, 5) if have_dash else None, arrow=tk.LAST if arrow_end else None)
	return item_id

def defaultRelationConfig(score):
	# return name & color of relation
	# TODO convert to a set querry, e.g return dict[score // 25]
	if(score <= 0):
		return "enemy", "red"
	elif(score <= 25):
		return "outraged", "orange"
	elif(score <= 75):
		return "cordial", "yellow"
	elif(score <= 100):
		return "friendly", "green"
	else:
		return "ally", "blue"

def findAnchor(vectorX, vectorY):
	"""Anchors used to put names on drawRelationGraph"""
	# return appropriate anchor of n/s-e/w basing on the vector from center to point
	# special: if vectorX or vectorY dominant, use n-s-e-w
	sizeY, sizeX = abs(vectorY), abs(vectorX)
	if(sizeY > sizeX * 10):
		return "n" if vectorY < 0 else "s"
	elif(sizeX > sizeY * 10):
		return "e" if vectorX > 0 else "w"
	# in other case, anchor at respective combined direction
	return ("n" if vectorY < 0 else "s") + ("e" if vectorX > 0 else "w")

def drawRelationGraph(canvas, relationScores, location=(0, 0), size=100.0):
	# support square size x size only
	assert len(relationScores) > 2, "Currently cannot draw scores for less than 3 factions"
	centerX, centerY = size / 2, size / 2
	# the size is reduced to make sure there is places for label
	size = min(size / 2 - 20, size / 3)
	numPoints = len(relationScores)
	lastScorePoint, lastMaxPoint = None, None
	for pointIdx, scoreTuple in enumerate(relationScores):
		# retarded me forgot that circle is 2pi
		radDegree = 2.0 * float(pointIdx) * math.pi / float(numPoints)
		factionName, score = scoreTuple
		scoreName, scoreColor = defaultRelationConfig(score)
		scoreLength = size * min(float(abs(score)) / 100.0, 1.0)
		# calculate the map and the positions of points relating to the centerPoint
		normalizedX, normalizedY = math.cos(radDegree), math.sin(radDegree)
		scoreX, scoreY = centerX + normalizedX * scoreLength, centerY + normalizedY * scoreLength
		maxX, maxY = centerX + normalizedX * size, centerY + normalizedY * size
		Debug.printDebug("score coord ({},{}), max coord ({},{})".format(scoreX, scoreY, maxX, maxY))
		# draw line from center to scorePoint in scoreColor; scorePoint to maxPoint in black
		canvas.create_line(centerX, centerY, scoreX, scoreY, fill=scoreColor)
		canvas.create_line(scoreX, scoreY, maxX, maxY, fill="black")
		# label the point with a tag and correct anchor
		label = tk.Label(canvas, text=scoreName)
		anchor=findAnchor(centerX - maxX, centerY - maxY)
		Debug.printDebug("score coord ({},{}), max coord ({},{})".format(scoreName, anchor, scoreX-maxX, scoreY-maxY))
		label.place(x=maxX, y=maxY, anchor=anchor)
		# draw the lines connecting to previous score/max points
		if(lastScorePoint is not None and lastMaxPoint is not None):
			canvas.create_line(scoreX, scoreY, lastScorePoint[0], lastScorePoint[1], fill="white")
			canvas.create_line(maxX, maxY, lastMaxPoint[0], lastMaxPoint[1], fill="black")
		else:
		# record the first point
			firstScorePoint, firstMaxPoint = (scoreX, scoreY), (maxX, maxY)
		lastScorePoint = scoreX, scoreY
		lastMaxPoint = maxX, maxY
	# do the drawing one last time to complete the whatever-gram
	canvas.create_line(scoreX, scoreY, firstScorePoint[0], firstScorePoint[1], fill="white")
	canvas.create_line(maxX, maxY, firstMaxPoint[0], firstMaxPoint[1], fill="black")
	return canvas
	
def drawTargetingArrow(canvas, coordinates, color="black"):
	assert isinstance(coordinates, tuple) and len(coordinates) == 4
	canvas.create_line(*coordinates, arrow=tk.LAST, fill=color)
	return canvas


'''
	This section is dedicated to the viewing of characters, vehicles and items with fluidly colored parts
'''

def showColorizableItem(canvas, itemString, items, processors=None, location=(0, 0)):
	# draw the pieces on the canvas, with each item being processed by the processor before drawing
	parts_dir = [itemString.format(item) for item in items]
	# create the parts as images
	if(processors is not None):
		assert(isinstance(processors, list) or callable(processors)), "processors is neither list nor callable. check - {}".format(processors)
		if(callable(processors)):
			process_fn = processors
			parts = [createImage(process_fn(part_dir)) for part_dir in parts_dir]
		else:
			assert len(parts_dir) == len(processors), "mismatch between processors and parts: {} - {}".format(parts_dir, processors)
			parts = [createImage(process_fn(part_dir)) for part_dir, process_fn in zip(parts_dir, processors)]
	else:
		parts = [createImage(part_dir) for part_dir in parts_dir]
	# draw images on canvas
	for part in parts:
		drawOnCanvas(canvas, part, location=location)
	return canvas

DEFAULT_ARMOR_LIST = list(range(12))
DEFAULT_ARMOR_LIST_NAME = ["outline_body", "outline_left_arm", "outline_right_arm", "fill_trim", "fill_deco", "fill_helmet", "fill_chest", "fill_half", "fill_quarter", "fill_left_backpack", "fill_right_backpack", "outline_scar"]
DEFAULT_ARMOR_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_ARMOR_LIST_NAME)}
# draw priority, so that the backpacks are drawn first
DEFAULT_ARMOR_DRAW_PRIORITY = [2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 1, 11]

DEFAULT_WEAPON_LIST = list(range(5))
DEFAULT_WEAPON_LIST_NAME = ["weapon_outline", "weapon_main", "weapon_deco", "weapon_arm", "weapon_opposite_arm"]
DEFAULT_WEAPON_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_WEAPON_LIST_NAME)}

DEFAULT_ACCESSORY_LIST = list(range(4))
DEFAULT_ACCESSORY_LIST_NAME = ["acc_outline", "acc_left", "acc_right", "acc_deco"]
DEFAULT_ACCESSORY_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_ACCESSORY_LIST_NAME)}

DEFAULT_VEHICLE_LIST = list(range(6))
DEFAULT_VEHICLE_LIST_NAME = ["frame", "primary", "secondary", "deco", "upper_hatch", "upper_hatch_color"]
DEFAULT_VEHICLE_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_VEHICLE_LIST_NAME)}

DEFAULT_V_WEAPON_LIST = list(range(4))
DEFAULT_V_WEAPON_LIST_NAME = ["frame", "primary", "secondary", "deco"]
DEFAULT_V_WEAPON_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_V_WEAPON_LIST_NAME)}


def showBlankVehicle(canvas, vehicleString, location=(0, 0)):
	items = DEFAULT_VEHICLE_LIST
	return showColorizableItem(canvas, vehicleString, items, location=location)

def showBlankArmor(canvas, armorString, location=(0, 0)):
	# show everything, uncolorized
	items = DEFAULT_ARMOR_LIST
	return showColorizableItem(canvas, armorString, items, location=location)

def showBlankWeapon(canvas, weaponString, location=(0, 0)):
	items = DEFAULT_WEAPON_LIST
	return showColorizableItem(canvas, weaponString, items, location=location)

def showBlankAccessory(canvas, accessoryString, location=(0, 0)):
	items = DEFAULT_ACCESSORY_LIST
	return showColorizableItem(canvas, accessoryString, items, location=location)

def showColorizedChassis(canvas, chassisString, colorScheme=None, location=(0, 0), keepHatch=True):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color)"
	scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color = colorScheme
	items = DEFAULT_VEHICLE_LIST
	primary_colorize = lambda partDir: colorize(tryOpenImage(partDir), primary_color)
	secondary_colorize = lambda partDir: colorize(tryOpenImage(partDir), secondary_color)
	deco_colorize = lambda partDir: colorize(tryOpenImage(partDir), deco_color)
	# do not color frames, otherwise do as needed
	processors = [do_nothing for item in items]
	processors[DEFAULT_VEHICLE_DICT_NAME["primary"]] = primary_colorize
	processors[DEFAULT_VEHICLE_DICT_NAME["secondary"]] = secondary_colorize
	processors[DEFAULT_VEHICLE_DICT_NAME["deco"]] = deco_colorize
	# upper hatch default to primary
	processors[DEFAULT_VEHICLE_DICT_NAME["upper_hatch_color"]] = primary_colorize
	if(not keepHatch):
		# remove the hatch
		items, processors = items[:-2], processors[:-2]
	return showColorizableItem(canvas, chassisString, items, processors=processors, location=location)

def showColorizedVehicleWeapons(canvas, vweaponString, colorScheme=None, location=(0, 0)):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color)"
	scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color = colorScheme
	items = DEFAULT_V_WEAPON_LIST
	primary_colorize = lambda partDir: colorize(tryOpenImage(partDir), primary_color)
	secondary_colorize = lambda partDir: colorize(tryOpenImage(partDir), secondary_color)
	deco_colorize = lambda partDir: colorize(tryOpenImage(partDir), deco_color)
	# do not color frames, otherwise do as needed
	processors = [do_nothing for item in items]
	processors[DEFAULT_V_WEAPON_DICT_NAME["primary"]] = primary_colorize
	processors[DEFAULT_V_WEAPON_DICT_NAME["secondary"]] = secondary_colorize
	processors[DEFAULT_V_WEAPON_DICT_NAME["deco"]] = deco_colorize
	return showColorizableItem(canvas, vweaponString, items, processors=processors, location=location)

def showColorizedArmor(canvas, armorString, colorScheme=None, location=(0,0), omitItems=[]):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color)"
	scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color = colorScheme
	items = DEFAULT_ARMOR_LIST
	primary_colorize = lambda partDir: colorize(tryOpenImage(partDir), primary_color)
	secondary_colorize = lambda partDir: colorize(tryOpenImage(partDir), secondary_color)
	deco_colorize = lambda partDir: colorize(tryOpenImage(partDir), deco_color)
	trim_colorize = lambda partDir: colorize(tryOpenImage(partDir), trim_color)
	processors = [primary_colorize for item in items]
	# the outline is always colorized in primary with care, fill_deco and fill_trim respectly in theirs color
	processors[DEFAULT_ARMOR_DICT_NAME["outline_body"]] = lambda partDir: colorize(tryOpenImage(partDir), primary_color, careful=True)
	processors[DEFAULT_ARMOR_DICT_NAME["fill_deco"]] = deco_colorize
	processors[DEFAULT_ARMOR_DICT_NAME["fill_trim"]] = trim_colorize
	processors[DEFAULT_ARMOR_DICT_NAME["outline_scar"]] = lambda partDir: tryOpenImage(partDir)
	
	if(scheme == "chest"):
		# only change in the chest
		kept_item = "fill_chest"
		processors[DEFAULT_ARMOR_DICT_NAME[kept_item]] = secondary_colorize
	elif(scheme == "half"):
		# change the half, right arm and right backpack
		kept_item = "fill_half"
		processors[DEFAULT_ARMOR_DICT_NAME[kept_item]] = secondary_colorize
		processors[DEFAULT_ARMOR_DICT_NAME["outline_right_arm"]] = secondary_colorize
		processors[DEFAULT_ARMOR_DICT_NAME["fill_right_backpack"]] = secondary_colorize
	elif(scheme == "quarter"):
		# change the half, right arm and right backpack
		kept_item = "fill_quarter"
		processors[DEFAULT_ARMOR_DICT_NAME[kept_item]] = secondary_colorize
		processors[DEFAULT_ARMOR_DICT_NAME["outline_right_arm"]] = secondary_colorize
		processors[DEFAULT_ARMOR_DICT_NAME["fill_right_backpack"]] = secondary_colorize
	elif(scheme == "helmet"):
		# only change for the helmet
		kept_item = "fill_helmet"
		processors[DEFAULT_ARMOR_DICT_NAME[kept_item]] = secondary_colorize
	else:
		raise ValueError("Unrecognized scheme: {}".format(scheme))
	# remove all other color_item
	for color_item in ["fill_chest", "fill_half", "fill_quarter", "fill_helmet"]:
		if(color_item != kept_item):
			processors[DEFAULT_ARMOR_DICT_NAME[color_item]] = omit
	
	# remove items specified in the list
	for item in omitItems:
		processors[DEFAULT_ARMOR_DICT_NAME[item]] = omit
	# reorder 
	_, items, processors = zip( *sorted(zip(DEFAULT_ARMOR_DRAW_PRIORITY, items, processors), key=lambda item: item[0]) )
	items = list(items)
	processors = list(processors)
	# show item
	return showColorizableItem(canvas, armorString, items, processors=processors, location=location)

def showColorizedWeapon(canvas, weaponString, colorScheme=None, location=(0,0), reverse=False):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color)"
	scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color = colorScheme
	items = list(DEFAULT_WEAPON_LIST)
	processors = [None for _ in items]
	if(scheme == "half" or scheme == "quarter"):
		arm_color = secondary_color
		other_arm_color = primary_color
	else:
		arm_color = other_arm_color = primary_color
	if(reverse):
		# left arm is always primary
		arm_color, other_arm_color = other_arm_color, arm_color
	if(reverse):
		# in reverse, flip every item if applicable
		try_open_image = lambda partDir: flipper(tryOpenImage(partDir))
	else:
		try_open_image = tryOpenImage
	# show item
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_outline"]] = lambda partDir: try_open_image(partDir)
	# the outline is uncolored, while deco and main, and opposite is colored with deco and primary/secondary depending on the circumstances
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_deco"]] = lambda partDir: colorize(try_open_image(partDir), deco_color)
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_main"]] = lambda partDir: colorize(try_open_image(partDir), weapon_color)
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_arm"]] = lambda partDir: colorize(try_open_image(partDir), arm_color)
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_opposite_arm"]] = lambda partDir: colorize(try_open_image(partDir), other_arm_color)
	return showColorizableItem(canvas, weaponString, items, processors=processors, location=location)

def showColorizedAccessory(canvas, accessoryString, colorScheme=None, location=(0, 0)):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color)"
	scheme, primary_color, secondary_color, trim_color, deco_color, weapon_color = colorScheme
	items = list(DEFAULT_ACCESSORY_LIST)
	processors = [None for _ in items]
	if(scheme == "half" or scheme == "quarter"):
		left_color = secondary_color
	else:
		left_color = primary_color
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_outline"]] = lambda partDir: tryOpenImage(partDir)
	# the outline is uncolored, while deco and main is colored with deco and acc respectively
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_deco"]] = lambda partDir: colorize(tryOpenImage(partDir), deco_color)
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_left"]] = lambda partDir: colorize(tryOpenImage(partDir), primary_color)
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_right"]] = lambda partDir: colorize(tryOpenImage(partDir), left_color)
	return showColorizableItem(canvas, accessoryString, items, processors=processors, location=location)

DEFAULT_ARMOR_DIR = ""
def showColorizedFullGears(canvas, resourceDict, colorScheme=None, location=(0,0)):
	# bundle the drawing together
	# TODO add default option for armor
	armor_dir = resourceDict.get("armor_dir", DEFAULT_ARMOR_DIR)
	armor_omission = resourceDict.get("armor_omit", [])
	accessory_overlay_flag = resourceDict.get("accessory_is_overlay", False)

	if(not accessory_overlay_flag and "accessory_dir" in resourceDict):
		# accessory is underlay here
			canvas = showColorizedAccessory(canvas, resourceDict["accessory_dir"], colorScheme=colorScheme, location=location)
	# draw armor and weapons
	canvas = showColorizedArmor(canvas, armor_dir, colorScheme=colorScheme, location=location, omitItems=armor_omission)
	if(accessory_overlay_flag and "accessory_dir" in resourceDict):
		# accessory is overlay here
		canvas = showColorizedAccessory(canvas, resourceDict["accessory_dir"], colorScheme=colorScheme, location=location)
		
	if("main_dir" in resourceDict):
		canvas = showColorizedWeapon(canvas, resourceDict["main_dir"], colorScheme=colorScheme, location=location, reverse=False)
	if("secondary_dir" in resourceDict):
		canvas = showColorizedWeapon(canvas, resourceDict["secondary_dir"], colorScheme=colorScheme, location=location, reverse=True)
	
	return canvas

def flipper(image):
	if(image is None):
		return None
	image = image.transpose(Image.FLIP_LEFT_RIGHT)
	return image

def colorize(image, color, careful=False):
	if(image is None):
		return None
	# convert with alpha
	red, green, blue, alpha = image.split()
	# if in careful mode, split the image further to white and non-white part and only colorize the white
	if(careful):
		r, g, b = red.load(), green.load(), blue.load()
		width, height = image.size
		
		for w in range(width):
			for h in range(height):
				if(r[w, h] < 255 or g[w, h] < 255 or b[w, h] < 255):
					# not white, record it to g as alpha
					g[w, h] = 0
				else:
					# white, record alpha to 0 and let red decide
					g[w, h] = 255
		# using the new white/alpha channel, create the mask to be colorized
#		colorizePart = Image.merge('LA', (r, g))
		# rip the alpha out again to colorize
		white = red
		secondary_alpha = green
		colorize_part = ImageOps.colorize(white, "black", color)
		colorize_part.putalpha(secondary_alpha)
		# paste the colorized part over the image
		image = Image.alpha_composite(image, colorize_part)
	else:
		image = image.convert('L')
		image = ImageOps.colorize(image, "black", color)
	# put the alpha back
	image.putalpha(alpha)
	return image

def img_half_transparent(image, transparency=0.5):
	if(image is None):
		return None
	# create a transparent mask to hog it over
	alpha_mask = image.split()[-1].point(lambda c: c * transparency)
	image.putalpha(alpha_mask)
	return image


def omit(image):
	return None

def do_nothing(image):
	return image
