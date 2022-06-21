package it.simonvic.pcpfiller.parts;

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

	}

}
