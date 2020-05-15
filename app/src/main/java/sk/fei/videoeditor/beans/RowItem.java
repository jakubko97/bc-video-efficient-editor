package sk.fei.videoeditor.beans;

import android.graphics.Bitmap;

import java.io.File;

public class RowItem{
    private Bitmap imageId;
    private String title;
    private String desc;
    private File file;

    public RowItem(Bitmap imageId, String title, String desc, File file) {
        this.imageId = imageId;
        this.title = title;
        this.desc = desc;
        this.file = file;
    }
    public Bitmap getImageId() {
        return imageId;
    }
    public void setImageId(Bitmap imageId) {
        this.imageId = imageId;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title + "\n" + desc;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
