package bnetp.friend;

public class FriendEntry {
    public String account = null;
    public byte status;
    public byte location;
    public int product;
    public String locationName;

    public FriendEntry(String account, byte status, byte location, int product, String locationName) {
        this.account = account;
        this.status = status;
        this.location = location;
        this.product = product;
        this.locationName = locationName;
    }
}
