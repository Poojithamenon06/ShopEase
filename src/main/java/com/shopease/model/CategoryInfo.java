package com.shopease.model;

/** Simple holder used for the category tiles on the home page. */
public class CategoryInfo {
    private final String name;
    private final String emoji;

    public CategoryInfo(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
}
