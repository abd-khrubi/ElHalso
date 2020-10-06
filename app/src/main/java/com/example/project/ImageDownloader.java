package com.example.project;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.project.utils.ThreadingHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class ImageDownloader {
    public interface DownloadCallback{
        void onImageDownloaded(String businessID, String imageName);
    }

    private static Executor bgExecutor = Executors.newCachedThreadPool();
    private static ArrayList<DownloadCallback> callbacks = new ArrayList<>();
    private static FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final String TAG = "ImageDownloader";

    synchronized public static void getImage(String imageName, String businessID, boolean isTemp, File downloadFolder, DownloadCallback callback) {
        if(!callbacks.contains(callback))
            callbacks.add(callback);
        bgDownload(imageName, businessID, isTemp, downloadFolder);
    }

    synchronized private static void bgDownload(final String imageName, final String businessID, final boolean isTemp, final File downloadFolder) {
        ThreadingHelper.runAsyncInBackground(new Runnable() {
            @Override
            public void run() {
                File localFile;
                try {
                    localFile = new File(downloadFolder, imageName);
                    if(localFile.exists()){
                        Log.d(TAG, "file <"+imageName+"> already exists");
                        downloadDone(businessID, imageName);
                        return;
                    }
                    localFile = new File(downloadFolder, imageName);
                    if(isTemp)
                        localFile.deleteOnExit();
                } catch(Exception e){
                    Log.e(TAG, "Failed to create file.");
                    Log.e(TAG, e.toString());
                    return;
                }

                storage.getReference().child(businessID + "/" + imageName).getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "image <" + imageName +"> downloaded successfully");
                        }
                        else {
                            Log.d(TAG, "image failed to download");
                        }
                        downloadDone(businessID, imageName);
                    }
                });
            }
        });
    }

    private synchronized static void downloadDone(String businessID, String imageName) {
        for (DownloadCallback callback: callbacks) {
            if(callback != null)
                callback.onImageDownloaded(businessID, imageName);
        }
    }

    public static void cancelBusiness(String businessID) {

    }

    public static void removeCallback(DownloadCallback callback) {
        callbacks.remove(callback);
    }
}
