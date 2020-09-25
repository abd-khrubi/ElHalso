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

    public static class tuple {
        public String businessID;
        public String imageName;
        public File downloadFolder;
        public boolean isTemp;

        public tuple(String id, String image, File downloadFolder, boolean isTemp){
            this.businessID = id;
            this.imageName = image;
            this.downloadFolder = downloadFolder;
            this.isTemp = isTemp;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            tuple other = (tuple)obj;
            return businessID.equals(other.businessID) && imageName.equals(other.imageName);
//                    && downloadFolder.toString().equals(other.downloadFolder.toString());
        }
    }

    private static Executor bgExecutor = Executors.newCachedThreadPool();
    private static ArrayList<DownloadCallback> callbacks = new ArrayList<>();
    private static ArrayList<tuple> toDownload = new ArrayList<>();
    private static FirebaseStorage storage = FirebaseStorage.getInstance();
    private static boolean currentlyDownloading = false;
    // todo: sysmtem for what to download and when
    private static ArrayList<String> imagesToDownload;
    private static File downloadFolder;

    private static final String TAG = "ImageDownloader";

    synchronized public static void getImage(String imageName, String businessID, boolean isTemp, File downloadFolder, DownloadCallback callback) {
        if(!callbacks.contains(callback))
            callbacks.add(callback);
        tuple tup = new tuple(businessID, imageName, downloadFolder, isTemp);
        if(!toDownload.contains(tup)){
            toDownload.add(tup);
        }
        startDownload();
    }

    synchronized private static void startDownload() {
        if(!currentlyDownloading && toDownload.size() >= 1) {
            currentlyDownloading = true;
            bgDownload(toDownload.get(0));
        }
    }

    synchronized private static void bgDownload(final tuple tup) {
        Log.d(TAG, "downloading " + tup.imageName);
//        bgExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
                File localFile = null;
                try {
                    localFile = new File(tup.downloadFolder, tup.imageName);
                    if(localFile.exists()){
                        Log.d(TAG, "file already exists");
                        downloadDone(tup.businessID, tup.imageName);
                        return;
                    }
                    localFile = new File(tup.downloadFolder, tup.imageName);
                    if(tup.isTemp)
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

                storage.getReference().child(tup.businessID + "/" + tup.imageName).getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "image downloaded successfully");
                        }
                        else {
                            Log.d(TAG, "image failed to download");
                        }
                        downloadDone(tup.businessID, tup.imageName);
                        toDownload.remove(tup);
                        currentlyDownloading = false;
                        startDownload();
                    }
                });
//            }
//        });
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
