package la.ggu.m16.m16chat.cv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

import bnetp.clan.ClanMember;
import bnetp.clan.ClanMemberRankIDs;
import bnetp.clan.ClanMemberStatusFlags;
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
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ClanMember cm = ClanMembers.get(position);
        if (cm != null) {
            switch(cm.online) {
                case ClanMemberStatusFlags.CLANMEMBERSTATUS_OFFLINE: {
                    v = vi.inflate(R.layout.custom_channel_user, null);
                    TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);

                    switch(cm.rank) {
                        case ClanMemberRankIDs.CLANMEMBERRANK_CHIEFTAIN: {
                            text1.setTextColor(0xFF0043F5);
                            break;
                        }
                        case ClanMemberRankIDs.CLANMEMBERRANK_SHAMAN: {
                            text1.setTextColor(0xFFBB110F);
                            break;
                        }
                        case ClanMemberRankIDs.CLANMEMBERRANK_GRUNT: {
                            text1.setTextColor(0xFF097e00);
                            break;
                        }
                    }
                    text1.setText(ParseUsername.parseColor(cm.username));
                    break;
                }
                default: {
                    v = vi.inflate(R.layout.custom_channel_user_online, null);
                    TextView text1 = (TextView) v.findViewById(R.id.channel_user_list_item);
                    TextView text2 = (TextView) v.findViewById(R.id.channel_user_list_item_location);
                    String locationText = cm.location;

                    switch(cm.rank) {
                        case ClanMemberRankIDs.CLANMEMBERRANK_CHIEFTAIN: {
                            text1.setTextColor(0xFF0043F5);
                            break;
                        }
                        case ClanMemberRankIDs.CLANMEMBERRANK_SHAMAN: {
                            text1.setTextColor(0xFFBB110F);
                            break;
                        }
                        case ClanMemberRankIDs.CLANMEMBERRANK_GRUNT: {
                            text1.setTextColor(0xFF097e00);
                            break;
                        }
                    }
                    text1.setText(cm.username);

                    switch (cm.online) {
                        case ClanMemberStatusFlags.CLANMEMBERSTATUS_NOT_IN_CHAT: {
                            locationText = "접속 중";
                            break;
                        }
                        case ClanMemberStatusFlags.CLANMEMBERSTATUS_IN_CHAT: {
                            locationText = "채널 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case ClanMemberStatusFlags.CLANMEMBERSTATUS_IN_A_PUBLIC_GAME: {
                            locationText = "공개 게임 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case ClanMemberStatusFlags.CLANMEMBERSTATUS_IN_A_PRIVATE_GAME_MUTUAL: {
                            locationText = "비공개 게임 (" + locationText + ")에 참여 중";
                            break;
                        }
                        case ClanMemberStatusFlags.CLANMEMBERSTATUS_IN_A_PRIVATE_GAME_NOT_MUTUAL: {
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
