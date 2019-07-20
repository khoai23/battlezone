import tkinter as tk
import python.lib_image as imageLib
import python.utils as utils

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
		# construct the data 
		self._initiateDefaultData(systemsCount=systemsCount)
		# draw the map
		self.redrawMap()
		self.updateFleetLocation(self._fleet_position, fleetImg=self._fleet_loc, fleetTarget=self._fleet_travel_destination)

	@property
	def canvas(self):
		return self._canvas

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

	def updateFleetLocation(self, fleetLocation, fleetImg="", fleetTarget=None):
		"""Update the map with new fleet position and its movement vector; create fleet if it doesn't exist"""
		if(self._fleet_img is None):
			self._fleet_img = imageLib.drawItem(self._canvas, fleetImg, location=(0, 0), return_canvas_id=True)
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
	
	def _initiateDefaultData(self, size=(640, 480), systemsCount=10, **kwargs):
		"""Construct map data using existing arguments"""
		# randomized list systems
		mapWidth, mapHeight = size
		systemsList = self._list_system = []
		for i in range(systemsCount):
			x, y = utils.roll_between(0.0, mapWidth), utils.roll_between(0.0, mapHeight)
			systemType = utils.roll_random_int(0, 6)
			systemsList.append((systemType, x, y))
		# home planet
		self._home_planet_idx = 0
		# fleet positions
		self._fleet_position = fleetX, fleetY = self._getPosOnPlanet(self._home_planet_idx)
		self._fleet_travel_destination = None 
		# create routes
		self._constructRoutes()

	def _getPosOnPlanet(self, planetIdx):
		_, planetX, planetY = self._list_system[planetIdx]
		offsetX, offsetY = Map.DEFAULT_MAP_FLEET_OFFSET
		return (planetX+offsetX, planetY+offsetY)
	
	def pack(self, **kwargs):
		self._canvas.pack(**kwargs)

	def grid(self, **kwargs):
		self._canvas.grid(**kwargs)
