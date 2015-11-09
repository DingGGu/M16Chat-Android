package bnetp;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.TimeZone;

import bnetp.Hash.*;
import bnetp.clan.ClanMember;
import bnetp.friend.FriendEntry;
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

    private boolean KEEP_THREAD = true;

    protected Socket makeSocket(String address, int port) throws UnknownHostException, IOException {
        Socket s;
        s = new Socket(address, port);
        s.setKeepAlive(true);
        return s;
    }

    public void setBnetProtocolInterface(BNetProtocolInterface bnetProtocolInterface) {
        this.mBNetProtocolInterface = bnetProtocolInterface;
    }

    public BNetProtocol(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void BNetConnect() throws Exception {
        if (mBNetProtocolInterface != null) {
            this.mBNetProtocolInterface.startChat();
        }
        socket = makeSocket("m16-chat.ggu.la", 6112);
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
                    assert is.available() == 0;

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
                    assert is.available() == 0;

                    if (pr.packetId == BNetProtocolPacketId.SID_AUTH_CHECK) {
                        if (result != 0) {
                            switch (result) {
                                case 0x201:
                                    if (mBNetProtocolInterface != null) {
                                        this.mBNetProtocolInterface.throwError("서버에서 밴 되었어요." + extraInfo);
                                    }
                                    break;
                                default:
                                    if (mBNetProtocolInterface != null) {
                                        this.mBNetProtocolInterface.throwError("알 수 없는 오류: " + Integer.toHexString(result));
                                    }
                                    break;
                            }
                            disconnect();
                            System.out.println("Disconnect!" + extraInfo);
                            break;
                        }
                    } else {
                        if (result != 2) {
                            disconnect();
                            if (mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("올바르지 않은 접근이에요." + extraInfo);
                            }
                            break;
                        }
                    }
                    System.out.println("Passed Check Revision");
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
                            if (mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("올바르지 않은 아이디에요.");
                            }
                            break;
                        case 0x02:
                            disconnect();
                            if (mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("비밀번호가 틀렸어요.");
                            }
                            break;
                        case 0x06:
                            disconnect();
                            if (mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("아이디가 잠겨있어요.");
                            }
                            break;
                        default:
                            disconnect();
                            if (mBNetProtocolInterface != null) {
                                this.mBNetProtocolInterface.throwError("알 수 없는 오류.");
                            }
                            break;
                    }
                    break;
                }

                case SID_ENTERCHAT: {
                    if (mBNetProtocolInterface != null) {
                        uniqueUserName = is.readNTString();
                        this.mBNetProtocolInterface.initUserInfo(uniqueUserName);
                    }
                    BNetChat();
                    break;
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
                        case EID_CHANNEL: {
                            if (mBNetProtocolInterface != null) {
                                String channel = is.readNTString();
                                this.mBNetProtocolInterface.clearChannelUser(channel);
                            }
                            break;
                        }
                        case EID_SHOWUSER: {
                            statstr = is.readStatString();
                            if (mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username, statstr);
                                this.mBNetProtocolInterface.addChannelUser(mBNetChannelUser);
                            }
                            break;
                        }

                        case EID_ERROR:
                        case EID_INFO: {
                            String message = is.readNTString();
                            if (mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_BROADCAST: {
                            String message = is.readNTString();
                            if (mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_WHISPER:
                        case EID_WHISPERSENT:
                        case EID_TALK:
                        case EID_EMOTE: {
                            String message = is.readNTString();
                            if (mBNetProtocolInterface != null) {
                                mBNetChatMessage = new BNetChatMessage(eid, username, message, flags);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_JOIN: {
                            statstr = is.readStatString();
                            if (mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username, statstr);
                                this.mBNetProtocolInterface.addChannelUser(mBNetChannelUser);
                                mBNetChatMessage = new BNetChatMessage(eid, username);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                        case EID_LEAVE: {
                            if (mBNetProtocolInterface != null) {
                                mBNetChannelUser = new BNetChannelUser(eid, username, statstr);
                                this.mBNetProtocolInterface.delChannelUser(mBNetChannelUser);
                                mBNetChatMessage = new BNetChatMessage(eid, username);
                                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                            }
                            break;
                        }
                    }
                    break;
                }
                case SID_FRIENDSLIST: {
                    byte numEntries = is.readByte();
                    FriendEntry[] entries = new FriendEntry[numEntries];

                    for (int i = 0; i < numEntries; i++) {
                        String uAccount = is.readNTString();
                        byte uStatus = is.readByte();
                        byte uLocation = is.readByte();
                        int uProduct = is.readDWord();
                        String uLocationName = is.readNTStringUTF8();

                        entries[i] = new FriendEntry(uAccount, uStatus, uLocation, uProduct, uLocationName);
                    }

                    if (mBNetProtocolInterface != null) {
                        this.mBNetProtocolInterface.dispatchFriendList(entries);
                    }
                    break;
                }

                case SID_CLANMEMBERLIST: {
                    is.readDWord();
                    byte numMembers = is.readByte();
                    ClanMember[] members = new ClanMember[numMembers];

                    for (int i = 0; i < numMembers; i++) {
                        String uName = is.readNTString();
                        byte uRank = is.readByte();
                        byte uOnline = is.readByte();
                        String uLocation = is.readNTStringUTF8();

                        members[i] = new ClanMember(uName, uRank, uOnline, uLocation);
                    }
                    if (mBNetProtocolInterface != null) {
                        this.mBNetProtocolInterface.dispatchClanMembers(members);
                    }
                    break;
                }

                case SID_CLANINVITATIONRESPONSE: {
                    int cookie = is.readDWord();
                    int clanTag = is.readDWord();
                    String clanName = is.readNTString();
                    String inviter = is.readNTString();

                    if (mBNetProtocolInterface != null) {
                        this.mBNetProtocolInterface.receiveClanInvitation(cookie, clanTag, clanName, inviter);
                    }
                    break;
                }

                case SID_CLANINFO: {
                    is.readByte();
                    int clanTag = is.readDWord();
                    int clanRank = is.readByte();

                    if (mBNetProtocolInterface != null) {
                        this.mBNetProtocolInterface.setClanRank(clanRank);
                    }
                    break;
                }

                case SID_CLANMEMBERSTATUSCHANGE: {
                    String username = is.readNTString();
                    int rank = is.readByte();
                    int status = is.readByte();
                    String location = is.readNTString();

                    if (uniqueUserName.toLowerCase().equals(username.toLowerCase())) {
                        if (mBNetProtocolInterface != null) {
                            this.mBNetProtocolInterface.setClanRank(rank);
                        }
                    }
                    break;
                }

                case SID_CLANREMOVEMEMBER: {
                    int cookie = is.readDWord();
                    int status = is.readByte();

                    if (status == 0) {
                        if (mBNetProtocolInterface != null) {
                            mBNetChatMessage = new BNetChatMessage(BNetChatEventId.EID_INFO, null, "성공적으로 클랜에서 추방시켰어요.");
                            this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
                        }
                    }
                }
            }
        }
    }

    public void sendFriendsList() {
        try {
            if (socket == null || socket.isClosed()) {
                if (mBNetProtocolInterface != null) {
                    this.mBNetProtocolInterface.throwError("서버와 연결이 되지않았어요.");
                    return;
                }
            }
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_FRIENDSLIST);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClanMemberList() {
        try {
            if (socket == null || socket.isClosed()) {
                if (mBNetProtocolInterface != null) {
                    this.mBNetProtocolInterface.throwError("서버와 연결이 되지않았어요.");
                    return;
                }
            }
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CLANMEMBERLIST);
            p.writeDWord(1);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendChatCommand(String data) {
        try {
            if (socket == null || socket.isClosed()) {
                if (mBNetProtocolInterface != null) {
                    this.mBNetProtocolInterface.throwError("서버와 연결이 되지않았어요.");
                    return;
                }
            }
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CHATCOMMAND);
            p.writeNTString(data);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResponseClanInvitation(int cookie, int clanTag, String inviter, int response) {
        try {
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CLANINVITATIONRESPONSE);
            p.writeDWord(cookie);
            p.writeDWord(clanTag);
            p.writeNTString(inviter);
            p.writeByte(response);
            p.sendPacket(BNetOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClanInvitation(String itemUserName) {
        try {
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CLANINVITATION);
            p.writeDWord(1);
            p.writeNTString(itemUserName);
            p.sendPacket(BNetOutputStream);
            if (mBNetProtocolInterface != null) {
                mBNetChatMessage = new BNetChatMessage(BNetChatEventId.EID_INFO, null, itemUserName + "님을 클랜에 초대했어요.");
                this.mBNetProtocolInterface.receiveMessage(mBNetChatMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClanRemoveMember(String itemUserName) {
        try {
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CLANREMOVEMEMBER);
            p.writeDWord(1);
            p.writeNTString(itemUserName);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendClanRankChange(String itemUserName, int newRank) {
        try {
            BNetProtocolPacket p = new BNetProtocolPacket(BNetProtocolPacketId.SID_CLANRANKCHANGE);
            p.writeDWord(1);
            p.writeNTString(itemUserName);
            p.writeByte(newRank);
            p.sendPacket(BNetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
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
        while (!Thread.currentThread().isInterrupted() && KEEP_THREAD) {
            try {
                BNetConnect();
                BNetLogin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void InterruptThread() {
        KEEP_THREAD = false;
    }
}
