package net.exmo.exmodifier.content.resources;

import net.exmo.exmodifier.util.exSerialize.ExSerialize;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Objects;

public final class ConfigPackInfo {
    private  Path filePath;
    private  String name;
    private  String description;

    public String getVersion() {
        return version;
    }

    public ConfigPackInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public Path getFilePath() {
        return filePath;
    }

    public ConfigPackInfo setFilePath(Path filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getName() {
        return name;
    }

    public ConfigPackInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ConfigPackInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public ConfigPackInfo setAuthor(String author) {
        this.author = author;
        return this;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ConfigPackInfo setIcon(ResourceLocation icon) {
        this.icon = icon;
        return this;
    }

    private  String author;
    private  String version;
    private  ResourceLocation icon;

    public ConfigPackInfo(Path filePath, String name, String description, String author, String version,
                          ResourceLocation icon) {
        this.filePath = filePath;
        this.name = name;
        this.description = description;
        this.author = author;
        this.version = version;
        this.icon = icon;
    }
    public ConfigPackInfo(){}

    public Path filePath() {
        return filePath;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String author() {
        return author;
    }

    public String version() {
        return version;
    }

    public ResourceLocation icon() {
        return icon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ConfigPackInfo) obj;
        return Objects.equals(this.filePath, that.filePath) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.author, that.author) &&
                Objects.equals(this.version, that.version) &&
                Objects.equals(this.icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, name, description, author, version, icon);
    }

    @Override
    public String toString() {
        return "ConfigPackInfo[" +
                "filePath=" + filePath + ", " +
                "name=" + name + ", " +
                "description=" + description + ", " +
                "author=" + author + ", " +
                "version=" + version + ", " +
                "icon=" + icon + ']';
    }

    public static final ExSerialize<ConfigPackInfo> exSerialize = ExSerialize.create(ConfigPackInfo::new)
            .addStringField("name", ConfigPackInfo::name,  ConfigPackInfo::setName)
            .addStringField("description", ConfigPackInfo::description,  ConfigPackInfo::setDescription)
            .addStringField("author", ConfigPackInfo::author,  ConfigPackInfo::setAuthor)
            .addStringField("version", ConfigPackInfo::version, ConfigPackInfo::setVersion)
            .addResourceLocationField("icon", ConfigPackInfo::icon,  ConfigPackInfo::setIcon);

}
