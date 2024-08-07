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

    private SVManager svManager;
    public SVManager getSVManager() { return svManager; }

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

    private BoxSongManager boxSongManager;
    public BoxSongManager getBoxSongManager() { return boxSongManager; }

    private JukeBoxManager jukeBoxManager;
    public JukeBoxManager getJukeBoxManager() { return jukeBoxManager; }

    private RadioManager radioManager;
    public RadioManager getRadioManager() { return radioManager; }

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

    private boolean supportsPaperFeature = false;
    public boolean supportsPaperFeature() { return supportsPaperFeature; }

    private boolean supportsTaskFeature = false;
    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public final String NAME = "GMusic";

    public final String RESOURCE = "84004";

    private static GMusicMain GPM;

    public static GMusicMain getInstance() { return GPM; }

    private void loadSettings(CommandSender Sender) {

        if(!connectDatabase(Sender)) return;

        getPlaySettingsManager().createTable();

        getSongManager().loadSongs();

        if(getCManager().R_ACTIVE) getRadioManager().playRadio();
    }

    private void linkBStats() {

        BStatsLink bstats = new BStatsLink(getInstance(), 4925);

        bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", () -> getCManager().L_LANG));
    }

    public void onLoad() {

        GPM = this;

        svManager = new SVManager(getInstance());
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
        boxSongManager = new BoxSongManager(getInstance());
        jukeBoxManager = new JukeBoxManager(getInstance());
        radioManager = new RadioManager(getInstance());

        musicUtil = new MusicUtil();

        preloadPluginDependencies();

        mManager = supportsPaperFeature() && getSVManager().isNewerOrVersion(18, 2) ? new MPaperManager(getInstance()) : new MSpigotManager(getInstance());
    }

    public void onEnable() {

        if(!versionCheck()) return;

        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        linkBStats();

        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        loadPluginDependencies(Bukkit.getConsoleSender());
        GPM.getUManager().checkForUpdates();
    }

    public void onDisable() {

        unload();
        getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void unload() {

        GPM.getRadioManager().stopRadio();
        GPM.getPlaySettingsManager().clearPlaySettingsCache();

        for(Player player : Bukkit.getOnlinePlayers()) {
            getPlaySettingsManager().setPlaySettings(player.getUniqueId(), getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
            getPlaySongManager().stopSong(player);
        }

        getDManager().close();
    }

    private void setupCommands() {

        getCommand("gmusic").setExecutor(new GMusicCommand(getInstance()));
        getCommand("gmusic").setTabCompleter(new GMusicTabComplete(getInstance()));
        getCommand("gmusic").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("agmusic").setExecutor(new GAMusicCommand(getInstance()));
        getCommand("agmusic").setTabCompleter(new AGMusicTabComplete(getInstance()));
        getCommand("agmusic").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
        getCommand("gmusicreload").setExecutor(new GMusicReloadCommand(getInstance()));
        getCommand("gmusicreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gmusicreload").setPermissionMessage(getMManager().getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {

        getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
        getServer().getPluginManager().registerEvents(new JukeBoxEvents(getInstance()), getInstance());
    }

    private void preloadPluginDependencies() {

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            supportsPaperFeature = true;
        } catch (ClassNotFoundException ignored) { supportsPaperFeature = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch (ClassNotFoundException ignored) { supportsTaskFeature = false; }
    }

    private void loadPluginDependencies(CommandSender Sender) { }

    public void reload(CommandSender Sender) {

        Bukkit.getPluginManager().callEvent(new GMusicReloadEvent(getInstance()));

        getCManager().reload();
        getMManager().loadMessages();

        unload();

        loadSettings(Sender);
        loadPluginDependencies(Sender);
        getUManager().checkForUpdates();
    }

    private boolean connectDatabase(CommandSender Sender) {

        boolean connect = getDManager().connect();

        if(connect) return true;

        getMManager().sendMessage(Sender, "Plugin.plugin-data");

        Bukkit.getPluginManager().disablePlugin(getInstance());

        return false;
    }

    private boolean versionCheck() {

        if(!getSVManager().isNewerOrVersion(17, 0)) {

            getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", getSVManager().getServerVersion());

            getUManager().checkForUpdates();

            Bukkit.getPluginManager().disablePlugin(getInstance());

            return false;
        }

        return true;
    }

}