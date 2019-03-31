package client;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ClientMain {
    private static void connectToServer(String serverName, CommandLine cmd, List<String> projectList, String[] args) throws IOException {
        // TODO: open connection
        boolean establishedConnection = true;

        if (establishedConnection) {
            Options connectedOptions = new Options();
            connectedOptions.addOption("s", "search", true, "search for project name")
                    .addOption("log", "login", true, "command to log into specified project");

            String searchTerm = cmd.getOptionValue("s");
            if (searchTerm == null || searchTerm.isEmpty()) {
                System.out.println("Search term may not be empty");
            }
            else {

                // TODO: search on server side
                System.out.print("Searching for project ==> " + searchTerm);
                if (projectList.contains(searchTerm)) {
                    Client client = new Client(args);
                    //TUI.main(args);
                }
                else {
                    System.out.println("project not found in database");
                }
            }

        }
    }

    private static void commandLineUI(String[] args) throws ParseException, IOException {
        Options options = new Options();
        options.addOption("c", "connect", true, "Send connect request to server(with name).")
                .addOption("h", "help", false, "print this message")
                .addOption("version", "print the version information and exit");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String serverName = cmd.getOptionValue("c");

        List<String> serverList = new ArrayList<>();
        List<String> projectList = new ArrayList<>();
        serverList.add("test");
        projectList.add("testproject");

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();

            final PrintWriter writer = new PrintWriter(System.out);
            formatter.printUsage(writer, 80, "Jira Analogue", options);
            writer.flush();
        } else if (!cmd.hasOption("c") || serverName == null || !serverList.contains(serverName)) {
            System.out.println("Please establish connection with specific server (enter -c with server name)");
            System.exit(1);
        } else {
            connectToServer(serverName, cmd, projectList, args);
        }


    }

    public static void main(String[] args) throws Exception {
        System.out.println("client.");

        commandLineUI(args);
    }
}
