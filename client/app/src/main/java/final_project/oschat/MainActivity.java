package final_project.oschat;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

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
    @BindView(R.id.chatDisplay) LinearLayout chatRoomList;
    @BindView(R.id.actionText) TextView logText;

    @BindView(R.id.addGroupWidget) LinearLayout addChatRoomWidget;   //widget 1
        @BindView(R.id.addButton) Button addChatRoomButton;
        @BindView(R.id.addGroupName) EditText addChatRoomNameEditText;
        @BindView(R.id.addGroupProgress) ProgressBar addChatRoomProgress;

    @BindView(R.id.createGroupWidget) LinearLayout createChatRoomWidget;   //widget 2
        @BindView(R.id.createButton) Button createChatRoomButton;
        @BindView(R.id.newGroupName) EditText createChatRoomNameText;
        @BindView(R.id.createGroupProgress) ProgressBar createChatRoomProgress;

    Animation widgetIn;
    Animation chatRoomIn;
    AsyncTask currentAsync;
    ScheduledExecutorService timedExecutor;
    int widgetShowing = 0;




    private void showWidget(int widget){
        createChatRoomWidget.setVisibility(View.GONE);
        createFab.setImageDrawable(getResources().getDrawable(R.drawable.create));
        createFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        addChatRoomWidget.setVisibility(View.GONE);
        addFab.setImageDrawable(getResources().getDrawable(R.drawable.group_add));
        addFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        if (widget == 2 && widgetShowing != 2){
            createChatRoomWidget.setVisibility(View.VISIBLE);
            createFab.setImageDrawable(getResources().getDrawable(R.drawable.close));
            createFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cancel)));
            widgetShowing = 2;
            createChatRoomWidget.startAnimation(widgetIn);
        }
        else if (widget == 1 && widgetShowing != 1){
            addChatRoomWidget.setVisibility(View.VISIBLE);
            widgetShowing = 1;
            addFab.setImageDrawable(getResources().getDrawable(R.drawable.close));
            addFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cancel)));
            addChatRoomWidget.startAnimation(widgetIn);
        }
        else{
            widgetShowing = 0;
        }
    }
    public void addChatRoomToList(int groupID, final String groupName){
        final LinearLayout chatRoomButton = new LinearLayout(this);
        chatRoomButton.setBackground((getResources().getDrawable(R.drawable.roundbox_group)));
        chatRoomButton.setPadding(60,30,60,30);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 20, 10, 20);
        chatRoomButton.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TypedArray ta = obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground});
            Drawable drawableFromTheme = ta.getDrawable(0 );
            ta.recycle();
            chatRoomButton.setForeground(drawableFromTheme);
        }

        TextView name = new TextView(this);
            name.setTextColor(getResources().getColor(R.color.white));
            name.setTextSize(20);
            name.setText(groupName);
            name.setGravity(Gravity.CENTER_VERTICAL);
            name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            chatRoomButton.addView(name);

        chatRoomButton.setClickable(true);
        chatRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), ChatView.class);
                i.putExtra("chatroom_name", groupName);
                MainActivity.this.startActivity(i);
            }
        });

        TranslateAnimation translate = new TranslateAnimation(0, 0, 200, 0);
        translate.setFillAfter(true);
        translate.setDuration(800 + groupID*70);
        chatRoomButton.startAnimation(translate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {chatRoomButton.setElevation(6);}

        chatRoomList.addView(chatRoomButton);
    }

    private void submitCreateChatRoom(){
        if (createChatRoomNameText.getText().length() == 0){
            Snackbar.make(createChatRoomWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        createChatRoomNameText.setEnabled(false);
        createChatRoomButton.setEnabled(false); createChatRoomButton.setVisibility(View.GONE);
        createChatRoomProgress.setVisibility(View.VISIBLE);

        //start thread process
    }
    public void successCreateChatRoom(){
        createChatRoomProgress.setVisibility(View.GONE);
        createChatRoomButton.setEnabled(true); createChatRoomButton.setVisibility(View.VISIBLE);
        Snackbar.make(createChatRoomWidget, "Chat room created successfully", Snackbar.LENGTH_LONG).show();
    }

    private void submitAddChatRoom(){
        if (addChatRoomNameEditText.getText().length() == 0){
            Snackbar.make(addChatRoomWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        addChatRoomNameEditText.setEnabled(false);
        addChatRoomButton.setEnabled(false); addChatRoomButton.setVisibility(View.GONE);
        addChatRoomProgress.setVisibility(View.VISIBLE);

        //start thread process
    }
    public void successAddChatRoom(){
        addChatRoomProgress.setVisibility(View.GONE);
        addChatRoomButton.setEnabled(true); addChatRoomButton.setVisibility(View.VISIBLE);
        Snackbar.make(addChatRoomWidget, "Chat room added successfully", Snackbar.LENGTH_LONG).show();
    }

    private void initUI(){
        setSupportActionBar(toolbar);

        createFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {showWidget(2);}
        });
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {showWidget(1);}
        });

        createChatRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                submitCreateChatRoom();}
        });
        addChatRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                submitAddChatRoom();}
        });

        addChatRoomProgress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);
        createChatRoomProgress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);

        widgetIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.card_in);
        chatRoomIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.card_slide_up_in);
    }




    private class importGroupsRunnable extends postSocketRunnable{
        @Override public void run() {
            chatRoomList.removeAllViews();
            for (int i = 0; i < returnedArray.length(); i++) {
                try {
                    addChatRoomToList(i, (String)returnedArray.get(i));}
                catch (JSONException e) {e.printStackTrace();}
            }
            currentAsync = null;
        }
    }

    void retrieveGroups(){
        if (currentAsync == null) {
            currentAsync = new socketTask("Get", 2000, new importGroupsRunnable()).execute();
        }
    }

    void initScheduler(){
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {retrieveGroups();}
        }, 0, 5, TimeUnit.SECONDS);
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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initUI();
        initScheduler();

        for (int i = 1; i <= 10; i++) { addChatRoomToList(i,"Chatroom " + i);}
    }

    @Override protected void onStart() {
        super.onStart();
    }
}
