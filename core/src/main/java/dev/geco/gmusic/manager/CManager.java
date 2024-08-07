package dev.geco.gmusic.manager;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.bukkit.configuration.file.*;

import dev.geco.gmusic.GMusicMain;

public class CManager {

    public String L_LANG;

    public boolean L_CLIENT_LANG;


    public boolean CHECK_FOR_UPDATE;

    public List<String> WORLDBLACKLIST = new ArrayList<>();


    public boolean S_EXTENDED_RANGE;

    public boolean S_FORCE_RESOURCES;


    public int JUKEBOX_RANGE;

    public int MAX_JUKEBOX_RANGE;


    public boolean A_SHOW_MESSAGES;

    public boolean A_SHOW_WHILE_PLAYING;


    public boolean R_ACTIVE;

    public boolean R_PLAY_ON_JOIN;


    public boolean ENVIRONMENT_EFFECTS;


    public int PS_TIME_UNTIL_SHUFFLE;

    public int PS_TIME_UNTIL_REPEAT;

    public boolean PS_SAVE_ON_QUIT;

    public int PS_D_PLAYLIST;

    public int PS_D_VOLUME;

    public boolean PS_D_JOIN;

    public int PS_D_PLAY_MODE;

    public boolean PS_D_PARTICLES;

    public boolean PS_D_REVERSE;


    public boolean G_DISABLE_RANDOM_SONG;

    public boolean G_DISABLE_PLAYLIST;

    public boolean G_DISABLE_OPTIONS;


    private final GMusicMain GPM;

    public CManager(GMusicMain GPluginMain) {

        GPM = GPluginMain;

        if(GPM.getSVManager().isNewerOrVersion(18, 2)) {
            try {
                File configFile = new File(GPM.getDataFolder(), "config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                InputStream configSteam = GPM.getResource("config.yml");
                if(configSteam != null) {
                    FileConfiguration configSteamConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configSteam, StandardCharsets.UTF_8));
                    if(!config.getKeys(true).equals(configSteamConfig.getKeys(true))) {
                        config.setDefaults(configSteamConfig);
                        YamlConfigurationOptions options = (YamlConfigurationOptions) config.options();
                        options.parseComments(true).copyDefaults(true).width(500);
                        config.loadFromString(config.saveToString());
                        for(String comments : config.getKeys(true)) config.setComments(comments, configSteamConfig.getComments(comments));
                        config.save(configFile);
                    }
                } else GPM.saveDefaultConfig();
            } catch (Throwable e) { GPM.saveDefaultConfig(); }
        } else GPM.saveDefaultConfig();

        reload();
    }

    public void reload() {

        GPM.reloadConfig();

        L_LANG = GPM.getConfig().getString("Lang.lang", "en_us").toLowerCase();
        L_CLIENT_LANG = GPM.getConfig().getBoolean("Lang.client-lang", true);

        CHECK_FOR_UPDATE = GPM.getConfig().getBoolean("Options.check-for-update", true);
        WORLDBLACKLIST = GPM.getConfig().getStringList("Options.WorldBlacklist");

        S_EXTENDED_RANGE = GPM.getConfig().getBoolean("Options.Sound.extened-range", true);
        S_FORCE_RESOURCES = GPM.getConfig().getBoolean("Options.Sound.force-resources", true);
        JUKEBOX_RANGE = GPM.getConfig().getInt("Options.jukebox-range", 50);
        MAX_JUKEBOX_RANGE = GPM.getConfig().getInt("Options.max-jukebox-range", 500);

        A_SHOW_MESSAGES = GPM.getConfig().getBoolean("Options.ActionBar.show-messages", true);
        A_SHOW_WHILE_PLAYING = GPM.getConfig().getBoolean("Options.ActionBar.show-while-playing", true);

        R_ACTIVE = GPM.getConfig().getBoolean("Options.Radio.active", true);
        R_PLAY_ON_JOIN = GPM.getConfig().getBoolean("Options.Radio.play-on-join", false);

        ENVIRONMENT_EFFECTS = GPM.getConfig().getBoolean("Options.environment-effects", true);

        PS_TIME_UNTIL_SHUFFLE = GPM.getConfig().getInt("Options.PlayerSettings.time-until-shuffle", 1000);
        PS_TIME_UNTIL_REPEAT = GPM.getConfig().getInt("Options.PlayerSettings.time-until-repeat", 1000);
        PS_SAVE_ON_QUIT = GPM.getConfig().getBoolean("Options.PlayerSettings.save-on-quit", true);
        PS_D_PLAYLIST = GPM.getConfig().getInt("Options.PlayerSettings.Default.playlist", 0);
        PS_D_VOLUME = GPM.getConfig().getInt("Options.PlayerSettings.Default.volume", 70);
        PS_D_JOIN = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.join", false);
        PS_D_PLAY_MODE = GPM.getConfig().getInt("Options.PlayerSettings.Default.play-mode", 0);
        PS_D_PARTICLES = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.particles", false);
        PS_D_REVERSE = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.reverse", false);

        G_DISABLE_RANDOM_SONG = GPM.getConfig().getBoolean("Options.GUI.disable-random-song", false);
        G_DISABLE_PLAYLIST = GPM.getConfig().getBoolean("Options.GUI.disable-playlist", false);
        G_DISABLE_OPTIONS = GPM.getConfig().getBoolean("Options.GUI.disable-options", false);
    }

}
