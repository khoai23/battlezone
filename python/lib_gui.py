# This lib will use tk to display
import tkinter as tk
import python.lib_image as imageLib
import python.lib_text as textLib
import python.manager as managerLib
import os, random

def testWindow():
	window = createWindow()
	mainLayout(window)
	window.after(ms=10000, func=lambda : window.destroy())
	window.mainloop()

def createWindow(screenName="Game", size=None):
	# create a window with a specific screenName
	window = tk.Tk()
	window.title(screenName)
	if(size is not None):
		window.geometry(size)
	return window

def mainLayout(window):
	assert isinstance(window, tk.Tk)
	# load all manager
	statManager = managerLib.IndividualStatManager("./res/data/AstartesStat.json")
	itemManager = managerLib.ItemManager("./res/data/ItemData.json", "./res/texture")
	enemyManager = managerLib.EnemyManager("./res/data/EnemyData.json")
	# create the layout
	# for now, just use a simple grid
	portrait = createPortrait(window)
	portrait.grid(row=0, column=0)
	dipWindow = createDiplomacyPane(window)
	dipWindow.grid(row=1, column=0)
	starMap = campaignMap(window)
	starMap.grid(row=0, column=1, columnspan=2)
	textPane, addTextFunc = conversationPanel(window)
	textPane.grid(row=1, column=1)
	addTextFunc("<ally>Advisor: <\\ally>Chapter Master, welcome back.")
	interactPane = interactionPanel(window, textFunc=addTextFunc, managers=(itemManager, statManager, enemyManager, ))
	interactPane.grid(row=1, column=2)

def drawAstartesOnCanvas(canvas, astartes):
	print("Will lose banner")
	colorScheme = ("quarter", "red", "blue", "gold", "brown", "cyan")
	canvas = imageLib.clearCanvas(canvas)
	canvas = imageLib.showColorizedFullGears(canvas, astartes.getDisplayData(), colorScheme=colorScheme)
	return canvas

def createPortrait(window):
	# TODO import the default size from other module when complete
	portrait_width = 167.0
	portrait_height = 232.0
	portrait = tk.Canvas(master=window, width=portrait_width, height=portrait_height + portrait_width)
	# hard_coded images and info
	armorDir = "./res/texture/view_unit/errant_{:d}.png"
	weaponRightDir = "./res/texture/view_weapon/weapon_plasma_{:d}.png"
	weaponLeftDir = "./res/texture/view_weapon/weapon_paxe_{:d}.png"
	accessoryDir = "./res/texture/view_unit/acc_halo_{:d}.png"
	colorScheme = ("quarter", "red", "blue", "gold", "brown", "cyan")
	drawDict = {
		"armor_dir": armorDir, "main_dir": weaponRightDir, "secondary_dir": weaponLeftDir, "accessory_dir": accessoryDir, "accessory_is_overlay": False, "armor_omit": ["outline_left_arm", "outline_scar"]
	}
	# view the character
	portrait = imageLib.showColorizedFullGears(portrait, drawDict, colorScheme=colorScheme)
	# view a resized custom badge
	bannerImage = imageLib.createImage("./res/texture/eye.png")
	bannerImage = imageLib.drawOnCanvas(portrait, bannerImage, location=(int(portrait_width / 2), int(portrait_height + portrait_width / 2)), anchor="center")
	# view the 
	return portrait

def createDiplomacyPane(window):
	dipWidth, dipHeight = 200, 100
	relationScores = [("IG", 125), ("Eldar", -26), ("Mechanicus", 50), ("Orks", -66), ("Chaos", 40), ("Inquisition", 83)]
	diplomacyFrame = tk.Frame(window)
	dipImage = tk.Canvas(master=diplomacyFrame, width=dipHeight * 2, height=dipHeight * 2)
	dipImage = imageLib.drawRelationGraph(dipImage, relationScores, location=(0, 0), size=dipHeight * 2)
	dipImage.pack()
	return diplomacyFrame

def campaignMap(window):
	safe, neutral, dangerous, stop = "green", "yellow", "red", "white"
	# try to make a star map of interconnecting systems
	mapWidth = 640
	mapHeight = 480
	starMap = tk.Canvas(master=window, width=mapWidth, height=mapHeight)
	backgroundImage = imageLib.drawItem(starMap, "./res/texture/bg_star.jpg", scale=0.5)
#	mapWidth, mapHeight = backgroundImage.width(), backgroundImage.height()
	systemDir = "./res/texture/starmap/star_{:d}.png"
	# generate random systems
	systemList = []
	for i in range(10):
		x, y = random.random() * mapWidth, random.random() * mapHeight
		systemType = random.randint(0, 6)
		imageLib.drawItem(starMap, systemDir.format(systemType), location=(x, y), anchor="center")
		systemList.append((systemType, x, y))
	# link up base on distances to each other
	# neutral on these
	route = {}
	routeKey = "{:d}-{:d}"
	getKey = lambda s1, s2: routeKey.format(s1, s2) if (s1 < s2) else routeKey.format(s2, s1)
	for idx, sys in enumerate(systemList):
		others = list(systemList)
		others.pop(idx)
		others = list(enumerate(others))
#		print(others)
		closestIdx, closest = min(others, key=lambda other: (sys[1]-other[1][1]) ** 2 + (sys[2]-other[1][2]) ** 2)
		key = getKey(idx, closestIdx)
		if(key not in route):
			# neutral
			route[key] = 1
			starMap.create_line(sys[1], sys[2], closest[1], closest[2], fill=safe, dash=(3,1))

#		print(x, y, systemType)
	starMap.update()
	
	return starMap

def conversationPanel(window):
	pane = tk.Frame(master=window)
	textPane = textLib.createTextWithScrollbar(pane)
	addTextFunc = lambda text: textLib.addFormatText(textPane, text)
	return pane, addTextFunc

def interactionPanel(window, textFunc=None, managers=None):
	assert textFunc is not None and managers is not None
	pane = tk.Frame(master=window)
	# pane is grid of buttons to interact with the game
	statManager = managers[1]
	enemyManager = managers[2]
	def callbackDrawAndAddText():
		# write message of new astartes
		astartes = statManager.createAstartes(level=35)
		choppaBoy = enemyManager.createIndividual("choppaboy")
		message = "<system>Test: <\\system>New Astartes created: {} - {:s}, HP/WS/BS/I/EXP: [{:.2f}/{:.2f}]/{:.2f}/{:.2f}/{:.2f}/{:.1f}".format(astartes, astartes.name, astartes.current_hp, astartes.base_hp, astartes.ws, astartes.bs, astartes.i, astartes.exp)
		textFunc(message)
		# write message about the ork
		message = "<system>Test: <\\system>New Choppa Boy created:{:s} - {}".format(choppaBoy.name, choppaBoy)
		textFunc(message)
#		drawAstartesOnCanvas(portrait, astartes)
#		textFunc("<system>Test: <\\system>Replaced the portrait with the new one")

	gameButton = tk.Button(pane, text="Test", command=lambda: defaultGameDialog(window, DefaultDialogBox, callback=callbackDrawAndAddText))
	gameButton.grid(row=0, column=0)

	itemManager = managers[0]
	armoryButton = tk.Button(pane, text="Armory", command=lambda: defaultGameDialog(window, ArmoryDialogBox, itemManagerObj=itemManager))
	armoryButton.grid(row=0, column=1)
	return pane

class DefaultDialogBox(tk.Toplevel):
	def __init__(self, master):
		super(DefaultDialogBox, self).__init__(master=master)
	
	def exit(self):
		self.destroy()

def defaultGameDialog(window, DialogBoxClass, callback=None, **kwargs):
	# assume the thing is inheriting from tk.Toplevel
	dialogBox = DialogBoxClass(window, **kwargs)
	window.wait_window(dialogBox)
	if(callback):
		callback()

class ArmoryDialogBox(DefaultDialogBox):
	def __init__(self, master, itemManagerObj=None):
		super(ArmoryDialogBox, self).__init__(master=master)
		assert itemManagerObj is not None
		self._itemManager = itemManagerObj
		# Construct the table of all items
		# TODO add a sort of scrollbar, as we are encountering overflow
		self._armoryPane = tk.Frame(master=self)
		self._counterLabels = []
		def button_command(item): 
			self._itemManager.changeItemCounts(item, 1)
			self.updateArmoryCount()

		for item_idx, (item, item_count) in enumerate(zip(self._itemManager.armoryItems, self._itemManager.armoryCounts)):
			itemLabel = tk.Label(master=self._armoryPane, text=item.name)
			itemLabel.grid(row=item_idx, column=0)
			counterLabel = tk.Label(master=self._armoryPane, text=item_count)
			counterLabel.grid(row=item_idx, column=1)
			self._counterLabels.append(counterLabel)
			# BLACK MAGIC LAMBDA!
			# just kidding, making a lambda with this default value would capture the item at compile time, hence solving the issue
			addButton = tk.Button(master=self._armoryPane, text="Add", command=lambda x=item: button_command(x))
			addButton.grid(row=item_idx, column=2)
		self._armoryPane.pack()

	def updateArmoryCount(self):
#		print(self._itemManager.armoryCounts)
		for label, value in zip(self._counterLabels, self._itemManager.armoryCounts):
			label.config(text=value)

class LoadoutDialogBox(DefaultDialogBox):
	def __init__(self, master, itemManagerObj=None, statManagerObj=None):
		super(LoadoutDialogBox, self).__init__(master=master)
		assert itemManagerObj and statManagerObj

testWindow()

