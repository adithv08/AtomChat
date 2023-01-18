package dev.atomtables.atomchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.net.URI;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;


public class MainActivity extends AppCompatActivity {
    IO.Options options = IO.Options.builder()
            // IO factory options
            .setForceNew(false)
            .setMultiplex(true)

            // low-level engine options
            .setTransports(new String[]{Polling.NAME, WebSocket.NAME})
            .setUpgrade(true)
            .setRememberUpgrade(false)
            .setPath("/socket.io/")
            .setQuery(null)
            .setExtraHeaders(null)

            // Manager options
            .setReconnection(true)
            .setReconnectionAttempts(Integer.MAX_VALUE)
            .setReconnectionDelay(1_000)
            .setReconnectionDelayMax(5_000)
            .setRandomizationFactor(0.5)
            .setTimeout(20_000)
            .setAuth(null)
            .build();
    URI ipAddress = URI.create("http://127.0.0.1");
    Socket socket = IO.socket(ipAddress, options);

    Bundle extras;
    SharedPreferences sharedPref;
    String current_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get username and ip address from shared settings
        sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        try {
            extras = getIntent().getExtras();
            if (!sharedPref.getString("ipAddress", "absolutely0value").equals("absolutely0value")) {
                // get ip address from shared preferences
                ipAddress = URI.create(sharedPref.getString("ipAddress", ""));
                socket = IO.socket(ipAddress, options);
            } else {
                // see if intent extras contain extras.getString("ipaddress")
                if (extras.getString("ipaddress") != null) {
                    // intent extras contain ip address
                    socket = IO.socket(URI.create(extras.getString("ipaddress")), options);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("ipAddress", extras.getString("ipaddress"));
                    editor.apply();
                } else {
                    // intent does not contain extras so force an error to move down to *catch* zone
                    System.out.println("intent does not contain extras so force an error to move down to *catch* zone");
                    throw new NullPointerException();
                }

            }
            if (!sharedPref.getString("username", "absolutely0value").equals("absolutely0value")) {
                // set username to one found in shared preferences
                current_username = sharedPref.getString("username", "");
            } else {
                // get username from intent extras
                current_username = extras.getString("username");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", extras.getString("username"));
                editor.apply();
            }
        } catch (Exception e) {
            // came from launcher, will test to see if username/ip address data exists
            System.out.println("did not come from an intent, exception thrown was " + e);
            sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            if (!sharedPref.getString("ipAddress", "absolutely0value").equals("absolutely0value") && !sharedPref.getString("username", "absolutely0value").equals("absolutely0value")) {
                // shared prefs contained username and ip data, will use that
                ipAddress = URI.create(sharedPref.getString("ipAddress", ""));
                socket = IO.socket(ipAddress, options);
                current_username = sharedPref.getString("username", "");
            } else {
                // no username/ip address data has been found, redirecting the user to the login page
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
            }
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AtomicInteger ki = new AtomicInteger();
        ki.set(0);
        socket.connect();
        System.out.println("status connected is " + socket.connected());
        EditText send_message = findViewById(R.id.send_message);
        final boolean[] typingState = {false};
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!send_message.getText().toString().equals("")) {
                        socket.emit("message_typing", current_username);
                        typingState[0] = true;
                    } else {
                        if (typingState[0]) {
                            socket.emit("no_message_typing", current_username);
                            typingState[0] = false;
                        }
                    }
                });
            }
        }, 0, 2500);
        // see if ime activates send action
        send_message.setOnEditorActionListener((v, actionId, event) -> {
            if (ki.get() == 0) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (socket.connected()) {
                        onSendMessage();
                    } else {
                        onConnectionIssue();
                    }
                    ki.getAndIncrement();
                    setTimeout(ki::getAndDecrement, 250);
                    return true;
                }
            } else {
                return true;
            }
            return false;
        });
        Button button = findViewById(R.id.send_button);
        // see if button activates send action
        button.setOnClickListener((v) -> {
            if (socket.connected()) {
                onSendMessage();
            } else {
                onConnectionIssue();
            }
        });
        // on error, try to reconnect
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            System.out.println(Arrays.toString(args));
            socket.connect();
        });
        // on new message, append to MainActivity
        socket.on("new_message", (arg) -> runOnUiThread(() -> {
            String arg2 = arg[0].toString();
            String[] arg3 = arg2.split("%&##\uE096%%@");
            onReceiveMessage(arg3[0], arg3[1]);
        }));
        // initializing typing list:
        String[] people_typing = {"", "", "", ""};
        final int[] index = {0};
        // on typing message, add person to typing list
        socket.on("message_typing", (username) -> runOnUiThread(() -> {
            // init typing_message element
            TextView mTextView = findViewById(R.id.typing_message);
            // if someone has started typing add them to the array
            String typing_message = username[0] + " is typing...";
            while (index[0] < people_typing.length) {
                // if index is empty go to the empty index and store data
                if (people_typing[index[0]] == null || people_typing[index[0]].isEmpty()) {
                    people_typing[index[0]] = (String) username[0];
                    break;
                }
                // append
                index[0]++;
            }
            // to prevent overflow this exists
            if (index[0] == people_typing.length) {
                System.out.println("many people are typing");
            }
            System.out.println(typing_message);
        }));
        // on no typing message, remove person from typing list
        socket.on("no_message_typing", (username) -> runOnUiThread(() -> {
            TextView mTextView = findViewById(R.id.typing_message);
            String typing_message = username[0] + " is no longer typing...";
            System.out.println(typing_message);
        }));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (socket.connected()) {
                    runOnUiThread(() -> {
                        EditText send_message = findViewById(R.id.send_message);
                        send_message.setHint("Message");
                        send_message.setHintTextColor(Color.parseColor("#757575"));
                        System.out.println("status connected is " + socket.connected());
                    });
                } else {
                    socket.connect();
                }
            }
        }, 0, 5000);


    }

    final int[] i = {1};

    public int dpToPx(int dp, Context context) {

        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }

    public void onSendMessage() {
        System.out.println("current status : " + socket.connected());
        EditText send_message = findViewById(R.id.send_message);
        String message = send_message.getText().toString();
        send_message.requestFocus();
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
            username.setText(current_username);
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
            socket.emit("send_message", message, current_username);
            ScrollView sv = findViewById(R.id.scr1);
            sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
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
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            case R.id.help:
                Intent i = new Intent(this, HelpActivity.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onReceiveMessage(String message, String user) {

        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
        LinearLayout parentLayout = findViewById(R.id.parentLayout);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        TextView message_new = new TextView(MainActivity.this);
        message_new.setText(message);
        message_new.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.received_message_shape));
        message_new.setTextColor(Color.parseColor("#000000"));
        message_new.setTextSize(16);
        relativeLayout.setPadding(dpToPx(4, MainActivity.this), dpToPx(4, MainActivity.this), 0, 0);
        RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        TextView username = new TextView(MainActivity.this);
        username.setText(user);
        username.setTextColor(Color.parseColor("#f3f3f3"));
        username.setTextSize(12);
        message_new.setId(i[0]);
        RelativeLayout.LayoutParams ulp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        ulp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        tlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        ulp.addRule(RelativeLayout.BELOW, i[0]);
        i[0]++;
        rlp.setMargins(10, 10, 5, 0);
        relativeLayout.setLayoutParams(rlp);
        message_new.setLayoutParams(tlp);
        username.setLayoutParams(ulp);
        relativeLayout.addView(message_new);
        relativeLayout.addView(username);
        parentLayout.addView(relativeLayout);
        ScrollView sv = findViewById(R.id.scr1);
        sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
    }

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onConnectionIssue() {
        EditText send_message = findViewById(R.id.send_message);
        send_message.setHint("Connection Issue");
        send_message.setHintTextColor(Color.parseColor("#eb4034"));
        send_message.setText("");

    }

}