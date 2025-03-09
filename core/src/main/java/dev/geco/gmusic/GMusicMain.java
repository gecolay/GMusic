package dev.geco.gmusic;

import dev.geco.gmusic.api.event.GMusicLoadedEvent;
import dev.geco.gmusic.api.event.GMusicReloadEvent;
import dev.geco.gmusic.cmd.GAMusicCommand;
import dev.geco.gmusic.cmd.GMusicCommand;
import dev.geco.gmusic.cmd.GMusicReloadCommand;
import dev.geco.gmusic.cmd.tab.EmptyTabComplete;
import dev.geco.gmusic.cmd.tab.GAMusicTabComplete;
import dev.geco.gmusic.cmd.tab.GMusicTabComplete;
import dev.geco.gmusic.event.JukeBoxEventHandler;
import dev.geco.gmusic.event.PlayerEventHandler;
import dev.geco.gmusic.metric.BStatsMetric;
import dev.geco.gmusic.service.BoxSongService;
import dev.geco.gmusic.service.ConfigService;
import dev.geco.gmusic.service.DataService;
import dev.geco.gmusic.service.JukeBoxService;
import dev.geco.gmusic.service.MessageService;
import dev.geco.gmusic.service.MidiService;
import dev.geco.gmusic.service.NBSService;
import dev.geco.gmusic.service.PermissionService;
import dev.geco.gmusic.service.PlaySettingsService;
import dev.geco.gmusic.service.PlaySongService;
import dev.geco.gmusic.service.RadioService;
import dev.geco.gmusic.service.SongService;
import dev.geco.gmusic.service.TaskService;
import dev.geco.gmusic.service.UpdateService;
import dev.geco.gmusic.service.VersionService;
import dev.geco.gmusic.service.message.PaperMessageService;
import dev.geco.gmusic.service.message.SpigotMessageService;
import dev.geco.gmusic.util.MusicUtil;
import dev.geco.gmusic.util.SteroNoteUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class GMusicMain extends JavaPlugin {

    public static final String NAME = "GMusic";
    public static final String RESOURCE_ID = "000000";

    private final int BSTATS_RESOURCE_ID = 4925;
    private static GMusicMain gMusicMain;
    private VersionService versionService;
    private ConfigService configService;
    private MessageService messageService;
    private UpdateService updateService;
    private PermissionService permissionService;
    private TaskService taskService;
    private DataService dataService;
    private SongService songService;
    private PlaySongService playSongService;
    private PlaySettingsService playSettingsService;
    private BoxSongService boxSongService;
    private JukeBoxService jukeBoxService;
    private RadioService radioService;
    private MidiService midiService;
    private NBSService nbsService;
    private MusicUtil musicUtil;
    private SteroNoteUtil steroNoteUtil;
    private boolean supportsPaperFeature = false;
    private boolean supportsTaskFeature = false;

    public static GMusicMain getInstance() { return gMusicMain; }

    public VersionService getVersionManager() { return versionService; }

    public ConfigService getConfigService() { return configService; }

    public MessageService getMessageService() { return messageService; }

    public UpdateService getUpdateService() { return updateService; }

    public PermissionService getPermissionService() { return permissionService; }

    public TaskService getTaskService() { return taskService; }

    public DataService getDataService() { return dataService; }

    public SongService getSongService() { return songService; }

    public PlaySongService getPlaySongService() { return playSongService; }

    public PlaySettingsService getPlaySettingsService() { return playSettingsService; }

    public BoxSongService getBoxSongService() { return boxSongService; }

    public JukeBoxService getJukeBoxService() { return jukeBoxService; }

    public RadioService getRadioService() { return radioService; }

    public MidiService getMidiService() { return midiService; }

    public NBSService getNBSService() { return nbsService; }

    public MusicUtil getMusicUtil() { return musicUtil; }

    public SteroNoteUtil getSteroNoteUtil() { return steroNoteUtil; }

    public boolean supportsPaperFeature() { return supportsPaperFeature; }

    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public void onLoad() {
        gMusicMain = this;

        versionService = new VersionService(this);
        configService = new ConfigService(this);

        updateService = new UpdateService(this);
        permissionService = new PermissionService();
        taskService = new TaskService(this);
        dataService = new DataService(this);
        songService = new SongService(this);
        playSongService = new PlaySongService(this);
        playSettingsService = new PlaySettingsService(this);
        boxSongService = new BoxSongService(this);
        jukeBoxService = new JukeBoxService(this);
        radioService = new RadioService(this);
        midiService = new MidiService(this);
        nbsService = new NBSService(this);

        musicUtil = new MusicUtil();
        steroNoteUtil = new SteroNoteUtil();

        loadFeatures();

        messageService = supportsPaperFeature && versionService.isNewerOrVersion(18, 2) ? new PaperMessageService(this) : new SpigotMessageService(this);
    }

    public void onEnable() {
        if(!versionCheck()) return;

        loadPluginDependencies();
        loadSettings(Bukkit.getConsoleSender());

        setupCommands();
        setupEvents();
        setupBStatsMetric();

        Bukkit.getPluginManager().callEvent(new GMusicLoadedEvent(this));

        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-enabled");

        printPluginLinks(Bukkit.getConsoleSender());
        updateService.checkForUpdates();
    }

    public void onDisable() {
        unload();
        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadSettings(CommandSender sender) {
        if(!connectDatabase(sender)) return;
        playSettingsService.createTable();
        songService.loadSongs();
        if(configService.R_ACTIVE) radioService.startRadio();
    }

    public void reload(CommandSender sender) {
        GMusicReloadEvent reloadEvent = new GMusicReloadEvent(this);
        Bukkit.getPluginManager().callEvent(reloadEvent);
        if(reloadEvent.isCancelled()) return;

        unload();
        configService.reload();
        messageService.loadMessages();
        loadPluginDependencies();
        loadSettings(sender);
        printPluginLinks(sender);
        updateService.checkForUpdates();

        Bukkit.getPluginManager().callEvent(new GMusicLoadedEvent(this));
    }

    private void unload() {
        dataService.close();
        songService.unloadSongs();
        radioService.stopRadio();
        playSettingsService.clearPlaySettingsCache();

        for(Player player : Bukkit.getOnlinePlayers()) {
            playSettingsService.setPlaySettings(player.getUniqueId(), playSettingsService.getPlaySettings(player.getUniqueId()));
            playSongService.stopSong(player);
        }
    }

    private void setupCommands() {
        getCommand("gmusic").setExecutor(new GMusicCommand(this));
        getCommand("gmusic").setTabCompleter(new GMusicTabComplete(this));
        getCommand("gmusic").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gamusic").setExecutor(new GAMusicCommand(this));
        getCommand("gamusic").setTabCompleter(new GAMusicTabComplete(this));
        getCommand("gamusic").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gmusicreload").setExecutor(new GMusicReloadCommand(this));
        getCommand("gmusicreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gmusicreload").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new JukeBoxEventHandler(this), this);
    }

    private boolean versionCheck() {
        if(versionService.isNewerOrVersion(18, 0) && !versionService.isAvailable()) {
            messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", versionService.getServerVersion());
            updateService.checkForUpdates();
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        return true;
    }

    private boolean connectDatabase(CommandSender sender) {
        boolean connected = dataService.connect();
        if(connected) return true;
        messageService.sendMessage(sender, "Plugin.plugin-data");
        Bukkit.getPluginManager().disablePlugin(this);
        return false;
    }

    private void loadFeatures() {
        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            supportsPaperFeature = true;
        } catch(ClassNotFoundException e) { supportsPaperFeature = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch(ClassNotFoundException e) { supportsTaskFeature = false; }
    }

    private void loadPluginDependencies() { }

    private void printPluginLinks(CommandSender sender) { }

    private void setupBStatsMetric() {
        BStatsMetric bStatsMetric = new BStatsMetric(this, BSTATS_RESOURCE_ID);

        bStatsMetric.addCustomChart(new BStatsMetric.SimplePie("plugin_language", () -> configService.L_LANG));
        bStatsMetric.addCustomChart(new BStatsMetric.AdvancedPie("minecraft_version_player_amount", () -> Map.of(versionService.getServerVersion(), Bukkit.getOnlinePlayers().size())));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("song_amount", () -> songService.getSongs().size()));
    }

}