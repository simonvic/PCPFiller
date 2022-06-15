import json
import os
import csv
from PCPart import *

def getPartFromName(partName, jsonDataPart):
	part = {
		"memory": RAM(jsonDataPart),
		"video-card": GPU(jsonDataPart)
	}
	return part.get(partName)

def convertParts(partName, inputDir, outputDir):
	dumpToCSV(readAndFormatParts(partName, inputDir, outputDir), outputDir, partName + ".csv")

def dumpToCSV(parts, outputDir, fileName):
	os.makedirs(os.path.dirname(outputDir + "/"), exist_ok=True)
	with open(outputDir + "/" + fileName, "w", newline='') as csvFile:
		csvWriter = csv.writer(csvFile, quoting=csv.QUOTE_NONNUMERIC)
		csvWriter.writerow(parts[0].jsonData.keys())
		for part in parts:
			csvWriter.writerow(part.jsonData.values())

def readAndFormatParts(partName, inputDir, outputDir):
	os.makedirs(os.path.dirname(outputDir + "/"), exist_ok=True)
	with open(inputDir + "/" + partName + ".json") as file:
		jsonData = json.load(file)[partName]
		completeParts = list()

		for jsonDataPart in jsonData:
			part = getPartFromName(partName, jsonDataPart)
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




