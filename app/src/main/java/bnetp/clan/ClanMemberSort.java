package bnetp.clan;

import java.util.Comparator;

public class ClanMemberSort implements Comparator<ClanMember>{
    @Override
    public int compare(ClanMember arg0, ClanMember arg1) {
        return arg0.rank > arg1.rank ? -1 : arg0.rank < arg1.rank ? 1 : 0;
    }
}
