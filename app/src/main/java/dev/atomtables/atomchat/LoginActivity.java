package dev.atomtables.atomchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button onLogin = findViewById(R.id.button);
        onLogin.setOnClickListener((v) -> {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            EditText usernameData = findViewById(R.id.usermane);
            EditText ipAddressData = findViewById(R.id.ip_address);
            i.putExtra("username", usernameData.getText());
            i.putExtra("ipaddress", ipAddressData.getText());
            startActivity(i);
        });
    }
}