package server;

import data.RawProject;
import data.RawTask;
import data.RawUser;

import java.util.ArrayList;
import java.util.List;

class RawServerData {
    List<RawProject> rawProjects = new ArrayList<>();

    // multiple projects can have the same task (requested by the other team)
    List<RawTask> rawTasks = new ArrayList<>();

    // (userSalts.size() == rawUsers.size) and they are in the same order.
    List<RawServerUser> rawUsers = new ArrayList<>();
    //List<byte[]> userSalts = new ArrayList<>();
}
