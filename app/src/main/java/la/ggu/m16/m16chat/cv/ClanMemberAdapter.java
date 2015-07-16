package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

import bnetp.clan.ClanMember;
import la.ggu.m16.m16chat.R;
import la.ggu.m16.m16chat.util.ParseUsername;

public class ClanMemberAdapter extends ArrayAdapter<ClanMember> {
    private CopyOnWriteArrayList<ClanMember> ClanMembers;
    private Context context;

    public ClanMemberAdapter(Context context, int textViewResourceId, CopyOnWriteArrayList<ClanMember> ClanMembers) {
        super(context, textViewResourceId, ClanMembers);
        this.context = context;
        this.ClanMembers = ClanMembers;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_channel_user, null);
        }
        ClanMember cm = ClanMembers.get(position);
        if (cm != null) {
            TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
            text1.setText(ParseUsername.parseColor(cm.username));
        }
        return v;
    }
}
