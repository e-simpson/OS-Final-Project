package final_project.oschat;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evans on 12/5/2017.
 */

public class ChatCreatorActivity extends AppCompatActivity {
    @BindView(R.id.chatCreatorToolbar) Toolbar toolbar;
    @BindView(R.id.createButton) Button createButton;
    @BindView(R.id.chatName) EditText groupNameEditText;
    @BindView(R.id.chatView) LinearLayout view;
    @BindView(R.id.progressCircle) ProgressBar progress;
    @BindView(R.id.progressCheck) ImageView check;



    private void uiInit(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat Creator");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {submitNewChatGroup();}
        });
    }

    public void groupCreateSuccess(){
        progress.setVisibility(View.GONE);
        check.setVisibility(View.VISIBLE);
    }

    private void submitNewChatGroup(){
        if (groupNameEditText.getText().length() == 0){
            Snackbar.make(view, "Enter a valid group name.", Snackbar.LENGTH_LONG).show();
            return;
        }
        groupNameEditText.setEnabled(false);
        createButton.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        //start thread process
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_creator);
        ButterKnife.bind(this);
        uiInit();
    }
}
