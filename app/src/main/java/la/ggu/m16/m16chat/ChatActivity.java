package la.ggu.m16.m16chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import bnetp.*;
import la.ggu.m16.m16chat.cv.ChatAdapter;

public class ChatActivity extends ActionBarActivity implements View.OnClickListener {

    private ListView chat_view;
    private ArrayList<BNetChatMessage> ChatItems = new ArrayList<BNetChatMessage>();
    private ChatAdapter ChatAdapter;
    private Spinner chat_spinner;
    private EditText chat_edittext;
    private Button chat_submit;

    private static int BACK_PRESSED_NUM;

    private Thread ChatThread = null;
    private BNetProtocol BNetProtocol = null;

    private Handler mChatHandler = new Handler();
    private Handler BackPressedHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_view = (ListView) findViewById(R.id.chat_view);
        ChatAdapter = new ChatAdapter(this, R.layout.custom_chat, ChatItems);
        chat_view.setAdapter(ChatAdapter);

        chat_spinner = (Spinner) findViewById(R.id.chat_spinner);
        chat_spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
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

        Intent intent = getIntent();
        String login_username = intent.getStringExtra("login_username");
        String login_password = intent.getStringExtra("login_password");
        BNetProtocol = new BNetProtocol(login_username, login_password);
        BNetProtocol.setBnetProtocolInterface(new BNetProtocolInterface() {
            @Override
            public void receiveMessage(final String message) {

            }

            @Override
            public void receiveMessage(final BNetChatMessage obj) {
                mChatHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(ChatItems != null && ChatAdapter != null) {
                            ChatItems.add(obj);
                            ChatAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void throwError(final String s) {
                ChatThread.interrupt();
                finish();
            }
        });

        ChatThread = new Thread(BNetProtocol);
        ChatThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_clear) {
            ChatItems.clear();
            ChatAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == chat_submit.getId()) {
            chatSendMessage();
        }
    }

    public void chatSendMessage() {
        String message = chat_edittext.getText().toString();
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
        String username = "test";
        BNetChatMessage mBNetChatMessage = new BNetChatMessage(username, message);
        ChatItems.add(mBNetChatMessage);
        ChatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
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
