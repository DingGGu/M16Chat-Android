package bnetp;

public class BNetChatMessage {
    public BNetChatEventId eid;
    public String username;
    public String message;
    public int flags;

    public BNetChatMessage(BNetChatEventId eid, String username) {
        this.eid = eid;
        this.username = username;
    }

    public BNetChatMessage(BNetChatEventId eid, String username, String message) {
        this.eid = eid;
        this.username = username;
        this.message = message;
    }

    public BNetChatMessage(BNetChatEventId eid, String username, String message, int flags) {
        this.eid = eid;
        this.username = username;
        this.message = message;
        this.flags = flags;
    }
}
