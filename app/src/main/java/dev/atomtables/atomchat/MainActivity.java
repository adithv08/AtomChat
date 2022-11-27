package dev.atomtables.atomchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.send_button);
        usernameSet("onLaunch");
        final int[] i = {1};
        button.setOnClickListener(v -> onSentMessage());
    }
    final int[] i = {32};

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
                    .setMessage("You can change this in settings")
                    .setPositiveButton("OK", (dialog1, which) -> callUsername(input.getText().toString(), "yes"))
                    .create();
            dialog.setView(input, (int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi) );
            dialog.show();
        }
    }

    public String callUsername(String username, String edit) {
        if (Objects.equals(edit, "yes")) {
            SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("username", username);
            editor.apply();
            return "";
        } else {
            SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            return sharedPref.getString("username", "notFound");
        }
    }

    public void changeUsername() {
        usernameSet("onCall");
    }

    public void onSentMessage() {
        EditText send_message = findViewById(R.id.send_message);
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
}