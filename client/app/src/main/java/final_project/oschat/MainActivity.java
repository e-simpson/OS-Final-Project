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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    //main activity UI binding
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.createFab) FloatingActionButton createFab;
    @BindView(R.id.addFab) FloatingActionButton addFab;
    @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.chatDisplay) LinearLayout chatRoomList;
    @BindView(R.id.screenNameText) TextView userNameText;
    @BindView(R.id.actionProgress) ProgressBar actionProgress;

    //widget 1 binding
    @BindView(R.id.addGroupWidget) LinearLayout addChatRoomWidget;
    @BindView(R.id.addButton) Button addChatRoomButton;
    @BindView(R.id.addGroupName) EditText addChatRoomNameEditText;
    @BindView(R.id.addGroupProgress) ProgressBar addChatRoomProgress;

    //widget 2 binding
    @BindView(R.id.createGroupWidget) LinearLayout createChatRoomWidget;
    @BindView(R.id.createButton) Button createChatRoomButton;
    @BindView(R.id.newGroupName) EditText createChatRoomNameText;
    @BindView(R.id.createGroupProgress) ProgressBar createChatRoomProgress;




    //-----Main Variables--------------------------------------------------
    AsyncTask currentAsync;                     //currently running async task
    ScheduledExecutorService timedExecutor;     //current timed executor that runs the scheduled server polling
    private ArrayList<Integer> myChatRooms = new ArrayList<>();     //arraylist to track joined chat rooms
    private Animation widgetIn;
    private int widgetShowing = 0;              //variable to track open widget
    private Boolean startAnimationComplete = false;
    private String screenName = "Anonymous";    //tracks current user screen name from setup




    //-----Server contact functions and UI thread callbacks--------------------------------------------------

    //callback after server successfully adds a chat room
    private class addChatRoomCallback extends postSocketRunnable{
        @Override public void run() {
            hideProgressWheel();
            successCreateChatRoom();
            getChatRooms();
        }
    }
    //ignites async task to add chat room
    void createChatRoom(String groupName){
        new socketAsyncTask("Create " + groupName, 2000, new addChatRoomCallback()).execute();
        actionProgress.setVisibility(View.VISIBLE);
    }

    //callback after server successfully joins a chat room
    private class joinChatRoomCallback extends postSocketRunnable{
        @Override public void run() {
            if (returnedArray != null && returnedArray.length() > 0) {
                try {
                    myChatRooms.add(returnedArray.getJSONObject(0).getInt("port"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            hideProgressWheel();
            successAddChatRoom();
            getChatRooms();
        }
    }
    //ignites async task to join chat room
    void joinChatRoom(String groupName){
        new socketAsyncTask("Join " + groupName, 2000, new joinChatRoomCallback()).execute();
        actionProgress.setVisibility(View.VISIBLE);
    }

    //callback after server successfully gets all the chat rooms
    private class getChatRoomCallback extends postSocketRunnable{
        @Override public void run() {
            if (returnedArray != null) {
                chatRoomList.removeAllViews();
                for (int i = 0; i < returnedArray.length(); i++) {
                    try {
                        addChatRoomToList(i, returnedArray.getJSONObject(1).getString("name"), returnedArray.getJSONObject(1).getInt("port"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

//            //for TESTING
//            System.out.println("@@@@@@@@@@@@@8 finish callback");
//            chatRoomList.removeAllViews();
//            myChatRooms.add(2); myChatRooms.add(5); myChatRooms.add(6);
//            for (int i = 1; i <= 10; i++) { addChatRoomToList(i,"Chat Room " + i, i);}


            currentAsync = null;
            hideProgressWheel();
            startAnimationComplete = true;
        }
    }
    //ignites async task to get all available chat room
    void getChatRooms(){
        if (currentAsync == null) {
            currentAsync = new socketAsyncTask("Get", 2000, new getChatRoomCallback()).execute();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionProgress.setVisibility(View.VISIBLE);
                }
            });
        }
    }




    //-----Server GET scheduler--------------------------------------------------
    //schedules a chat room list refresh every 2 seconds
    void initScheduler(){
        ScheduledExecutorService timedExecutor = Executors.newScheduledThreadPool(1);
        timedExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
//                Snackbar.make(chatRoomList, "Trying to get rooms", Snackbar.LENGTH_SHORT).show();
                getChatRooms();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }




    //-----UI related functions--------------------------------------------------
    //used to show a widget (add or join a group
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

    //adds a chat room button to the main list view
    //makes it un-clickable if the chat room has not been joined
    private void addChatRoomToList(int groupID, final String chatRoomName, final int port){
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
        name.setText(chatRoomName);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chatRoomButton.addView(name);

        chatRoomButton.setClickable(true);
        chatRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                openChatRoomView(chatRoomName, port);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {chatRoomButton.setElevation(6);}

        if (!myChatRooms.contains(port)){
            chatRoomButton.setAlpha(0.5F);
            chatRoomButton.setClickable(false);
            name.setTextColor(getResources().getColor(R.color.lightgrey));
            chatRoomButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    Snackbar.make(createChatRoomWidget, "Join this chat room to enter.", Snackbar.LENGTH_LONG)
                            .setAction("JOIN", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addChatRoomNameEditText.setText(chatRoomName);
                                    showWidget(1);
                                }
                            })
                            .show();
                }
            });
            chatRoomButton.setBackground((getResources().getDrawable(R.drawable.roundbox_unjoined)));
        }

        if(!startAnimationComplete){
            TranslateAnimation translate = new TranslateAnimation(0, 0, 200, 0);
            translate.setFillAfter(true);
            translate.setDuration(800 + groupID*70);
            chatRoomButton.startAnimation(translate);
        }

        chatRoomList.addView(chatRoomButton);
    }

    //called when a chat room button is pressed
    //opens the chat view activity
    private void openChatRoomView(String chatRoomName, int port){
        Intent i = new Intent(getBaseContext(), ChatActivity.class);
        i.putExtra("chatroom_name", chatRoomName);
        i.putExtra("port", port);
        i.putExtra("screenName", screenName);
        MainActivity.this.startActivity(i);
    }

    //hides the progress wheel using handler
    private void hideProgressWheel(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                actionProgress.setVisibility(View.INVISIBLE);
            }
        }, 600);
    }

    //called when create chat button is pressed
    private void submitCreateChatRoom(){
        if (createChatRoomNameText.getText().length() == 0){
            Snackbar.make(createChatRoomWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        createChatRoomNameText.setEnabled(false);
        createChatRoomButton.setEnabled(false); createChatRoomButton.setVisibility(View.GONE);
        createChatRoomProgress.setVisibility(View.VISIBLE);

        createChatRoom(createChatRoomNameText.getText().toString());
    }
    //called after successful create chat room
    private void successCreateChatRoom(){
        createChatRoomProgress.setVisibility(View.GONE);
        createChatRoomButton.setEnabled(true); createChatRoomButton.setVisibility(View.VISIBLE);
        Snackbar.make(createChatRoomWidget, "Chat room created successfully", Snackbar.LENGTH_LONG).show();
    }

    //called when join chat button is pressed
    private void submitAddChatRoom(){
        if (addChatRoomNameEditText.getText().length() == 0){
            Snackbar.make(addChatRoomWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        addChatRoomNameEditText.setEnabled(false);
        addChatRoomButton.setEnabled(false); addChatRoomButton.setVisibility(View.GONE);
        addChatRoomProgress.setVisibility(View.VISIBLE);

        joinChatRoom(addChatRoomNameEditText.getText().toString());
    }
    //called after successful join chat room
    private void successAddChatRoom(){
        addChatRoomProgress.setVisibility(View.GONE);
        addChatRoomButton.setEnabled(true); addChatRoomButton.setVisibility(View.VISIBLE);
        Snackbar.make(addChatRoomWidget, "Chat room added successfully", Snackbar.LENGTH_LONG).show();
    }

    //initiates the UI resources
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
    }




    //-----Override activity methods--------------------------------------------------

    //receives log in activity success and saves the username to a variable for later use
    //then starts the scheduled GET synchronization
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 7) {
            if (resultCode == RESULT_OK) {
                screenName = data.getStringExtra("name");
                userNameText.setText(screenName);
                initScheduler();
            }
        }
    }

    //initializes the view, binds the UI, and starts the login intent
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initUI();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 7);
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
