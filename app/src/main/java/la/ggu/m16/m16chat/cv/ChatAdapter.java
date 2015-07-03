package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import bnetp.BNetChatEventId;
import bnetp.BNetChatMessage;
import la.ggu.m16.m16chat.R;
import la.ggu.m16.m16chat.util.ParseUsername;

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
        BNetChatMessage bcm = ChatItems.get(position);
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (bcm != null) {
            switch (bcm.eid) {
                case EID_JOIN: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setText(ParseUsername.parseColor(bcm.username) + " 님이 입장하셨습니다.");
                    break;
                }
                case EID_LEAVE: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setText(ParseUsername.parseColor(bcm.username) + " 님이 퇴장하셨습니다.");
                    break;
                }
                case EID_ERROR:
                case EID_INFO: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setText(bcm.message);
                    break;
                }
                default: {
                    v = vi.inflate(R.layout.custom_chat, null);
                    TextView mChatUserName = (TextView) v.findViewById(R.id.chat_username);
                    TextView mChatMessage = (TextView) v.findViewById(R.id.chat_message);
                    mChatUserName.setText(bcm.username);
                    mChatMessage.setText(bcm.message);
                    break;
                }
            }
        }
        return v;
    }
}
