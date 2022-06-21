package it.simonvic.pcpfiller.parts;

import java.io.Reader;

/**
 *
 * @author simonvic
 */
public abstract class PCPartsJSON {
	
	public static PCPartsJSON from(Reader reader, PCPart.Type partType) {
		return switch (partType) {
			case MEMORY -> MemoriesJSON.from(reader);
		};
	}
	
	public abstract String toCSV();
	
}
