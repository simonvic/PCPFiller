package it.simonvic.pcpfiller.classifiers;

import java.util.Random;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

/**
 *
 * @author simonvic
 */
public class MemoryClassifier extends PCPartClassifier {

	public MemoryClassifier() {
		super(new RandomForest());
	}

	@Override
	public String getClassName() {
		return "priceEuro";
	}

	@Override
	public String[] getAttributesToIgnore() {
		return new String[]{"model", "pricePerGBEuro", "color"};
	}

	@Override
	public Evaluation evaluate(Instances dataset) throws Exception {
		Evaluation eval = new Evaluation(dataset);

		// cross validation (10-fold)
		// @NOTE the seed is always the same, so we get reproduceable results (like in Weka GUI)
		eval.crossValidateModel(classifier, dataset, 10, new Random(1));
		return eval;
	}

}
