package it.simonvic.pcpfiller;

import it.simonvic.pcpfiller.parts.PCPart;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 *
 * @author simonvic
 */
public class Main {

	private static final Logger log = LogManager.getLogger();

	private static final Option OPT_HELP = optionOf("                h,help                                  ?Show this help");
	private static final Option OPT_VERBOSE = optionOf("             v,verbose                               ?Be verbose. Can be repeated for more verbosity");
	private static final Option OPT_LOAD_MODEL = optionOf("          m,load-model       :model-file          ?Specify to load a saved model");
	private static final Option OPT_EVAL_MODEL = optionOf("            eval-model                            ?Specify to evaluate the model");
	private static final Option OPT_SAVE_MODEL = optionOf("          M,save-model       :model-file          ?Specify to save the model");
	private static final Option OPT_PART = optionOf("                p,part             :pc-part-name        ?Specify what pcpart to fill. For a list of supported parts, see --supported-parts");
	private static final Option OPT_SUPPORTED_PARTS = optionOf("     P,supported-parts                       ?Get a list of the supported parts");
	private static final Option OPT_MISSING_TOKEN = optionOf("         missing-token    :token               ?Specify what token to be used when data is missing. Default: '?' (question mark)");
	private static final Option OPT_TEMP_DIR = optionOf("              temp-dir         :directory           ?Specify where to store temporary files used by PCPFiller. Default: '/tmp/PCPFiller'");
	private static final Option OPT_FROM_JSON = optionOf("           j,from-json        :json-file           ?Load the dataset from a json file");
	private static final Option OPT_FROM_CSV = optionOf("            c,from-csv         :csv-file            ?Load the dataset from a csv file");
	private static final Option OPT_OUT_DATASET = optionOf("         O,out-dataset      :output-file         ?Specify where to store the output dataset");
	private static final Option OPT_OUT_DATASET_FORMAT = optionOf("  F,out-format       :output-format       ?Specify what format to use when saving dataset");

	private static final Options OPTIONS = optionsOf(
		OPT_HELP, OPT_VERBOSE, OPT_LOAD_MODEL, OPT_EVAL_MODEL, OPT_SAVE_MODEL,
		OPT_PART, OPT_SUPPORTED_PARTS, OPT_FROM_JSON, OPT_FROM_CSV,
		OPT_MISSING_TOKEN, OPT_TEMP_DIR, OPT_OUT_DATASET, OPT_OUT_DATASET_FORMAT
	);

	private static int verbosity = 0;
	private static Path tempDir = Path.of("/tmp/PCPFiller");
	private static String missingToken = "?";
	private static Path csvSourcePath;
	private static Path jsonSourcePath;
	private static Path modelPathToLoad;
	private static boolean evaluateModel;
	private static Path modelSavePath;
	private static Path outputDatasetPath;
	private static DatasetFormat outputDatasetFormat = DatasetFormat.ARFF;
	private static PCPart.Type partToFill;

	public static String getMissingToken() {
		return missingToken;
	}

	public static void main(String... args) throws IOException, Exception {
		try {
			parseOptions(args);
		} catch (MissingOptionException | InteruptiveOptionException ex) {
			return;
		} catch (ParseException ex) {
			printHelp();
			log.error(ex);
			return;
		} catch (PCPartNotSupportedException ex) {
			log.error(ex);
			return;
		}

		log.info("Making temp directory: " + tempDir);
		tempDir.toFile().mkdirs();

		// @todo improve Weka exceptions
		PCPFiller filler = new PCPFiller(partToFill, loadDataset());

		log.info("Summary:");
		filler.toSummaryString()
			.lines()
			.map(line -> "\t" + line)
			.forEach(log::info);

		if (modelPathToLoad != null) {
			log.info("Loading model: " + modelPathToLoad);
			filler.loadModel(modelPathToLoad);
		} else {
			log.info("Training model...");
			filler.trainModel();
			log.info("Done!");
		}

		if (evaluateModel) {
			log.info("Evaluating model...");
			Evaluation eval = filler.evaluate();
			eval.toSummaryString()
				.lines()
				.skip(1)
				.map(line -> "\t" + line)
				.forEach(log::info);
		}

		if (modelSavePath != null) {
			log.info("Saving model to: " + modelSavePath);
			modelSavePath.getParent().toFile().mkdirs();
			filler.saveModel(modelSavePath);
			log.info("Done!");
		}

		log.info("Filling...");
		filler.fill();
		log.info("Done!");

		log.info("Summary:");
		filler.toSummaryString()
			.lines()
			.map(line -> "\t" + line)
			.forEach(log::info);

		if (outputDatasetPath != null) {
			log.info("Saving dataset to: " + outputDatasetPath);
			outputDatasetPath.getParent().toFile().mkdirs();
			filler.saveDataset(outputDatasetPath, outputDatasetFormat);
			log.info("Done!");
		}
	}

	private static Instances loadDataset() throws IOException {
		if (jsonSourcePath != null && csvSourcePath != null) {
			log.warn("Both CSV and JSON have been specified. CSV will be used!");
			return Utils.instancesFromCSV(csvSourcePath.toFile());
		}

		if (csvSourcePath != null) {
			log.info("Loading CSV dataset: " + csvSourcePath);
			return Utils.instancesFromCSV(csvSourcePath.toFile());
		}

		if (jsonSourcePath != null) {
			log.info("Loading JSON dataset: " + jsonSourcePath);
			return Utils.instancesFromJSON(jsonSourcePath.toFile(), partToFill);
		}

		return null;
	}

	private static void printHelp() {
		new HelpFormatter().printHelp("PCPFiller", OPTIONS);
	}

	private static void printSupportedParts() {
		log.info("Supported PC parts:");
		for (String part : PCPFiller.getSupportedParts()) {
			log.info("\t- " + part);
		}
	}

	private static void parseOptions(String[] args) throws ParseException, InteruptiveOptionException, PCPartNotSupportedException, DatasetFormatSupportedException {
		CommandLine cli = new DefaultParser().parse(OPTIONS, args);

		verbosity = (int) Stream.of(cli.getOptions())
			.filter(OPT_VERBOSE::equals)
			.count();

		if (cli.hasOption(OPT_HELP)) {
			printHelp();
			throw new InteruptiveOptionException();
		}

		if (cli.hasOption(OPT_SUPPORTED_PARTS)) {
			printSupportedParts();
			throw new InteruptiveOptionException();
		}

		if (!cli.hasOption(OPT_PART)) {
			log.error("You need to specify at least one PC part!");
			printSupportedParts();
			throw new MissingOptionException("");
		}

		if (!cli.hasOption(OPT_FROM_CSV) && !cli.hasOption(OPT_FROM_JSON)) {
			log.error("You need to specify at least a CSV or JSON source!");
			printHelp();
			throw new MissingOptionException("");
		}

		try {
			partToFill = PCPart.Type.valueOf(cli.getOptionValue(OPT_PART).toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new PCPartNotSupportedException(cli.getOptionValue(OPT_PART));
		}

		if (cli.hasOption(OPT_FROM_CSV)) {
			csvSourcePath = Path.of(cli.getOptionValue(OPT_FROM_CSV));
		}

		if (cli.hasOption(OPT_FROM_JSON)) {
			jsonSourcePath = Path.of(cli.getOptionValue(OPT_FROM_JSON));
		}

		if (cli.hasOption(OPT_MISSING_TOKEN)) {
			missingToken = cli.getOptionValue(OPT_MISSING_TOKEN);
		}

		if (cli.hasOption(OPT_TEMP_DIR)) {
			tempDir = Path.of(cli.getOptionValue(OPT_TEMP_DIR));
		}

		evaluateModel = cli.hasOption(OPT_EVAL_MODEL);

		if (cli.hasOption(OPT_LOAD_MODEL)) {
			modelPathToLoad = Path.of(cli.getOptionValue(OPT_LOAD_MODEL));
		}

		if (cli.hasOption(OPT_SAVE_MODEL)) {
			modelSavePath = Path.of(cli.getOptionValue(OPT_SAVE_MODEL));
		}

		if (cli.hasOption(OPT_OUT_DATASET)) {
			outputDatasetPath = Path.of(cli.getOptionValue(OPT_OUT_DATASET));
		}

		if (cli.hasOption(OPT_OUT_DATASET_FORMAT)) {
			try {
				if (!cli.hasOption(OPT_OUT_DATASET)) {
					log.warn("Output dataset file not specified. Ignoring format option...");
				} else {
					outputDatasetFormat = DatasetFormat.valueOf(cli.getOptionValue(OPT_OUT_DATASET_FORMAT).toUpperCase());
				}
			} catch (IllegalArgumentException ex) {
				throw new DatasetFormatSupportedException(cli.getOptionValue(OPT_OUT_DATASET_FORMAT));
			}
		}
	}

	private static Options optionsOf(String[] options) {
		Options opts = new Options();
		Stream.of(options)
			.map(Main::optionOf)
			.forEach(opts::addOption);
		return opts;
	}

	private static Options optionsOf(Option... options) {
		Options opts = new Options();
		for (Option option : options) {
			opts.addOption(option);
		}
		return opts;
	}

	private static Option optionOf(String option) {
		Option.Builder o = Option.builder();

		// If present, set description and trim it
		int descIndex = option.indexOf("?");
		if (descIndex != -1) {
			o.desc(option.substring(descIndex + 1));
			option = option.substring(0, descIndex);
		}

		// Remove all spaces
		option = option.replaceAll(" ", "");

		// If present, set argument and trim it
		int argIndex = option.indexOf(":");
		if (argIndex != -1) {
			String argName = option.substring(argIndex + 1);
			o.hasArg();
			o.argName(argName.isBlank() ? "arg" : argName);
			option = option.substring(0, argIndex);
		}

		// If present, set short and/or long option name
		if (option.contains(",")) {
			String[] optNames = option.split(",");
			o.option(optNames[0]);
			o.longOpt(optNames[1]);
		} else {
			if (option.length() <= 1) {
				o.option(option);
			} else {
				o.longOpt(option);
			}
		}

		return o.build();
	}
}
