package bnetp;

public interface BNetProtocolInterface {
    void receiveMessage(BNetChatMessage obj);

    void throwError(String s);

    void addChannelUser(BNetChannelUser mBNetChannelUser);
    void delChannelUser(BNetChannelUser mBNetChannelUser);
    void clearChannelUser(String channel);
}
