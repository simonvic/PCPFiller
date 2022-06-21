package it.simonvic.pcpfiller;

import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
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

	private static final Option OPT_HELP = optionOf("h,help?Show this help");
	private static final Option OPT_VERBOSE = optionOf("v,verbose?Be verbose. Can be repeated for more verbosity");

	private static final Logger log = LogManager.getLogger();

	protected static int verbosity = 0;

	public static void main(String... args) {

		Options opts = optionsOf(OPT_HELP, OPT_VERBOSE);
		try {
			parseOptions(opts, args);
		} catch (ParseException ex) {
			printHelp(opts);
			System.err.println(ex.getMessage());
			return;
		}
		
		System.out.println(verbosity);

	}

	private static void printHelp(Options opts) {
		new HelpFormatter().printHelp("PCPFiller", opts);
	}

	private static void parseOptions(Options opts, String[] args) throws ParseException {
		CommandLine cli = new DefaultParser().parse(opts, args);

		if (cli.hasOption(OPT_HELP)) {
			printHelp(opts);
			return;
		}

		verbosity = (int) Stream.of(cli.getOptions())
			.filter(opt -> opt.equals(OPT_VERBOSE))
			.count();
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
