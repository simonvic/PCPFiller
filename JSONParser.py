import json
import os
import csv
from pcpartpicker import API

class Part:

	def __init__(self, jsonData):
		self.jsonData = jsonData

	def isIncomplete(self):
		return float(self.jsonData["price"][1]) == 0.0

	def format(self):
		self.jsonData["price"] = float(self.jsonData["price"][1])


class GPU(Part):

	def isIncomplete(self):
		return super().isIncomplete() or self.jsonData["core_clock"] is None or self.jsonData["boost_clock"] is None or self.jsonData["color"] is None or self.jsonData["length"] is None

	def format(self):
		super().format()
		self.jsonData["vram"] = toGigaBytes(self.jsonData["vram"]["total"])
		self.jsonData["core_clock"] = toGigaBytes(self.jsonData["core_clock"]["cycles"])
		self.jsonData["boost_clock"] = toGigaBytes(self.jsonData["boost_clock"]["cycles"])


class RAM(Part):

	def format(self):
		super().format()
		self.jsonData["speed"] = toMegaHertz(self.jsonData["speed"]["cycles"])
		self.jsonData["module_size"] = toGigaBytes(self.jsonData["module_size"]["total"])
		self.jsonData["price_per_gb"] = float(self.jsonData["price_per_gb"][1])
		self.jsonData["error_correction"] = bool(self.jsonData["error_correction"] == "ECC / Registered")

######################################################################################################################

def toGigaBytes(bytes):
	return bytes/1000000000

def toMegaHertz(hertz):
	return hertz/1000000;

def dumpToCSV(parts, outputDir, fileName):
	os.makedirs(os.path.dirname(outputDir + "/"), exist_ok=True)
	with open(outputDir + "/" + fileName, "w", newline='') as csvFile:
		csvWriter = csv.writer(csvFile, quoting=csv.QUOTE_NONNUMERIC)
		csvWriter.writerow(parts[0].jsonData.keys())
		for part in parts:
			csvWriter.writerow(part.jsonData.values())

def getPartOfName(partName, jsonDataPart):
	part = {
		"memory": RAM(jsonDataPart),
		"video-card": GPU(jsonDataPart)
	}
	return part.get(partName)

def readAndFormatParts(partName):
	os.makedirs(os.path.dirname(partsSourceDir + "/"), exist_ok=True)
	with open(partsSourceDir + "/" + partName + ".json") as file:
		jsonData = json.load(file)[partName]
		completeParts = list()

		for jsonDataPart in jsonData:
			part = getPartOfName(partName, jsonDataPart)
			if not part.isIncomplete():
				part.format()
				completeParts.append(part)
		
	print(partName + ":")
	dataCount = len(jsonData)
	completeDataCount = len(completeParts)
	print("\tAvailable parts: {:d}".format(dataCount))
	print("\tComplete parts:  {:d}".format(completeDataCount))
	print("\tRatio:           {:.3f} %".format(completeDataCount * 100 / dataCount))
	print("")
	
	return completeParts

def convertParts(partName):
	dumpToCSV(readAndFormatParts(partName), formattedPartsOutputDir, partName + ".csv")

#################################################
#

partsSourceDir = "parts"
formattedPartsOutputDir = "parts_formatted"

convertParts("video-card")
convertParts("memory")
