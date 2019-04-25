package server;

import common.Project;
import common.User;
import data.RawProject;
import data.RawTask;
import data.RawUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RawServerData {
    public List<RawProject> rawProjects = new ArrayList<>();

    // (userSalts.size() == rawUsers.size) and they are in the same order.
    public List<RawUser> rawUsers = new ArrayList<>();
    public List<byte[]> userSalts = new ArrayList<>();
}
