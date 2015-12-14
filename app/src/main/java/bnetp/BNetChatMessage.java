package bnetp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BNetChatMessage {
    public Date timestamp;
    public BNetChatEventId eid;
    public String username;
    public String message;
    public int flags;

    public BNetChatMessage(BNetChatEventId eid, String username) {
        this.timestamp = new Date();
        this.eid = eid;
        this.username = username;
    }

    public BNetChatMessage(BNetChatEventId eid, String username, String message) {
        this.timestamp = new Date();
        this.eid = eid;
        this.username = username;
        this.message = message;
    }

    public BNetChatMessage(BNetChatEventId eid, String username, String message, int flags) {
        this.timestamp = new Date();
        this.eid = eid;
        this.username = username;
        this.message = message;
        this.flags = flags;
    }

    public String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(timestamp);
    }
}
