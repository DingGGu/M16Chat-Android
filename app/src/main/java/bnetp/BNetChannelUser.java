package bnetp;

import bnetp.util.StatString;

public class BNetChannelUser {
    public BNetChatEventId eid;
    public String username;
    public StatString statstr;

    public BNetChannelUser(BNetChatEventId eid, String username, StatString statstr) {
        this.eid = eid;
        this.username = username;
        this.statstr = statstr;
    }
}
