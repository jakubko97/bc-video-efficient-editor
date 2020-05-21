package sk.fei.videoeditor.beans;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Date;

public class RowItem implements Comparable<RowItem>{
    private Bitmap imageId;
    private String title;
    private String desc;
    private File file;
    private Date dateCreated;
    private String size;
    private boolean expanded;


    public RowItem(String title, String desc, File file, Date dateCreated) {
        this.imageId = imageId;
        this.title = title;
        this.desc = desc;
        this.file = file;
        this.dateCreated = dateCreated;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @Override
    public int compareTo(RowItem o) {
        if (getDateCreated() == null || o.getDateCreated() == null)
            return 0;
        return getDateCreated().compareTo(o.getDateCreated());
    }

    @Override
    public String toString() {
        return "RowItem{" +
                "imageId=" + imageId +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", file=" + file +
                ", dateCreated=" + dateCreated +
                ", size='" + size + '\'' +
                '}';
    }
}
