package com.example.project;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncOperations {

    private Runnable getUIwork(){
        return new Runnable() {
            @Override
            public void run() {
                // UI work (usually stuff like myTextView.setText(...) and such)
            }
        };
    }

    void sendToMainThread(){
        Runnable runMe = getUIwork(); // create work ...
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(runMe);
        // or
        long millisecondsInTheFuture = 3000;
        handler.postDelayed(runMe, millisecondsInTheFuture);
    }

    private Runnable getBackgroundWork(){
        return new Runnable() {
            @Override
            public void run() {
                // async work here
            }
        };
    }

    void asyncThread(){
        Thread thread = new Thread(getBackgroundWork());
        thread.start();

        // pros: easy to write. "fire and forget"
        // cons: can't be stopped, a lot of overhead launching a new thread for each work
    }

    void executor(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(getBackgroundWork());

        // don't forget to shut down when finished:

        executor.shutdown();
        // OR
        List<Runnable> allWorkNotExecuted = executor.shutdownNow();


        // pros:
        // can be stopped - good for activities to avoid memory leaks
        // overhead is just when first launching
    }

    void moreExecutors(){
        ExecutorService e;

        e = Executors.newFixedThreadPool(5);
        // always 5 threads

        e = Executors.newCachedThreadPool();
        // start with 0 threads, create more if needed, if quite long enough - start killing threads
        // good if you have many short-lived runnables

        e = Executors.newSingleThreadExecutor();
        // one thread
        // good if you need to assure a FIFO behaviour of your runnables (e.g. don't start a new one until the previous is completely done)


        // and there are more... type "Executors." and let android studio complete you
    }



    // there are many types of schedulers, we will see just the single-threaded version
    void schedulerExecutor(){
        ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();

        // than you have:
        e.execute(getBackgroundWork());

        // and also
        e.schedule(getBackgroundWork(), 15, TimeUnit.SECONDS);
        // to decide when to run a work
    }



    static class TextFileDownloadTask extends AsyncTask<String, Integer, String> {
        // async task is generic on :
        // Params - the type of parameter it receives when created (in our case: String, the text-file url to bgDownload)
        // Progress - the type of event it can send when progresses through the mission (in our case: int, from 0 to 100 how much of the file it has downloaded)
        // Result - the type of result it sends back (in our case: String, the text in the file)



        // called from a BG thread
        @Override
        protected String doInBackground(String... strings) {
            String fileUrl = strings[0];

            // mimic going to the web... downloadin the file...
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}

                publishProgress(i);
            }

            // mimic reading the file...
            String fileContent = "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much.";

            return fileContent;
        }

        @Override
        protected void onPreExecute() {
            // do here what you want - you're in the UI thread
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            // do here what you want - you're in the UI thread
        }

        @Override
        protected void onPostExecute(String result) {
            // do here what you want - you're in the UI thread
        }

        @Override
        protected void onCancelled() {
            // do here what you want - you're in the UI thread
        }
    }

    void asyncTask(){
        TextFileDownloadTask task = new TextFileDownloadTask();
        task.execute("http://www.hogwartsishere.com/library/book/7107/chapter/1/"); // bgDownload harry potter 1

        // when wanting to kill the task -
        task.cancel(true);

        // pros:
        //      very specific to Activity, very easy to handle
        // cons:
        //      can't run more than one task in parallel
        //      HUGE amount of launch overhead and thread switching
        //      it was developed to help activities but doesn't really support orientation change or anything else from the activity lifecycle

    }







    // classic example:

    // say we want from our activity to bgDownload a file.
    // we will create:
    // 1. FileDownloader class, to be referenced from the app
    // can look like this:

    static class FileDownloader {
        interface DownloadCallback {
            void onFileDownloaded(String fileUrl, String content);
        }

        Executor executor = Executors.newSingleThreadExecutor(); // some executor
        ArrayList<DownloadCallback> callbacks = new ArrayList<>(); // store all callbacks

        synchronized public void getFile(final String fileUrl, DownloadCallback callback) {
            callbacks.add(callback);
            bgDownload(fileUrl);
        }

        private void bgDownload(final String fileUrl) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // get the file... somehow...
                    // long blocking work is here...
                    String fileContent = "got some file content";
                    downloadDone(fileUrl, fileContent);
                }
            });
        }

        private synchronized void downloadDone(String fileUrl, String fileContent) {
            for (DownloadCallback callback: callbacks) {
                callback.onFileDownloaded(fileUrl, fileContent);
            }
        }

        public void removeCallback(DownloadCallback callback) {
            callbacks.remove(callback);
        }
    }


    // custom application like this:
    class MyApp extends Application {
        FileDownloader downloader;

        @Override
        public void onCreate() {
            super.onCreate();
            downloader = new FileDownloader();
        }
    }

    // and in your activity you will see something like this:

    class MyActivity extends Activity implements FileDownloader.DownloadCallback {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // ... blah blah blah ...

            FileDownloader downloader = ((MyApp) getApplicationContext()).downloader;


            downloader.getFile("https://wiki.com/harry_potter_1.txt", this);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            FileDownloader downloader = ((MyApp) getApplicationContext()).downloader;
            downloader.removeCallback(this);
        }

        @Override
        public void onFileDownloaded(String fileUrl, String content) {
            // we are in a bg thread here!
            // but it's ok bcs activities inheirt this cool method:
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // yay now we are in the UI thread!
                }
            });
        }
    }
}