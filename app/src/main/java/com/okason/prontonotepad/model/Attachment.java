package com.okason.prontonotepad.model;

/**
 * Created by vokafor on 1/18/2017.
 */

public class Attachment {
    private String id;
    private String localUriPath;
    private String cloudPath;
    private String fileName;
    private String mime_type;

    public Attachment(String name, String path, String type){
        fileName = name;
        localUriPath = path;
        mime_type = type;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalUriPath() {
        return localUriPath;
    }

    public void setLocalUriPath(String localUriPath) {
        this.localUriPath = localUriPath;
    }

    public String getCloudPath() {
        return cloudPath;
    }

    public void setCloudPath(String cloudPath) {
        this.cloudPath = cloudPath;
    }

    public String getName() {
        return fileName;
    }

    public void setName(String fileName) {
        this.fileName = fileName;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }
}
