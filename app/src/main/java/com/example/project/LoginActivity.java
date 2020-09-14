package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.WebDialog;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
//import com.firebase.ui.auth.AuthUI;
//import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {
    private CallbackManager mCallerbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private static final String TAG = "LoginAuthentication";
    private boolean isBusinessLogin;
    private static final int RC_GOOGLE_SIGN_IN = 901;
    private static final int RC_EMAIL_SIGN_IN = 902;
    private static final int BUSINESS_LOGO_ID = R.drawable.common_google_signin_btn_icon_disabled;
    private static final int REGULAR_LOGO_ID = R.drawable.common_google_signin_btn_icon_dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signOut();
        setupFacebookLogin();
        setupGoogleLogin();
        isBusinessLogin = false;
    }

    public void emailLogin(View v){
        Intent intent = new Intent(this, EmailLoginActivity.class);
        startActivityForResult(intent, RC_EMAIL_SIGN_IN);
    }

    public void showTypesDialog(View view){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_user_type, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final AlertDialog alertD = alertDialogBuilder.create();

        ImageButton regularBtn = (ImageButton) promptView.findViewById(R.id.regularImgBtn);
        ImageButton businessBtn = (ImageButton) promptView.findViewById(R.id.businessImgBtn);

        regularBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoginType(false);
                alertD.dismiss();
            }
        });
        businessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoginType(true);
                alertD.dismiss();
            }
        });

        alertD.show();
    }

    private void updateLoginType(boolean isBusiness){
        if(this.isBusinessLogin == isBusiness)
            return;

        ImageButton logoImg = (ImageButton) findViewById(R.id.logoImgBtn);
        this.isBusinessLogin = isBusiness;
        if(isBusiness){
            logoImg.setImageResource(BUSINESS_LOGO_ID);
        }
        else {
            logoImg.setImageResource(REGULAR_LOGO_ID);
        }
    }

    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_oauth2_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton googleSigninBtn = (SignInButton) findViewById(R.id.googleLoginBtn);
        googleSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            }
        });
    }

    private void setupFacebookLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallerbackManager = CallbackManager.Factory.create();
        LoginButton fbLoginBtn = (LoginButton) findViewById(R.id.fbLoginBtn);
        fbLoginBtn.registerCallback(mCallerbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Successfully authenticated to Facebook");
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Cancel authenticating to Facebook");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "Error authenticating to Facebook");
            }
        });
    }

    private void handleFacebookToken(AccessToken token){
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Sign in to Firebase with credential successful");
                    successfulLogin();
                }
                else{
                    Log.d(TAG, "Sign in to Firebase with credential failed");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallerbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.d(TAG, "Google sign in failed", e);
            }
        }
        else if(requestCode == RC_EMAIL_SIGN_IN && resultCode == RESULT_OK){
            Log.d(TAG, "signed in with email");
            successfulLogin();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            successfulLogin();
                        } else {
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void successfulLogin() {
        if(isBusinessLogin){
            
        }
        else {
            // start client login
        }
    }

}