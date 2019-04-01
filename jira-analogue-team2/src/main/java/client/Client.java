package client;

import apacheUI.TUI;
import messages.JiraMessageHandler;

import java.io.IOException;

public class Client implements JiraMessageHandler {
    TUI tui;

    Client(String[] args) throws IOException {
        tui = new TUI(args);
    }

    public boolean createTask(byte[] data) {
        return false;
    }

    public boolean removeTask(byte[] data) {
        return false;
    }

    public boolean updateTimeTask(byte[] data) {
        return false;
    }

    public boolean setStatusTask(byte[] data) {
        return false;
    }
}
