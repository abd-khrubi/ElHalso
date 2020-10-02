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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private MutableLiveData<Integer> selectedImagesSize;
    private boolean orderChanged;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Context context, ArrayList<String> gallery, File galleryFolder, boolean isEditMode, StartDragListener startDragListener){
        this.context = context;
        this.gallery = gallery;
        this.galleryFolder = galleryFolder;
        this.selectedImages = new ArrayList<>();
        this.selectedImagesSize = new MutableLiveData<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
        this.downloadedGallery = new ArrayList<>();
        this.orderChanged = false;
    }

    public boolean isOrderChanged() {
        return orderChanged;
    }

    public LiveData<Integer> getSelectedImagesSize() {
        return selectedImagesSize;
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
            selectedImagesSize.postValue(selectedImages.size());
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

        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);

        if(!downloadedGallery.contains(gallery.get(position))) {
            holder.imageProgress.setVisibility(View.VISIBLE);
            return;
        }

        holder.imageProgress.setVisibility(View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(gallery.get(position)));

        File file = new File(galleryFolder, gallery.get(position));
        Picasso.get().load(Uri.fromFile(file)).fit().into(holder.imageView);

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
                        selectedImagesSize.postValue(selectedImages.size());
                    }
                    else {
                        selectedImages.add(gallery.get(position));
                        selectedImagesSize.postValue(selectedImages.size());
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
                selectedImagesSize.postValue(selectedImages.size());
                triggerSelecting();
                return true;
            }
        });
    }

    private void viewImageInDefaultViewer(int position) {
        Log.d(TAG, "viewing image " + gallery.get(position) + " at adapter " + (position + 1));
        Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(galleryFolder, gallery.get(position)));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return gallery.size();
    }

    @Override
    public void onImageMoved(int fromPosition, int toPosition) {
        if(fromPosition != toPosition)
            orderChanged = true;
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
