import python.lib_gui as guiLib
import python.utils as utils
import tkinter as tk
import python.manager as managerLib

import sys

def testWindow():
	window = createWindow()
	mainLayout(window)
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
	# have single overall manager
	manager = managerLib.OverallManager("Generic Chapter", "Idunno Company")
	manager.createDefaultCompany()
	# create the layout
	# for now, just use a simple grid
	portrait = guiLib.createPortrait(window, companyCommander=manager.company.commander, colorScheme=manager.colorScheme)
	portrait.grid(row=0, column=0)
	dipWindow = guiLib.createDiplomacyPane(window)
	dipWindow.grid(row=1, column=0)
	starMap = guiLib.campaignMap(window)
	starMap.grid(row=0, column=1, columnspan=2)
	# assign the map into the Overall manager
	manager.map = starMap
	textPane, addTextFunc = guiLib.conversationPanel(window)
	textPane.grid(row=1, column=1)
	addTextFunc("<ally>Advisor: <\\ally>Chapter Master, welcome back.")
	interactPane = guiLib.interactionPanel(window, textFunc=addTextFunc, overallManager=manager)
	interactPane.grid(row=1, column=2)

if __name__ == "__main__":
	debug_level = utils.tryConvertStringToInt(sys.argv[-1])
	if(debug_level in utils.Debug.MESSAGE_LEVEL_NAME or (isinstance(debug_level, int) and len(utils.Debug.MESSAGE_LEVEL_NAME) > debug_level)):
		utils.Debug.changeMessageLevel(debug_level)
	testWindow()
