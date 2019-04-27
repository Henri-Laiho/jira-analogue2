package server;

import data.RawUser;

public class RawServerUser extends RawUser {
    byte[] salt;

    public RawServerUser(long userId, String username, byte[] passwordHash, String userEmail, Long lastOnlineMS, long[] projects, int[] projectRights, long[] friendList, byte[] salt) {
        super(userId, username, passwordHash, userEmail, lastOnlineMS, projects, projectRights, friendList);
        this.salt = salt;
    }
}
