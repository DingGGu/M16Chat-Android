package la.ggu.m16.m16chat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private EditText login_username, login_password;
    private Button login_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login_username = (EditText) findViewById(R.id.login_username);
        login_password = (EditText) findViewById(R.id.login_password);
        login_submit = (Button) findViewById(R.id.login_submit);
        login_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == login_submit.getId()) {
            String username = login_username.getText().toString();
            String password = login_password.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "아이디 비밀번호를 입력하지 않았어요.", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, ChatActivity.class));
            }
        }
    }
}
