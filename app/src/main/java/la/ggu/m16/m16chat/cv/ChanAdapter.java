package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import bnetp.BNetChannelUser;
import bnetp.BNetChatMessage;
import la.ggu.m16.m16chat.R;
import la.ggu.m16.m16chat.util.ParseUsername;

public class ChanAdapter extends ArrayAdapter<BNetChannelUser> {
    private CopyOnWriteArrayList<BNetChannelUser> ChanUsers;
    private Context context;

    public ChanAdapter(Context context, int textViewResourceId, CopyOnWriteArrayList<BNetChannelUser> ChanUsers) {
        super(context, textViewResourceId, ChanUsers);
        this.context = context;
        this.ChanUsers = ChanUsers;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_channel_user, null);
        }
        BNetChannelUser bcu = ChanUsers.get(position);
        if (bcu != null) {
            TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
            text1.setText(ParseUsername.parseColor(bcu.username));
            if (bcu.statstr.getClan() != null) {
                TextView text2 = (TextView) v.findViewById(R.id.channel_user_list_clan);
                text2.setTextColor(0xffe66100);
                text2.setText(bcu.statstr.getClan());
            }
        }
        return v;
    }
}
