package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

import bnetp.friend.FriendEntry;
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
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_channel_user, null);
        }
        FriendEntry fe = Friends.get(position);
        if (fe != null) {
            TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
            text1.setText(ParseUsername.parseColor(fe.account));
        }
        return v;
    }
}
