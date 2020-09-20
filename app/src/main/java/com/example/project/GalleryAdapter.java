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

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract{
    private Business business;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<Integer> selectedImages;
    private GalleryImageListener clickListener;
    private StartDragListener startDragListener;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Business business, boolean isEditMode, StartDragListener startDragListener){
        this.business = business;
        this.selectedImages = new ArrayList<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
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
        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(position));
        // todo: set gallery image
        Log.d(TAG, "setting image " + business.getGallery().get(position) + " at position " + position);

        holder.imageBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_MOVE) {
                    startDragListener.requestDrag(holder);
                }
                return false;
            }
        });
//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "in onClick");
//                if(selecting) {
//                    if(selectedImages.contains(position)){
//                        selectedImages.remove(selectedImages.indexOf(position));
//                        holder.selectedBox.setChecked(false);
//                        if(selectedImages.isEmpty()){
//                            triggerSelecting();
//                            return;
//                        }
//                    }
//                    else {
//                        selectedImages.add(position);
//                        holder.selectedBox.setChecked(true);
//                    }
//                    notifyItemChanged(position);
//                    return;
//                }
//                // todo: view image
//                Log.d(TAG, "viewing image at " + position);
//            }
//        };
//
//        holder.imageBtn.setOnClickListener(clickListener);
//        holder.selectedBox.setOnClickListener(clickListener);
//
//        if(!isEditMode)
//            return;
//
//        holder.imageBtn.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Log.d(TAG, "in onLongClick");
//                selectedImages.add(position);
//                holder.selectedBox.setChecked(true);
//                triggerSelecting();
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return business.getGallery().size();
    }

    @Override
    public void onImageMoved(int fromPosition, int toPosition) {
        Log.d(TAG, "moved image " + fromPosition + " to " + toPosition);
    }

    @Override
    public void onImageSelected(ImageHolder imageHolder) {
        Log.d(TAG, "selected image");
    }

    @Override
    public void onImageClear(ImageHolder imageHolder) {
        Log.d(TAG, "clear image");
    }
}
