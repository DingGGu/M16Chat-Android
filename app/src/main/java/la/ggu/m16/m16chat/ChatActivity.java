package la.ggu.m16.m16chat;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import bnetp.*;
import la.ggu.m16.m16chat.cv.ChanAdapter;
import la.ggu.m16.m16chat.cv.ChatAdapter;
import la.ggu.m16.m16chat.util.ParseUsername;

public class ChatActivity extends ActionBarActivity implements View.OnClickListener {

    private DrawerLayout chat_activity;
    private RelativeLayout chat_drawer;
    private ListView channel_user_list;
    private ArrayList<BNetChannelUser> ChanUsers = new ArrayList<BNetChannelUser>();
    private ChanAdapter ChanAdapter;

    private ListView chat_view;
    private ArrayList<BNetChatMessage> ChatItems = new ArrayList<BNetChatMessage>();
    private ChatAdapter ChatAdapter;
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
    private int THREAD_COUNT;

    private Thread ChatThread = null;
    private BNetProtocol BNetProtocol = null;

    private Handler mChatHandler = new Handler();
    private Handler mChannelUserHandler = new Handler();
    private Handler BackPressedHandler = new Handler();
    private Handler errorHandler;

    private NotificationManager mNotificationManager;
    private int mNotificationNumber;
    private Uri DefaultAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        assert getSupportActionBar() != null;
        getSupportActionBar().setElevation(0);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        chat_activity = (DrawerLayout) findViewById(R.id.chat_activity);
        chat_drawer = (RelativeLayout) findViewById(R.id.chat_drawer);

        channel_user_list = (ListView) findViewById(R.id.channel_user_list);
        ChanAdapter = new ChanAdapter(this, R.id.channel_user_list_item, ChanUsers);
        channel_user_list.setAdapter(ChanAdapter);
        channel_user_list.setOnItemClickListener(new ChanUserClickListener());

        chat_menu_channel = (LinearLayout) findViewById(R.id.chat_menu_channel);
        chat_menu_channel.setOnClickListener(this);
        chat_menu_alarm = (LinearLayout) findViewById(R.id.chat_menu_alarm);
        chat_menu_alarm.setOnClickListener(this);
        chat_menu_clear = (LinearLayout) findViewById(R.id.chat_menu_clear);
        chat_menu_clear.setOnClickListener(this);

        chat_view = (ListView) findViewById(R.id.chat_view);
        ChatAdapter = new ChatAdapter(this, R.layout.custom_chat, ChatItems);
        chat_view.setAdapter(ChatAdapter);

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
                    this.throwError("잠시 후 다시 시도해주세요.");
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
                            //TODO: Remove Channel Users
//                            ChanUsers.remove(obj);
//                            ChanAdapter.notifyDataSetChanged();
//                            Iterator<BNetChannelUser> it = ChanUsers.iterator();
//                            while (it.hasNext()) {
//                                BNetChannelUser cu = it.next();
//                                if (cu.username.equals(obj.username)) {
//                                    ChanUsers.remove(cu);
//                                    ChanAdapter.notifyDataSetChanged();
//                                }
//                            }
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
                            switch(obj.eid) {
                                case EID_TALK:
                                case EID_WHISPER: {
                                    if(obj.message.toLowerCase().contains(uniqueUserName.toLowerCase())) { //알림
                                        PendingIntent pendingintent = PendingIntent.getActivity(ChatActivity.this, 0, new Intent(ChatActivity.this, ChatActivity.class), PendingIntent.FLAG_NO_CREATE);

                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(ChatActivity.this)
                                                        .setSmallIcon(R.drawable.ic_m16_chat)
                                                        .setContentTitle(obj.username + " 님이 언급했어요.")
                                                        .setContentText(obj.message)
                                                        .setNumber(++mNotificationNumber)
                                                        .setAutoCancel(true)
                                                        .setVibrate(new long[] { 1000, 1000 })
                                                        .setSound(DefaultAlarmSound)
                                                        .setContentIntent(pendingintent);
                                        mNotificationManager.notify(0, mBuilder.build());
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
            public void throwError(final String s) {
                Message msg = new Message();
                msg.obj = s;
                errorHandler.handleMessage(msg);
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
                        Toast.makeText(ChatActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    }
                });
                super.handleMessage(msg);
            }
        };
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
        // Inflate the menu; this adds items to the action bar if it is present.
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

    private class ChanUserClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            ChanUserSelectItem(position);
        }
    }

    private void ChanUserSelectItem(int position) {
        final BNetChannelUser ChanUsersItem = ChanUsers.get(position);

        final CharSequence[] DialogFunctions = {"귓속말 (/w)", "정보 (/finger)", "추방 (/kick)", "채널밴 (/ban)"};
        AlertDialog.Builder Builder = new AlertDialog.Builder(this);
        final String ItemUserName = ParseUsername.parseColor(ChanUsersItem.username);
        Builder.setTitle(ItemUserName);
        Builder.setItems(DialogFunctions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        chat_edittext.setText("/w " + ItemUserName + " ");
                        break;
                    case 1:
                        chat_edittext.setText("/finger " + ItemUserName);
                        chatSendMessage();
                        break;
                    case 2:
                        chat_edittext.setText("/kick " + ItemUserName);
                        chatSendMessage();
                        break;
                    case 3:
                        chat_edittext.setText("/ban " + ItemUserName);
                        chatSendMessage();
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
        if (v.getId() == chat_menu_clear.getId()) {
            ChatItems.clear();
            ChatAdapter.notifyDataSetChanged();
            Toast.makeText(this, "채팅을 청소했어요.", Toast.LENGTH_SHORT).show();
            chat_activity.closeDrawer(chat_drawer);
        }
    }

    public void chatSendMessage() {
        String message = chat_edittext.getText().toString();
        if (message.length() == 0) {
            Toast.makeText(this, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(chat_spinner.getSelectedItemPosition()) {
            case 0:
                message = "/c m "+message;
                break;
            case 1:
                message = "/f m "+message;
                break;
            case 2:
                break;
            case 3:
                message = "/"+message;
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
            ChatThread.interrupt();
            super.onBackPressed();
        }
    }
}
