package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 *
 * @author simonvic
 */
public class PCPFiller {

	//@todo replace with enum
	public static String[] getSupportedParts() {
		return new String[]{"memory"};
	}

	public static String getMissingToken() {
		return Main.getMissingToken();
	}

	private static final Logger log = LogManager.getLogger();

	protected PCPartClassifier pcpClassifier;
	protected Instances dataset;

	public PCPFiller(PCPart.Type partType, Instances dataset) {
		this.pcpClassifier = getPCPClassifier(partType);
		this.dataset = dataset;
		dataset.setClass(dataset.attribute(pcpClassifier.getClassName()));
		try {
			this.dataset = removeAttributes(this.dataset, pcpClassifier.getAttributesToIgnore());
		} catch (Exception ex) {
			log.warn("Can't remove attributes: " + Arrays.toString(pcpClassifier.getAttributesToIgnore()));
			log.warn(ex);
		}
	}

	public void trainModel() throws Exception {
		pcpClassifier.train(dataset);
	}

	public Evaluation evaluate() throws Exception {
		return pcpClassifier.evaluate(dataset);
	}

	public void saveModel(Path path) throws Exception {
		pcpClassifier.save(path);
	}

	public void loadModel(Path path) throws Exception {
		pcpClassifier.load(path);
	}

	public void fill() throws Exception {
		for (Instance i : dataset.stream().toList()) {
			i.setClassValue(pcpClassifier.classify(i));
		}
	}

	private PCPartClassifier getPCPClassifier(PCPart.Type partType) {
		return switch (partType) {
			case MEMORY ->
				new MemoryClassifier();
		};
	}

	protected static Instances removeAttributes(Instances dataset, String... attributesNames) throws Exception {
		Remove filter = new Remove();
		filter.setAttributeIndicesArray(Utils.indecesOf(dataset, attributesNames));
		filter.setInputFormat(dataset);
		return Filter.useFilter(dataset, filter);
	}

	public Instances getDataset() {
		return dataset;
	}

	void saveDataset(Path outputPath, DatasetFormat outputDatasetFormat) throws IOException {
		switch (outputDatasetFormat) {
			case ARFF -> saveDatasetARFF(outputPath);
			case CSV -> saveDatasetCSV(outputPath);
			case JSON -> saveDatasetJSON(outputPath);
		}
	}

	private void saveDatasetARFF(Path outputPath) throws IOException {
		Files.write(outputPath, dataset.toString().getBytes(), StandardOpenOption.CREATE);
	}

	private void saveDatasetCSV(Path outputPath) {
		throw new UnsupportedOperationException("CSV output format not supported yet.");
	}

	private void saveDatasetJSON(Path outputPath) {
		throw new UnsupportedOperationException("JSON output format not supported yet.");
	}

	public int getInstancesCount() {
		return dataset.numInstances();
	}

	public int getCompleteInstancesCount() throws Exception {
		RemoveWithValues filterRemoveIncomplete = new RemoveWithValues();
		filterRemoveIncomplete.setMatchMissingValues(true);
		filterRemoveIncomplete.setInputFormat(dataset);
		return Filter.useFilter(dataset, filterRemoveIncomplete).numInstances();
	}

	public int getIncompleteInstancesCount() throws Exception {
		return getInstancesCount() - getCompleteInstancesCount();
	}

	public String toSummaryString() {
		StringBuilder sb = new StringBuilder();
		try {
			int totalInstancesCount = getInstancesCount();
			int completeCount = getCompleteInstancesCount();
			int incompleteCount = getIncompleteInstancesCount();

			sb.append(String.format("Total instances:     %d\n", totalInstancesCount));
			sb.append(String.format("Complete instances:  %d\n", completeCount));
			sb.append(String.format("Icomplete instances: %d\n", incompleteCount));
			sb.append(String.format("Usable ratio:        %d %%\n", completeCount * 100 / totalInstancesCount));
		} catch (Exception ex) {
			log.warn(ex);
		}
		return sb.toString();
	}

}
