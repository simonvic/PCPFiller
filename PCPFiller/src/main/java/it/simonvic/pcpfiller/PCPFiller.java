package it.simonvic.pcpfiller;

import com.google.gson.Gson;
import it.simonvic.pcpfiller.parts.Memory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 *
 * @author simonvic
 */
public class PCPFiller {

	protected static final Logger L = LogManager.getLogger();
	
	public static final String MISSING_VALUE_TOKEN = "?";

	private void convertJSONToCSV() throws IOException {
		Reader jsonReader = new InputStreamReader(PCPFiller.class.getResourceAsStream("parts/memory.json"));
		Memory.JSON.Root jsonObj = new Gson().fromJson(jsonReader, Memory.JSON.getType());

		StringBuilder sb = new StringBuilder();
		sb.append(Memory.getCSVHeader());
		jsonObj.memory
			.stream()
			.map(Memory.JSON::build)
			.map(Memory::toCSV)
			.map(csvEntry -> csvEntry.replace("null", MISSING_VALUE_TOKEN))
			.forEach(csvEntry -> sb.append(csvEntry).append("\n"));

		Files.writeString(Path.of("/tmp/memory.csv"), sb, StandardOpenOption.CREATE);
	}

	private void trainModel(Classifier classifier, Instances trainDataset) throws IOException, Exception {
		System.out.println("Training model...");

		// set "price" as class
		trainDataset.setClass(trainDataset.attribute("priceEuro"));

		// Remove columns "model", "pricePerGB" and "color"
		Remove filterRemoveAttributes = new Remove();
		filterRemoveAttributes.setAttributeIndicesArray(indecesOf(trainDataset,
			"model",
			"pricePerGBEuro",
			"color"
		));
		filterRemoveAttributes.setInputFormat(trainDataset);
		trainDataset = Filter.useFilter(trainDataset, filterRemoveAttributes);

		// Remove instances with missing data
		RemoveWithValues filterRemoveIncomplete = new RemoveWithValues();
		filterRemoveIncomplete.setMatchMissingValues(true);
		filterRemoveIncomplete.setInputFormat(trainDataset);
		trainDataset = Filter.useFilter(trainDataset, filterRemoveIncomplete);

		classifier.buildClassifier(trainDataset);

		System.out.println(classifier);
	}

	private Evaluation evaluateModel(Classifier classifier, Instances trainDataset) throws Exception {
		System.out.println("Evaluating model...");
		// cross validation (10-fold)
		Evaluation eval = new Evaluation(trainDataset);
		// @NOTE the seed is always the same, so we get reproduceable results (like in Weka GUI)
		eval.crossValidateModel(classifier, trainDataset, 10, new Random(1));
		return eval;
	}

	public void run() throws IOException, Exception {
		convertJSONToCSV();

		Classifier classifier;
		try {
			classifier = loadModel("/tmp/memory.model");
		} catch (FileNotFoundException | ClassNotFoundException ex) {
			System.out.println("Can't create model! Creating new!");
			classifier = new RandomForest();
			Instances trainDataset = getInstancesOf(Path.of("/tmp/memory.csv").toFile());
			trainModel(classifier, trainDataset);
			Evaluation eval = evaluateModel(classifier, trainDataset);
			System.out.println(eval.toSummaryString());
			saveModel(classifier, "/tmp/memory.model");
		}

		Instances dataset = getInstancesOf(Path.of("/tmp/memory.csv").toFile());
		dataset.setClass(dataset.attribute("priceEuro"));
		// Remove columns "model", "pricePerGB" and "color"
		Remove filterRemoveAttributes = new Remove();
		filterRemoveAttributes.setAttributeIndicesArray(indecesOf(dataset,
			"model",
			"pricePerGBEuro",
			"color"
		));
		filterRemoveAttributes.setInputFormat(dataset);
		dataset = Filter.useFilter(dataset, filterRemoveAttributes);

		// Remove instances with missing data
		RemoveWithValues filterRemoveIncomplete = new RemoveWithValues();
		filterRemoveIncomplete.setMatchMissingValues(true);
		filterRemoveIncomplete.setInputFormat(dataset);
		Instances usable = Filter.useFilter(dataset, filterRemoveIncomplete);

		System.out.printf("Initital dataset instances: %d\n", dataset.numInstances());
		System.out.printf("Usable instances:           %d\n", usable.numInstances());
		System.out.printf("Ratio:                      %d %%\n", usable.numInstances() * 100 / dataset.numInstances());

		for (Instance instance : dataset.stream().toList()) {
			instance.setClassValue(classifier.classifyInstance(instance));
		}

		Instances newUsable = Filter.useFilter(dataset, filterRemoveIncomplete);

		System.out.printf("New Usable instances: %d\n", newUsable.numInstances());
		System.out.printf("Ratio:                %d %%\n", newUsable.numInstances() * 100 / dataset.numInstances());

	}

	private static Instances getInstancesOf(String csvValues) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new ByteArrayInputStream(csvValues.getBytes()));
		return csvLoader.getDataSet();
	}

	private static Instances getInstancesOf(File csvFile) throws IOException {
		CSVLoader csvLoader = new CSVLoader();
		csvLoader.setSource(new FileInputStream(csvFile));
		return csvLoader.getDataSet();
	}

	private static int[] indecesOf(Instances dataset, String... attributesNames) {
		return List.of(attributesNames)
			.stream()
			.mapToInt(attributeName -> dataset.attribute(attributeName).index())
			.toArray();
	}

	private static void saveModel(Classifier classifier, String destinationFilePath) throws Exception {
		System.out.println("Saving model...");
		weka.core.SerializationHelper.write(destinationFilePath, classifier);
	}

	private static Classifier loadModel(String filePath) throws Exception {
		System.out.println("Loading model...");
		return (Classifier) weka.core.SerializationHelper.read(filePath);
	}
}
