package it.simonvic.pcpfiller.parts;

import it.simonvic.pcpfiller.PCPartNotSupportedException;

/**
 *
 * @author simonvic
 */
public abstract sealed class PCPart permits Memory {

	public enum Type {
		MEMORY, VIDEO_CARD, CPU //etc.
	}

//	public abstract String getCSVHeader();
	public static String getCSVHeader(PCPart.Type partType) throws PCPartNotSupportedException {
		return switch (partType) {
			case MEMORY ->
				Memory.getCSVHeader();
			default -> throw new PCPartNotSupportedException(partType);
		};
	}

	public abstract String toCSV();

	public abstract static class JSON {
				
		public abstract PCPart build();		
		
	}

}
