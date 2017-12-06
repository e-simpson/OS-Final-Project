package final_project.oschat;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.createFab) FloatingActionButton createFab;
    @BindView(R.id.addFab) FloatingActionButton addFab;

    @BindView(R.id.addGroupWidget) LinearLayout addGroupWidget;   //widget 1
        @BindView(R.id.addButton) Button addGroupButton;
        @BindView(R.id.addGroupName) EditText addGroupNameEditText;
//        @BindView(R.id.newGroupProgress) ProgressBar addGroupProgress;
//        @BindView(R.id.newGroupProgressCheck) ImageView addGroupCheck;

    @BindView(R.id.createGroupWidget) LinearLayout createGroupWidget;   //widget 2
        @BindView(R.id.createButton) Button createGroupButton;
        @BindView(R.id.newGroupName) EditText createGroupNameEditText;
//        @BindView(R.id.newGroupProgress) ProgressBar createGroupProgress;
//        @BindView(R.id.newGroupProgressCheck) ImageView createGroupCheck;


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
        }
        else if (widget == 1 && widgetShowing != 1){
            addGroupWidget.setVisibility(View.VISIBLE);
            widgetShowing = 1;
            addFab.setImageDrawable(getResources().getDrawable(R.drawable.close));
            addFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.cancel)));
        }
        else{widgetShowing = 0;}
    }





    private void submitCreateGroup(){
        if (createGroupNameEditText.getText().length() == 0){
            Snackbar.make(createGroupWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        createGroupNameEditText.setEnabled(false);
        createGroupButton.setEnabled(false);
        createFab.setEnabled(false); addFab.setEnabled(false);
//        createGroupProgress.setVisibility(View.VISIBLE);

        //start thread process
    }

    public void successCreateGroup(){
//        createGroupProgress.setVisibility(View.GONE);
//        createGroupCheck.setVisibility(View.VISIBLE);
    }

    private void submitAddGroup(){
        if (addGroupNameEditText.getText().length() == 0){
            Snackbar.make(addGroupWidget, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        addGroupNameEditText.setEnabled(false);
        addGroupButton.setEnabled(false);
        createFab.setEnabled(false); addFab.setEnabled(false);
//        addGroupProgress.setVisibility(View.VISIBLE);

        //start thread process
    }

    public void successAddGroup(){
//        addGroupProgress.setVisibility(View.GONE);
//        addGroupCheck.setVisibility(View.VISIBLE);
    }





    private void initUI(){
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initUI();
    }



}
