package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
		this.dataset = dataset;
		this.pcpClassifier = getPCPClassifier(partType);
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
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	private void saveDatasetJSON(Path outputPath) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

}
