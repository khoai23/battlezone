# This lib will use tk to display
import tkinter as tk
import tkinter.ttk as ttk
import python.lib_image as imageLib
import python.lib_text as textLib
import python.manager as managerLib
import python.utils as utils
import python.combat as combat
import sys
from python.map import Map

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
#	utils.Debug.changeMessageLevel("INFO")
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
	dipWidth = dipHeight = 200
	relationScores = [("IG", 125), ("Eldar", -26), ("Mechanicus", 50), ("Orks", -66), ("Chaos", 40), ("Inquisition", 83)]
	diplomacyFrame = tk.Frame(window)
	dipImage = tk.Canvas(master=diplomacyFrame, width=dipWidth, height=dipHeight)
	dipImage = imageLib.drawRelationGraph(dipImage, relationScores, location=(0, 0), size=dipHeight)
	dipImage.pack()
	return diplomacyFrame

def campaignMap(window):
	# try to make a star map of interconnecting systems
	mapSize = (640, 480)
	starMap = Map(window, mapSize, 10)
	
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
	conversationManager = overallManager.conversationManager
	# get your commander for your filthy dress-up game
	astartes = overallManager.company.commander
	def callbackDrawAndAddText(dialogbox):
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
		defaultGameDialog(window, BattleDialogBox, yourUnits=yourUnits, battleManagerObj=combatManager, conversationManagerObj=conversationManager, missionObj=testMission)
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
		callback(dialogBox)

class ArmoryDialogBox(DefaultDialogBox):
	def __init__(self, master, itemManagerObj=None):
		super(ArmoryDialogBox, self).__init__(master=master)
		# construct the display on the right hand side
		assert itemManagerObj is not None
		self._itemManager = itemManagerObj
		self._armoryPane = tk.Frame(master=self)
		self._armoryPane.grid(row=0, column=0)
		self._interactionPane = tk.Frame(master=self)
		self._interactionPane.grid(row=0, column=1)
		self.buildTreeview()

	def buildTreeview(self):
		# create the treeview
		tree = self._armoryTreeview = ttk.Treeview(master=self._armoryPane)
		tree.pack()
		tree["columns"] = ("stat", "available", "damaged", "requested")
		# declare the sizes
		all_columns = ["#0"] + list(tree["columns"])
		width_and_min_width = [(200, 150), (300, 150), (50, 25), (75, 25), (75, 25)]
		for col, (w, min_w) in zip(all_columns, width_and_min_width):
			tree.column(col, width=w, minwidth=min_w)
		# declare the headings, set them to bind on the left
		headings = ["Item", "Item Stat", "Ready", "Damaged", "Requested"]
		for col, h, in zip(all_columns, headings):
			tree.heading(col, text=h, anchor=tk.W)

		# populate it with the data from the itemManager
		items_grouped = self._itemManager.getItemsByGroup()
		for group_name, items in items_grouped.items():
			group = tree.insert("", 1, text=group_name, values=("", "", "", ""))
			for item in items:
				available_count = self._itemManager.getItemCount(item)
				damaged_count = self._itemManager.getItemCount(item, damagedItem=True)
				tree.insert(group, "end", text=item.name, values=(item.getStatDisplay(), available_count, damaged_count, 0))

	def buildInteraction(self):
		# requisition arms and armours from the Mechanicus,
		# TODO return when time is implemented
		pass

class RosterDialogBox(DefaultDialogBox):
	def __init__(self, master, company):
		super(RosterDialogBox, self).__init__(master=master)
		self._company = company

	def buildTreeview(self):
		tree = self._tree = ttk.Treeview(master=self)
		tree.pack()
		tree["columns"] = ("name", "title", "level", "stat", "equipment")
		# declare the sizes
		all_columns = ["#0"] + list(tree["columns"])
		width_and_min_width = [(150, 100), (150, 100), (75, 50), (200, 175), (250, 175)]
		for col, (w, min_w) in zip(all_columns, width_and_min_width):
			tree.column(col, width=w, minwidth=min_w)
		# declare the headings, set them to bind on the left
		headings = ["Name", "Rank", "Level", "Stat", "Equipment"]
		for col, h, in zip(all_columns, headings):
			tree.heading(col, text=h, anchor=tk.W)

	def updateTree(self):
		tree = self._tree
		# populate it with the data from the company
		for squad in self._company.squads:
			squad_obj = tree.insert("", "end", text=squad.name, values=("", "", "", ""))
			# move the squad leader to the front
			squad_list = list(squad._members)
			squad_list.remove(squad._leader)
			squad_list = [squad.squad_leader] + squad_list
			for idx, member in squad_list:
				tree.insert(squad_obj, "end", text=member.name, values=())

class LoadoutDialogBox(DefaultDialogBox):
	LIST_SELECTOR_CONFIG = [("main", "weapon"), ("secondary", "weapon"), ("armor", "armor"), ("accessory", "accessory")]
	EMPTY_STR = "Empty"
	def __init__(self, master, target=None, colorScheme=None, itemManagerObj=None, statManagerObj=None):
		super(LoadoutDialogBox, self).__init__(master=master)
		assert itemManagerObj and statManagerObj
		self._itemManager = itemManagerObj
		self._statManager = statManagerObj
		self._target = target
		self._target_original_equipments = dict(target.equipments)
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
		utils.Debug.printDebug("Event called from: {}".format(args))
		self._populateChoices()
		self._changeDisplay()

	def exit(self, cancel=True):
		if(cancel):
			# reset all changes
			self._target.equipments = self._target_original_equipments
			utils.Debug.printDebug("Revert to target original equipments: ", self._target.equipments)
		else:
			utils.Debug.printDebug("Target changed from set {} to set {}".format(self._target_original_equipments, self._target.equipments))
		self.destroy()

class BattleDialogBox(DefaultDialogBox):
	NO_ORDER_STR = "No Order"
	END_TURN_STR = "End Turn"
	TOOGLE_TURN_STR = "Automatic End Turn"
	DEFAULT_MAP_WIDTH = 350
	DEFAULT_MAP_HEIGHT = 450
	def __init__(self, master, yourUnits=None, battleManagerObj=None, conversationManagerObj=None, missionObj=None):
		super(BattleDialogBox, self).__init__(master=master)
		self._yourUnits = yourUnits
		self._battleManager = battleManagerObj
		self._conversationManager = conversationManagerObj
		self._mission = missionObj

		self._constructBattleList()
		self._updateComponent()
		
		width, height = (BattleDialogBox.DEFAULT_MAP_WIDTH, BattleDialogBox.DEFAULT_MAP_HEIGHT)
		self._combatInstance = combat.Battle(self._yourUnits, self._mission.composition, battleDimensions=(width, height))
		# construct the field
		self._field = tk.Canvas(master=self, width=width, height=height)
		self._field.grid(row=0, column=0)
		# other control variables
		self._combatEnded = False
		self._autoEndTurn = -1.0
		self._autoFunctionRunId = None

	def _updateBattleMap(self, extra_lines=None):
		# clear all
		self._field = imageLib.clearCanvas(self._field)
		if(extra_lines):
			# draw the walk lines received during movement
			for args, kwargs in extra_lines:
				imageLib.drawArrow(self._field, *args, **kwargs)

		for unit, pos in zip(self._combatInstance._all_units, self._combatInstance.coordinates):
			# draw units, dead ones get transparency
			if(pos is None):
				continue
			x, y = pos
			badge = unit.badge
			badge_path = self._battleManager.getBadgePath(badge)
			image_is_half_transparent = not unit.alive
			imageLib.drawItem(self._field, badge_path, location=(x, y), anchor="center", half_transparent=image_is_half_transparent)

	def toggleAutoEndTurn(self):
		if(self._autoEndTurn > 0.0):
			utils.Debug.printDebug("Toggle AutoEndTurn to off, from interval {}".format(self._autoEndTurn))
			self._autoEndTurn = -1.0
			self.after_cancel(self._autoFunctionRunId)
		else:
			try:
				new_auto_endturn_interval = float(self._moveTurnWaitTime.get())
				if(new_auto_endturn_interval <= 0.0):
					utils.Debug.printDebug("Toggle failed, wait_time is non-positive float.")
					return
			except ValueError:
				utils.Debug.printDebug("Toggle failed, wait_time cannot be read from tk.Entry.")
				return
			self._autoEndTurn = new_auto_endturn_interval
			self._autoFunctionRunId = self._autoEndTurnFn()

	def _autoEndTurnFn(self):
		if(self._combatEnded):
			utils.Debug.printDebug("Combat already ended for _autoEndTurnFn")
			self._autoEndTurn = -1.0
			self._autoFunctionRunId = None
			return
		# run the end turn
		self.endTurn()
		# schedule the next auto and record it to allow canceling
		assert self._autoEndTurn > 0.0, "Error! _autoEndTurnFn called with value < 0.0"
		self._autoFunctionRunId = self.after(int(1000.0 * self._autoEndTurn), self._autoEndTurnFn)

	def _constructBattleList(self):
		# for now, just have a text box for vox channel, each unit of friendly get nametag + status (label), healthbar (progressbar), and tactic selector to override the usual 
		vox_panel = tk.Frame(master=self)
		vox_panel.grid(row=1, column=0, rowspan=2, columnspan=2)
		self._voxLog = textLib.createTextWithScrollbar(vox_panel)
		self._moveTurnButton = tk.Button(master=self, text=BattleDialogBox.END_TURN_STR, command= lambda: self.endTurn())
		self._moveTurnButton.grid(row=1, column=2)
		# add an entry that restrict the value in numericals
		num_validator = lambda char, entry_val: char in "01234567890."
		vcmd = (self.register(num_validator), "%S", "%P")
		self._moveTurnWaitTime = tk.Entry(master=self, text="3.0", validate="key", validatecommand=vcmd)
		self._moveTurnWaitTime.grid(row=2, column=3)
		self._moveTurnAutoButton = tk.Button(master=self, text=BattleDialogBox.TOOGLE_TURN_STR, command= lambda: self.toggleAutoEndTurn())
		self._moveTurnAutoButton.grid(row=2, column=2)

		main_panel = tk.Frame(master=self)
		main_panel.grid(row=0, column=1, columnspan=3)
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
			squad_status_label = tk.Label(master=main_panel, textvariable=squad_status_var)
			squad_status_label.grid(row=upper_row, column=1)
			# tactic selector
			squad_selection_dropdown = tk.OptionMenu(main_panel, squad_tactic_var, BattleDialogBox.NO_ORDER_STR, *self._battleManager.getSquadTactics(unit, bare=True))
			squad_selection_dropdown.grid(row=upper_row, column=2)
			# health bar
			squad_health_bar = ttk.Progressbar(master=main_panel, value=100, maximum=100)
			squad_health_bar.grid(row=lower_row, column=0, columnspan=3)
			# save to the list
			self._turnComponent.append( (unit, squad_status_var, squad_tactic_var, squad_health_bar) )
		# hostile
		for i, enemyUnit in enumerate(self._mission.composition):
			# similarly to the ones above
			upper_row, lower_row = i*2, i*2+1
			enemy_status_var = tk.StringVar()
			# nametag
			enemy_name_label = tk.Label(master=main_panel, text=enemyUnit.name)
			enemy_name_label.grid(row=upper_row, column=4)
			# status
			enemy_status_label = tk.Label(master=main_panel, textvariable=enemy_status_var)
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
			status_var.set("HP: {:.1f}/{:.1f}, Battle-able: {:d}/{:d}".format(current_hp, total_hp, remaining_members, total_members))
			if(total_hp == 0):
				# to prevent divide by zero
				total_hp = 1
			health_bar["value"] = 100 * current_hp / total_hp

	def endTurn(self):
		# move the turn forward
		# keep a dict of all the tactics forced by player
		if(self._combatEnded or self._combatInstance.combatEnded()):
			self._combatEnded = True
			self._writeToVox("Combat ended, no more update!!")
			self._updateComponent()
			self._updateBattleMap()
			return
		forced_tactic = {}
		for unit, _, tactic_var, _ in self._turnComponent:
			if(tactic_var is None):
				# enemy tactic, ignore
				continue
			tactic = self._battleManager.getTacticByName(tactic_var.get())
			if(tactic is not None):
				forced_tactic[unit] = tactic
		# move units around
		# keep the lines to be drawn later
		line_drawer = []
		line_recorder = lambda *args, **kwargs: line_drawer.append( (args, kwargs) )
		self._combatInstance.moveUnitsRandomly(voxStreamFn=self._writeToVox, drawFn=line_recorder)
		# deploy randomly
		self._combatInstance.deployUnitsRandomly(deployChance=0.5, voxStreamFn=self._writeToVox)
		# every units will try to attack the closest enemy within their range
		tacticBonusCalculator = lambda a, d: self._battleManager.getOpposingSquadTacticBonus(a, d, forceTacticDict=forced_tactic, voxStreamFn=self._writeToVox)
		self._combatInstance.initiateCombat(tacticBonusFn=tacticBonusCalculator, voxStreamFn=self._writeToVox, drawFn=line_recorder)
		# once done, reload
		self._updateComponent()
		self._updateBattleMap(extra_lines=line_drawer)

	def _writeToVox(self, texts):
		if(isinstance(texts, str)):
			texts = [texts]
		for text in texts:
			textLib.addFormatText(self._voxLog, text)

if __name__ == "__main__":
	debug_level = utils.tryConvertStringToInt(sys.argv[-1])
	if(debug_level in utils.Debug.MESSAGE_LEVEL_NAME or (isinstance(debug_level, int) and len(utils.Debug.MESSAGE_LEVEL_NAME) > debug_level)):
		utils.Debug.changeMessageLevel(debug_level)
	testWindow()

