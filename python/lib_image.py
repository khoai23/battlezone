# here will handle all images processing
from tkinter import PhotoImage, Label
import PIL.Image as Image
import PIL.ImageOps as ImageOps
import PIL.ImageTk as ImageTk
import os, math

def drawOnCanvas(canvas, image, location=(0,0), scale=1.0, anchor="nw"):
	if(image is None):
		return canvas
#	print(type(image), vars(image))
	# due to a tkinter bug, canvas must keep track of images that was drawn into it.
	if not hasattr(canvas, "imageList") or not isinstance(getattr(canvas, "imageList"), list):
		# may bite me in the ass later, but oh well
		canvas.imageList = []
	canvas.imageList.append(image)
	x, y = location
	canvas.create_image(x, y, image=image, anchor=anchor)
	return image

def clearCanvas(canvas):
	if(hasattr(canvas, "imageList")):
		del canvas.imageList 
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

def drawItem(canvas, itemDir, location=(0, 0), scale=1.0, anchor="nw"):
	image = createImage(itemDir)
	drawOnCanvas(canvas, image, location=location, scale=scale, anchor=anchor)
	return image

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
		print(scoreX, scoreY, maxX, maxY)
		# draw line from center to scorePoint in scoreColor; scorePoint to maxPoint in black
		canvas.create_line(centerX, centerY, scoreX, scoreY, fill=scoreColor)
		canvas.create_line(scoreX, scoreY, maxX, maxY, fill="black")
		# label the point with a tag and correct anchor
		label = Label(canvas, text=scoreName)
		anchor=findAnchor(centerX - maxX, centerY - maxY)
		print(scoreName, anchor, scoreX-maxX, scoreY-maxY)
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
	


'''
	This section is dedicated to the viewing of characters, vehicles and items with fluidly colored parts
'''

def showColorizableItem(canvas, itemString, items, processors=None, location=(0, 0)):
	# draw the armor pieces on the canvas, with each item being processed by the processor before drawing
	partsDir = [itemString.format(item) for item in items]
	# create the parts as images
	if(processors is not None):
		assert(isinstance(processors, list) or callable(processors)), "processors is neither list nor callable. check - {}".format(processors)
		if(callable(processors)):
			processFn = processors
			parts = [createImage(processFn(partDir)) for partDir in partsDir]
		else:
			assert len(partsDir) == len(processors), "mismatch between processors and parts: {} - {}".format(partsDir, processors)
			parts = [createImage(processFn(partDir)) for partDir, processFn in zip(partsDir, processors)]
	else:
		parts = [createImage(partDir) for partDir in partsDir]
	# draw images on canvas
	for part in parts:
		drawOnCanvas(canvas, part, location=location)
	return canvas

DEFAULT_ARMOR_LIST = list(range(12))
DEFAULT_ARMOR_LIST_NAME = ["outline_body", "outline_left_arm", "outline_right_arm", "fill_trim", "fill_deco", "fill_helmet", "fill_chest", "fill_half", "fill_quarter", "fill_left_backpack", "fill_right_backpack", "outline_scar"]
DEFAULT_ARMOR_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_ARMOR_LIST_NAME)}

DEFAULT_WEAPON_LIST = list(range(4))
DEFAULT_WEAPON_LIST_NAME = ["weapon_outline", "weapon_main", "weapon_deco", "weapon_arm"]
DEFAULT_WEAPON_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_WEAPON_LIST_NAME)}

DEFAULT_ACCESSORY_LIST = list(range(4))
DEFAULT_ACCESSORY_LIST_NAME = ["acc_outline", "acc_left", "acc_right", "acc_deco"]
DEFAULT_ACCESSORY_DICT_NAME = {name:idx for idx, name in enumerate(DEFAULT_ACCESSORY_LIST_NAME)}

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

def showColorizedArmor(canvas, armorString, colorScheme=None, location=(0,0), omitItems=[]):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor)"
	scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor = colorScheme
	items = DEFAULT_ARMOR_LIST
	primaryColorize = lambda partDir: colorize(tryOpenImage(partDir), primaryColor)
	secondaryColorize = lambda partDir: colorize(tryOpenImage(partDir), secondaryColor)
	processors = [primaryColorize for item in items]
	# the outline is always colorized in primary with care, fill_deco and fill_trim respectly in theirs color
	processors[DEFAULT_ARMOR_DICT_NAME["outline_body"]] = lambda partDir: colorize(tryOpenImage(partDir), primaryColor, careful=True)
	processors[DEFAULT_ARMOR_DICT_NAME["fill_deco"]] = lambda partDir: colorize(tryOpenImage(partDir), decoColor)
	processors[DEFAULT_ARMOR_DICT_NAME["fill_trim"]] = lambda partDir: colorize(tryOpenImage(partDir), trimColor)
	processors[DEFAULT_ARMOR_DICT_NAME["outline_scar"]] = lambda partDir: tryOpenImage(partDir)
	
	if(scheme == "chest"):
		# only change in the chest
		processors[DEFAULT_ARMOR_DICT_NAME["fill_chest"]] = secondaryColorize
	elif(scheme == "half"):
		# change the half, right arm and right backpack
		processors[DEFAULT_ARMOR_DICT_NAME["fill_half"]] = secondaryColorize
		processors[DEFAULT_ARMOR_DICT_NAME["outline_right_arm"]] = secondaryColorize
		processors[DEFAULT_ARMOR_DICT_NAME["fill_right_backpack"]] = secondaryColorize
	elif(scheme == "quarter"):
		# change the half, right arm and right backpack
		processors[DEFAULT_ARMOR_DICT_NAME["fill_quarter"]] = secondaryColorize
		processors[DEFAULT_ARMOR_DICT_NAME["outline_right_arm"]] = secondaryColorize
		processors[DEFAULT_ARMOR_DICT_NAME["fill_right_backpack"]] = secondaryColorize
	else:
	#if(scheme == "helmet"):
		# only change for the helmet
		processors[DEFAULT_ARMOR_DICT_NAME["fill_helmet"]] = secondaryColorize
	
	# remove items specified in the list
	for item in omitItems:
		processors[DEFAULT_ARMOR_DICT_NAME[item]] = omit
	
	# show item
	return showColorizableItem(canvas, armorString, items, processors=processors, location=location)

def showColorizedWeapon(canvas, weaponString, colorScheme=None, location=(0,0), reverse=False):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor)"
	scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor = colorScheme
	items = list(DEFAULT_WEAPON_LIST)
	processors = [None for _ in items]
	arm_color = primaryColor
	if(scheme == "half" or scheme == "quarter"):
		armColor = secondaryColor
	if(reverse):
		# left arm is always primary
		armColor = primaryColor
	if(reverse):
		# in reverse, flip every item if applicable
		useTryOpenImage = lambda partDir: flipper(tryOpenImage(partDir))
	else:
		useTryOpenImage = tryOpenImage
	# show item
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_outline"]] = lambda partDir: useTryOpenImage(partDir)
	# the outline is uncolored, while deco and main is colored with deco and weapon respectively
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_deco"]] = lambda partDir: colorize(useTryOpenImage(partDir), decoColor)
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_main"]] = lambda partDir: colorize(useTryOpenImage(partDir), weaponColor)
	processors[DEFAULT_WEAPON_DICT_NAME["weapon_arm"]] = lambda partDir: colorize(useTryOpenImage(partDir), armColor)
	return showColorizableItem(canvas, weaponString, items, processors=processors, location=location)

def showColorizedAccessory(canvas, accessoryString, colorScheme=None, location=(0, 0)):
	assert len(colorScheme) == 6, "colorScheme must be a tuple of (scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor)"
	scheme, primaryColor, secondaryColor, trimColor, decoColor, weaponColor = colorScheme
	items = list(DEFAULT_ACCESSORY_LIST)
	processors = [None for _ in items]
	if(scheme == "half" or scheme == "quarter"):
		leftColor = secondaryColor
	else:
		leftColor = secondaryColor
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_outline"]] = lambda partDir: tryOpenImage(partDir)
	# the outline is uncolored, while deco and main is colored with deco and acc respectively
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_deco"]] = lambda partDir: colorize(tryOpenImage(partDir), decoColor)
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_left"]] = lambda partDir: colorize(tryOpenImage(partDir), primaryColor)
	processors[DEFAULT_ACCESSORY_DICT_NAME["acc_right"]] = lambda partDir: colorize(tryOpenImage(partDir), leftColor)
	return showColorizableItem(canvas, accessoryString, items, processors=processors, location=location)

DEFAULT_ARMOR_DIR = ""
def showColorizedFullGears(canvas, resourceDict, colorScheme=None, location=(0,0)):
	# bundle the drawing together
	# TODO add default option for armor
	armorDir = resourceDict.get("armor_dir", DEFAULT_ARMOR_DIR)
	armorOmission = resourceDict.get("armor_omit", [])
	accessoryOverlayFlag = resourceDict.get("accessory_is_overlay", False)

	if(not accessoryOverlayFlag and "accessory_dir" in resourceDict):
		# accessory is underlay here
			canvas = showColorizedAccessory(canvas, resourceDict["accessory_dir"], colorScheme=colorScheme, location=location)
	# draw armor and weapons
	canvas = showColorizedArmor(canvas, armorDir, colorScheme=colorScheme, location=location, omitItems=armorOmission)
	if(accessoryOverlayFlag and "accessory_dir" in resourceDict):
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
		secondaryAlpha = green
		colorizePart = ImageOps.colorize(white, "black", color)
		colorizePart.putalpha(secondaryAlpha)
		# paste the colorized part over the image
		image = Image.alpha_composite(image, colorizePart)
	else:
		image = image.convert('L')
		image = ImageOps.colorize(image, "black", color)
	# put the alpha back
	image.putalpha(alpha)
	return image

def omit(image):
	return None
