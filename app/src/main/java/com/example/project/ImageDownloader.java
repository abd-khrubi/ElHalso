package com.example.project;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// todo: currently for 1 business
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
        bgExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File localFile = null;
                try {
                    localFile = new File(downloadFolder, imageName);
                    if(localFile.exists()){
                        Log.d(TAG, "file already exists");
                        downloadDone(businessID, imageName);
                        return;
                    }
                    localFile = new File(downloadFolder, imageName);
                    if(isTemp)
                        localFile.deleteOnExit();
//                    if(isTemp) {
//                        int idx = imageName.lastIndexOf('.');
//                        // todo: downloading same file twice would create 2 temp files..?
//                        File[] files = downloadDir.listFiles(new FilenameFilter() {
//                            @Override
//                            public boolean accept(File dir, String name) {
//                                if(name.contains(imageName))
//                                    return true;
//                                return false;
//                            }
//                        });
//                        if(files.length)
//                        localFile = downloadDir.createTempFile(imageName.substring(0, idx), imageName.substring(idx + 1));
//                        localFile.deleteOnExit();
//                    } else {
//                        localFile = new File(downloadDir, imageName);
//                        if(localFile.exists()){
//                            Log.d(TAG, "file already exists");
//                            downloadDone(imageName);
//                            return;
//                        }
//                    }
                }catch(Exception e){
                    Log.e(TAG, "Failed to create file.");
                    Log.e(TAG, e.toString());
                    return;
                }

                storage.getReference().child(businessID + "/" + imageName).getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "image downloaded successfully");
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
            callback.onImageDownloaded(businessID, imageName);
        }
    }

    public static void cancelAll() {
        callbacks.clear();
    }

    public static void removeCallback(DownloadCallback callback) {
        callbacks.remove(callback);
    }
}
