package it.simonvic.pcpfiller.classifiers;

import java.nio.file.Path;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author simonvic
 */
public abstract class PCPartClassifier {

	protected Classifier classifier;

	public PCPartClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	
	/**
	 * Load the classifier model from a file
	 * 
	 * @param path
	 * @throws Exception 
	 */
	public void load(Path path) throws Exception {
		classifier = (Classifier) weka.core.SerializationHelper.read(path.toString());
	}

	
	/**
	 * Save the classifier model to a file
	 * 
	 * @param path
	 * @throws Exception 
	 */
	public void save(Path path) throws Exception {
		weka.core.SerializationHelper.write(path.toString(), classifier);
	}

	/**
	 * Get the class attribute name that the classifier must work on
	 * 
	 * @return class name
	 */
	public abstract String getClassName();

	
	/**
	 * Get a list of attributes names to ignore
	 * 
	 * @return attributes to ignore
	 */
	public abstract String[] getAttributesToIgnore();

	
	/**
	 * Train the classifier on the given dataset
	 * 
	 * @param dataset
	 * @throws Exception 
	 */
	public void train(Instances dataset) throws Exception {
		classifier.buildClassifier(dataset);
	}

	
	/**
	 * Evaluate the classifier model on the given dataset
	 * 
	 * @param dataset
	 * @return result evaluation
	 * @throws Exception 
	 */
	public abstract Evaluation evaluate(Instances dataset) throws Exception;

	
	/**
	 * Classify and instance
	 * 
	 * @param instance
	 * @return double value if class is numeric value or class index if nominal
	 * @throws Exception 
	 */
	public double classify(Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
	}

}
