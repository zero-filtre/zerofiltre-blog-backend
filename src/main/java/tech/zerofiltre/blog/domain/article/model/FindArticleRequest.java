package tech.zerofiltre.blog.domain.article.model;

import tech.zerofiltre.blog.domain.user.model.*;

public class FindArticleRequest {
    private int pageNumber;
    private int pageSize;
    private Status status;
    private String tag;
    private User user;
    private boolean byPopularity;
    private boolean yours;

    public FindArticleRequest() {
    }

    public FindArticleRequest(int pageNumber, int pageSize, Status status, User user) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.status = status;
        this.user = user;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isYours() {
        return yours;
    }

    public void setYours(boolean yours) {
        this.yours = yours;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isByPopularity() {
        return byPopularity;
    }

    public void setByPopularity(boolean byPopularity) {
        this.byPopularity = byPopularity;
    }
}
