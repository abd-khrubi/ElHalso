package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.Files;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract {
    private static final float FULL_ALPHA = 1.0f;
    private static final float SELECTED_ALPHA = 0.7f;

    private Context context;
    private ArrayList<String> gallery;
    private ArrayList<String> downloadedGallery;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<String> selectedImages;
    private StartDragListener startDragListener;
    private File galleryFolder;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Context context, ArrayList<String> gallery, File galleryFolder, boolean isEditMode, StartDragListener startDragListener){
        this.context = context;
        this.gallery = gallery;
        this.galleryFolder = galleryFolder;
        this.selectedImages = new ArrayList<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
        this.downloadedGallery = new ArrayList<>();
    }

    public void addDownloadedImage(String imageName){
        if(!downloadedGallery.contains(imageName))
            downloadedGallery.add(imageName);
        Log.d(TAG, "adding image " + imageName);
        notifyItemChanged(gallery.indexOf(imageName));
    }

    public ArrayList<String> getSelectedImages() {
        return selectedImages;
    }

    public boolean getIsSelecting(){
        return selecting;
    }

    public void triggerSelecting() {
        if(!isEditMode)
            return;
        selecting = !selecting;
        if(!selecting) {
            selectedImages.clear();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public synchronized void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {
        if(position >= gallery.size())
            return;

        if(!downloadedGallery.contains(gallery.get(position)))
            return;

        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);
//        holder.selectedBox.setChecked(selectedImages.contains(gallery.get(position)));

        File file = new File(galleryFolder, gallery.get(position));
        if(!file.exists())
            Log.d(TAG, "file does not exist o.o");
        Log.d(TAG, "trying to draw " + gallery.get(position));
        Picasso.get().load(Uri.fromFile(file)).resize(R.dimen.gallery_image_width, R.dimen.gallery_image_height).into(holder.imageView);
//            FirebaseHandler.getInstance().fetchImageForBusinessIntoImageHolder(context, new Business("mpjMmySxxydICDPTlZ4k"), gallery.get(position), holder);
//            Picasso.get().load(gallery.get(position)).resize(R.dimen.gallery_image_width, R.dimen.gallery_image_height)
//                    .onlyScaleDown().placeholder(R.drawable.ic_action_syncing).into(holder.imageView);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selecting) {
                    if(selectedImages.contains(gallery.get(position))){
                        selectedImages.remove(gallery.get(position));
                        if(selectedImages.isEmpty()){
                            triggerSelecting();
                            return;
                        }
                    }
                    else {
                        selectedImages.add(gallery.get(position));
                    }
                    notifyItemChanged(position);
                    return;
                }
                viewImageInDefaultViewer(position);
            }
        };

        holder.imageView.setOnClickListener(clickListener);
        holder.selectedBox.setOnClickListener(clickListener);

        if(!isEditMode)
            return;

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(selecting && startDragListener != null) {
                    startDragListener.requestDrag(holder);
                    return true;
                }
                selectedImages.add(gallery.get(position));
                triggerSelecting();
                return true;
            }
        });
    }

    private void viewImageInDefaultViewer(int position) {
        // todo: view image
        Log.d(TAG, "viewing image " + gallery.get(position) + " at adapter " + (position + 1));
        Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(galleryFolder, gallery.get(position)));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
//        if(gallery.get(position).charAt(0) != '#'){
//            Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(galleryFolder, gallery.get(position)));
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            context.startActivity(intent);
//
//
//            // pulling image from picasso cache
//            final String CACHE_PATH = context.getCacheDir().getAbsolutePath() + "/picasso-cache/";
//            File[] files = new File(CACHE_PATH).listFiles();
//            for (File file:files)
//            {
//                String fname= file.getName();
//                if (fname.contains(".") && fname.substring(fname.lastIndexOf(".")).equals(".0"))
//                {
//                    try
//                    {
//                        BufferedReader br = new BufferedReader(new FileReader(file));
//                        if (br.readLine().equals(gallery.get(position)))
//                        {
//
//                            String image_path =  CACHE_PATH + fname.replace(".0", ".1");
//                            File curFile = new File(image_path);
//                            if (curFile.exists())
//                            {
//                                File tempImage = File.createTempFile("toview", ".jpg");
//                                if(!tempImage.exists())
//                                    tempImage.createNewFile();
//                                tempImage.deleteOnExit();
//                                Files.copy(curFile, tempImage);
//                                Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", tempImage);
//                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                                context.startActivity(intent);
//                            }
//                        }
//                    }
//                    catch (IOException e)
//                    {
//                        Log.e(TAG, "Failed to read/copy image from cache");
//                    }
//                }
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return gallery.size();
    }

    @Override
    public void onImageMoved(int fromPosition, int toPosition) {
        String toSwap = gallery.remove(fromPosition);
        gallery.add(toPosition, toSwap);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onImageSelected(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(SELECTED_ALPHA);
    }

    @Override
    public void onImageClear(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(FULL_ALPHA);
        notifyDataSetChanged();
    }
}
