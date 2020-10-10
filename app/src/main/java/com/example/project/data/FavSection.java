package com.example.project.data;

import com.example.project.data.Business;

import java.util.List;

public class FavSection {
    private String sectionName;
    private List<Business> sectionItems;

    public FavSection(String sectionName, List<Business> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<Business> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<Business> sectionItems) {
        this.sectionItems = sectionItems;
    }
}
