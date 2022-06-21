package it.simonvic.pcpfiller.parts;

import com.google.gson.Gson;
import it.simonvic.pcpfiller.Main;
import it.simonvic.pcpfiller.PCPFiller;
import java.io.Reader;
import java.util.List;

/**
 *
 * @author simonvic
 */
public class MemoriesJSON extends PCPartsJSON {

	List<Memory.JSON> memory;

	public static MemoriesJSON from(Reader reader) {
		return new Gson().fromJson(reader, MemoriesJSON.class);
	}

	@Override
	public String toCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append(Memory.getCSVHeader());
		memory.stream()
			.map(PCPart.JSON::build)
			.map(PCPart::toCSV)
			.map(csvEntry -> csvEntry.replace("null", PCPFiller.getMissingToken()))
			.forEach(csvEntry -> sb.append(csvEntry).append("\n"));
		return sb.toString();
	}

}
