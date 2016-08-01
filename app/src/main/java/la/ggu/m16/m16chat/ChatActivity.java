package la.ggu.m16.m16chat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import bnetp.*;
import bnetp.clan.ClanInvitationResponse;
import bnetp.clan.ClanMember;
import bnetp.clan.ClanMemberSort;
import bnetp.friend.FriendEntry;
import bnetp.friend.FriendSort;
import bnetp.util.HexDump;
import la.ggu.m16.m16chat.cv.ChanAdapter;
import la.ggu.m16.m16chat.cv.ChatAdapter;
import la.ggu.m16.m16chat.cv.FriendAdapter;
import la.ggu.m16.m16chat.cv.ClanMemberAdapter;
import la.ggu.m16.m16chat.util.ParseUsername;
import la.ggu.m16.m16chat.util.PreferencesControl;

public class ChatActivity extends ActionBarActivity implements View.OnClickListener {

    private DrawerLayout chat_activity;
    private RelativeLayout chat_drawer;
    private ListView channel_user_list;
    private CopyOnWriteArrayList<BNetChannelUser> ChanUsers = new CopyOnWriteArrayList<>();
    private ChanAdapter ChanAdapter;
    private ListView friend_user_list;
    private CopyOnWriteArrayList<FriendEntry> Friends = new CopyOnWriteArrayList<>();
    private FriendAdapter FriendAdapter;
    private ListView clan_user_list;
    private CopyOnWriteArrayList<ClanMember> ClanMembers = new CopyOnWriteArrayList<>();
    private ClanMemberAdapter ClanMemberAdapter;

    private ListView chat_view;
    private ArrayList<BNetChatMessage> ChatItems = new ArrayList<BNetChatMessage>();
    private ChatAdapter ChatAdapter;

    private FrameLayout accept_box_wrap;
    private TextView s_request_text;
    private Button s_request_accept_btn;
    private Button s_request_decline_btn;
    private ClanInvitationResponse CLAN_INVITATION_RES = null;

    private Spinner chat_spinner;
    private EditText chat_edittext;
    private Button chat_submit;

    private LinearLayout chat_menu_channel;
    private LinearLayout chat_menu_alarm;
    private LinearLayout chat_menu_clear;

    private static int BACK_PRESSED_NUM;
    private int ChannelUsersNum;
    private String ChannelName;
    private String uniqueUserName;
    private int ClanRank = 0;
    private int THREAD_COUNT;

    private Thread ChatThread = null;
    private BNetProtocol BNetProtocol = null;

    private Handler mChatHandler = new Handler();
    private Handler mChannelUserHandler = new Handler();
    private Handler mFriendEntryHandler = new Handler();
    private Handler mClanMemberHandler = new Handler();
    private Handler BackPressedHandler = new Handler();
    private Handler sRequestLayoutHandler = new sRequestLayoutHandler(this);
    private Handler errorHandler;

    private NotificationManager mNotificationManager;
    private int mNotificationNumber;
    private Button chat_menu_tab_channel;
    private Button chat_menu_tab_friend;
    private Button chat_menu_tab_clan;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        assert getSupportActionBar() != null;
        getSupportActionBar().setElevation(0);

        App application = (App) getApplication();
        mTracker = application.getDefaultTracker();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        chat_activity = (DrawerLayout) findViewById(R.id.chat_activity);
        chat_drawer = (RelativeLayout) findViewById(R.id.chat_drawer);

        channel_user_list = (ListView) findViewById(R.id.channel_user_list);
        ChanAdapter = new ChanAdapter(this, R.id.channel_user_list_item, ChanUsers);
        channel_user_list.setAdapter(ChanAdapter);
        channel_user_list.setOnItemClickListener(new ChanUserClickListener());

        friend_user_list = (ListView) findViewById(R.id.friend_user_list);
        FriendAdapter = new FriendAdapter(this, R.id.channel_user_list_item, Friends);
        friend_user_list.setAdapter(FriendAdapter);
        friend_user_list.setOnItemClickListener(new FriendUserClickListener());

        clan_user_list = (ListView) findViewById(R.id.clan_user_list);
        ClanMemberAdapter = new ClanMemberAdapter(this, R.id.channel_user_list_item, ClanMembers);
        clan_user_list.setAdapter(ClanMemberAdapter);
        clan_user_list.setOnItemClickListener(new ClanUserClickListener());

        chat_menu_channel = (LinearLayout) findViewById(R.id.chat_menu_channel);
        chat_menu_channel.setOnClickListener(this);
        chat_menu_alarm = (LinearLayout) findViewById(R.id.chat_menu_alarm);
        chat_menu_alarm.setOnClickListener(this);
        chat_menu_clear = (LinearLayout) findViewById(R.id.chat_menu_clear);
        chat_menu_clear.setOnClickListener(this);

        chat_menu_tab_channel = (Button) findViewById(R.id.chat_menu_tab_channel);
        chat_menu_tab_channel.setOnClickListener(this);
        chat_menu_tab_friend = (Button) findViewById(R.id.chat_menu_tab_friend);
        chat_menu_tab_friend.setOnClickListener(this);
        chat_menu_tab_clan = (Button) findViewById(R.id.chat_menu_tab_clan);
        chat_menu_tab_clan.setOnClickListener(this);

        chat_view = (ListView) findViewById(R.id.chat_view);
        ChatAdapter = new ChatAdapter(this, R.layout.custom_chat, ChatItems);
        chat_view.setAdapter(ChatAdapter);
        chat_view.setOnItemClickListener(new ChatItemClickListener());

        accept_box_wrap = (FrameLayout) findViewById(R.id.accept_box_wrap);
        s_request_text = (TextView) findViewById(R.id.s_request_text);
        s_request_accept_btn = (Button) findViewById(R.id.s_request_accept_btn);
        s_request_accept_btn.setOnClickListener(this);
        s_request_decline_btn = (Button) findViewById(R.id.s_request_decline_btn);
        s_request_decline_btn.setOnClickListener(this);

        chat_spinner = (Spinner) findViewById(R.id.chat_spinner);
        chat_spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.chat_spinner,
                getResources().getStringArray(R.array.CHAT_SPINNER_ITEMS)));
        chat_spinner.setSelection(2);

        chat_edittext = (EditText) findViewById(R.id.chat_edittext);
        chat_submit = (Button) findViewById(R.id.chat_submit);
        chat_submit.setOnClickListener(this);

        chat_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    chatSendMessage();
                    handled = true;
                }
                return handled;
            }
        });

        THREAD_COUNT = 0;

        Intent intent = getIntent();
        String login_username = intent.getStringExtra("login_username");
        String login_password = intent.getStringExtra("login_password");
        BNetProtocol = new BNetProtocol(login_username, login_password);
        BNetProtocol.setBnetProtocolInterface(new BNetProtocolInterface() {
            @Override
            public void startChat() {
                if (THREAD_COUNT > 2) {
                    this.throwError("잠시 후 다시 시도해주세요.\n (서버 문제일 수도 있으니 홈페이지를 확인하세요)");
                }
                THREAD_COUNT = THREAD_COUNT + 1;
            }

            @Override
            public void initUserInfo(final String un) {
                uniqueUserName = un;
            }

            @Override
            public void addChannelUser(final BNetChannelUser obj) {
                mChannelUserHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ChanUsers != null && ChanAdapter != null) {
                            ChannelUsersNum++;
                            setTitle();
                            ChanUsers.add(obj);
                            ChanAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void delChannelUser(final BNetChannelUser obj) {
                mChannelUserHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ChanUsers != null && ChanAdapter != null) {
                            ChannelUsersNum--;
                            setTitle();
                            ChanUsers.remove(obj);
                            ChanAdapter.notifyDataSetChanged();
                            Iterator<BNetChannelUser> it = ChanUsers.iterator();
                            while (it.hasNext()) {
                                BNetChannelUser cu = it.next();
                                if (cu.username.equals(obj.username)) {
                                    ChanUsers.remove(cu);
                                    ChanAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void clearChannelUser(final String channel) {
                mChannelUserHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ChanUsers != null && ChanAdapter != null) {
                            ChannelName = channel;
                            ChannelUsersNum = 0;
                            setTitle();
                            ChanUsers.clear();
                            ChanAdapter.notifyDataSetChanged();

                            BNetChatMessage mBNetChatMessage = new BNetChatMessage(BNetChatEventId.EID_CHANNEL, null, ChannelName);
                            ChatItems.add(mBNetChatMessage);
                            ChatAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void receiveMessage(final BNetChatMessage obj) {
                mChatHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ChatItems != null && ChatAdapter != null) {
                            switch (obj.eid) {
                                case EID_TALK:
                                case EID_WHISPER:
                                case EID_EMOTE: {
                                    String ALARM_TOGGLE = PreferencesControl.getInstance(ChatActivity.this).get(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, null);
                                    Uri ALARM_SOUND = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.alarm);
                                    long[] ALARM_VIBRATE = {0, 100};
                                    if (ALARM_TOGGLE.equals(PreferencesControl.ALARM_OFF))
                                        break;
                                    if (obj.message.toLowerCase().contains(uniqueUserName.toLowerCase())) { //알림
                                        PendingIntent pendingintent = PendingIntent.getActivity(ChatActivity.this, 0, new Intent(ChatActivity.this, ChatActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                                            NotificationCompat.Builder mBuilder =
                                                    new NotificationCompat.Builder(ChatActivity.this)
                                                            .setSmallIcon(R.drawable.ic_m16_chat)
                                                            .setTicker(obj.username + " 님이 언급했어요.")
                                                            .setContentTitle(obj.username + " 님이 언급했어요.")
                                                            .setContentText(obj.message)
                                                            .setAutoCancel(true)
                                                            .setDefaults(Notification.DEFAULT_LIGHTS)
                                                            .setVibrate(ALARM_VIBRATE)
                                                            .setSound(ALARM_SOUND)
                                                            .setContentIntent(pendingintent);
                                            mNotificationManager.notify(0, mBuilder.build());
                                        } else {
                                            Notification notification = new Notification(R.drawable.ic_m16_chat, obj.username + "님이 언급했어요.", System.currentTimeMillis());
                                            notification.flags = Notification.FLAG_AUTO_CANCEL;
                                            notification.defaults = Notification.DEFAULT_LIGHTS;
                                            notification.sound = ALARM_SOUND;
                                            notification.vibrate = ALARM_VIBRATE;
                                            notification.setLatestEventInfo(ChatActivity.this, obj.username + "님이 언급했어요.", "", pendingintent);
                                            mNotificationManager.notify(0, notification);
                                        }
                                    }
                                    break;
                                }
                            }

                            ChatItems.add(obj);
                            ChatAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void dispatchFriendList(final FriendEntry[] entries) {
                mFriendEntryHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Friends != null && FriendAdapter != null) {
                            Friends.clear();
                            Arrays.sort(entries, new FriendSort());
                            Collections.addAll(Friends, entries);
                            FriendAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void dispatchClanMembers(final ClanMember[] members) {
                mClanMemberHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ClanMembers != null && ClanMemberAdapter != null) {
                            ClanMembers.clear();
                            Arrays.sort(members, new ClanMemberSort());
                            Collections.addAll(ClanMembers, members);
                            ClanMemberAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void receiveClanInvitation(int cookie, int clanTag, String clanName, String inviter) {
                CLAN_INVITATION_RES = new ClanInvitationResponse(cookie, clanTag, clanName, inviter);

                String message = inviter + "님이 클랜에 초대하였습니다.\n" + "[Clan " + HexDump.DWordToPretty(clanTag) + "] " + clanName;
                final BNetChatMessage obj = new BNetChatMessage(BNetChatEventId.EID_INFO, inviter, message);
                mChatHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ChatItems != null && ChatAdapter != null) {
                            ChatItems.add(obj);
                            ChatAdapter.notifyDataSetChanged();
                        }
                    }
                });

                Message msg = new Message();
                msg.obj = message;
                sRequestLayoutHandler.sendMessage(msg);
            }

            @Override
            public void setClanRank(int rank) {
                ClanRank = rank;
            }

            @Override
            public void throwError(final String s) {
                Message msg = new Message();
                msg.obj = s;
                errorHandler.handleMessage(msg);
                BNetProtocol.InterruptThread();
                ChatThread.interrupt();
                finish();
            }

        });

        ChatThread = new Thread(BNetProtocol);
        ChatThread.start();

        errorHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                errorHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                });
                super.handleMessage(msg);
            }
        };

        String ALARM_TOGGLE = PreferencesControl.getInstance(this).get(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, null);
        if (ALARM_TOGGLE == null) {
            PreferencesControl.getInstance(this).set(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, PreferencesControl.ALARM_ON);
        }
    }

    public void setTitle() {
        setTitle(ChannelName + " (" + ChannelUsersNum + ")");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!chat_activity.isDrawerOpen(chat_drawer)) {
                chat_activity.openDrawer(chat_drawer);
            }
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_channel:
                if (!chat_activity.isDrawerOpen(chat_drawer)) {
                    chat_activity.openDrawer(chat_drawer);
                } else {
                    chat_activity.closeDrawer(chat_drawer);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ChatItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ChatItemSelectItem(position);
        }
    }

    private void ChatItemSelectItem(int position) {
        final BNetChatMessage ChatMessageItem = ChatItems.get(position);

        Toast.makeText(this, ChatMessageItem.getTimestamp(), Toast.LENGTH_SHORT).show();
    }

    private class ChanUserClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ChanUserSelectItem(position);
        }
    }

    private void ChanUserSelectItem(int position) {
        final BNetChannelUser ChanUsersItem = ChanUsers.get(position);

        ArrayList<String> listItems = new ArrayList<>();
        listItems.add("귓속말 (/w)");
        listItems.add("정보 (/finger)");
        listItems.add("추방 (/kick)");
        listItems.add("채널밴 (/ban)");

        if (ClanRank >= 3 && ChanUsersItem.statstr.checkClan()) {
            listItems.add("클랜 초대");
        }

        final CharSequence[] DialogFunctions = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        final String ItemUserName = ParseUsername.parseColor(ChanUsersItem.username);
        Builder.setTitle(ItemUserName);
        Builder.setItems(DialogFunctions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chat_spinner.setSelection(2);
                        chat_edittext.setText("/w " + ItemUserName + " ");
                        break;
                    case 1:
                        BNetProtocol.sendChatCommand("/finger " + ItemUserName);
                        break;
                    case 2:
                        BNetProtocol.sendChatCommand("/kick " + ItemUserName);
                        break;
                    case 3:
                        BNetProtocol.sendChatCommand("/ban " + ItemUserName);
                        break;
                    case 4:
                        if (ClanRank >= 3 && ChanUsersItem.statstr.checkClan())
                            BNetProtocol.sendClanInvitation(ItemUserName);
                        break;
                }
            }
        });

        Builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog Alert = Builder.create();
        Alert.show();

        chat_activity.closeDrawer(chat_drawer);
    }

    private class FriendUserClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            FriendUserSelectItem(position);
        }
    }

    private void FriendUserSelectItem(int position) {
        final FriendEntry FriendUsersItem = Friends.get(position);

        final CharSequence[] DialogFunctions = {"귓속말 (/w)", "정보 (/finger)", "친구 등급 올리기 (/f p)", "친구 등급 낮추기 (/f d)", "친구 목록에서 지우기 (/f r)"};
        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        final String ItemUserName = FriendUsersItem.account;
        Builder.setTitle(ItemUserName);
        Builder.setItems(DialogFunctions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chat_spinner.setSelection(2);
                        chat_edittext.setText("/w " + ItemUserName + " ");
                        break;
                    case 1:
                        BNetProtocol.sendChatCommand("/finger " + ItemUserName);
                        break;
                    case 2:
                        BNetProtocol.sendChatCommand("/f p " + ItemUserName);
                        BNetProtocol.sendFriendsList();
                        break;
                    case 3:
                        BNetProtocol.sendChatCommand("/f d " + ItemUserName);
                        BNetProtocol.sendFriendsList();
                        break;
                    case 4:
                        BNetProtocol.sendChatCommand("/f r " + ItemUserName);
                        BNetProtocol.sendFriendsList();
                        break;
                }
            }
        });
        Builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog Alert = Builder.create();
        Alert.show();

        chat_activity.closeDrawer(chat_drawer);
    }

    private class ClanUserClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ClanUserSelectItem(position);
        }
    }

    private void ClanUserSelectItem(int position) {
        final ClanMember ClanUsersItem = ClanMembers.get(position);

        final ArrayList<String> listItems = new ArrayList<>();
        listItems.add("귓속말 (/w)");
        listItems.add("정보 (/finger)");
        if (ClanRank >= 3 && ClanUsersItem.rank <= 3) {
            listItems.add("클랜 등급 변경");
            if (ClanUsersItem.rank <= 2)
                listItems.add("클랜 추방");
        }


        final CharSequence[] DialogFunctions = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        final String ItemUserName = ClanUsersItem.username;
        Builder.setTitle(ItemUserName);
        Builder.setItems(DialogFunctions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chat_spinner.setSelection(2);
                        chat_edittext.setText("/w " + ItemUserName + " ");
                        break;
                    case 1:
                        BNetProtocol.sendChatCommand("/finger " + ItemUserName);
                        break;
                    case 2: {
                        if (ClanRank >= 3 && ClanUsersItem.rank <= 3) {
                            ArrayList<String> listItems2 = new ArrayList<>();
                            AlertDialog.Builder Builder2 = new AlertDialog.Builder(ChatActivity.this);
                            Builder2.setTitle(ItemUserName);
                            switch (ClanRank) {
                                case 3: {
                                    listItems2.add("그런트");
                                    listItems2.add("피언");

                                    final CharSequence[] DialogFunction2 = listItems2.toArray(new CharSequence[listItems2.size()]);
                                    Builder2.setItems(DialogFunction2, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    BNetProtocol.sendClanRankChange(ItemUserName, 2);
                                                    break;
                                                case 1:
                                                    BNetProtocol.sendClanRankChange(ItemUserName, 1);
                                                    break;
                                            }
                                        }
                                    });

                                    break;
                                }
                                case 4: {
                                    listItems2.add("샤먼");
                                    listItems2.add("그런트");
                                    listItems2.add("피언");

                                    final CharSequence[] DialogFunction2 = listItems2.toArray(new CharSequence[listItems2.size()]);
                                    Builder2.setItems(DialogFunction2, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    BNetProtocol.sendClanRankChange(ItemUserName, 3);
                                                    break;
                                                case 1:
                                                    BNetProtocol.sendClanRankChange(ItemUserName, 2);
                                                    break;
                                                case 2:
                                                    BNetProtocol.sendClanRankChange(ItemUserName, 1);
                                                    break;
                                            }
                                        }
                                    });
                                    break;
                                }
                            }

                            Builder2.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog Alert2 = Builder2.create();
                            Alert2.show();

                            break;
                        }
                    }
                    case 3: {
                        if (ClanRank >= 3 && ClanUsersItem.rank <= 2) {
                            AlertDialog.Builder Builder2 = new AlertDialog.Builder(ChatActivity.this);
                            Builder2.setMessage("정말 추방시킬거에요?");
                            Builder2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BNetProtocol.sendClanRemoveMember(ClanUsersItem.username);
                                }
                            });
                            Builder2.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog Alert2 = Builder2.create();
                            Alert2.show();
                        }
                        break;
                    }
                }
            }
        });
        Builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog Alert = Builder.create();
        Alert.show();

        chat_activity.closeDrawer(chat_drawer);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == chat_submit.getId()) {
            chatSendMessage();
        }
        if (v.getId() == chat_menu_channel.getId()) {
            AlertDialog.Builder Builder = new AlertDialog.Builder(this);
            Builder.setTitle("채널 변경");
            Builder.setMessage("입장할 채널을 입력하세요.");
            final EditText channel_input = new EditText(this);
            Builder.setView(channel_input);

            Builder.setPositiveButton("입장", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String channel = channel_input.getText().toString();
                    chat_edittext.setText("/join " + channel);
                    chatSendMessage();
                }
            });

            Builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            Builder.show();
            chat_activity.closeDrawer(chat_drawer);
        }
        if (v.getId() == chat_menu_alarm.getId()) {
            String ALARM_TOGGLE = PreferencesControl.getInstance(this).get(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, null);
            if (ALARM_TOGGLE.equals(PreferencesControl.ALARM_ON)) {
                PreferencesControl.getInstance(this).set(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, PreferencesControl.ALARM_OFF);
                Toast.makeText(this, "닉네임 언급 알람을 해제했어요.", Toast.LENGTH_SHORT).show();
            } else {
                PreferencesControl.getInstance(this).set(PreferencesControl.ALARM_DATA_PREF, PreferencesControl.ALARM_SET, PreferencesControl.ALARM_ON);
                Toast.makeText(this, "닉네임 언급 알람을 설정했어요.", Toast.LENGTH_SHORT).show();
            }
        }
        if (v.getId() == chat_menu_clear.getId()) {
            ChatItems.clear();
            ChatAdapter.notifyDataSetChanged();
            Toast.makeText(this, "채팅을 청소했어요.", Toast.LENGTH_SHORT).show();
            chat_activity.closeDrawer(chat_drawer);
        }
        if (v.getId() == chat_menu_tab_channel.getId()) {
            friend_user_list.setVisibility(ListView.INVISIBLE);
            clan_user_list.setVisibility(ListView.INVISIBLE);
            channel_user_list.setVisibility(ListView.VISIBLE);
        }
        if (v.getId() == chat_menu_tab_friend.getId()) {
            BNetProtocol.sendFriendsList();
            clan_user_list.setVisibility(ListView.INVISIBLE);
            channel_user_list.setVisibility(ListView.INVISIBLE);
            friend_user_list.setVisibility(ListView.VISIBLE);
        }
        if (v.getId() == chat_menu_tab_clan.getId()) {
            BNetProtocol.sendClanMemberList();

            friend_user_list.setVisibility(ListView.INVISIBLE);
            channel_user_list.setVisibility(ListView.INVISIBLE);
            clan_user_list.setVisibility(ListView.VISIBLE);

        }

        if (v.getId() == s_request_accept_btn.getId()) {
            if (CLAN_INVITATION_RES != null) {
                BNetProtocol.sendResponseClanInvitation(
                        CLAN_INVITATION_RES.cookie,
                        CLAN_INVITATION_RES.clanTag,
                        CLAN_INVITATION_RES.inviter,
                        6); //decline code: 6
            }
            accept_box_wrap.setVisibility(FrameLayout.INVISIBLE);
        }

        if (v.getId() == s_request_decline_btn.getId()) {
            if (CLAN_INVITATION_RES != null) {
                BNetProtocol.sendResponseClanInvitation(
                        CLAN_INVITATION_RES.cookie,
                        CLAN_INVITATION_RES.clanTag,
                        CLAN_INVITATION_RES.inviter,
                        4); //decline code: 4
            }
            accept_box_wrap.setVisibility(FrameLayout.INVISIBLE);
        }
    }

    public void chatSendMessage() {
        String message = chat_edittext.getText().toString();
        if (message.length() == 0) {
            Toast.makeText(this, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 80) {
            Toast.makeText(this, "메세지를 너무 길게 입력했어요. (" + message.length() + "/80)", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (chat_spinner.getSelectedItemPosition()) {
            case 0:
                message = "/c m " + message;
                break;
            case 1:
                message = "/f m " + message;
                break;
            case 2:
                break;
            case 3:
                message = "/" + message;
                chat_spinner.setSelection(2);
                break;
        }
        BNetProtocol.sendChatCommand(message);
        chat_edittext.setText("");
        if (Pattern.matches("^/(.*)", message)) {
            return;
        }
        BNetChatMessage mBNetChatMessage = new BNetChatMessage(BNetChatEventId.EID_TALK, BNetProtocol.getUsername(), message);
        ChatItems.add(mBNetChatMessage);
        ChatAdapter.notifyDataSetChanged();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("chatSendMessage")
                .build());
    }

    @Override
    public void onBackPressed() {
        if (chat_activity.isDrawerOpen(chat_drawer)) {
            chat_activity.closeDrawer(chat_drawer);
            return;
        }
        Toast.makeText(this, "한번 더 누르면 종료해요.", Toast.LENGTH_SHORT).show();
        BACK_PRESSED_NUM++;
        BackPressedHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BACK_PRESSED_NUM = 0;
            }
        }, 2000);
        if (BACK_PRESSED_NUM == 2) {
            BNetProtocol.InterruptThread();
            ChatThread.interrupt();
            super.onBackPressed();
        }
    }

    private static class sRequestLayoutHandler extends Handler {
        private final WeakReference<ChatActivity> mActivity;

        public sRequestLayoutHandler(ChatActivity activity) {
            mActivity = new WeakReference<ChatActivity>(activity);
        }

        @Override
        public void handleMessage(final Message msg) {
            final ChatActivity activity = mActivity.get();

            if (activity != null) {
                new CountDownTimer(30000, 1000) {
                    public void onTick(long sec) {
                        activity.s_request_accept_btn.setText("수락 (" + sec / 1000 + ")");
                    }

                    public void onFinish() {
                        activity.accept_box_wrap.setVisibility(FrameLayout.INVISIBLE);
                    }
                }.start();
                activity.s_request_text.setText((String) msg.obj);
                activity.accept_box_wrap.setVisibility(FrameLayout.VISIBLE);

            }
        }
    }
}
