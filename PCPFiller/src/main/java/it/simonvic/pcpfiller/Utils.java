package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
import it.simonvic.pcpfiller.parts.PCPartsJSON;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 *
 * @author simonvic
 */
public class Utils {

	public static Instances instancesFromCSV(String csvValues) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new ByteArrayInputStream(csvValues.getBytes()));
		return csvLoader.getDataSet();
	}

	public static Instances instancesFromCSV(File csvFile) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new FileInputStream(csvFile));
		return csvLoader.getDataSet();
	}

	public static Instances instancesFromJSON(File jsonFile, PCPart.Type partType) throws IOException {

		Reader jsonReader = new InputStreamReader(new FileInputStream(jsonFile));
		String csv = PCPartsJSON.from(jsonReader, partType).toCSV();
		System.out.println(csv);
//		Files.writeString(Path.of("/tmp/memory.csv"), sb, StandardOpenOption.CREATE);
		return instancesFromCSV(csv);
	}

	public static int[] indecesOf(Instances dataset, String... attributesNames) {
		return List.of(attributesNames)
			.stream()
			.mapToInt(attributeName -> dataset.attribute(attributeName).index())
			.toArray();
	}

}
