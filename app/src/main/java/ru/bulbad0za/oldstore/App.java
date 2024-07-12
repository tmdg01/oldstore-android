package ru.bulbad0za.oldstore;

import java.io.Serializable;
import java.util.List;

public class App implements Serializable {
    private String id;
    private String name;
    private String shortDescription;
    private String description;
    private String icon;
    private String file;
    private String category;
    private String minAndroidVersion;
    private String applicationVersion;
    private List<String> architectures;
    private List<Source> sources;

    public App(String id, String name, String shortDescription, String description, String icon, String file, String category, String minAndroidVersion, String applicationVersion, List<String> architectures, List<Source> sources) {
        this.id = id;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.icon = icon;
        this.file = file;
        this.category = category;
        this.minAndroidVersion = minAndroidVersion;
        this.applicationVersion = applicationVersion;
        this.architectures = architectures;
        this.sources = sources;
    }

    // Геттеры для всех полей
    public String getId() { return id; }
    public String getName() { return name; }
    public String getShortDescription() { return shortDescription; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public String getFile() { return file; }
    public String getCategory() { return category; }
    public String getMinAndroidVersion() { return minAndroidVersion; }
    public String getApplicationVersion() { return applicationVersion; }
    public List<String> getArchitectures() { return architectures; }
    public List<Source> getSources() { return sources; }
}
