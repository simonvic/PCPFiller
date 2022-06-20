package it.simonvic.pcpfiller;

import com.google.gson.Gson;
import it.simonvic.pcpfiller.parts.Memory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author simonvic
 */
public class PCPFiller {

	public static void main(String[] args) throws IOException {

		Reader jsonReader = new InputStreamReader(PCPFiller.class.getResourceAsStream("parts/memory.json"));
		Memory.JSON.Root jsonObj = new Gson().fromJson(jsonReader, Memory.JSON.getType());

		StringBuilder sb = new StringBuilder();
		sb.append(Memory.getCSVHeader());
		jsonObj.memory
			.stream()
			.map(Memory.JSON::build)
			.map(Memory::toCSV)
			.map(csvEntry -> csvEntry.replace("null","?"))
			.forEach(csvEntry -> sb.append(csvEntry).append("\n"));
		
		Files.writeString(Path.of("/tmp/memory.csv"), sb, StandardOpenOption.CREATE);

	}
}
