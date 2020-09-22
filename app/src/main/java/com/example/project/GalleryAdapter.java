package com.example.project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract{
    private static final float FULL_ALPHA = 1.0f;
    private static final float SELECTED_ALPHA = 0.7f;
    private ImageDrawer drawer;
    private ArrayList<String> gallery;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<String> selectedImages;
    private ArrayList<Integer> imagesOrder;
    private StartDragListener startDragListener;
    private File galleryFolder;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(ImageDrawer drawer, ArrayList<String> gallery, File galleryFolder, boolean isEditMode, StartDragListener startDragListener){
        this.drawer = drawer;
        this.gallery = gallery;
        this.galleryFolder = galleryFolder;
        this.selectedImages = new ArrayList<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
        this.imagesOrder = new ArrayList<>();
        for(int i=0;i<gallery.size();i++)
            imagesOrder.add(i);
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
    public void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {
        if(position >= gallery.size())
            return;
//        final int imageIndex = imagesOrder.get(position);

        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(gallery.get(position)));
        // todo: set gallery image
        if(gallery.get(position).charAt(0) == '#'){
            holder.imageView.setBackgroundColor(Color.parseColor(gallery.get(position).split("\\.")[0]));
            holder.textView.setText(gallery.get(position).split("\\.")[1]);
        }
        else {
            drawer.drawImage(holder, galleryFolder, gallery.get(position));
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selecting) {
                    if(selectedImages.contains(gallery.get(position))){
                        selectedImages.remove(gallery.get(position));
                        holder.selectedBox.setChecked(false);
                        if(selectedImages.isEmpty()){
                            triggerSelecting();
                            return;
                        }
                    }
                    else {
                        selectedImages.add(gallery.get(position));
                        holder.selectedBox.setChecked(true);
                    }
                    notifyItemChanged(position);
                    return;
                }
                // todo: view image
                Log.d(TAG, "viewing image " + gallery.get(position) + "at adapter " + (position + 1));
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
