import random

roll_random = random.random
roll_random_int = random.randint

def variance(val, fluctuation=0.5):
	return float(val) * (1.0 + random.uniform(-fluctuation, fluctuation))

def roll_between(lower_bound, upper_bound):
	return lower_bound + (upper_bound-lower_bound) * roll_random()

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

def roll_percentage(percentage):
	return roll_random() * 100.0 > percentage

def select_random(roll_list):
	return roll_list[roll_random_int(0, len(roll_list)-1)]

def select_within_range(tuple_range):
	return roll_random_int(tuple_range[0], tuple_range[1])

def select_with_preference(roll_list, preference_fn):
	# double the chances of those which passes the preference_fn
	extended_roll_list = roll_list + [item for item in roll_list if preference_fn(item)]
	return select_random(extended_roll_list)

def sublist_by_chance(roll_list, chance):
	# create a list by roll for chance for each item in list
	return [item for item in roll_list if roll_random() <= chance]

def _splitJSONBlock(jsonBlock, key):
	Debug.printDebug(jsonBlock)
	key = jsonBlock.pop(key)
	return key, jsonBlock

def convertJSONToDictObject(jsonList, referenceKey, classProto):
	"""Convert the list of json objects to dict which refer to the created class using ClassProto(jsonBlock)"""
	return { k: classProto(v) for k, v in [_splitJSONBlock(block, referenceKey) for block in jsonList] }

def tryConvertStringToInt(string):
	try:
		int_value = int(string)
	except ValueError:
		return string
	return int_value

class Debug:
	MESSAGE_LEVEL = 0
	MESSAGE_LEVEL_NAME = ["DEBUG", "INFO", "WARNING", "ERROR"]
	@staticmethod
	def changeMessageLevel(newLevel):
		"""Debug will show messages on this level and above"""
		if(isinstance(newLevel, str)):
			newLevel = Debug.MESSAGE_LEVEL_NAME.index(newLevel)
		assert isinstance(newLevel, int), "Value {} must be int/str, but is {}".format(newLevel)
		Debug.MESSAGE_LEVEL = newLevel
	
	@staticmethod
	def printMsg(level, *message):
		if(level >= Debug.MESSAGE_LEVEL):
			level_name = Debug.MESSAGE_LEVEL_NAME[level]
			print("{}: ".format(level_name), *message)
	
	@staticmethod
	def printDebug(*message):
		Debug.printMsg(0, *message)
	
	@staticmethod
	def printError(*message):
		Debug.printMsg(3, *message)
