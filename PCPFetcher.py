import os
import argparse
from pcpartpicker import API

api = API()

def fetchParts(partName):
	with open(args.FETCH_OUTPUT_DIR + "/" + partName + ".json", "w") as f:
		f.write(api.retrieve(partName).to_json())

def parseArgs():
	parser = argparse.ArgumentParser()
	parser.add_argument("-f", "--fetch", 
						help="Fetch parts from PCPartPicker. Can be repeated for more part types. 'ALL' will fetch all parts",
						type=str, action="append", dest="PARTS_TO_FETCH", metavar="<part name>")

	parser.add_argument("-o", "--output-dir", 
						help="Specify where to save fetched parts",
						type=str, dest="FETCH_OUTPUT_DIR", metavar="<directory>", default="./parts")

	parser.add_argument("--region", 
						help="Set the region to be used when fetching from PCPartPicker",
						type=str, dest="REGION", metavar="<region>", default="it")

	parser.add_argument("-p", "--supported-parts", help="Print all supported part names", action='store_true')
	parser.add_argument("-r", "--supported-regions", help="Print all supported regions", action='store_true')
	return parser.parse_args()


###################################################################
## MAIN

args = parseArgs()

api.set_region(args.REGION)

if args.supported_parts:
	print("Supported parts: " + str(api.supported_parts))

if args.supported_regions:
	print("Supported regions: " + str(api.supported_regions))

if args.supported_parts or args.supported_regions:
	quit()

os.makedirs(os.path.dirname(args.FETCH_OUTPUT_DIR + "/"), exist_ok=True)

if args.PARTS_TO_FETCH is not None:
	for partName in api.supported_parts if "ALL" in args.PARTS_TO_FETCH else args.PARTS_TO_FETCH:
		if partName in api.supported_parts:
			fetchParts(partName)
		else:
			print(partName + " is not currently supported by PCPartPicker.")
			