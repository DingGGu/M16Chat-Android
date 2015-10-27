package bnetp.clan;

/**
 * Created by DingGGu on 2015. 10. 27..
 */
public class ClanInvitationResponse {
    public final int cookie;
    public final int clanTag;
    public String clanName;
    public String inviter;

    public ClanInvitationResponse(int cookie, int clanTag, String clanName, String inviter) {
        this.cookie = cookie;
        this.clanTag = clanTag;
        this.clanName = clanName;
        this.inviter = inviter;
    }
}
