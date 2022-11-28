package dev.atomtables.atomchat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.send_button);
        usernameSet("onLaunch");
        button.setOnClickListener(v -> onSendMessage());
    }
    final int[] i = {1};

    public int dpToPx(int dp, Context context) {

        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public void usernameSet(String activity) {
        if (Objects.equals(activity, "onLaunch")) {
            if (Objects.equals(callUsername("", "no"), "notFound")) {
                final EditText input = new EditText(MainActivity.this);

                float dpi = MainActivity.this.getResources().getDisplayMetrics().density;
                AlertDialog dialog = (new AlertDialog.Builder(MainActivity.this))
                        .setTitle("Please choose a username")
                        .setMessage("You can change this in settings")
                        .setPositiveButton("OK", (dialog12, which) -> callUsername(input.getText().toString(), "yes"))
                        .create();
                dialog.setView(input, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );
                dialog.show();


            }
        } else if (Objects.equals(activity, "onCall")) {
            final EditText input = new EditText(MainActivity.this);
            float dpi = MainActivity.this.getResources().getDisplayMetrics().density;
            AlertDialog dialog = (new AlertDialog.Builder(MainActivity.this))
                    .setTitle("Please choose a username")
                    .setMessage("Note this will not change your username for any prior texts!")
                    .setPositiveButton("OK", (dialog1, which) -> callUsername(input.getText().toString(), "yes"))
                    .create();
            dialog.setView(input, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );
            dialog.show();
        }
    }

    public String callUsername(String username, String edit) {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        if (Objects.equals(edit, "yes")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.apply();
            return "";
        } else {
            return sharedPref.getString("username", "notFound");
        }
    }


    public void onSendMessage() {
        EditText send_message = findViewById(R.id.send_message);
        send_message.requestFocus();
        String message = send_message.getText().toString();
        send_message.setHint("Message");
        send_message.setHintTextColor(Color.parseColor("#757575"));
        if (!message.equals("")) {
            send_message.setText("");
            RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
            LinearLayout parentLayout = findViewById(R.id.parentLayout);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            TextView message_new = new TextView(MainActivity.this);
            message_new.setText(message);
            message_new.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.sent_message_shape));
            message_new.setTextColor(Color.parseColor("#000000"));
            message_new.setTextSize(16);

            RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            TextView username = new TextView(MainActivity.this);
            username.setText(callUsername("", "no"));
            username.setTextColor(Color.parseColor("#f3f3f3"));
            username.setTextSize(12);
            message_new.setId(i[0]);

            RelativeLayout.LayoutParams ulp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            ulp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            tlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            ulp.addRule(RelativeLayout.BELOW, i[0]);
            i[0]++;
            rlp.setMargins(10, 10, 5, 0);
            relativeLayout.setLayoutParams(rlp);
            message_new.setLayoutParams(tlp);
            username.setLayoutParams(ulp);
            relativeLayout.addView(message_new);
            relativeLayout.addView(username);
            parentLayout.addView(relativeLayout);

        } else {
            send_message.setHint("Please type a message");
            send_message.setHintTextColor(Color.parseColor("#eb4034"));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.user_change:
                usernameSet("onCall");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}