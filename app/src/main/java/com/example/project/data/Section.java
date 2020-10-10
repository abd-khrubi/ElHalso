package com.example.project.data;

import java.util.List;

public class Section {
    private String sectionName;
    private List<BusinessRow> sectionItems;

    public Section(String sectionName, List<BusinessRow> sectionItems) {
        this.sectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public List<BusinessRow> getSectionItems() {
        return sectionItems;
    }

    public void setSectionItems(List<BusinessRow> sectionItems) {
        this.sectionItems = sectionItems;
    }

}
