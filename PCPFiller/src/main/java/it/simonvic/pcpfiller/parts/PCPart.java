package it.simonvic.pcpfiller.parts;

import it.simonvic.pcpfiller.classifiers.MemoryClassifier;
import it.simonvic.pcpfiller.classifiers.PCPartClassifier;
import java.io.Reader;

/**
 *
 * @author simonvic
 */
public abstract sealed class PCPart permits Memory {

	public enum Type {
		MEMORY
	}

	/**
	 * Get the CSV header line (attributes list) of a specified pc part type
	 *
	 * @param partType
	 * @return CSV header line
	 */
	public static String getCSVHeader(PCPart.Type partType) {
		return switch (partType) {
			case MEMORY ->
				Memory.getCSVHeader();
		};
	}

	/**
	 * Get the implemented classifier of a specified pc part type
	 *
	 * @param partType
	 * @return classifier
	 */
	public static PCPartClassifier classifierOf(PCPart.Type partType) {
		return switch (partType) {
			case MEMORY ->
				new MemoryClassifier();
		};
	}

	/**
	 * Convert to a CSV line
	 *
	 * @return CSV formatted string
	 */
	public abstract String toCSV();

	/**
	 * JSON object representation that acts as a builder
	 */
	public abstract static class JSON {

		/**
		 * Build the PCPart from the JSON object
		 *
		 * @return PCPart
		 */
		public abstract PCPart build();

		/**
		 * JSON root object
		 */
		public abstract static class Root {

			/**
			 * Read a JSON root object from a file for the specified part type
			 *
			 * @param reader
			 * @param partType
			 * @return JSON root object
			 */
			public static PCPart.JSON.Root from(Reader reader, PCPart.Type partType) {
				return switch (partType) {
					case MEMORY ->
						Memory.JSON.Root.from(reader);
				};
			}

			/**
			 * Convert the JSON root object to a CSV line
			 *
			 * @return CSV line
			 */
			public abstract String toCSV();
		}

	}

}
