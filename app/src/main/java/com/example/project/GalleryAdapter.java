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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract{
    private static final float FULL_ALPHA = 1.0f;
    private static final float SELECTED_ALPHA = 0.7f;

    private Context context;
    private ArrayList<String> gallery;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<String> selectedImages;
    private StartDragListener startDragListener;
    private File galleryFolder;

    private static final String TAG = "GalleryAdapter";

    private static final int IMAGE_HEIGHT = 125;
    private static final int IMAGE_WIDTH = 125;

    public GalleryAdapter(Context context, ArrayList<String> gallery, File galleryFolder, boolean isEditMode, StartDragListener startDragListener){
        this.context = context;
        this.gallery = gallery;
        this.galleryFolder = galleryFolder;
        this.selectedImages = new ArrayList<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
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

        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(gallery.get(position)));

        if(gallery.get(position).charAt(0) == '#'){
            holder.imageView.setImageBitmap(null);
            holder.imageView.setBackgroundColor(Color.parseColor(gallery.get(position).split("\\.")[0]));
            holder.textView.setText(gallery.get(position).split("\\.")[1]);
        }
        else {
            File file = new File(galleryFolder, gallery.get(position));
            Picasso.get().load(Uri.fromFile(file)).resize(IMAGE_WIDTH, IMAGE_HEIGHT).into(holder.imageView);
        }

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
                // todo: view image
                Log.d(TAG, "viewing image " + gallery.get(position) + " at adapter " + (position + 1));
                if(gallery.get(position).charAt(0) != '#'){
                    Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(galleryFolder, gallery.get(position)));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                }
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
