package it.simonvic.pcpfiller;

import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 *
 * @author simonvic
 */
public class MemoryClassifier extends PCPartClassifier {

	public MemoryClassifier(Instances dataset) throws Exception {
		super(dataset, new RandomForest());
		setClass("priceEuro");
		removeAttributes("model", "pricePerGBEuro", "color");
	}

	@Override
	public void train() throws Exception {
		buildClassifier();
	}

	@Override
	public Evaluation evaluate() throws Exception {
		Evaluation eval = new Evaluation(dataset);
		
		// cross validation (10-fold)
		// @NOTE the seed is always the same, so we get reproduceable results (like in Weka GUI)
		eval.crossValidateModel(classifier, dataset, 10, new Random(1));
		return eval;
	}


}
