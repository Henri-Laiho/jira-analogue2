package apacheUI;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;



public class CLITester {

    public static void test(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("c", "connect", false, "Send connect request to server.")
                .addOption("s", "search", true, "search for project name");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (!cmd.hasOption("c")) {
            System.out.println("Please establish connection first (enter -c) ");
            System.exit(1);
        }

        String searchTerm = cmd.getOptionValue("s");

        if (searchTerm == null || searchTerm.equalsIgnoreCase("")) {
            System.out.println("Search term may not be empty");
        }

        System.out.printf("Searching for project ==> " + searchTerm);


        HelpFormatter formatter = new HelpFormatter();

        final PrintWriter writer = new PrintWriter(System.out);
        formatter.printUsage(writer, 80, "CLITester", options);
        writer.flush();
    }
    public static void main(String[] args) throws ParseException, IOException {


        Runtime.getRuntime().exec(new String[] {"cmd", "/K", "Start"});
        test(args);

        }
    }
