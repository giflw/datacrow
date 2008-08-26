package net.datacrow.core.web.model;

import net.datacrow.core.web.DcSecured;

public abstract class Sortable extends DcSecured {
    
    private String sort;
    private boolean ascending;

    protected Sortable(String defaultSortColumn) {
        sort = defaultSortColumn;
        ascending = isDefaultAscending(defaultSortColumn);
    }

    protected abstract void sort(String column, boolean ascending);

    protected boolean isDefaultAscending(String sortColumn) {
        return true;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void sort(String sortColumn) {
        if (sortColumn == null) 
            throw new IllegalArgumentException("Argument sortColumn must not be null.");

        if (sort.equals(sortColumn)) {
            ascending = !ascending;
        } else {
            sort = sortColumn;
            ascending = isDefaultAscending(sort);
        }

        sort(sort, ascending);
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
}
