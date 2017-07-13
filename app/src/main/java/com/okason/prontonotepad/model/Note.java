package com.okason.prontonotepad.model;

public class Note {
    private String noteId;
    private String title;
    private String content;
    private long dateCreated;
    private long dateModified;
    private long nextReminder;
    private String audioPath;
    private boolean cloudAudioExists;
    private String imagePath;
    private boolean cloudImageExists;
    private String sketchPath;
    private boolean cloudSketchExists;
    private String categoryName;
    private String categoryId;
    private String noteType;

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public long getNextReminder() {
        return nextReminder;
    }

    public void setNextReminder(long nextReminder) {
        this.nextReminder = nextReminder;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public boolean isCloudAudioExists() {
        return cloudAudioExists;
    }

    public void setCloudAudioExists(boolean cloudAudioExists) {
        this.cloudAudioExists = cloudAudioExists;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isCloudImageExists() {
        return cloudImageExists;
    }

    public void setCloudImageExists(boolean cloudImageExists) {
        this.cloudImageExists = cloudImageExists;
    }

    public String getSketchPath() {
        return sketchPath;
    }

    public void setSketchPath(String sketchPath) {
        this.sketchPath = sketchPath;
    }

    public boolean isCloudSketchExists() {
        return cloudSketchExists;
    }

    public void setCloudSketchExists(boolean cloudSketchExists) {
        this.cloudSketchExists = cloudSketchExists;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }
}
