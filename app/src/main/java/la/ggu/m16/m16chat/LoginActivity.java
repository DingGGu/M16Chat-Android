package la.ggu.m16.m16chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import la.ggu.m16.m16chat.util.PreferencesControl;


public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText login_username, login_password;
    private Button login_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView text_go_homepage = (TextView) findViewById(R.id.text_go_homepage);
        text_go_homepage.setText(Html.fromHtml("<a href=\"https://m16.ggu.la\">M16 홈페이지</a> "));
        text_go_homepage.setMovementMethod(LinkMovementMethod.getInstance());

        login_username = (EditText) findViewById(R.id.login_username);
        login_password = (EditText) findViewById(R.id.login_password);
        login_submit = (Button) findViewById(R.id.login_submit);
        login_submit.setOnClickListener(this);

        String PrefUserName = PreferencesControl.getInstance(this).get(PreferencesControl.USER_DATA_PREF, PreferencesControl.USER_NAME, null);
        login_username.setText(PrefUserName);

        login_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendLogin();
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == login_submit.getId()) {
            sendLogin();
        }
    }

    public void sendLogin() {
        String username = login_username.getText().toString();
        String password = login_password.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "아이디 비밀번호를 입력하지 않았어요.", Toast.LENGTH_SHORT).show();
        } else {
            PreferencesControl.getInstance(this).set(PreferencesControl.USER_DATA_PREF, PreferencesControl.USER_NAME, username);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("login_username", username);
            intent.putExtra("login_password", password);
            startActivity(intent);
        }
    }
}
