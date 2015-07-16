package bnetp;

import bnetp.clan.ClanMember;
import bnetp.friend.FriendEntry;

public interface BNetProtocolInterface {
    void startChat();
    void initUserInfo(String uniqueUserName);

    void receiveMessage(BNetChatMessage obj);

    void throwError(String s);

    void addChannelUser(BNetChannelUser mBNetChannelUser);
    void delChannelUser(BNetChannelUser mBNetChannelUser);
    void clearChannelUser(String channel);

    void dispatchFriendList(FriendEntry[] entries);

    void dispatchClanMembers(ClanMember[] members);
}
