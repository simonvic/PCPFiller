package it.simonvic.pcpfiller;

import java.nio.file.Path;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author simonvic
 */
public abstract class PCPartClassifier {

	protected Classifier classifier;
	protected Instances dataset;

	public PCPartClassifier(Instances dataset, Classifier classifier) {
		this.dataset = dataset;
		this.classifier = classifier;
	}

	public void load(Path path) throws Exception {
		classifier = (Classifier) weka.core.SerializationHelper.read(path.toString());
	}
	
	public void save(Path path) throws Exception {
		weka.core.SerializationHelper.write(path.toString(), classifier);
	}
	
	public abstract void train() throws Exception;

	public abstract Evaluation evaluate() throws Exception;

	public double classify(Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
	}

	protected void setClass(String attributeName) {
		dataset.setClass(dataset.attribute(attributeName));
	}

	protected void removeAttributes(String... attributesNames) throws Exception {
		Remove filter = new Remove();
		filter.setAttributeIndicesArray(Utils.indecesOf(dataset, attributesNames));
		filter.setInputFormat(dataset);
		dataset = Filter.useFilter(dataset, filter);
	}

	protected void buildClassifier() throws Exception {
		classifier.buildClassifier(dataset);
	}

	

}
