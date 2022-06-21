package it.simonvic.pcpfiller.parts;

/**
 *
 * @author simonvic
 */
public abstract sealed class PCPart permits Memory {

	abstract String toCSV();
	
	public abstract static class JSON {
		
	}

}
