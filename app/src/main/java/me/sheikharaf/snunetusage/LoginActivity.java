package me.sheikharaf.snunetusage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class LoginActivity extends AppCompatActivity {
    String NetId = null, password = null;
    EditText etNetId, etPass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);

        final SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("MYPREFERENCES", Context.MODE_PRIVATE);
        NetId = sharedPreferences.getString("username", null);
        password = sharedPreferences.getString("password", null);

        if(NetId != null && password != null){
            Intent startMain = new Intent(getApplicationContext(), MainActivity.class);
            startMain.putExtra("username", NetId);
            startMain.putExtra("password", password);
            startActivity(startMain);
        }

        setContentView(R.layout.activity_login);

        etNetId = (EditText) findViewById(R.id.email);
        etPass = (EditText) findViewById(R.id.password);
        Button btLogin = (Button) findViewById(R.id.email_sign_in_button);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetId = etNetId.getText().toString();
                password = etPass.getText().toString();
                if(isValid()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", NetId);
                    editor.putString("password", password);
                    editor.commit();
                    Intent startMain = new Intent(getApplicationContext(), MainActivity.class);
                    startMain.putExtra("username", NetId);
                    startMain.putExtra("password", password);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(startMain);
                }
            }
        });

        TextView tvCredit = (TextView) findViewById(R.id.textView2);
        tvCredit.setText(Html.fromHtml(getString(R.string.credit)));
        tvCredit.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private boolean isValid() {
        if(NetId.length() != 5) {
            etNetId.setError("Invalid NetID");
            return false;
        }
        if(password.length() == 0) {
            etPass.setError("Empty password");
            return false;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}