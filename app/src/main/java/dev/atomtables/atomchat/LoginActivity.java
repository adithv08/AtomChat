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
            String usernameString = String.valueOf(usernameData.getText());
            String ipaddressString = String.valueOf(ipAddressData.getText());
            i.putExtra("username", usernameString);
            i.putExtra("ipaddress", ipaddressString);
            System.out.println("username is " + usernameString + " and ip address is " + ipaddressString);
            startActivity(i);
        });
    }
}