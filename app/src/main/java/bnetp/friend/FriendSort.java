package bnetp.friend;

import java.util.Comparator;

public class FriendSort implements Comparator<FriendEntry> {
    @Override
    public int compare(FriendEntry arg0, FriendEntry arg1) {
        return arg0.location > arg1.location ? -1 : arg0.location < arg1.location ? 1: 0;
    }
}