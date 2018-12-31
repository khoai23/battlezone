# This lib will use tk to display
import tkinter as tk
import tkinter.ttk as ttk
import python.lib_image as imageLib
import python.lib_text as textLib
import python.manager as managerLib
from python.utils import Debug
import os, random

def testWindow():
	window = createWindow()
	mainLayout(window)
#	window.after(ms=10000, func=lambda : window.destroy())
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
#	Debug.changeMessageLevel("INFO")
	# load all manager
#	statManager = managerLib.IndividualStatManager("./res/data/AstartesStat.json")
#	itemManager = managerLib.ItemManager("./res/data/ItemData.json", "./res/texture")
#	enemyManager = managerLib.EnemyManager("./res/data/EnemyData.json")
#	combatManager = managerLib.CombatManager("./res/data/CombatTactic.json")
	# have single overall manager
#	color_scheme = ("half", "red", "blue", "gold", "brown", "cyan")
	manager = managerLib.OverallManager("Generic Chapter", "Idunno Company")
	manager.createDefaultCompany()
	# create the layout
	# for now, just use a simple grid
	portrait = createPortrait(window, companyCommander=manager.company.commander, colorScheme=manager.colorScheme)
	portrait.grid(row=0, column=0)
	dipWindow = createDiplomacyPane(window)
	dipWindow.grid(row=1, column=0)
	starMap = campaignMap(window)
	starMap.grid(row=0, column=1, columnspan=2)
	textPane, addTextFunc = conversationPanel(window)
	textPane.grid(row=1, column=1)
	addTextFunc("<ally>Advisor: <\\ally>Chapter Master, welcome back.")
	interactPane = interactionPanel(window, textFunc=addTextFunc, overallManager=manager)
	interactPane.grid(row=1, column=2)

def createPortrait(window, companyCommander=None, colorScheme=None):
	# TODO import the default size from other module when complete
	portrait = tk.Canvas(master=window, width=imageLib.DEFAULT_CANVAS_WIDTH, height=imageLib.DEFAULT_CANVAS_WIDTH + imageLib.DEFAULT_CANVAS_HEIGHT)
	# view the company commander
	drawDict = companyCommander.getDisplayData()
	portrait = imageLib.showColorizedFullGears(portrait, drawDict, colorScheme=colorScheme)
	# view a resized custom badge
	bannerImage = imageLib.createImage("./res/texture/eye.png")
	bannerImage = imageLib.drawOnCanvas(portrait, bannerImage, location=(int(imageLib.DEFAULT_CANVAS_WIDTH / 2), int(imageLib.DEFAULT_CANVAS_HEIGHT + imageLib.DEFAULT_CANVAS_WIDTH / 2)), anchor="center")
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
#		Debug.printDebug(others)
		closestIdx, closest = min(others, key=lambda other: (sys[1]-other[1][1]) ** 2 + (sys[2]-other[1][2]) ** 2)
		key = getKey(idx, closestIdx)
		if(key not in route):
			# neutral
			route[key] = 1
			starMap.create_line(sys[1], sys[2], closest[1], closest[2], fill=safe, dash=(3,1))

#		Debug.printDebug(x, y, systemType)
	starMap.update()
	
	return starMap

def conversationPanel(window):
	pane = tk.Frame(master=window)
	textPane = textLib.createTextWithScrollbar(pane)
	addTextFunc = lambda text: textLib.addFormatText(textPane, text)
	return pane, addTextFunc

def interactionPanel(window, textFunc=None, overallManager=None):
	assert textFunc is not None and overallManager is not None
	pane = tk.Frame(master=window)
	# pane is grid of buttons to interact with the game
	itemManager = overallManager.itemManager
	statManager = overallManager.statManager
	enemyManager = overallManager.enemyManager
	combatManager = overallManager.combatManager
	# create the random dude for dress-up game
	astartes = overallManager.company.commander
	def callbackDrawAndAddText():
		# write message of new astartes
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

	armoryButton = tk.Button(pane, text="Armory", command=lambda: defaultGameDialog(window, ArmoryDialogBox, itemManagerObj=itemManager))
	armoryButton.grid(row=0, column=1)

	equipButton = tk.Button(pane, text="Equip", command=lambda: defaultGameDialog(window, LoadoutDialogBox, target=astartes, colorScheme=overallManager.colorScheme, itemManagerObj=itemManager, statManagerObj=statManager))
	equipButton.grid(row=1, column=0)

	def sendWholeCompanyToTestMission():
		# load a predetermined mission
		testMission = overallManager.enemyManager.createMission("m_ork_base_1")
		# load the entire company as yourUnits
		yourUnits = overallManager.company.squads
		# run defaultGameDialog
		defaultGameDialog(BattleDialogBox, yourUnits=yourUnits, battleManagerObj=combatManager, mission=testMission)
	# bind the button with your object
	combatButton = tk.Button(pane, text="Combat", command=sendWholeCompanyToTestMission)
	combatButton.grid(row=1, column=1)
		
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
		Debug.printDebug(self._itemManager.armoryCounts)
		for label, value in zip(self._counterLabels, self._itemManager.armoryCounts):
			label.config(text=value)

class LoadoutDialogBox(DefaultDialogBox):
	LIST_SELECTOR_CONFIG = [("main", "weapon"), ("secondary", "weapon"), ("armor", "armor"), ("accessory", "accessory")]
	EMPTY_STR = "Empty"
	def __init__(self, master, target=None, colorScheme=None, itemManagerObj=None, statManagerObj=None):
		super(LoadoutDialogBox, self).__init__(master=master)
		assert itemManagerObj and statManagerObj
		self._itemManager = itemManagerObj
		self._statManager = statManagerObj
		self._target = target
		self._target_original_equipments = dict(astartes.equipments)
		self.colorScheme = colorScheme
		# create the canvas and selections
		self._mainCanvas = tk.Canvas(master=self, width=imageLib.DEFAULT_CANVAS_WIDTH, height=imageLib.DEFAULT_CANVAS_HEIGHT)
		# pack it at correct positions
		self._mainCanvas.grid(row=0, column=0, rowspan=5)
		# keep a reference to populate
		self._listSelectors = [self._constructOptionMenu(slot, slotItemType, defaultObj=self._target.equipments[slot]) for slot, slotItemType in LoadoutDialogBox.LIST_SELECTOR_CONFIG]
		# default values
		for i, selectorTuple in enumerate(self._listSelectors):
			selectorSlot, selectorType, selectorStringVar, selectorObj = selectorTuple
			# grid
			selectorObj.grid(row=i, column=1, columnspan=2)
			# set and add tracing
			selectorStringVar.trace("w", self.onModifyEvent)
		# endbox
		okButton = tk.Button(master=self, text="OK", command=lambda: self.exit(cancel=False))
		okButton.grid(row=4, column=1)
		cancelButton = tk.Button(master=self, text="Cancel", command=lambda: self.exit(cancel=True))
		cancelButton.grid(row=4, column=2)
		self._changeDisplay()

	def _constructOptionMenu(self, slot, slotItemType, defaultObj=None):
		"""For each slot and its item type, create a tuple of (slot, itemType, stringVar, OptionMenu)"""
		displayVar = tk.StringVar(self)
		if(defaultObj):
			displayVar.set(defaultObj.name)
		else:
			displayVar.set(LoadoutDialogBox.EMPTY_STR)
		itemsAndCounts = self._itemManager.getItemsByType(slotItemType, available=False)
		slotSelector = tk.OptionMenu(self, displayVar, LoadoutDialogBox.EMPTY_STR, *[item.name for item, count in itemsAndCounts])
		return (slot, slotItemType, displayVar, slotSelector)

	def _populateChoices(self):
		# update the equipments using the list
		# TODO use this to limit the wargear access using statManager
		for itemSlot, itemType, itemStringVar, _ in self._listSelectors:
			itemName = itemStringVar.get()
			self._target.equipments[itemSlot] = self._itemManager.searchItemByName(itemName, itemType)
		return

	def _changeDisplay(self):
		# equip the astartes by the selected items
		# equip all that in the selector
		# change
		self._mainCanvas = imageLib.clearCanvas(self._mainCanvas)
		self._mainCanvas = imageLib.showColorizedFullGears(self._mainCanvas, self._target.getDisplayData(), colorScheme=self.colorScheme)
#		self._mainCanvas.update()

	def onModifyEvent(self, *args):
		Debug.printDebug("Event called from: {}".format(args))
		self._populateChoices()
		self._changeDisplay()

	def exit(self, cancel=True):
		if(cancel):
			# reset all changes
			self._target.equipments = self._target_original_equipments
			Debug.printDebug("Revert to target original equipments: ", self._target.equipments)
		else:
			Debug.printDebug("Target changed from set {} to set {}".format(self._target_original_equipments, self._target.equipments))
		self.destroy()

class BattleDialogBox(DefaultDialogBox):
	NO_ORDER_STR = "No Order"
	END_TURN_STR = "End Turn"
	def __init__(self, master, yourUnits=None, battleManagerObj=None, missionObj=None):
		super(ArmoryDialogBox, self).__init__(master=master)
		assert unitManagerObj is not None and mission is not None
		self._yourUnits = yourUnits
		self._battleManager = battleManagerObj
		self._mission = missionObj

		self._constructBattleList()
		self._updateComponent()

	def _constructBattleList(self):
		# for now, just have a text box for vox channel, each unit of friendly get nametag + status (label), healthbar (progressbar), and tactic selector to override the usual 
		vox_panel = tk.Frame(master=self)
		vox_panel.grid(row=0, column=0)
		self._voxLog = textLib.createTextWithScrollbar(vox_panel)
		self._moveTurnButton = tk.Button(master=vox_panel, text=BattleDialogBox.END_TURN_STR, command= lambda: self._endTurn())
		self._moveTurnButton.grid(row=1, column=1)

		main_panel = tk.Frame(master=self)
		main_panel.grid(row=0, column=1)
		self._turnComponent = []
		# friendly
		for i, unit in enumerate(self._yourUnits):
			upper_row, lower_row = i*2, i*2+1
			squad_status_var = tk.StringVar()
			squad_tactic_var = tk.StringVar()
			# nametag
			squad_name_label = tk.Label(master=main_panel, text=unit.name)
			squad_name_label.grid(row=upper_row, column=0)
			# status
			squad_status_label = tk.Label(master=main_panel, text=squad_status_var)
			squad_status_label.grid(row=upper_row, column=1)
			# tactic selector
			squad_selection_dropdown = tk.OptionMenu(main_panel, squad_tactic_var, BattleDialogBox.NO_ORDER_STR, *self._battleManager.getSquadTactics(unit))
			squad_selection_dropdown.grid(row=upper_row, column=2)
			# health bar
			squad_health_bar = ttk.Progressbar(master=main_panel, value=100, maximum=100)
			squad_health_bar.grid(row=lower_row, column=0, columnspan=3)
			# save to the list
			self._turnComponent.append( (unit, squad_status_var, squad_tactic_var, squad_health_bar) )
		# hostile
		for i, enemyUnit in enumerate(self._mission.units):
			# similarly to the ones above
			upper_row, lower_row = i*2, i*2+1
			enemy_status_var = tk.StringVar()
			# nametag
			enemy_name_label = tk.Label(master=main_panel, text=enemyUnit.name)
			enemy_name_label.grid(row=upper_row, column=4)
			# status
			enemy_status_label = tk.Label(master=main_panel, text=enemy_status_var)
			enemy_status_label.grid(row=upper_row, column=5)
			# health bar
			enemy_health_bar = ttk.Progressbar(master=main_panel, value=100, maximum=100)
			enemy_health_bar.grid(row=lower_row, column=4, columnspan=2)
			# save to the list
			self._turnComponent.append( (enemyUnit, enemy_status_var, None, enemy_health_bar) )
		# separator
		# create a fixed width empty frame as separator
		separator = tk.Frame(master=main_panel, width=10)
		separator.grid(row=0, column=3)

	def _updateComponent(self):
		# update the components each turn for both side
		for unit, status_var, tactic_var, health_bar in self._turnComponent:
			current_hp, total_hp, remaining_members, total_members = unit.getSquadHp()
			# reset tactic, update status and bar
			if(tactic_var):
				# tactic is only available to friendly side
				tactic_var.set(BattleDialogBox.NO_ORDER_STR)
			status_var.set("HP: {:.1f}/{:.1f}, Battle-able: {:d}/{:d}".format(current_hp, total_hp, remaining_member, total_members))
			if(total_hp == 0):
				# to prevent divide by zero
				total_hp = 1
			health_bar["value"] = 100 * current_hp / total_hp

	def _endTurn(self):
		# move the turn forward
		raise NotImplementedError()

	def _writeToVox(self, text):
		textLib.addFormatText(self._voxLog, text)

def battleDialog(window, yourUnits, battleManager, mission):
	"""See LoadoutDialogBox for details"""
	dialogBox = BattleDialogBox(window, yourUnits=yourUnits, battleManagerObj=battleManager, missionObj=mission)
	window.wait_window(dialogBox)

testWindow()

