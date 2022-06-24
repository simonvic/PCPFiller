package it.simonvic.pcpfiller.parts;

import java.io.Reader;

/**
 *
 * @author simonvic
 */
public abstract sealed class PCPart permits Memory {

	public enum Type {
		MEMORY
	}

//	public abstract String getCSVHeader();
	public static String getCSVHeader(PCPart.Type partType) {
		return switch (partType) {
			case MEMORY ->
				Memory.getCSVHeader();
		};
	}

	public abstract String toCSV();

	public abstract static class JSON {

		public abstract PCPart build();

		public abstract static class Root {

			public static PCPart.JSON.Root from(Reader reader, PCPart.Type partType) {
				return switch (partType) {
					case MEMORY ->
						Memory.JSON.Root.from(reader);
				};
			}

			public abstract String toCSV();
		}

	}

}
