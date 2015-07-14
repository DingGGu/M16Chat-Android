package bnetp.clan;

/**
 * Created by DingGGu on 2015-07-14.
 */
public class ClanMember {
    String username;
    byte rank;
    byte online;
    String location;

    public ClanMember(String username, byte rank, byte online, String location) {
        this.username = username;
        this.rank = rank;
        this.online = online;
        this.location = location;
    }
}
