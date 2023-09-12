package dev.geco.gmusic;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.*;

import dev.geco.gmusic.api.event.*;
import dev.geco.gmusic.cmd.*;
import dev.geco.gmusic.cmd.tab.*;
import dev.geco.gmusic.events.*;
import dev.geco.gmusic.link.*;
import dev.geco.gmusic.manager.*;
import dev.geco.gmusic.manager.mm.*;
import dev.geco.gmusic.util.*;

public class GMusicMain extends JavaPlugin {

    private CManager cManager;
    public CManager getCManager() { return cManager; }

    private DManager dManager;
    public DManager getDManager() { return dManager; }

    private MidiManager midiManager;
    public MidiManager getMidiManager() { return midiManager; }

    private NBSManager nbsManager;
    public NBSManager getNBSManager() { return nbsManager; }

    private PlaySettingsManager playSettingsManager;
    public PlaySettingsManager getPlaySettingsManager() { return playSettingsManager; }

    private SongManager songManager;
    public SongManager getSongManager() { return songManager; }

    private PlaySongManager playSongManager;
    public PlaySongManager getPlaySongManager() { return playSongManager; }

    private UManager uManager;
    public UManager getUManager() { return uManager; }

    private PManager pManager;
    public PManager getPManager() { return pManager; }

    private TManager tManager;
    public TManager getTManager() { return tManager; }

    private MManager mManager;
    public MManager getMManager() { return mManager; }

    private MusicUtil musicUtil;
    public MusicUtil getMusicUtil() { return musicUtil; }

    private boolean spigotBased = false;
    public boolean isSpigotBased() { return spigotBased; }

    private boolean basicPaperBased = false;
    public boolean isBasicPaperBased() { return basicPaperBased; }

    private boolean paperBased = false;
    public boolean isPaperBased() { return paperBased; }

    public final String NAME = "GMusic";

    public final String RESOURCE = "84004";

    private static GMusicMain GPM;

    public static GMusicMain getInstance() { return GPM; }

    private void loadSettings(CommandSender Sender) {

        if(!connectDatabase(Sender)) return;

        getPlaySettingsManager().createTable();

        getSongManager().loadSongs();
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4925);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getCManager().L_LANG));
    }

    public void onLoad() {

        GPM = this;

        cManager = new CManager(getInstance());
        dManager = new DManager(getInstance());
        uManager = new UManager(getInstance());
        pManager = new PManager(getInstance());
        tManager = new TManager(getInstance());
        midiManager = new MidiManager(getInstance());
        nbsManager = new NBSManager(getInstance());
        playSettingsManager = new PlaySettingsManager(getInstance());
        songManager = new SongManager(getInstance());
        playSongManager = new PlaySongManager(getInstance());

        musicUtil = new MusicUtil();

        preloadPluginDependencies();

        mManager = isBasicPaperBased() ? new PMManager(getInstance()) : new SMManager(getInstance());
    }

    public void onEnable() {

        loadSettings(Bukkit.getConsoleSender());
        if(!versionCheck()) return;

        setupCommands();
        setupEvents();
        linkBStats();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        loadPluginDependencies(Bukkit.getConsoleSender());
        GPM.getUManager().checkForUpdates();
    }

    public void onDisable() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            getPlaySettingsManager().setPlaySettings(player.getUniqueId(), getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
            getPlaySongManager().stopSong(player);
        }

        getDManager().close();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void setupCommands() {

        getCommand("gmusic").setExecutor(new GMusicCommand(getInstance()));
        getCommand("gmusic").setTabCompleter(new GMusicTabComplete(getInstance()));
        getCommand("gmusic").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gamusic").setExecutor(new GAMusicCommand(getInstance()));
        getCommand("gamusic").setTabCompleter(new GAMusicTabComplete(getInstance()));
        getCommand("gamusic").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gmusicreload").setExecutor(new GMusicReloadCommand(getInstance()));
        getCommand("gmusicreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gmusicreload").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {

        getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
    }

    private void preloadPluginDependencies() {

        try {
            Class.forName("org.spigotmc.event.entity.EntityDismountEvent");
            spigotBased = true;
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            basicPaperBased = true;
        } catch (ClassNotFoundException ignored) { }

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            paperBased = true;
        } catch (ClassNotFoundException ignored) { }
    }

    private void loadPluginDependencies(CommandSender Sender) { }

    public void reload(CommandSender Sender) {

        Bukkit.getPluginManager().callEvent(new GMusicReloadEvent(getInstance()));

        getCManager().reload();
        getMManager().loadMessages();

        for(Player player : Bukkit.getOnlinePlayers()) {
            getPlaySettingsManager().setPlaySettings(player.getUniqueId(), getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
            getPlaySongManager().stopSong(player);
        }

        getDManager().close();

        loadSettings(Sender);
        loadPluginDependencies(Sender);
        GPM.getUManager().checkForUpdates();
    }

    private boolean connectDatabase(CommandSender Sender) {

        boolean connect = getDManager().connect();

        if(connect) return true;

        getMManager().sendMessage(Sender, "Plugin.plugin-data");

        Bukkit.getPluginManager().disablePlugin(getInstance());

        return false;
    }

    private boolean versionCheck() {

        if(!NMSManager.isNewerOrVersion(17, 0)) {

            String version = Bukkit.getServer().getClass().getPackage().getName();

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", version.substring(version.lastIndexOf('.') + 1));

            GPM.getUManager().checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

}