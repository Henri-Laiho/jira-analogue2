package server;

import common.User;
import data.RawUser;


public class ServerUser extends User {
    private byte[] salt;

    ServerUser(RawServerUser rawUser) {
        super(rawUser);
        this.salt = rawUser.salt;
    }

    @Override
    public RawServerUser toRawUser() {
        RawUser rawUser = super.toRawUser();
        return new RawServerUser(rawUser.userId, rawUser.username, rawUser.passwordHash, rawUser.userEmail, rawUser.lastOnlineMS, rawUser.projects, rawUser.projectRights, rawUser.friendList, salt);
    }

    byte[] getSalt() {
        return salt;
    }
}
