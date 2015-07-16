package bnetp.clan;

/**
 * Created by DingGGu on 2015-07-14.
 */
public class ClanMember {
    public String username;
    public byte rank;
    public byte online;
    public String location;

    public ClanMember(String username, byte rank, byte online, String location) {
        this.username = username;
        this.rank = rank;
        this.online = online;
        this.location = location;
    }
}
