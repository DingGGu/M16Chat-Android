package la.ggu.m16.m16chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import bnetp.*;

public class ChatActivity extends ActionBarActivity implements View.OnClickListener {

    private ListView chat_view;
    private ArrayList<String> ChatItems = new ArrayList<String>();
    private ArrayAdapter<String> ChatAdapter;
    private EditText chat_edittext;
    private Button chat_submit;

    private static int BACK_PRESSED_NUM;

    private Thread ChatThread = null;
    private BNetProtocol BNetProtocol = null;

    private Handler mChatHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat_view = (ListView) findViewById(R.id.chat_view);
        ChatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ChatItems);
        chat_view.setAdapter(ChatAdapter);

        chat_edittext = (EditText) findViewById(R.id.chat_edittext);
        chat_submit = (Button) findViewById(R.id.chat_submit);
        chat_submit.setOnClickListener(this);

        Intent intent = getIntent();
        String login_username = intent.getStringExtra("login_username");
        String login_password = intent.getStringExtra("login_password");
        BNetProtocol = new BNetProtocol(login_username, login_password);
        BNetProtocol.setBnetProtocolInterface(new BNetProtocolInterface() {
            @Override
            public void receiveMessage(final String message) {
                mChatHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(ChatItems != null && ChatAdapter != null) {
                            ChatItems.add(message);
                            ChatAdapter.notifyDataSetChanged();
                        }
                    }
                });
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
            String message = chat_edittext.getText().toString();
            BNetProtocol.sendChatCommand(message);
            chat_edittext.setText("");
            ChatItems.add(message);
            ChatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
