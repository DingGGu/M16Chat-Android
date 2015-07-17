package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

import bnetp.friend.FriendEntry;
import bnetp.friend.FriendLocationIDs;
import bnetp.friend.FriendStatusFlags;
import la.ggu.m16.m16chat.R;
import la.ggu.m16.m16chat.util.ParseUsername;

public class FriendAdapter extends ArrayAdapter<FriendEntry> {
    private CopyOnWriteArrayList<FriendEntry> Friends;
    private Context context;

    public FriendAdapter(Context context, int textViewResourceId, CopyOnWriteArrayList<FriendEntry> Friends) {
        super(context, textViewResourceId, Friends);
        this.context = context;
        this.Friends = Friends;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FriendEntry fe = Friends.get(position);
        if (fe != null) {
            switch (fe.location) {
                case FriendLocationIDs.FRIENDLOCATION_OFFLINE: {
                    v = vi.inflate(R.layout.custom_channel_user, null);
                    TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
                    text1.setText(fe.account);
                    break;
                }
                default: {
                    v = vi.inflate(R.layout.custom_channel_user_online, null);
                    TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
                    TextView text2 = (TextView) v.findViewById(R.id.channel_user_list_item_location);
                    String locationText = fe.locationName;
                    text1.setText(fe.account);

                    switch (fe.location) {
                        case FriendLocationIDs.FRIENDLOCATION_NOT_IN_CHAT: {
                            locationText = "접속 중";
                            break;
                        }
                        case FriendLocationIDs.FRIENDLOCATION_IN_CHAT: {
                            locationText = "채널 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case FriendLocationIDs.FRIENDLOCATION_IN_A_PUBLIC_GAME: {
                            locationText = "공개 게임 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case FriendLocationIDs.FRIENDLOCATION_IN_A_PRIVATE_GAME_MUTUAL: {
                            locationText = "비공개 게임 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case FriendLocationIDs.FRIENDLOCATION_IN_A_PRIVATE_GAME_NOT_MUTUAL: {
                            locationText = "비공개 게임 중";
                            break;
                        }
                    }
                    text2.setText(locationText);
                    break;
                }
            }

        }
        return v;
    }
}
