package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class EmailLoginActivity extends AppCompatActivity {

    private boolean signupMode;
    private static final String EMAIL_REGEX = "^(.+)@(.+)\\.(.+)$";
    private static final String TAG = "EmailSign";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        signupMode = true;
        changeSignMode(findViewById(R.id.signinBtn));
    }

    public void changeSignMode(View v){
        boolean toSignupMode = v.getId() == R.id.signupBtn;
        if(signupMode == toSignupMode)
            return;

        signupMode = toSignupMode;
        TextView emailTxt = (TextView) findViewById(R.id.emailTxt);
        TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
        TextView passTxt = (TextView) findViewById(R.id.passTxt);
        TextView pass2Txt = (TextView) findViewById(R.id.pass2Txt);
        Button signBtn = (Button) findViewById(R.id.signBtn);

        nameTxt.setVisibility(signupMode ? View.VISIBLE : View.GONE);
        pass2Txt.setVisibility(signupMode ? View.VISIBLE : View.GONE);

        ConstraintLayout layout = findViewById(R.id.emailLayout);
        ConstraintSet set = new ConstraintSet();
        if(signupMode){
            set.connect(nameTxt.getId(), ConstraintSet.TOP, emailTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(passTxt.getId(), ConstraintSet.TOP, nameTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(pass2Txt.getId(), ConstraintSet.TOP, passTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(signBtn.getId(), ConstraintSet.TOP, pass2Txt.getId(), ConstraintSet.BOTTOM, 20);
            signBtn.setText("Sign up");
        }
        else {
            set.connect(passTxt.getId(), ConstraintSet.TOP, emailTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(signBtn.getId(), ConstraintSet.TOP, passTxt.getId(), ConstraintSet.BOTTOM, 20);
            signBtn.setText("Sign in");
        }
    }

    private boolean validateDetails(){
        TextView emailTxt = (TextView) findViewById(R.id.emailTxt);
        TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
        TextView passTxt = (TextView) findViewById(R.id.passTxt);
        TextView pass2Txt = (TextView) findViewById(R.id.pass2Txt);

        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(emailTxt.getText().toString());
        if(!matcher.matches()){
            showMessage("Email address is invalid.");
            return false;
        }

        if(passTxt.getText().toString().length() < 6){
            showMessage("Password length is too short.");
            return false;
        }
        if(signupMode && !pass2Txt.getText().toString().equals(passTxt.getText().toString())){
            showMessage("Passwords do not match.");
            return false;
        }
        if(signupMode && nameTxt.getText().toString().length() < 3){
            showMessage("Name is too short.");
            return false;
        }

        return true;
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void signMethod(View v){
        if(!validateDetails())
            return;

        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        String pass = ((EditText)findViewById(R.id.passTxt)).getText().toString();
        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        if(signupMode){
            final String name = ((EditText)findViewById(R.id.nameTxt)).getText().toString();
            mFirebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Sign up success");

                                // updating user name
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                mFirebaseAuth.getCurrentUser().updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                            Log.d(TAG, "Name update success");
                                        else
                                            Log.d(TAG, "Name update failed");

                                        Intent intent = new Intent();
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                    }
                                });
                            } else {
                                Log.d(TAG, "Authentication failed", task.getException());
                                showMessage("Sign up failed. Please try again later.");
                            }

                        }
                    });
        }
        else {
            mFirebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Authentication success");
                                Intent intent = new Intent();
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            } else {
                                Log.d(TAG, "Authentication failed", task.getException());
                                showMessage("Authentication failed!");
                            }
                        }
                    });
        }

    }
}