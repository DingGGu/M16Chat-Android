package bnetp;

public class BNetChannelUser {
    public BNetChatEventId eid;
    public String username;

    public BNetChannelUser(BNetChatEventId eid, String username) {
        this.eid = eid;
        this.username = username;
    }
}
