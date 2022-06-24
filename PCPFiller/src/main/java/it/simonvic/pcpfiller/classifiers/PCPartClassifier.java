package it.simonvic.pcpfiller;

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

	public void load(Path path) throws Exception {
		classifier = (Classifier) weka.core.SerializationHelper.read(path.toString());
	}

	public void save(Path path) throws Exception {
		weka.core.SerializationHelper.write(path.toString(), classifier);
	}

	public abstract String getClassName();

	public abstract String[] getAttributesToIgnore();

	public void train(Instances dataset) throws Exception {
		classifier.buildClassifier(dataset);
	}

	public abstract Evaluation evaluate(Instances dataset) throws Exception;

	public double classify(Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
	}

}
