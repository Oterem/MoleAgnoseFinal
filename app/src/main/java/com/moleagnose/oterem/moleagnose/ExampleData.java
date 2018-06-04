package com.moleagnose.oterem.moleagnose;

/**
 * Created by oterem on 30/05/2018.
 */

public class ExampleData {

    private String name;
    private String url;
    private String imageUrl;

    public ExampleData(String name, String url, String imageUrl){
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {return imageUrl;}

    public String getUrl() {
        return url;
    }
}
