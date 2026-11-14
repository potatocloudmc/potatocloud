package net.potatocloud.common.config;

public interface Config {

    void load();

    void save();

    void reload();

    ConfigNode get(String path);

    void set(String path, Object value);

    void remove(String path);

}
