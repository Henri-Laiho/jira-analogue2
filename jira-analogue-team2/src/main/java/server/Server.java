package server;

import Messages.JiraMessageHandler;

public class Server implements JiraMessageHandler {

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
