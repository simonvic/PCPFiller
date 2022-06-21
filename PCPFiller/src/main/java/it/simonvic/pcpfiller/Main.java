package it.simonvic.pcpfiller;

import java.nio.file.Path;
import java.util.logging.Level;
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

/**
 *
 * @author simonvic
 */
public class Main {

	private static final Logger log = LogManager.getLogger();

	private static final Option OPT_HELP = optionOf("            h,help                                  ?Show this help");
	private static final Option OPT_VERBOSE = optionOf("         v,verbose                               ?Be verbose. Can be repeated for more verbosity");
	private static final Option OPT_LOAD_MODEL = optionOf("      m,load-model       :model-file          ?Specify to load a saved model");
	private static final Option OPT_SAVE_MODEL = optionOf("      M,save-model       :model-file          ?Specify to save the model");
	private static final Option OPT_PART = optionOf("            p,part             :pc-part-name        ?Specify what pcpart to fill. For a list of supported parts, see --supported-parts");
	private static final Option OPT_SUPPORTED_PARTS = optionOf(" P,supported-parts                       ?Get a list of the supported parts");
	private static final Option OPT_FROM_JSON = optionOf("       j,from-json        :json-file           ?Load the dataset from a json file");
	private static final Option OPT_FROM_CSV = optionOf("        c,from-csv         :csv-file            ?Load the dataset from a csv file");
	private static final Option OPT_MISSING_TOKEN = optionOf("     missing-token    :token               ?Specify what token to be used when data is missing. Default: '?' (question mark)");
	private static final Option OPT_TEMP_DIR = optionOf("          temp-dir         :directory           ?Specify where to store temporary files used by PCPFiller. Default: '/tmp/PCPFiller'");

	protected static int verbosity = 0;
	protected static Path tempDir = Path.of("/tmp/PCPFiller");
	protected static String missingToken = "?";
	protected static Path csvSource;
	protected static Path jsonSource;
	protected static Path modelToLoad;
	protected static Path modelSaveFile;
	protected static String partToFill;

	public static void main(String... args) {

		Options opts = optionsOf(
			OPT_HELP, OPT_VERBOSE, OPT_LOAD_MODEL, OPT_SAVE_MODEL,
			OPT_PART, OPT_SUPPORTED_PARTS, OPT_FROM_JSON, OPT_FROM_CSV,
			OPT_MISSING_TOKEN, OPT_TEMP_DIR
		);
		try {
			parseOptions(opts, args);
		} catch (MissingOptionException | InteruptiveOptionException ex) {
			return;
		} catch (ParseException ex) {
			printHelp(opts);
			System.err.println(ex.getMessage());
			return;
		}

		if (verbosity >= 3) log.info("Making temp directory: " + tempDir);
		tempDir.toFile().mkdirs();

		if (jsonSource != null && csvSource != null) {
			log.warn("Both CSV and JSON have been specified. CSV will be used!");
		} else if (jsonSource != null) {
			if (verbosity >= 1) log.info("Loading JSON dataset: " + jsonSource);
			// blah blah
		} else if (csvSource != null) {
			if (verbosity >= 1) log.info("Loading CSV dataset: " + csvSource);
			// blah blah
		}

		if (verbosity >= 1 && modelToLoad != null) log.info("Model to load: " + modelToLoad);
		if (verbosity >= 1 && modelSaveFile != null) log.info("Model will be saved in : " + modelSaveFile);
		if (verbosity >= 1) log.info("Missing token: " + missingToken);

		log.info("PCPart to fill: " + partToFill);
	}

	private static void printHelp(Options opts) {
		new HelpFormatter().printHelp("PCPFiller", opts);
	}

	private static void printSupportedParts() {
		log.info("Supported PC parts:");
		for (String part : PCPFiller.getSupportedParts()) {
			log.info("\t- " + part);
		}
	}

	private static void parseOptions(Options opts, String[] args) throws ParseException, InteruptiveOptionException {
		CommandLine cli = new DefaultParser().parse(opts, args);

		verbosity = (int) Stream.of(cli.getOptions())
			.filter(OPT_VERBOSE::equals)
			.count();

		if (cli.hasOption(OPT_HELP)) {
			printHelp(opts);
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
			printHelp(opts);
			throw new MissingOptionException("");
		}
		
		partToFill = cli.getOptionValue(OPT_PART);

		if (cli.hasOption(OPT_FROM_CSV)) {
			csvSource = Path.of(cli.getOptionValue(OPT_FROM_CSV));
		}

		if (cli.hasOption(OPT_FROM_JSON)) {
			jsonSource = Path.of(cli.getOptionValue(OPT_FROM_JSON));
		}

		if (cli.hasOption(OPT_MISSING_TOKEN)) {
			missingToken = cli.getOptionValue(OPT_MISSING_TOKEN);
		}

		if (cli.hasOption(OPT_TEMP_DIR)) {
			tempDir = Path.of(cli.getOptionValue(OPT_TEMP_DIR));
		}

		if (cli.hasOption(OPT_LOAD_MODEL)) {
			modelToLoad = Path.of(cli.getOptionValue(OPT_LOAD_MODEL));
		}

		if (cli.hasOption(OPT_SAVE_MODEL)) {
			modelSaveFile = Path.of(cli.getOptionValue(OPT_SAVE_MODEL));
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
