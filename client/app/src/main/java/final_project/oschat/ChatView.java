package final_project.oschat;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evans on 12/5/2017.
 */

public class ChatView extends AppCompatActivity {
    @BindView(R.id.chatViewToolbar) Toolbar toolbar;
    @BindView(R.id.chats) LinearLayout chatsList;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.textBox) EditText textBox;
    @BindView(R.id.sendButton) Button sendButton;

    Intent intent;
    AsyncTask currentAsync;
    ScheduledExecutorService timedExecutor;




    //Server contact functions and UI thread callbacks
    private class sendChatCallback extends postSocketRunnable{
        @Override public void run() {
            textBox.setText("");
            getChats();
        }
    }
    void sendChat(String message){
        new socketTask("Write " + message, intent.getIntExtra("port", 0), new sendChatCallback()).execute();
    }

    private class importChatsRunnable extends postSocketRunnable{
        @Override
        public void run() {
            if (returnedArray != null) {
                chatsList.removeAllViews();
                for (int i = 0; i < returnedArray.length(); i++) {
                    try {
                        addMessageToList(returnedArray.getJSONObject(i).getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            currentAsync = null;
        }
    }
    void getChats(){
        if (currentAsync == null) {
            currentAsync = new socketTask("Get", intent.getIntExtra("port", 0), new importChatsRunnable()).execute();
        }
    }

    void initScheduler(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {getChats();}
        }, 0, 5, TimeUnit.SECONDS);

        for (int i = 1; i <= 20; i++) { addMessageToList("Chat" + i);}
    }




    //UI related functions
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

    private void addMessageToList(final String message){
        final LinearLayout chatroomButton = new LinearLayout(this);
        chatroomButton.setBackground((getResources().getDrawable(R.drawable.roundbox_chat)));
        chatroomButton.setPadding(60,30,60,30);
        chatroomButton.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 20, 10, 20);
        chatroomButton.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedArray ta = obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground});
            Drawable drawableFromTheme = ta.getDrawable(0 );
            ta.recycle();
            chatroomButton.setForeground(drawableFromTheme);
        }

        TextView name = new TextView(this);
        name.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        name.setTextSize(20);
        name.setText(message);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chatroomButton.addView(name);

        chatroomButton.setClickable(true);
        chatroomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {   }
        });

        TranslateAnimation translate = new TranslateAnimation(0, 0, 200, 0);
        translate.setFillAfter(true);
        translate.setDuration(800);
        chatroomButton.startAnimation(translate);

        chatsList.addView(chatroomButton);
        scrollToBottom();
    }

    private void uiInit(){
        intent = getIntent();

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




    //Override activity methods
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_view);
        ButterKnife.bind(this);
        uiInit();
        initScheduler();
    }

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