package bnetp.clan;

public class ClanMemberStatusFlags {
    public static final byte CLANMEMBERSTATUS_OFFLINE                       = (byte)0x00;
    public static final byte CLANMEMBERSTATUS_NOT_IN_CHAT					= (byte)0x01;
    public static final byte CLANMEMBERSTATUS_IN_CHAT						= (byte)0x02;
    public static final byte CLANMEMBERSTATUS_IN_A_PUBLIC_GAME				= (byte)0x03;
    public static final byte CLANMEMBERSTATUS_IN_A_PRIVATE_GAME_NOT_MUTUAL	= (byte)0x04;
    public static final byte CLANMEMBERSTATUS_IN_A_PRIVATE_GAME_MUTUAL		= (byte)0x05;
}
