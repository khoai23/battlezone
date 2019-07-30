import tkinter as tk
import python.lib_image as imageLib
import python.utils as utils
import math

class Map():
	"""A wrapper around a Canvas to draw systems and fleet positions"""
	COLORING = [None, "red", "orange", "yellow", "green"]
	DEFAULT_MAP_FLEET_OFFSET = (10, 10)

	def __init__(self, master, size, systemsCount=10):
		self._background = "./res/texture/bg_star.jpg"
		self._system_loc = "./res/texture/starmap/star_{:d}.png"
		self._fleet_loc = "./res/texture/starmap/starhulk.png"
		# draw the star with the background
		mapWidth, mapHeight = size
		self._canvas = tk.Canvas(master=master, width=mapWidth, height=mapHeight)
		# initiate the image
		self._fleet_img = None
		self._destination_arrow = None
		# initiate movement stack
		self._movement_stack = []
		# construct the data 
		self._initiateDefaultData(systemsCount=systemsCount)
		# draw the map
		self.redrawMap()
		self._updateFleetLocation(self._fleet_position, fleetImg=self._fleet_loc, fleetTarget=self._fleet_travel_destination)

	@property
	def canvas(self):
		return self._canvas

	@property
	def systems_count(self):
		return len(self._list_system)

	def _constructRoutes(self):
		"""Construct random routes between existing systems"""
		systems_count = len(self._list_system)
		safe_route_exist = [False] * systems_count
		self._routes = dict()
		for i in range(systems_count):
			for j in range(i+1, systems_count):
				# safety is randomized
				route_safety = max(utils.roll_between(-50.0, 100.0), 0.0)
				self._routes[(i, j)] = route_safety
				if(route_safety >= 80.0):
					safe_route_exist[i] = safe_route_exist[j] = True
			if(not safe_route_exist[i]):
				# upgrade a random route to safe
				random_selected_dest = utils.roll_random_int(0, systems_count-2)
				if(random_selected_dest >= i):
					random_selected_dest += 1;
					key = (i, random_selected_dest)
				else:
					key = (random_selected_dest, i)
				self._routes[key] = utils.roll_between(80.0, 100.0)

	def _checkRoutes(self, outSystem, inSystem):
		"""Return route strength between the two system, 0.0 to 100.0"""
		if(outSystem > inSystem):
			outSystem, inSystem = inSystem, outSystem
		# route is (smaller, larger) key
		route_key = (outSystem, inSystem)
		return self._routes.get(route_key, 0.0)
	
	def _distance(self, outCoord, inCoord):
		"""Return the distance between the two coordinates (in pixels?)"""
		outX, outY = outCoord
		inX, inY = inCoord
		return math.sqrt( (outX-inX) ** 2 + (outY-inY) ** 2 )

	def redrawMap(self):
		"""Redraw the canvas using current data. Only draw systems and routes"""
		# clear everything
		imageLib.clearCanvas(self._canvas)
		# draw the background
		imageLib.drawItem(self._canvas, self._background, scale=0.5)
		# draw systems
		for system_type, x, y in self._list_system:
			imageLib.drawItem(self._canvas, self._system_loc.format(system_type), location=(x, y), anchor="center")
		# draw routes
		systems_count = len(self._list_system)
		for i in range(systems_count):
			for j in range(i+1, systems_count):
				route_str = self._routes[(i, j)]
				route_color = Map.COLORING[int(route_str / 20.0)]
				if(route_color is None):
					# do not draw
					continue
				_, x1, y1 = self._list_system[i]
				_, x2, y2 = self._list_system[j]
				self._canvas.create_line(x1, y1, x2, y2, fill=route_color, dash=(3,1))
		self._canvas.update()
	
	def _initiateDefaultData(self, size=(640, 480), systemsCount=10, **kwargs):
		"""Construct map data using existing arguments"""
		# randomized list systems
		mapWidth, mapHeight = size
		systemsList = self._list_system = []
		for i in range(systemsCount):
			x, y = utils.roll_between(0.0, mapWidth), utils.roll_between(0.0, mapHeight)
			systemType = utils.roll_random_int(0, 6)
			systemsList.append((systemType, x, y))
		# chapter fortresses location
		self._home_idx = 0
		# fleet positions
		self._fleet_position = fleetX, fleetY = self._getPosOnSystem(self._home_idx)
		self._orbit_system = self._home_idx
		self._fleet_travel_destination = None 
		# create routes
		self._constructRoutes()

	def _updateFleetLocation(self, fleetLocation, fleetImg="", fleetTarget=None):
		"""Update the map with new fleet position and its movement vector; create fleet if it doesn't exist"""
		if(self._fleet_img is None):
			self._fleet_img = imageLib.drawItem(self._canvas, fleetImg, location=(0, 0), anchor="center", return_canvas_id=True)
			print("Fleet img: ", self._fleet_img)
		# move the fleet to location
		offsetX, offsetY = fleetLocation
		currentX, currentY = self._canvas.coords(self._fleet_img)
		offsetX, offsetY = offsetX-currentX, offsetY-currentY 
		self._canvas.move(self._fleet_img, offsetX, offsetY)
		# if presenting a target, draw a white arrow toward it, else try to remove the arrow
		if(self._destination_arrow is not None):
			self._canvas.delete(self._destination_arrow)
		if(fleetTarget is not None):
			self._destination_arrow = imageLib.drawArrow(self._canvas, fleetLocation, fleetTarget, color="white", arrow_end=True)
		else:
			self._destination_arrow = None
		self._canvas.update()

	def _getPosOnSystem(self, systemIdx):
		_, systemX, systemY = self._list_system[systemIdx]
		offsetX, offsetY = Map.DEFAULT_MAP_FLEET_OFFSET
		return (systemX+offsetX, systemY+offsetY)

	def moveToSystemEstimate(self, targetSystemIdx):
		"""From current system (self._orbit_system) toward the new system, return tuple of (distance, route strength)"""
		if(self._orbit_system == targetSystemIdx):
			return None
		route_str = self._checkRoutes(self._orbit_system, targetSystemIdx)
		in_coordinate = self._getPosOnSystem(targetSystemIdx)
		distance = self._distance(self._fleet_position, in_coordinate)
		return (distance, route_str)
	
	def moveToSystemConfirmed(self, targetSystemIdx, estimatedTime):
		"""Create an array of turn-end position as the fleet move toward the {targetSystemIdx} in {estimatedTime} turn"""
		# flush everything
		del self._movement_stack[:]
		# generate move positions
		inX, inY = self._getPosOnSystem(targetSystemIdx)
		outX, outY = self._fleet_position
		eta = estimatedTime
		offsetX = (inX - outX) / eta
		offsetY = (inY - outY) / eta
		move_positions = [ (inX - offsetX * turn, inY - offsetY * turn, None if turn != 0 else targetSystemIdx) for turn in range(eta-1, -1, -1)]
		# re-add them into stack in inverse order (for pop())
		self._movement_stack.extend(move_positions[::-1])
		# save the destination position
		self._fleet_travel_destination = targetSystemIdx
		fleet_target = self._getPosOnSystem(self._fleet_travel_destination)
		self._updateFleetLocation(self._fleet_position, fleetTarget=fleet_target)

	def endTurnTrigger(self):
		"""Move the fleet forward if there are still movement left in the stack, else stay idle"""
		if(len(self._movement_stack) == 0):
			return
		fleetX, fleetY, current_orbit = self._movement_stack.pop()
		self._orbit_system = current_orbit
		self._fleet_position = fleetX, fleetY
		# if had arrived, remove the destination in waiting
		if(self._fleet_travel_destination == self._orbit_system):
			self._fleet_travel_destination = None
		fleet_target = None if self._fleet_travel_destination is None else self._getPosOnSystem(self._fleet_travel_destination)
		self._updateFleetLocation(self._fleet_position, fleetTarget=fleet_target)

	def pack(self, **kwargs):
		self._canvas.pack(**kwargs)

	def grid(self, **kwargs):
		self._canvas.grid(**kwargs)
