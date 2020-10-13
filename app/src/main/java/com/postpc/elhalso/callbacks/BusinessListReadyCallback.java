package com.postpc.elhalso.callbacks;

import com.postpc.elhalso.data.Business;

import java.util.List;

public interface BusinessListReadyCallback {
    void onBusinessListReady(List<Business> businessList);
}
