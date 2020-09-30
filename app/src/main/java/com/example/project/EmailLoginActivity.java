package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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

        setSupportActionBar((Toolbar) findViewById(R.id.emailLoginToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setSubtitle("Email Login");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        Button resetBtn = (Button) findViewById(R.id.resetBtn);


        nameTxt.setVisibility(signupMode ? View.VISIBLE : View.GONE);
        pass2Txt.setVisibility(signupMode ? View.VISIBLE : View.GONE);
        resetBtn.setVisibility(!signupMode ? View.VISIBLE : View.GONE);

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

    public void resetPassword(View view) {
        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                        else {
                            Log.d(TAG, "Failed to send email. " + task.getException().toString());
                        }
                    }
                });
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void signMethod(View v){
        if(!validateDetails())
            return;

        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        String pass = ((EditText)findViewById(R.id.passTxt)).getText().toString();

        if(signupMode){
            signup(email, pass);
        }
        else {
            signin(email, pass);
        }
    }

    private void signin(String email, String password) {
        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().getUser().isEmailVerified()){
                                showMessage("Please verify your email first!");
                                return;
                            }
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

    private void signup(String email, String password) {
        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        final String name = ((EditText)findViewById(R.id.nameTxt)).getText().toString();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(new Continuation<AuthResult, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<AuthResult> task) throws Exception {
                        if(task.isSuccessful()){
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            return mFirebaseAuth.getCurrentUser().updateProfile(profile);
                        } else {
                            Log.d(TAG, "Authentication failed ", task.getException());
                            showMessage("Email already in use. Please choose a different one.");
                            return null;
                        }
                    }
                }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                if(task.isSuccessful()){
                    return mFirebaseAuth.getCurrentUser().sendEmailVerification();
                } else {
                    Log.d(TAG, "Failed to update user");
                    return null;
                }
            }
        }).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showMessage("Verification Email sent");
                } else {
                    Log.d(TAG, "Authentication failed", task.getException());
                    showMessage("Sign up failed. Please try again later.");
                }

            }
        });
    }
}