package com.example.project.callbacks;

import com.example.project.data.Business;

import java.util.List;

public interface BusinessListReadyCallback {
    void onBusinessListReady(List<Business> businessList);
}
