import tkinter as tk
import re

TEXT_TAG_DEFAULT = {
	"normal": {},
	"ally": {"foreground": "green"},
	"enemy": {"foreground": "red"},
	"neutral": {"foreground": "yellow"},
	"system": {"foreground": "blue"}
}

def createTextWithScrollbar(root, textTag=TEXT_TAG_DEFAULT):
	# pane and scrollbar
	text_pane = tk.Text(master=root, height=8, width=50)
	scrollbar = tk.Scrollbar(master=root)
	text_pane.pack(side=tk.LEFT, fill=tk.Y)
	scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
	# bind the two items
	# also disable writing into this pane
	text_pane.config(yscrollcommand=scrollbar.set, state=tk.DISABLED)
	scrollbar.config(command=text_pane.yview)
	# prepare the tags
	for key in textTag:
		text_pane.tag_config(key, **textTag[key])
	# return the pane
	return text_pane

def addText(pane, text, cursor=tk.END, tag=None, safety=True):
	# enable, insert and disable again
	# disable safety if pane is not disabled in the first place
	if(safety):
		pane.config(state=tk.NORMAL)
	pane.insert(cursor, text, tag)
	if(safety):
		pane.config(state=tk.DISABLED)

# each \ is a \\, hence \{g0} became \{6}1
TEXT_TAG_SPLITTER = re.compile("<(.+?)>(.+?)<\\\\\\1>")
# the tagged data is in <{tag}>{content}<\{tag}> format
def addFormatText(pane, unformattedText, cursor=tk.END, endline=True):
	# re.split with captured group keep the groups
	if(endline):
		unformattedText = unformattedText + '\n'
	splitted_text = re.split(TEXT_TAG_SPLITTER, unformattedText)
	# if anything matched the textTagDefault entry, the next one is the target of the tag
	texts = []
	format_flag = False
	format_tag = "normal"
	for text in splitted_text:
		if(format_flag == True):
			# bind the two items together and reset
			texts.append((format_tag, text))
			format_flag = False
			format_tag = "normal"
		elif(text in TEXT_TAG_DEFAULT):
			# enable the tag
			format_flag = True
			format_tag = text
		else:
			# add as default
			texts.append((format_tag, text))
	
	# minimalize the disabling of pane. might not be really necessary
	pane.config(state=tk.NORMAL)
	for tag, text in texts:
		addText(pane, text, cursor=cursor, tag=tag, safety=False)
	pane.config(state=tk.DISABLED)
