package it.simonvic.pcpfiller.exceptions;

/**
 *
 * @author simonvic
 */
public class PCPartNotSupportedException extends Exception {

	public PCPartNotSupportedException(String partType) {
		super("The " + partType + " is not supported.");
	}

}
