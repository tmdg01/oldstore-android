package ru.bulbad0za.oldstore;

import java.io.Serializable;

public class Source implements Serializable {
    private String name;
    private String link;

    public Source(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() { return name; }
    public String getLink() { return link; }
}
