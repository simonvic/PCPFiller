package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;

/**
 *
 * @author simonvic
 */
public class PCPartNotSupportedException extends Exception {

	public PCPartNotSupportedException(PCPart.Type partType) {
		super("The " + partType + " is not supported.");
	}

}
