package server;

import common.Project;
import common.User;
import data.RawUser;

import java.util.List;

public class ServerUser extends User {
    private byte[] salt;

    ServerUser(RawServerUser rawUser) {
        super(rawUser);
        this.salt = rawUser.salt;
    }

    @Override
    public RawServerUser toRawUser(List<Project> allProjects) {
        RawUser rawUser = super.toRawUser(allProjects);
        return new RawServerUser(rawUser.userId, rawUser.username, rawUser.passwordHash, rawUser.userEmail, rawUser.lastOnlineMS, rawUser.projects, rawUser.projectRights, rawUser.friendList, salt);
    }

    byte[] getSalt() {
        return salt;
    }
}
