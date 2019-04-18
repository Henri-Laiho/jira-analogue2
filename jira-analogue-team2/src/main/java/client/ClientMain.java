package client;

import common.Connection;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ClientMain {

    private static String userNow;

    private static void setUserNow(String userNow) {
        ClientMain.userNow = userNow;
    }
    public static String getUser() {
        return userNow;
    }
    private static boolean connectToServer(String serverName, CommandLine cmd, Client client) throws IOException, InterruptedException {

        // open connection
        int port = Connection.DEFAULT_PORT;
        String ip = serverName;
        if (serverName.contains(":")) {
            String[] parts = serverName.split(":");
            ip = parts[0];
            port = Integer.parseInt(parts[1]);
        }
        client.connect(ip, port);

        String user = cmd.getOptionValue("user");
        setUserNow(user);
        String pass = cmd.getOptionValue("pass");

        if (user != null && pass != null) {
            if (client.login(user, pass)) {
                System.out.println("User " + user + " logged in.");
                return true;

            } else
                System.out.println("Login failed.");
        } else {
            System.out.println("Please log in (enter -user <username> and -pass <password>)");
        }
        return false;


        // project selection on TUI main screen
        /*String searchTerm = cmd.getOptionValue("s");

        if (searchTerm == null || searchTerm.isEmpty()) {
            System.out.println("Search term may not be empty");
        } else {

            System.out.print("Searching for project list ==> " + searchTerm);
            if (projectList.contains(searchTerm)) {

            } else {
                System.out.println("project not found in database");
            }
        }*/

    }

    private static void commandLineUI(String[] args) throws ParseException, IOException, InterruptedException {
        Options options = new Options();
        options.addOption("c", "connect", true, "Send connect request to server(with name and port separated by ':').")
                .addOption("h", "help", false, "print this message")
                .addOption("v", "version", false, "print the version information and exit")
                .addOption("s", "search", true, "search for project name(override)")
                .addOption("user", "username", true, "command to log in with this username or email")
                .addOption("pass", "password", true, "command to log in with this password");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        String serverName = cmd.getOptionValue("c");

        List<String> serverList = new ArrayList<>();
        //List<String> projectList = new ArrayList<>();
        serverList.add("localhost:" + Connection.DEFAULT_PORT);
        //projectList.add("testproject");

        Client client = new Client();

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            final PrintWriter writer = new PrintWriter(System.out);
            formatter.printUsage(writer, 80, "Jira Analogue", options);
            writer.flush();
        } else if (!cmd.hasOption("c") || serverName == null/* || !serverList.contains(serverName)*/) {
            System.out.println("Please establish connection with specific server (enter -c with server name)");
            System.exit(1);
        } /*else if (connectToServer(serverName, cmd, client) && cmd.hasOption("s") && !(cmd.getOptionValue("s") == null)) {
            String projectName = cmd.getOptionValue("s");
            int userId = cmd.getOptionValue("user").;
            if (Client.getProjectList().contains(projectName)) { //seperate non-static class + new method in Client?
                client.startProjectTUI(args);
            }
        }*/
        else if (connectToServer(serverName, cmd, client)) {
            client.startTUI(args);
        }


    }

    public static void main(String[] args) throws Exception {
        System.out.println("client.");
        commandLineUI(args);
    }
}
