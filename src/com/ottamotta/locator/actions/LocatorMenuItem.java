package com.ottamotta.locator.actions;

public class LocatorMenuItem implements Comparable<LocatorMenuItem> {

    private String title;
    private int order;
    private Runnable command;
    private int imageResourceId;

    public LocatorMenuItem(String title, int order, Runnable command) {
        this.title = title;
        this.order = order;
        this.command = command;
    }

    public void run() {
        command.run();
    }

    public String getTitle() {
        return title;
    }

    public int getOrder() {
        return order;
    }

    public Runnable getCommand() {
        return command;
    }

    public boolean hasImage() {
        return imageResourceId != 0;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    @Override
    public int compareTo(LocatorMenuItem another) {
        if (another == null) return 1;
        if (order > another.order) return 1;
        if (order < another.order) return -1;
        return 0;
    }
}
