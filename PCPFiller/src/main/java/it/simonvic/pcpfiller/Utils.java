package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
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

	/**
	 * Load instances from a CSV formatted string
	 *
	 * @param csvValues
	 * @return instances
	 * @throws IOException
	 */
	public static Instances instancesFromCSV(String csvValues) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new ByteArrayInputStream(csvValues.getBytes()));
		return csvLoader.getDataSet();
	}

	/**
	 * Load instances from a CSV file
	 *
	 * @param csvFile
	 * @return instances
	 * @throws IOException
	 */
	public static Instances instancesFromCSV(File csvFile) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new FileInputStream(csvFile));
		return csvLoader.getDataSet();
	}

	/**
	 * Load instances from a JSON file
	 *
	 * @param jsonFile
	 * @param partType
	 * @return instances
	 * @throws IOException
	 */
	public static Instances instancesFromJSON(File jsonFile, PCPart.Type partType) throws IOException {
		Reader jsonReader = new InputStreamReader(new FileInputStream(jsonFile));
		String csv = PCPart.JSON.Root.from(jsonReader, partType).toCSV();
		//@todo save temporary csv?
//		Files.writeString(Path.of("/tmp/memory.csv"), sb, StandardOpenOption.CREATE);
		return instancesFromCSV(csv);
	}

	/**
	 * Get the indeces of a list of attributes of a dataset
	 *
	 * @param dataset
	 * @param attributesNames
	 * @return indeces
	 */
	public static int[] indecesOf(Instances dataset, String... attributesNames) {
		return List.of(attributesNames)
			.stream()
			.mapToInt(attributeName -> dataset.attribute(attributeName).index())
			.toArray();
	}

}
