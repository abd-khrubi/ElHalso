package com.example.project;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageHolder extends RecyclerView.ViewHolder {
    public ImageButton imageBtn;
    public CheckBox selectedBox;
    public String image;

    public ImageHolder(@NonNull View itemView) {
        super(itemView);
        imageBtn = itemView.findViewById(R.id.imageItem);
        selectedBox = itemView.findViewById(R.id.selectedBox);
    }
}
