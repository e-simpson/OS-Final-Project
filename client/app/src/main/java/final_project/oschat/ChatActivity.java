package final_project.oschat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evans on 12/5/2017.
 */

public class ChatActivity extends AppCompatActivity {

    //main activity UI binding
    @BindView(R.id.chatViewToolbar) Toolbar toolbar;
    @BindView(R.id.chats) LinearLayout chatsList;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.textBox) EditText textBox;
    @BindView(R.id.sendButton) Button sendButton;



    //-----Main Variables--------------------------------------------------
    AsyncTask currentAsync;
    ScheduledExecutorService timedExecutor;
    private Intent intent;
    private Boolean startAnimationComplete = false;
    private int port;
    private String screenName;




    //-----Server contact functions and UI thread callbacks--------------------------------------------------
    //callback after server successfully sends a chat
    private class sendChatCallback extends postSocketRunnable{
        @Override public void run() {
            textBox.setText("");
            getChats();
        }
    }
    //ignites async task to send a chat to the proper port
    void sendChat(String message){
        new socketAsyncTask(screenName + " Write " + message, port, new sendChatCallback()).execute();
    }

    //callback after server successfully gets all a chats
    //adds them to the view
    private class importChatsRunnable extends postSocketRunnable{
        @Override
        public void run() {
            if (returnedArray != null) {
                chatsList.removeAllViews();
                for (int i = 0; i < returnedArray.length(); i++) {
                    try {
                        addMessageToList(returnedArray.getJSONObject(i).getString("message"), returnedArray.getJSONObject(i).getString("user"), returnedArray.getJSONObject(i).getLong("time"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

//            //for TESTING
//            chatsList.removeAllViews();
//            for (int i = 1; i <= 20; i++) { addMessageToList("Chat" + i, "Anonymous", System.currentTimeMillis());}

            currentAsync = null;
            startAnimationComplete = true;
        }
    }
    //ignites async task to retrieve all chats for a specific
    void getChats(){
        Snackbar.make(chatsList, "Refreshing", Snackbar.LENGTH_SHORT);
        if (currentAsync == null) {
            currentAsync = new socketAsyncTask("Get", port, new importChatsRunnable()).execute();
        }
    }




    //-----Server GET scheduler--------------------------------------------------
    //schedules a chat room list refresh every second
    void initScheduler(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {getChats();}
        }, 0, 1, TimeUnit.SECONDS);
    }




    //-----UI related functions--------------------------------------------------
    //scrolls the user to the bottom when first opening the list
    private void scrollToBottom(){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {Thread.sleep(100);} catch (InterruptedException e) {}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }).start();
    }

    //adds a chat bubble the main chat list
    @SuppressLint("SetTextI18n")
    private void addMessageToList(final String message, final String userName, final long utcTime){
        LinearLayout chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);


        final LinearLayout chatBubble = new LinearLayout(this);
        chatBubble.setBackground((getResources().getDrawable(R.drawable.roundbox_chat)));
        chatBubble.setPadding(60,30,60,30);
        chatBubble.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 20, 10, 20);
        chatBubble.setLayoutParams(params);
        chat.addView(chatBubble);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedArray ta = obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground});
            Drawable drawableFromTheme = ta.getDrawable(0 );
            ta.recycle();
            chatBubble.setForeground(drawableFromTheme);
        }

        TextView words = new TextView(this);
        words.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        words.setTextSize(20);
        words.setText(message);
        words.setGravity(Gravity.CENTER_VERTICAL);
        words.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chatBubble.addView(words);



        final LinearLayout nameAndDate = new LinearLayout(this);
        nameAndDate.setOrientation(LinearLayout.VERTICAL);
        nameAndDate.setGravity(Gravity.END);
        nameAndDate.setPadding(60,0,60,40);

        TextView user = new TextView(this);
        user.setTextColor(getResources().getColor(R.color.nav));
        user.setTextSize(10);
        user.setText("-" + userName);
        user.setGravity(Gravity.END);
        user.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        user.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nameAndDate.addView(user);

        TextView time = new TextView(this);
        time.setTextColor(getResources().getColor(R.color.nav));
        time.setTextSize(8);
        time.setTypeface(null, Typeface.ITALIC);
        Date date = new Date(utcTime);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss" );
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        time.setText(formatter.format(date));
        time.setGravity(Gravity.END);
        time.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        time.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nameAndDate.addView(time);

        chat.addView(nameAndDate);


        chatBubble.setClickable(true);
        chatBubble.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {}
        });

        if (!startAnimationComplete){
            TranslateAnimation translate = new TranslateAnimation(0, 0, 200, 0);
            translate.setFillAfter(true);
            translate.setDuration(800);
            chat.startAnimation(translate);
            scrollToBottom();
        }

        chatsList.addView(chat);
    }

    //initiates the UI resources
    private void uiInit(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(intent.getStringExtra("chatroom_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(10);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (textBox.length() > 0){
                    sendChat(textBox.getText().toString());
                }
            }
        });

        this.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }




    //-----Override activity methods--------------------------------------------------
    //disables ending the login activity to enter the main activity before setup
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //initializes the view, binds the UI, and saves the port and screen name
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        ButterKnife.bind(this);

        intent = getIntent();
        port = intent.getIntExtra("port", 0);
        screenName = intent.getStringExtra("screenName");

        uiInit();
    }

    //starts the scheduled GET synchronization
    @Override protected void onStart() {
        super.onStart();
        initScheduler();
    }

    //properly shuts down an active timed executor, and currently running async task, if there is one
    @Override protected void onStop() {
        super.onStop();
        if (timedExecutor != null && !timedExecutor.isShutdown()){
            timedExecutor.shutdown();
        }
        if (currentAsync != null && (!currentAsync.isCancelled() || currentAsync.getStatus() == AsyncTask.Status.RUNNING)){
            currentAsync.cancel(true);
        }
    }
}