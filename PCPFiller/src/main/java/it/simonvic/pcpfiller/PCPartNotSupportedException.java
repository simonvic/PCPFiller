package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;

/**
 *
 * @author simonvic
 */
public class PCPartNotSupportedException extends Exception {

	public PCPartNotSupportedException(String partType) {
		super("The " + partType + " is not supported.");
	}

}
