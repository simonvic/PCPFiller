import os
from pcpartpicker import API


api = API()
api.set_region("it")

if not os.path.exists("parts"):
    os.makedirs("parts")

for part in api.supported_parts:
	with open("parts/" + part + ".json", "w") as f:
		f.write(api.retrieve(part).to_json())


