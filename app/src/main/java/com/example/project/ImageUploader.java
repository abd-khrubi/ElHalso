package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ImageUploader extends ListenableWorker {
    private CallbackToFutureAdapter.Completer<Result> callback;
    private Context appContext;
    private Business business;
    private Uri imageUri;
    private boolean isLogo;
    private static Gson gson = new Gson();

    private static final String TAG = "ImageUploader";

    public static void addImageUpload(Context appContext, Business business, Uri imageUri, boolean isLogo) {
        WorkManager workManager = WorkManager.getInstance(appContext);
        OneTimeWorkRequest.Builder imageUploadBuilder = new OneTimeWorkRequest.Builder(ImageUploader.class);
        Data.Builder data = new Data.Builder();
        Map<String, Object> map = new HashMap<>();
        map.put("business", gson.toJson(business));
        map.put("imageUri", imageUri.toString());
        map.put("isLogo", isLogo);
        data.putAll(map);
        imageUploadBuilder.setInputData(data.build());
        imageUploadBuilder.setConstraints(Constraints.NONE);
        String uniqueTaskName = "Upload" + imageUri.toString() + (isLogo ? "1" : "0");
        workManager.enqueueUniqueWork(uniqueTaskName, ExistingWorkPolicy.KEEP, imageUploadBuilder.build());
    }

    // todo: logo?
    public ImageUploader(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.appContext = appContext;
        Map<String, Object> map = workerParams.getInputData().getKeyValueMap();
        this.business = (Business) gson.fromJson((String) map.get("business"), Business.class);
        this.imageUri = Uri.parse((String) map.get("imageUri"));
        this.isLogo = (Boolean) map.get("isLogo");
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Result> completer) throws Exception {
                callback = completer;
                return null;
            }
        });

        uploadImage();
        return future;
    }

    private void uploadImage() {
        String imageFileName = DocumentFile.fromSingleUri(appContext, imageUri).getName();
        Log.d(TAG, "uploading image");

        if(business.getGallery().contains(imageFileName)){
            int idx = imageFileName.lastIndexOf('.');
            imageFileName = imageFileName.substring(0,idx) + "_1" + imageFileName.substring(idx);
        }
        final String imageName = imageFileName;
        // adding image to storage
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(business.getId() + "/" + imageName);
        ref.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(TAG, "putting storage file is " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.e(TAG, task.getException().toString());
//                    throw task.getException();
                }
//                try {
//                      todo: not getting permissions for copy..
//                    copyImageToAppDir(imageName);
//                } catch (Exception e) {
//                    Log.e(TAG, e.toString());
//                }
                if(isLogo) {
                    business.setLogo(imageName);
                    return FirebaseFirestore.getInstance().collection("business").document(business.getId()).update("logo", imageName);
                }
                else {
                    business.addImage(imageName);
                    return FirebaseFirestore.getInstance().collection("business").document(business.getId()).update("gallery", FieldValue.arrayUnion(imageName));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent();
                    intent.setAction(AppLoader.UPLOAD_BROADCAST);
                    intent.putExtra("business", business);
                    intent.putExtra("newImage", imageName);
                    intent.putExtra("isLogo", isLogo);
                    appContext.sendBroadcast(intent);

                    if(callback != null)
                        callback.set(Result.success());
                    Log.d(TAG, "image uploaded to storage and firestore successfully");
                } else {
                    Log.d(TAG, "image failed to upload to firestore");
                    Log.d(TAG, task.getException().toString());
                    if(callback != null)
                        callback.set(Result.failure());
                }
            }
        });
    }

    private void copyImageToAppDir(String imageName) throws IOException {
        File businessDir = new File(appContext.getFilesDir(), business.getId());
        if(!businessDir.exists()) {
            businessDir.mkdir();
        }
        File file = new File(businessDir, imageName);
        if(file.exists()){
            return;
        }

        InputStream input = appContext.getContentResolver().openInputStream(imageUri);
        Files.copy(input, file.toPath());
        input.close();
    }
}
