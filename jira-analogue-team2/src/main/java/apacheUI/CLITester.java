package apacheUI;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


//testimiseks <args -c test -s testproject>

public class CLITester {

    static void test(String[] args) throws ParseException, IOException {
        boolean establishedConnection;
        //lisab uued CMD optionid
        Options options = new Options();
        options.addOption("c", "connect", true, "Send connect request to server(with name).")
                .addOption("h", "help", false, "print this message")
                .addOption("version", "print the version information and exit")
                .addOption("s", "search", true, "search for project name")
                .addOption("log", "login", true, "command to log into specified project");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String serverName = cmd.getOptionValue("c");
        List<String> serverList = new ArrayList<>();
        List<String> projectList = new ArrayList<>();
        serverList.add("test");
        projectList.add("testproject");

        if (!cmd.hasOption("c") || serverName == null || !serverList.contains(serverName)) {
            System.out.println("Please establish connection with specific server (enter -c with server name)");
            System.exit(1);
        } else {

            establishedConnection = true;

            if (establishedConnection) {

                String searchTerm = cmd.getOptionValue("s");
                if (searchTerm == null || searchTerm.equalsIgnoreCase("")) {
                    System.out.println("Search term may not be empty");
                } else {
                    System.out.print("Searching for project ==> " + searchTerm + " \n");
                    if (projectList.contains(searchTerm)) {
                        TUI.main(args);
                    } else {
                        System.out.println("project not found in database");
                    }
                }
                HelpFormatter formatter = new HelpFormatter();

                final PrintWriter writer = new PrintWriter(System.out);
                formatter.printUsage(writer, 80, "CLITester", options);
                writer.flush();

            }
        }
    }

    public static void main(String[] args) throws ParseException, IOException {
        test(args);
    }
}
