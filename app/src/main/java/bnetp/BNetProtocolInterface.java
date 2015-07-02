package bnetp;

public interface BNetProtocolInterface {
    void receiveMessage(String message);
    void receiveMessage(BNetChatMessage obj);

    void throwError(String s);
}
