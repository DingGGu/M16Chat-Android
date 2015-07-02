package bnetp;

public interface BNetProtocolInterface {
    void receiveMessage(String message);

    void throwError(String s);
}
