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

        if(savedInstanceState != null){
            ((TextView)findViewById(R.id.emailTxt)).setText(savedInstanceState.getString("email", null));
            ((TextView)findViewById(R.id.nameTxt)).setText(savedInstanceState.getString("name", null));
            ((TextView)findViewById(R.id.passTxt)).setText(savedInstanceState.getString("pass", null));
            ((TextView)findViewById(R.id.pass2Txt)).setText(savedInstanceState.getString("pass2", null));
            signupMode = !savedInstanceState.getBoolean("signupMode", true);
        }
        changeSignMode(null);

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
        TextView emailTxt = (TextView) findViewById(R.id.emailTxt);
        TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
        TextView passTxt = (TextView) findViewById(R.id.passTxt);
        TextView pass2Txt = (TextView) findViewById(R.id.pass2Txt);
        TextView changeSignBtn = (Button) findViewById(R.id.changeSignBtn);
        Button signBtn = (Button) findViewById(R.id.signBtn);
        Button resetBtn = (Button) findViewById(R.id.resetBtn);

        signupMode = !signupMode;

        nameTxt.setVisibility(signupMode ? View.VISIBLE : View.GONE);
        pass2Txt.setVisibility(signupMode ? View.VISIBLE : View.GONE);
        resetBtn.setVisibility(!signupMode ? View.VISIBLE : View.GONE);

        ConstraintSet set = new ConstraintSet();
        if(signupMode){
            set.connect(nameTxt.getId(), ConstraintSet.TOP, emailTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(passTxt.getId(), ConstraintSet.TOP, nameTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(pass2Txt.getId(), ConstraintSet.TOP, passTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(signBtn.getId(), ConstraintSet.TOP, pass2Txt.getId(), ConstraintSet.BOTTOM, 20);
            signBtn.setText("Sign up");
            changeSignBtn.setText("Sign in");
        }
        else {
            set.connect(passTxt.getId(), ConstraintSet.TOP, emailTxt.getId(), ConstraintSet.BOTTOM, 20);
            set.connect(signBtn.getId(), ConstraintSet.TOP, passTxt.getId(), ConstraintSet.BOTTOM, 20);
            signBtn.setText("Sign in");
            changeSignBtn.setText("Sign up");
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
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches()){
            showMessage("Email address is invalid.");
            return;
        }
        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Reset Password", "Sending verification email...");
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                        if (task.isSuccessful()) {
                            showMessage("Email sent!");
                            Log.d(TAG, "Email sent.");
                        }
                        else {
                            showMessage("Failed to send email");
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

        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Signing in", "Connecting to " + getString(R.string.app_name) + "...");
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                        if(task.isSuccessful()) {
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

        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Signing up", "Creating " + getString(R.string.app_name) + " user...");
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
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            Log.d(TAG, "Authentication failed ", task.getException());
//                            throw new Exception();
                            showMessage("Email already in use. Please choose a different one.");
                            return null;
                        }
                    }
                }).continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if(task.getException() instanceof NullPointerException){
                            return null;
                        }
                        if(task.isSuccessful()){
                            ((AppLoader)getApplicationContext()).showLoadingDialog(EmailLoginActivity.this, "Signing up", "Sending verification email...");
                            return mFirebaseAuth.getCurrentUser().sendEmailVerification();
                        } else {
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            showMessage("Failed to create user data");
                            Log.d(TAG, "Failed to update user");
                            return null;
                        }
                    }
                }).addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.getException() instanceof NullPointerException){
                            return;
                        }
                        if (task.isSuccessful()) {
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            showMessage("Verification Email sent");
                        } else {
                            Log.d(TAG, "Authentication failed", task.getException());
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            showMessage("Sending verification email failed");
                        }

                    }
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", ((TextView)findViewById(R.id.emailTxt)).getText().toString());
        outState.putString("name", ((TextView)findViewById(R.id.nameTxt)).getText().toString());
        outState.putString("pass", ((TextView)findViewById(R.id.passTxt)).getText().toString());
        outState.putString("pass2", ((TextView)findViewById(R.id.pass2Txt)).getText().toString());
        outState.putBoolean("signupMode", signupMode);
    }
}