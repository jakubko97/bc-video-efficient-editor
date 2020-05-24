package sk.fei.videoeditor.beans;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Album implements List<RowItem> {

    private ArrayList<RowItem> rowItems = new ArrayList<>();
    private File file;
    private String name;
    private Bitmap image;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setRowItems(ArrayList<RowItem> rowItems) {
        this.rowItems = rowItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Album() {
    }

    public ArrayList<RowItem> getRowItems() {
        return rowItems;
    }

    public int getSize() {
        return rowItems.size();
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return false;
    }

    @NonNull
    @Override
    public Iterator<RowItem> iterator() {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        return null;
    }

    @Override
    public boolean add(RowItem rowItem) {
        return false;
    }

    @Override
    public boolean remove(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends RowItem> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends RowItem> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public RowItem get(int index) {
        return null;
    }

    @Override
    public RowItem set(int index, RowItem element) {
        return null;
    }

    @Override
    public void add(int index, RowItem element) {

    }

    @Override
    public RowItem remove(int index) {
        return null;
    }

    @Override
    public int indexOf(@Nullable Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(@Nullable Object o) {
        return 0;
    }

    @NonNull
    @Override
    public ListIterator<RowItem> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<RowItem> listIterator(int index) {
        return null;
    }

    @NonNull
    @Override
    public List<RowItem> subList(int fromIndex, int toIndex) {
        return null;
    }
}
