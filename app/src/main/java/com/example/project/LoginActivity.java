package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
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
        FirebaseUser fUser = mFirebaseAuth.getCurrentUser();
        final User user = new User(fUser.getUid(), fUser.getDisplayName(), fUser.getEmail());
        final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        final LiveData<Boolean> userUpdateDone = firebaseHandler.getUpdate();

        firebaseHandler.updateOrCreateFirebaseUser(user);

        // wait for user fetch to end
        userUpdateDone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                    return;
                userUpdateDone.removeObserver(this);
                ((AppLoader) getApplicationContext()).setUser(user);

                if(isBusinessLogin) {
                    // regular user log in
                }
                else {
                    // need to fetch business for user
                    LiveData<Boolean> businessUpdateDone = firebaseHandler.getUpdate();
                    firebaseHandler.fetchBusinessForUser(user);
                    businessUpdateDone.observe(LoginActivity.this, new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if (!aBoolean)
                                return;
                            userUpdateDone.removeObserver(this);
                            Business business = (Business) firebaseHandler.getUpdatedObject();
                            goToBusiness(business);
                        }
                    });
                }
            }
        });
    }

    private void goToBusiness(final Business business) {
        ((AppLoader) getApplicationContext()).setBusiness(business);
        Intent intent;
        if(business.getName() == null) {
             intent = new Intent(this, BusinessActivity.class);
        }
        else {
            intent = new Intent(this, EditBusinessActivity.class);
        }
        startActivity(intent);
        finish();
//        final LiveData<Boolean> update = FirebaseHandler.getInstance().getUpdate();
//        GeoPoint myLoc = new GeoPoint(32.818209, 35.250729);
//        FirebaseHandler.getInstance().fetchNearbyBusinesses(myLoc, 5);
//        update.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean aBoolean) {
//                if(!aBoolean)
//                    return;
//                update.removeObserver(this);
//                Log.d(TAG, "size: " + ((ArrayList<Business>) FirebaseHandler.getInstance().getUpdatedObject()).size());
//                for(Business business : (ArrayList<Business>) FirebaseHandler.getInstance().getUpdatedObject()){
//                    Log.d(TAG, business.getName());
//                }
//            }
//        });
//        FirebaseHandler.getInstance().fetchImageForBusiness(business, "oc.jpg", this.getFilesDir());
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.withAppendedPath(Uri.fromFile(this.getFilesDir()), business.getId()), "image/*");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivity(intent);
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 177);
//
//        if(requestCode == SELECT && resultCode == RESULT_OK) {
//            if(data.getClipData() != null) {
//                ArrayList<Uri> list = new ArrayList<>();
//
//                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
//                    list.add(data.getClipData().getItemAt(i).getUri());
//                }
//                Bitmap bitmap1 = null, bitmap2 = null;
//                try {
//                    bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), list.get(0));
//                    bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), list.get(1));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                bitmap1 = Bitmap.createScaledBitmap(bitmap1,150, 150, true);
//                ((ImageButton)findViewById(R.id.tempImg)).setImageBitmap(bitmap1);
//                bitmap2 = Bitmap.createScaledBitmap(bitmap2,150, 150, true);
//                ((ImageButton)findViewById(R.id.tempImg2)).setImageBitmap(bitmap2);
//            }
//            else if(data.getData() != null){
//                Uri uri = data.getData();
////                FirebaseHandler.getInstance().addImageToBusinessGallery(new Business(), uri, "image2.jpg");
//                Log.d(TAG, uri.getLastPathSegment());
//                Log.d(TAG, uri.getPath());
//                Log.d(TAG, DocumentFile.fromSingleUri(this, uri).getName());
////                Bitmap bitmap= BitmapFactory.decodeFile(uri.getPath());
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                bitmap = Bitmap.createScaledBitmap(bitmap,((ImageButton)findViewById(R.id.tempImg)).getWidth(), ((ImageButton)findViewById(R.id.tempImg)).getHeight(), true);
//                ((ImageButton)findViewById(R.id.tempImg)).setImageBitmap(bitmap);
////                ((ImageButton)findViewById(R.id.tempImg)).setImageURI(uri);
//            }
//        }
    }

}