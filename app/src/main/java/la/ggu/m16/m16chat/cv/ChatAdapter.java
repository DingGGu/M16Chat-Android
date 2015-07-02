package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import bnetp.BNetChatMessage;
import la.ggu.m16.m16chat.R;

public class ChatAdapter extends ArrayAdapter<BNetChatMessage> {
    private ArrayList<BNetChatMessage> ChatItems;
    private Context context;

    public ChatAdapter(Context context, int textViewResourceId, ArrayList<BNetChatMessage> ChatItems) {
        super(context, textViewResourceId, ChatItems);
        this.context = context;
        this.ChatItems = ChatItems;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_chat, null);
        }
        BNetChatMessage bcm = ChatItems.get(position);
        if (bcm != null) {
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            tt.setText(bcm.username);
            bt.setText(bcm.message);
        }
        return v;
    }
}
