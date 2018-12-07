import tkinter as tk
import re

textTagDefault = {
	"normal": {},
	"ally": {"foreground": "green"},
	"enemy": {"foreground": "red"},
	"neutral": {"foreground": "yellow"},
	"system": {"foreground": "blue"}
}

def createTextWithScrollbar(root, textTag=textTagDefault):
	# pane and scrollbar
	textPane = tk.Text(master=root, height=8, width=50)
	scrollbar = tk.Scrollbar(master=root)
	textPane.pack(side=tk.LEFT, fill=tk.Y)
	scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
	# bind the two items
	# also disable writing into this pane
	textPane.config(yscrollcommand=scrollbar.set, state=tk.DISABLED)
	scrollbar.config(command=textPane.yview)
	# prepare the tags
	for key in textTag:
		textPane.tag_config(key, **textTag[key])
	# return the pane
	return textPane

def addText(pane, text, cursor=tk.END, tag=None, safety=True):
	# enable, insert and disable again
	# disable safety if pane is not disabled in the first place
	if(safety):
		pane.config(state=tk.NORMAL)
	pane.insert(cursor, text, tag)
	if(safety):
		pane.config(state=tk.DISABLED)

# each \ is a \\, hence \{g0} became \{6}1
textTagSplitter = re.compile("<(.+?)>(.+?)<\\\\\\1>")
# the tagged data is in <{tag}>{content}<\{tag}> format
def addFormatText(pane, unformattedText, cursor=tk.END, endline=True):
	# re.split with captured group keep the groups
	if(endline):
		unformattedText = unformattedText + '\n'
	splittedText = re.split(textTagSplitter, unformattedText)
	# if anything matched the textTagDefault entry, the next one is the target of the tag
	texts = []
	formatFlag = False
	formatTag = "normal"
	for text in splittedText:
		if(formatFlag == True):
			# bind the two items together and reset
			texts.append((formatTag, text))
			formatFlag = False
			formatTag = "normal"
		elif(text in textTagDefault):
			# enable the tag
			formatFlag = True
			formatTag = text
		else:
			# add as default
			texts.append((formatTag, text))
	
	# minimalize the disabling of pane. might not be really necessary
	pane.config(state=tk.NORMAL)
	for tag, text in texts:
		addText(pane, text, cursor=cursor, tag=tag, safety=False)
	pane.config(state=tk.DISABLED)
