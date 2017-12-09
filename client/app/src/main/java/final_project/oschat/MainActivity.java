package final_project.oschat;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.createFab) FloatingActionButton createFab;
    @BindView(R.id.addFab) FloatingActionButton addFab;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.chatDisplay) LinearLayout groupList;
    @BindView(R.id.actionText) TextView logText;

    @BindView(R.id.addGroupWidget) LinearLayout addGroupWidget;   //widget 1
        @BindView(R.id.addButton) Button addGroupButton;
        @BindView(R.id.addGroupName) EditText addGroupNameEditText;
        @BindView(R.id.addGroupProgress) ProgressBar addGroupProgress;

    @BindView(R.id.createGroupWidget) LinearLayout createGroupWidget;   //widget 2
        @BindView(R.id.createButton) Button createGroupButton;
        @BindView(R.id.newGroupName) EditText createGroupNameEditText;
        @BindView(R.id.createGroupProgress) ProgressBar createGroupProgress;



    Animation widgetIn;
    private int widgetShowing = 0;
    private void showWidget(int widget){
        createGroupWidget.setVisibility(View.GONE);
        createFab.setImageDrawable(getResources().getDrawable(R.drawable.create));
        createFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        addGroupWidget.setVisibility(View.GONE);
        addFab.setImageDrawable(getResources().getDrawable(R.drawable.group_add));
        addFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        if (widget == 2 && widgetShowing != 2){
            createGroupWidget.setVisibility(View.VISIBLE);
            createFab.setImageDrawable(getResources().getDrawable(R.drawable.close));
            createFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cancel)));
            widgetShowing = 2;
            createGroupWidget.startAnimation(widgetIn);
        }
        else if (widget == 1 && widgetShowing != 1){
            addGroupWidget.setVisibility(View.VISIBLE);
            widgetShowing = 1;
            addFab.setImageDrawable(getResources().getDrawable(R.drawable.close));
            addFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cancel)));
            addGroupWidget.startAnimation(widgetIn);
        }
        else{
            widgetShowing = 0;
        }
    }

    Animation groupIn;
    public void addChatroomToList(int groupID, final String groupName){
        final LinearLayout chatroomButton = new LinearLayout(this);
        chatroomButton.setBackground((getResources().getDrawable(R.drawable.roundbox_group)));
        chatroomButton.setPadding(60,30,60,30);

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
            name.setTextColor(getResources().getColor(R.color.white));
            name.setTextSize(20);
            name.setText(groupName);
            name.setGravity(Gravity.CENTER_VERTICAL);
            name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            chatroomButton.addView(name);

        chatroomButton.setClickable(true);
        chatroomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), ChatView.class);
                i.putExtra("chatroom_name", groupName);
                MainActivity.this.startActivity(i);
            }
        });

        TranslateAnimation translate = new TranslateAnimation(0, 0, 200, 0);
        translate.setFillAfter(true);
        translate.setDuration(800 + groupID*70);
        chatroomButton.startAnimation(translate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {chatroomButton.setElevation(6);}

        groupList.addView(chatroomButton);
    }

    private void submitCreateGroup(){
        if (createGroupNameEditText.getText().length() == 0){
            Snackbar.make(createGroupWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        createGroupNameEditText.setEnabled(false);
        createGroupButton.setEnabled(false); createGroupButton.setVisibility(View.GONE);
        createGroupProgress.setVisibility(View.VISIBLE);

        //start thread process
    }
    public void successCreateGroup(){
        createGroupProgress.setVisibility(View.GONE);
        createGroupButton.setEnabled(true); createGroupButton.setVisibility(View.VISIBLE);
        Snackbar.make(createGroupWidget, "Group created successfully", Snackbar.LENGTH_LONG).show();
    }

    private void submitAddGroup(){
        if (addGroupNameEditText.getText().length() == 0){
            Snackbar.make(addGroupWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        addGroupNameEditText.setEnabled(false);
        addGroupButton.setEnabled(false); addGroupButton.setVisibility(View.GONE);
        addGroupProgress.setVisibility(View.VISIBLE);

        //start thread process
    }
    public void successAddGroup(){
        addGroupProgress.setVisibility(View.GONE);
        addGroupButton.setEnabled(true); addGroupButton.setVisibility(View.VISIBLE);
        Snackbar.make(addGroupWidget, "Group added successfully", Snackbar.LENGTH_LONG).show();
    }

    private void initUI(){
        setSupportActionBar(toolbar);

        createFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {showWidget(2);}
        });
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {showWidget(1);}
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {submitCreateGroup();}
        });
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {submitAddGroup();}
        });

        addGroupProgress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);
        createGroupProgress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);

        widgetIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.card_in);
        groupIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.card_slide_up_in);
    }

    AsyncTask currentAsync;
    private class chatListGet extends AsyncTask<Void, Void, Void> {
        String result;

        protected Void doInBackground(Void... voids) {
            try {
                Socket socket;
                try {socket = new Socket("127.0.0.1", 2000);}
                catch (IOException e) {throw e;}
                if (!socket.isConnected()){ return null;}

                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("Get");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String incoming = null;
                while (incoming == null) {
                    incoming = in.readLine();
                    if (incoming != null) {
                        result = incoming;
                        break;
                    }
                }

                try {
                    socket.close();
                    out.flush();
                    out.close();
                    in.close();
                }
                catch (IOException e) {e.printStackTrace();}
            }
            catch (Exception e) {e.printStackTrace();}
            return null;
        }

        @Override protected void onPostExecute(Void res) {
            if (result != null){
                groupList.removeAllViews();
                try {
                    JSONArray groupArray = new JSONArray(result);
                    for (int i = 0; i < groupArray.length(); i++) {
                        addChatroomToList(i, (String)groupArray.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            currentAsync = null;
            logText.setText("");
        }
    }


    ScheduledExecutorService exec;
    void initScheduler(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
//                logText.setText("Updimadating");
                if (currentAsync == null) {
                    currentAsync = new chatListGet().execute();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }



    @Override protected void onStop() {
        super.onStop();
        if (exec != null && !exec.isShutdown()){
            exec.shutdown();
        }
        if (currentAsync != null && (!currentAsync.isCancelled() || currentAsync.getStatus() == AsyncTask.Status.RUNNING)){
            currentAsync.cancel(true);
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initUI();
        initScheduler();

        for (int i = 1; i <= 10; i++) { addChatroomToList(i,"Chatroom " + i);}
    }

    @Override protected void onStart() {
        super.onStart();
    }
}
