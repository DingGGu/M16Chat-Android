package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
                case EID_CHANNEL: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    v.setBackgroundColor(0xfff4e4ce);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setTextColor(0xff4C2001);
                    mChatSingleRow.setText("채널 입장: "+bcm.message);
                    break;
                }
                case EID_JOIN: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    v.setBackgroundColor(0xffF0FEE6);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setTextColor(0xff53B84D);
                    mChatSingleRow.setText(ParseUsername.parseColor(bcm.username) + " 님이 입장하셨습니다.");
                    break;
                }
                case EID_LEAVE: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    v.setBackgroundColor(0xffF0FEE6);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setTextColor(0xff53B84D);
                    mChatSingleRow.setText(ParseUsername.parseColor(bcm.username) + " 님이 퇴장하셨습니다.");
                    break;
                }
                case EID_ERROR:{
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    v.setBackgroundColor(0xffFFEEE5);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setTextColor(0xffB23838);
                    mChatSingleRow.setText(bcm.message);
                    break;
                }
                case EID_INFO: {
                    v = vi.inflate(R.layout.custom_chat_single, null);
                    v.setBackgroundColor(0xffe6f7ff);
                    TextView mChatSingleRow = (TextView) v.findViewById(R.id.chat_single_row);
                    mChatSingleRow.setTextColor(0xff398DB2);
                    mChatSingleRow.setText(bcm.message);
                    break;
                }
                case EID_BROADCAST: {
                    v = vi.inflate(R.layout.custom_chat, null);
                    v.setBackgroundColor(0xff1D1D1D);
                    TextView mChatUserName = (TextView) v.findViewById(R.id.chat_username);
                    TextView mChatMessage = (TextView) v.findViewById(R.id.chat_message);
                    mChatUserName.setTextColor(0xffFFFFFF);
                    mChatUserName.setText(bcm.username);
                    mChatMessage.setTextColor(0xffFFFFFF);
                    mChatMessage.setText(bcm.message);
                    break;
                }
                case EID_WHISPERSENT: {
                    v = vi.inflate(R.layout.custom_chat, null);
                    v.setBackgroundColor(0xffFFE5F6);
                    TextView mChatUserName = (TextView) v.findViewById(R.id.chat_username);
                    TextView mChatMessage = (TextView) v.findViewById(R.id.chat_message);
                    mChatUserName.setTextColor(0xffFF34F1);
                    mChatUserName.setText("[보냄] "+bcm.username);
                    mChatMessage.setText(bcm.message);
                    break;
                }
                case EID_WHISPER: {
                    v = vi.inflate(R.layout.custom_chat, null);
                    v.setBackgroundColor(0xffFFE5F6);
                    TextView mChatUserName = (TextView) v.findViewById(R.id.chat_username);
                    TextView mChatMessage = (TextView) v.findViewById(R.id.chat_message);
                    mChatUserName.setTextColor(0xffFF34F1);
                    mChatUserName.setText(bcm.username);
                    mChatMessage.setText(bcm.message);
                    break;
                }
                default: {
                    v = vi.inflate(R.layout.custom_chat, null);
                    TextView mChatUserName = (TextView) v.findViewById(R.id.chat_username);
                    TextView mChatMessage = (TextView) v.findViewById(R.id.chat_message);
                    if (bcm.flags == 8)
                        mChatUserName.setTextColor(0xff8905d5);
                    else
                        mChatUserName.setTextColor(0xffe4b011);
                    mChatUserName.setText(bcm.username);
                    mChatMessage.setText(bcm.message);
                    break;
                }
            }
        }
        return v;
    }
}
