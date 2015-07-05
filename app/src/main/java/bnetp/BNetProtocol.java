package bnetp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.TimeZone;

import bnetp.Hash.*;
import bnetp.util.ByteArray;
import bnetp.util.StatString;

public class BNetProtocol extends Thread implements Runnable {

    private String username, password;

    protected Socket socket = null;
    private BNetInputStream BNInputStream = null;
    private DataOutputStream BNetOutputStream = null;

    private BNetChannelUser mBNetChannelUser = null;
    private BNetChatMessage mBNetChatMessage = null;

    private int serverToken = 0;
    private final int clientToken = Math.abs(new Random().nextInt());

    private BNetProtocolInterface mBNetProtocolInterface = null;

    private Integer nlsRevision = null;

    private SRP srp = null;

    private String uniqueUserName;

    protected Socket makeSocket(String address, int port) throws UnknownHostException, IOException {
        Socket s;
        InetAddress addr = InetAddress.getByName(address);
        s = new Socket(addr, port);
        s.setKeepAlive(true);
        return s;
    }

    public void setBnetProtocolInterface(BNetProtocolInterface bnetProtocolInterface) {
        this.mBNetProtocolInterface = bnetProtocolInterface;
    }

    public BNetProtocol(String username, String password) {
        this.username = username;
        this.password = password;
        if(mBNetProtocolInterface != null) {
            this.mBNetProtocolInterface.startChat();
        }
    }

    public void BNetConnect() throws Exception {
        socket = makeSocket("119.194.195.251", 5004);
        BNInputStream = new BNetInputStream(socket.getInputStream());
        BNetOutputStream = new DataOutputStream(socket.getOutputStream());

        socket.setSoTimeout(1000);

        BNetOutputStream.writeByte(0x01);

        System.out.println("Connect to Server");

        BNetAuthInfo();
    }

    public void BNetAuthInfo() throws IOException {
        BNetProtocolPacket p;
        int tzBias = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / -60000;

        p = new BNetProtocolPacket(BNetProtocolPacketId.SID_AUTH_INFO);
        p.writeDWord(0);
        p.writeDWord(0x49583836); // Platform IX86
        p.writeDWord(0x44534852); // Warcraft III
        p.writeDWord(0x00000000); // Version byte
        p.writeDWord("koKR");
        p.writeDWord(0); // Local IP
        p.writeDWord(tzBias); // TZ bias
        p.writeDWord(0x412); // Locale ID
        p.writeDWord(0x412); // Language ID
        p.writeNTString("KOR"); // Country abreviation
        p.writeNTString("Korea"); // Country
        p.sendPacket(BNetOutputStream);
    }

    private BNetPacketReader obtainPacket() throws IOException {
        byte magic;
        do {
            magic = BNInputStream.readByte();
        } while (magic != (byte) 0xFF);
        try {
            return new BNetPacketReader(BNInputStream);
        } catch (SocketTimeoutException e) {
            throw new IOException("Unexpected socket timeout while reading packet", e);
        }
    }

    public void BNetLogin() throws Exception {
        while (!socket.isClosed()) {
            BNetPacketReader pr;
            try {
                pr = obtainPacket();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (SocketException e) {
                if (socket == null) break;
                if (socket.isClosed()) break;
                throw e;
            }

            BNetInputStream is = pr.getData();
            switch (pr.packetId) {
                case SID_OPTIONALWORK:
                case SID_EXTRAWORK:
                case SID_REQUIREDWORK:
                    break;
                case SID_NULL: {
                    BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_NULL);
                    p.sendPacket(BNetOutputStream);
                    break;
                }

                case SID_PING: {
                    System.out.println("PONG!");
                    BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_PING);
                    p.writeDWord(is.readDWord());
                    p.sendPacket(BNetOutputStream);
                    break;
                }

                case SID_AUTH_INFO: {
                    if (pr.packetId == BNetProtocolPacketId.SID_AUTH_INFO) {
                        nlsRevision = is.readDWord();
                        serverToken = is.readDWord();
                        is.skip(4); // int udpValue = is.readDWord();
                    }
                    assert (is.available() == 0);

                    BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_AUTH_CHECK);
                    p.writeDWord(clientToken);  // Client Token
                    p.writeDWord(0);   // EXE Version
                    p.writeDWord(0);   // EXE Hash
                    p.writeDWord(1);            // Number of CD-Keys
                    p.writeDWord(0);            // Spawn CD-Key
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeDWord(0x00000000);
                    p.writeNTString("war3.exe 03/18/11 20:03:55 471040");
                    p.writeNTString("Chat");
                    p.sendPacket(BNetOutputStream);
                    break;
                }

                case SID_AUTH_CHECK: {
                    int result = is.readDWord();
                    String extraInfo = is.readNTString();
                    assert (is.available() == 0);

                    if (pr.packetId == BNetProtocolPacketId.SID_AUTH_CHECK) {
                        if (result != 0) {
                            switch (result) {
                                case 0x211:
                                    System.out.println("BANNED!" + extraInfo);
                                    break;
                                default:
                                    System.out.println("Unknown Error" + Integer.toHexString(result));
                                    break;
                            }
                            disconnect();
                            System.out.println("Disconnect!" + extraInfo);
                            break;
                        }
                        System.out.println("Passed Check Revision");
                    } else {
                        if (result != 2) {
                            disconnect();
                            System.out.println("Failed Version Check!");
                            break;
                        }
                        System.out.println("Passed Check Revision");
                    }
                    sendAuth();
                    break;
                }

                case SID_LOGONRESPONSE2: {
                    int result = is.readDWord();
                    switch (result) {
                        case 0x00: // Success
                            System.out.println("Login successful; entering chat.");
                            sendEnterChat();
                            sendJoinChannelFirst();
                            break;
                        case 0x01:
                            disconnect();
                            if(mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("올바르지 않은 아이디에요.");
                            }
                            break;
                        case 0x02:
                            disconnect();
                            if(mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("비밀번호가 틀렸어요.");
                            }
                            break;
                        case 0x06:
                            disconnect();
                            if(mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("아이디가 잠겨있어요.");
                            }
                            break;
                        default:
                            disconnect();
                            if(mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("알 수 없는 오류.");
                            }
                            break;
                    }
                    break;
                }

                case SID_ENTERCHAT: {
                    if(mBNetProtocolInterface != null) {
                        uniqueUserName = is.readNTString();
                        this.mBNetProtocolInterface.initUserInfo(uniqueUserName);
                    }
                    BNetChat();
                }
            }
        }
    }

    public void sendAuth() throws Exception {
        String username = this.username;
        String password = this.password;
        int passwordHash[] = DoubleHash.doubleHash(password.toLowerCase(), clientToken, serverToken);

        BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_LOGONRESPONSE2);
        p.writeDWord(clientToken);
        p.writeDWord(serverToken);
        p.writeDWord(passwordHash[0]);
        p.writeDWord(passwordHash[1]);
        p.writeDWord(passwordHash[2]);
        p.writeDWord(passwordHash[3]);
        p.writeDWord(passwordHash[4]);
        p.writeNTString(username);
        p.sendPacket(BNetOutputStream);
    }

    private void sendEnterChat() throws Exception {
        BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_ENTERCHAT);
        p.writeNTString("");
        p.writeNTString("");
        p.sendPacket(BNetOutputStream);
    }

    public void sendJoinChannelFirst() throws Exception {
        BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_JOINCHANNEL);
        p.writeDWord(1);
        p.writeNTString("Android");
        p.sendPacket(BNetOutputStream);
    }

    private void BNetChat() throws Exception {
        while (!socket.isClosed()) {
            BNetPacketReader pr;
            try {
                pr = obtainPacket();
            } catch (SocketTimeoutException e) {
                continue;
            } catch (SocketException e) {
                if (socket == null) break;
                if (socket.isClosed()) break;
                throw e;
            }

            BNetInputStream is = pr.getData();
            switch (pr.packetId) {
                case SID_CHATEVENT: {
                    BNetChatEventId eid = BNetChatEventId.values()[is.readDWord()];

                    int flags = is.readDWord();
                    int ping = is.readDWord();
                    is.skip(12);
                    String username = is.readNTString();

                    ByteArray data = null;
                    StatString statstr = null;

                    switch (eid) {
                        case EID_CHANNEL:{
                            if(mBNetProtocolInterface != null) {
                                String channel = is.readNTString();
                                this.mBNetProtocolInterface.clearChannelUser(channel);
                            }
                            break;
                        }
                        case EID_SHOWUSER:{
                            if(mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username);
                                this.mBNetProtocolInterface.addChannelUser(mBNetChannelUser);
                            }
                            break;
                        }

                        case EID_ERROR:
                        case EID_INFO: {
                            String message = is.readNTString();
                            if(mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_BROADCAST: {
                            String message = is.readNTString();
                            if(mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_WHISPER:
                        case EID_WHISPERSENT:
                        case EID_TALK: {
                            String message = is.readNTString();
                            if(mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_JOIN: {
                            if(mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username);
                                this.mBNetProtocolInterface.addChannelUser(mBNetChannelUser);
                                mBNetChatMessage = new BNetChatMessage(eid, username);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_LEAVE: {
                            if(mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username);
                                this.mBNetProtocolInterface.delChannelUser(mBNetChannelUser);
                                mBNetChatMessage = new BNetChatMessage(eid, username);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        default:
                            Log.e("EID", String.valueOf(eid));
                            break;

                    }
                }
            }
        }
    }

    public void sendChatCommand (String data) {
        try {
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CHATCOMMAND);
            p.writeNTString(data);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername () {
        return this.uniqueUserName;
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            InterruptThread();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                BNetConnect();
                BNetLogin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void InterruptThread() {
        this.interrupt();
    }
}
