package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ConfigService {

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
    public int PS_D_PLAYLIST_MODE;
    public int PS_D_VOLUME;
    public boolean PS_D_JOIN;
    public int PS_D_PLAY_MODE;
    public boolean PS_D_PARTICLES;
    public boolean PS_D_REVERSE;
    public boolean G_DISABLE_RANDOM_SONG;
    public boolean G_DISABLE_PLAYLIST;
    public boolean G_DISABLE_OPTIONS;
    public boolean G_DISABLE_SEARCH;

    private final GMusicMain gMusicMain;

    public ConfigService(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;

        if(!gMusicMain.getVersionManager().isNewerOrVersion(18, 2)) {
            try {
                File configFile = new File(gMusicMain.getDataFolder(), "config.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                InputStream configSteam = gMusicMain.getResource("config.yml");
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
                } else gMusicMain.saveDefaultConfig();
            } catch(Throwable e) { gMusicMain.saveDefaultConfig(); }
        } else gMusicMain.saveDefaultConfig();

        reload();
    }

    public void reload() {
        gMusicMain.reloadConfig();

        L_LANG = gMusicMain.getConfig().getString("Lang.lang", "en_us").toLowerCase();
        L_CLIENT_LANG = gMusicMain.getConfig().getBoolean("Lang.client-lang", true);

        CHECK_FOR_UPDATE = gMusicMain.getConfig().getBoolean("Options.check-for-update", true);
        WORLDBLACKLIST = gMusicMain.getConfig().getStringList("Options.WorldBlacklist");

        S_EXTENDED_RANGE = gMusicMain.getConfig().getBoolean("Options.Sound.extened-range", true);
        S_FORCE_RESOURCES = gMusicMain.getConfig().getBoolean("Options.Sound.force-resources", true);

        JUKEBOX_RANGE = gMusicMain.getConfig().getInt("Options.jukebox-range", 50);
        MAX_JUKEBOX_RANGE = gMusicMain.getConfig().getInt("Options.max-jukebox-range", 500);

        A_SHOW_MESSAGES = gMusicMain.getConfig().getBoolean("Options.ActionBar.show-messages", true);
        A_SHOW_WHILE_PLAYING = gMusicMain.getConfig().getBoolean("Options.ActionBar.show-while-playing", true);

        R_ACTIVE = gMusicMain.getConfig().getBoolean("Options.Radio.active", true);
        R_PLAY_ON_JOIN = gMusicMain.getConfig().getBoolean("Options.Radio.play-on-join", false);

        ENVIRONMENT_EFFECTS = gMusicMain.getConfig().getBoolean("Options.environment-effects", true);

        PS_TIME_UNTIL_SHUFFLE = gMusicMain.getConfig().getInt("Options.PlayerSettings.time-until-shuffle", 1000);
        PS_TIME_UNTIL_REPEAT = gMusicMain.getConfig().getInt("Options.PlayerSettings.time-until-repeat", 1000);
        PS_SAVE_ON_QUIT = gMusicMain.getConfig().getBoolean("Options.PlayerSettings.save-on-quit", true);
        PS_D_PLAYLIST_MODE = gMusicMain.getConfig().getInt("Options.PlayerSettings.Default.playlist-mode", 0);
        PS_D_VOLUME = gMusicMain.getConfig().getInt("Options.PlayerSettings.Default.volume", 70);
        PS_D_JOIN = gMusicMain.getConfig().getBoolean("Options.PlayerSettings.Default.join", false);
        PS_D_PLAY_MODE = gMusicMain.getConfig().getInt("Options.PlayerSettings.Default.play-mode", 0);
        PS_D_PARTICLES = gMusicMain.getConfig().getBoolean("Options.PlayerSettings.Default.particles", false);
        PS_D_REVERSE = gMusicMain.getConfig().getBoolean("Options.PlayerSettings.Default.reverse", false);

        G_DISABLE_RANDOM_SONG = gMusicMain.getConfig().getBoolean("Options.GUI.disable-random-song", false);
        G_DISABLE_PLAYLIST = gMusicMain.getConfig().getBoolean("Options.GUI.disable-playlist", false);
        G_DISABLE_OPTIONS = gMusicMain.getConfig().getBoolean("Options.GUI.disable-options", false);
        G_DISABLE_SEARCH = gMusicMain.getConfig().getBoolean("Options.GUI.disable-search", false);
    }

}
