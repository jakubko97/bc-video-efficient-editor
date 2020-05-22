package sk.fei.videoeditor.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Directory {

    private List<Directory> files = new ArrayList<>();
    private String name;
    private File file;
    private boolean hasVideo = false;

    public boolean isHasVideo() {
        return hasVideo;
    }

    public void setHasVideo(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


//    public void addFiles(List<File> files){
//        files.add(files);
//    }


    public List<Directory> getFiles() {
        return files;
    }

    public void setFiles(List<Directory> files) {
        this.files = files;
    }

    public Directory() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
