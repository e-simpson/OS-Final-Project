package final_project.oschat;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by evans on 12/14/2017.
 */

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.nameInput) EditText nameText;
    @BindView(R.id.completeSetupButton) Button setupButton;
    @BindView(R.id.parentView) LinearLayout LoginView;


    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        setupButton.setEnabled(false);
        onLoginSuccess();
    }

    public void onLoginSuccess() {
        setupButton.setEnabled(false);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", nameText.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void onLoginFailed() {
        setupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();

        if (name.isEmpty() || name.length() < 3 || name.length() > 14) {
            Snackbar.make(LoginView, "Enter a valid 3-14 character screen name", Snackbar.LENGTH_LONG).show();
            valid = false;
        } else {
            nameText.setError(null);
        }

        return valid;
    }



    //Override activity methods
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { login();}
        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
