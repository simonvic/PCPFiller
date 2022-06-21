package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
import java.nio.file.Path;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author simonvic
 */
public class PCPFiller {

	public static String[] getSupportedParts() {
		return new String[]{"memory"};
	}

	public static String getMissingToken() {
		return Main.getMissingToken();
	}

	protected PCPartClassifier pcpClassifier;
	protected Instances dataset;

	public PCPFiller(PCPart.Type partType, Instances dataset) throws PCPartNotSupportedException, Exception {
		this.dataset = dataset;
		this.pcpClassifier = getPCPClassifier(partType);
	}

	public void trainModel() throws Exception {
		pcpClassifier.train();
	}

	public Evaluation evaluate() throws Exception {
		return pcpClassifier.evaluate();
	}

	public void saveModel(Path path) throws Exception {
		pcpClassifier.save(path);
	}

	public void loadModel(Path path) throws Exception {
		pcpClassifier.load(path);
	}

	public void fill() throws Exception {
		
	}

	private PCPartClassifier getPCPClassifier(PCPart.Type partType) throws PCPartNotSupportedException, Exception {
		return switch (partType) {
			case MEMORY ->
				new MemoryClassifier(dataset);
			default -> throw new PCPartNotSupportedException(partType);
		};
	}

}
