package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.classifiers.PCPartClassifier;
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

	public static String getMissingToken() {
		return Main.getMissingToken();
	}

	private static final Logger log = LogManager.getLogger();

	protected PCPartClassifier pcpClassifier;
	protected Instances dataset;

	public PCPFiller(PCPart.Type partType, Instances dataset) {
		this.pcpClassifier = PCPart.classifierOf(partType);
		this.dataset = dataset;
		dataset.setClass(dataset.attribute(pcpClassifier.getClassName()));
		try {
			this.dataset = removeAttributes(this.dataset, pcpClassifier.getAttributesToIgnore());
		} catch (Exception ex) {
			log.warn("Can't remove attributes: " + Arrays.toString(pcpClassifier.getAttributesToIgnore()));
			log.warn(ex);
		}
	}

	public Instances getDataset() {
		return dataset;
	}

	/**
	 * Train the model
	 *
	 * @throws Exception
	 */
	public void trainModel() throws Exception {
		pcpClassifier.train(dataset);
	}

	/**
	 * Evaluate the previously trained model
	 *
	 * @return
	 * @throws Exception
	 */
	public Evaluation evaluate() throws Exception {
		return pcpClassifier.evaluate(dataset);
	}

	/**
	 * Save the previously trained model to a binary file
	 *
	 * @param path
	 * @throws Exception
	 */
	public void saveModel(Path path) throws Exception {
		pcpClassifier.save(path);
	}

	/**
	 * Load a previously trained and saved model from a binary file
	 *
	 * @param path
	 * @throws Exception
	 */
	public void loadModel(Path path) throws Exception {
		pcpClassifier.load(path);
	}

	/**
	 * Perform the filling of the missing data in the dataset instances
	 *
	 * @throws Exception
	 */
	public void fill() throws Exception {
		for (Instance i : dataset.stream().toList()) {
			i.setClassValue(pcpClassifier.classify(i));
		}
	}

	/**
	 * Helper method to remove attributes from a dataset, given their name, using a Weka filter
	 *
	 * @param dataset
	 * @param attributesNames
	 * @return dataset without the attributes
	 * @throws Exception
	 */
	protected static Instances removeAttributes(Instances dataset, String... attributesNames) throws Exception {
		Remove filter = new Remove();
		filter.setAttributeIndicesArray(Utils.indecesOf(dataset, attributesNames));
		filter.setInputFormat(dataset);
		return Filter.useFilter(dataset, filter);
	}

	/**
	 * Save the dataset in its current state to a file, using the specified format
	 *
	 * @param outputPath
	 * @param outputDatasetFormat
	 * @throws IOException
	 */
	public void saveDataset(Path outputPath, DatasetFormat outputDatasetFormat) throws IOException {
		switch (outputDatasetFormat) {
			case ARFF -> saveDatasetARFF(outputPath);
			case CSV -> saveDatasetCSV(outputPath);
			case JSON -> saveDatasetJSON(outputPath);
		}
	}

	/**
	 * Save the dataset in ARFF format
	 *
	 * @param outputPath
	 * @throws IOException
	 */
	private void saveDatasetARFF(Path outputPath) throws IOException {
		Files.write(outputPath, dataset.toString().getBytes(), StandardOpenOption.CREATE);
	}

	/**
	 * Save the dataset in CSV format
	 *
	 * @param outputPath
	 * @throws IOException
	 */
	private void saveDatasetCSV(Path outputPath) throws IOException {
		throw new UnsupportedOperationException("CSV output format not supported yet.");
	}

	/**
	 * Save the dataset in JSON format
	 *
	 * @param outputPath
	 * @throws IOException
	 */
	private void saveDatasetJSON(Path outputPath) throws IOException {
		throw new UnsupportedOperationException("JSON output format not supported yet.");
	}

	/**
	 * Get the count of instances in the dataset
	 *
	 * @return instances count
	 */
	public int getInstancesCount() {
		return dataset.numInstances();
	}

	/**
	 * Get the count of instances in the dataset that don't have missing values
	 *
	 * @return instances count
	 * @throws Exception
	 */
	public int getCompleteInstancesCount() throws Exception {
		RemoveWithValues filterRemoveIncomplete = new RemoveWithValues();
		filterRemoveIncomplete.setMatchMissingValues(true);
		filterRemoveIncomplete.setInputFormat(dataset);
		return Filter.useFilter(dataset, filterRemoveIncomplete).numInstances();
	}

	/**
	 * Get the count of instances in the dataset that have missing values
	 *
	 * @return
	 * @throws Exception
	 */
	public int getIncompleteInstancesCount() throws Exception {
		return getInstancesCount() - getCompleteInstancesCount();
	}

	/**
	 * Get the summary of the current dataset state
	 *
	 * @return summary string
	 */
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
