def toGigaBytes(bytes):
	return bytes/1000000000

def toMegaHertz(hertz):
	return hertz/1000000;


class PCPart:

	def __init__(self, jsonData):
		self.jsonData = jsonData

	def isIncomplete(self):
		return float(self.jsonData["price"][1]) == 0.0

	def format(self):
		self.jsonData["price"] = float(self.jsonData["price"][1])




class GPU(PCPart):

	def isIncomplete(self):
		return super().isIncomplete() or self.jsonData["core_clock"] is None or self.jsonData["boost_clock"] is None or self.jsonData["color"] is None or self.jsonData["length"] is None

	def format(self):
		super().format()
		self.jsonData["vram"] = toGigaBytes(self.jsonData["vram"]["total"])
		self.jsonData["core_clock"] = toGigaBytes(self.jsonData["core_clock"]["cycles"])
		self.jsonData["boost_clock"] = toGigaBytes(self.jsonData["boost_clock"]["cycles"])




class RAM(PCPart):

	def format(self):
		super().format()
		self.jsonData["speed"] = toMegaHertz(self.jsonData["speed"]["cycles"])
		self.jsonData["module_size"] = toGigaBytes(self.jsonData["module_size"]["total"])
		self.jsonData["price_per_gb"] = float(self.jsonData["price_per_gb"][1])
		self.jsonData["error_correction"] = bool(self.jsonData["error_correction"] == "ECC / Registered")

