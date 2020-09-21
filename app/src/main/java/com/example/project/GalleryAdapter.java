package com.example.project;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract{
    private static final float FULL_ALPHA = 1.0f;
    private static final float SELECTED_ALPHA = 0.7f;
    private Business business;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<Integer> selectedImages;
    private ArrayList<Integer> imagesOrder;
    private GalleryImageListener clickListener;
    private StartDragListener startDragListener;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Business business, boolean isEditMode, StartDragListener startDragListener){
        this.business = business;
        this.selectedImages = new ArrayList<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
        this.imagesOrder = new ArrayList<>();
        for(int i=0;i<business.getGallery().size();i++)
            imagesOrder.add(i);
    }

    public void setImageClickListener(GalleryImageListener clickListener) {
        this.clickListener = clickListener;
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
        if(position >= business.getGallery().size())
            return;
        final int imageIndex = imagesOrder.get(position);
        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(imageIndex));
        // todo: set gallery image
        Log.d(TAG, "setting image " + business.getGallery().get(imageIndex) + " at position " + position);

//        holder.imageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                }
//                return true;
//            }
//        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "in onClick");
                if(selecting) {
                    if(selectedImages.contains(imageIndex)){
                        selectedImages.remove(selectedImages.indexOf(imageIndex));
                        holder.selectedBox.setChecked(false);
                        if(selectedImages.isEmpty()){
                            triggerSelecting();
                            return;
                        }
                    }
                    else {
                        selectedImages.add(imageIndex);
                        holder.selectedBox.setChecked(true);
                    }
                    notifyItemChanged(position);
                    return;
                }
                // todo: view image
                Log.d(TAG, "viewing image " + business.getGallery().get(imageIndex) + "at " + position);
            }
        };

        holder.imageView.setOnClickListener(clickListener);
        holder.selectedBox.setOnClickListener(clickListener);

        if(!isEditMode)
            return;

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(selecting) {
                    startDragListener.requestDrag(holder);
                    return true;
                }
                selectedImages.add(imageIndex);
                triggerSelecting();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return business.getGallery().size();
    }

    @Override
    public void onImageMoved(int fromPosition, int toPosition) {
        Collections.swap(imagesOrder, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onImageSelected(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(SELECTED_ALPHA);
    }

    @Override
    public void onImageClear(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(FULL_ALPHA);
    }
}
