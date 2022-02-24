package tech.zerofiltre.blog.domain;

import java.util.*;

public class Page<T> {

    private int pageSize;
    private int pageNumber;
    private int numberOfElements;
    private long totalNumberOfElements;
    private int totalNumberOfPages;
    private List<T> content = new ArrayList<>();
    private boolean hasNext;
    private boolean hasPrevious;

    public Page() {
    }

    public Page(int pageSize, int pageNumber, int numberOfElements, int totalNumberOfElements, int totalNumberOfPages, List<T> content, boolean hasNext, boolean hasPrevious) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.numberOfElements = numberOfElements;
        this.totalNumberOfElements = totalNumberOfElements;
        this.totalNumberOfPages = totalNumberOfPages;
        if (content != null)
            this.content = content;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

    public void setTotalNumberOfElements(long totalNumberOfElements) {
        this.totalNumberOfElements = totalNumberOfElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public int getTotalNumberOfPages() {
        return totalNumberOfPages;
    }

    public void setTotalNumberOfPages(int totalNumberOfPages) {
        this.totalNumberOfPages = totalNumberOfPages;
    }
}
