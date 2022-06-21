package it.simonvic.pcpfiller.parts;

import it.simonvic.pcpfiller.PCPartNotSupportedException;
import java.io.Reader;

/**
 *
 * @author simonvic
 */
public abstract class PCPartsJSON {
	
	public static PCPartsJSON from(Reader reader, PCPart.Type partType) throws PCPartNotSupportedException {
		return switch (partType) {
			case MEMORY -> MemoriesJSON.from(reader);
			default -> throw new PCPartNotSupportedException(partType);
		};
	}
	
	public abstract String toCSV();
	
}
