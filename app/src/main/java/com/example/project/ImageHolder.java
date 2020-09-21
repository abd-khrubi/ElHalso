package com.example.project;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageHolder extends RecyclerView.ViewHolder{
    public ImageView imageView;
    public CheckBox selectedBox;
    public TextView textView;
    public String image;

    public ImageHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageItem);
        selectedBox = itemView.findViewById(R.id.selectedBox);
        textView = itemView.findViewById(R.id.textView);
    }
}
