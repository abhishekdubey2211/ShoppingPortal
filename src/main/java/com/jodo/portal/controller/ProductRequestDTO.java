package com.jodo.portal.controller;
import java.util.List;

import com.jodo.portal.model.FilterCriteria;

public class ProductRequestDTO {
    private int page;
    private int size;
    private String sortBy;
    private String sortDir;
    private List<FilterCriteria> filterCriteriaList;

    // Getters and setters

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public List<FilterCriteria> getFilterCriteriaList() {
        return filterCriteriaList;
    }

    public void setFilterCriteriaList(List<FilterCriteria> filterCriteriaList) {
        this.filterCriteriaList = filterCriteriaList;
    }
}
