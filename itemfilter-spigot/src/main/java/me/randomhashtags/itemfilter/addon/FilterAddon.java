package me.randomhashtags.itemfilter.addon;

import me.randomhashtags.itemfilter.universal.UVersionable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class FilterAddon implements UVersionable {
    protected File file;
    protected YamlConfiguration yml;
    public void load(File file) {
        if(file.exists()) {
            this.file = file;
            yml = YamlConfiguration.loadConfiguration(file);
        }
    }
    public File getFile() {
        return file;
    }
    public YamlConfiguration getYaml() {
        return yml;
    }
    public String getYamlName() {
        return file.getName().split("\\.yml")[0];
    }

    public void save() {
        try {
            yml.save(file);
            yml = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
