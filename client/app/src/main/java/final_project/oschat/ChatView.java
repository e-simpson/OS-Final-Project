package final_project.oschat;

import android.content.Intent;
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

public class ChatView extends AppCompatActivity {
    @BindView(R.id.chatViewToolbar) Toolbar toolbar;
    Intent intent;



    private void uiInit(){
        intent = getIntent();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(intent.getStringExtra("chatroom_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_view);
        ButterKnife.bind(this);
        uiInit();
    }
}