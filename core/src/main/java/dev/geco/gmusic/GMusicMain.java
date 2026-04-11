package dev.geco.gmusic;

import dev.geco.gmusic.api.event.GMusicLoadedEvent;
import dev.geco.gmusic.api.event.GMusicReloadEvent;
import dev.geco.gmusic.cmd.GAdminMusicCommand;
import dev.geco.gmusic.cmd.GMusicCommand;
import dev.geco.gmusic.cmd.GMusicReloadCommand;
import dev.geco.gmusic.cmd.tab.EmptyTabComplete;
import dev.geco.gmusic.cmd.tab.GAdminMusicTabComplete;
import dev.geco.gmusic.cmd.tab.GMusicTabComplete;
import dev.geco.gmusic.event.DiscEventHandler;
import dev.geco.gmusic.event.JukeBoxEventHandler;
import dev.geco.gmusic.event.PlayerEventHandler;
import dev.geco.gmusic.link.GriefPreventionLink;
import dev.geco.gmusic.link.PlaceholderAPILink;
import dev.geco.gmusic.link.PlotSquaredLink;
import dev.geco.gmusic.link.WorldGuardLink;
import dev.geco.gmusic.metric.BStatsMetric;
import dev.geco.gmusic.service.ConfigService;
import dev.geco.gmusic.service.DataService;
import dev.geco.gmusic.service.DiscService;
import dev.geco.gmusic.service.JukeBoxService;
import dev.geco.gmusic.service.MessageService;
import dev.geco.gmusic.service.converter.MidiConverter;
import dev.geco.gmusic.service.converter.NBSConverter;
import dev.geco.gmusic.service.PermissionService;
import dev.geco.gmusic.service.PlaySettingsService;
import dev.geco.gmusic.service.PlayService;
import dev.geco.gmusic.service.RadioService;
import dev.geco.gmusic.service.SongService;
import dev.geco.gmusic.service.TaskService;
import dev.geco.gmusic.service.UpdateService;
import dev.geco.gmusic.service.VersionService;
import dev.geco.gmusic.service.converter.WavConverter;
import dev.geco.gmusic.service.message.PaperMessageService;
import dev.geco.gmusic.service.message.SpigotMessageService;
import dev.geco.gmusic.util.EnvironmentUtil;
import dev.geco.gmusic.util.SteroNoteUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class GMusicMain extends JavaPlugin {

    public static final String NAME = "GMusic";

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
    private PlayService playService;
    private PlaySettingsService playSettingsService;
    private JukeBoxService jukeBoxService;
    private DiscService discService;
    private RadioService radioService;
    private MidiConverter midiConverter;
    private NBSConverter nbsConverter;
    private WavConverter wavConverter;
    private EnvironmentUtil environmentUtil;
    private SteroNoteUtil steroNoteUtil;
    private GriefPreventionLink griefPreventionLink;
    private PlaceholderAPILink placeholderAPILink;
    private PlotSquaredLink plotSquaredLink;
    private WorldGuardLink worldGuardLink;
    private BStatsMetric bStatsMetric;
    private boolean supportsTaskFeature = false;
    private boolean isPaperServer = false;
    private boolean isFoliaServer = false;

    public static GMusicMain getInstance() { return gMusicMain; }

    public VersionService getVersionService() { return versionService; }

    public ConfigService getConfigService() { return configService; }

    public MessageService getMessageService() { return messageService; }

    public UpdateService getUpdateService() { return updateService; }

    public PermissionService getPermissionService() { return permissionService; }

    public TaskService getTaskService() { return taskService; }

    public DataService getDataService() { return dataService; }

    public SongService getSongService() { return songService; }

    public PlayService getPlayService() { return playService; }

    public PlaySettingsService getPlaySettingsService() { return playSettingsService; }

    public JukeBoxService getJukeBoxService() { return jukeBoxService; }

    public DiscService getDiscService() { return discService; }

    public RadioService getRadioService() { return radioService; }

    public MidiConverter getMidiConverter() { return midiConverter; }

    public NBSConverter getNBSConverter() { return nbsConverter; }

    public WavConverter getWavConverter() { return wavConverter; }

    public EnvironmentUtil getEnvironmentUtil() { return environmentUtil; }

    public SteroNoteUtil getSteroNoteUtil() { return steroNoteUtil; }

    public GriefPreventionLink getGriefPreventionLink() { return griefPreventionLink; }

    public PlaceholderAPILink getPlaceholderAPILink() { return placeholderAPILink; }

    public PlotSquaredLink getPlotSquaredLink() { return plotSquaredLink; }

    public WorldGuardLink getWorldGuardLink() { return worldGuardLink; }

    public boolean supportsTaskFeature() { return supportsTaskFeature; }

    public boolean isPaperServer() { return isPaperServer; }

    public boolean isFoliaServer() { return isFoliaServer; }

    public void onLoad() {
        gMusicMain = this;

        versionService = new VersionService(this);
        configService = new ConfigService(this);

        updateService = new UpdateService(this);
        permissionService = new PermissionService();
        taskService = new TaskService(this);
        dataService = new DataService(this);
        songService = new SongService(this);
        playService = new PlayService(this);
        playSettingsService = new PlaySettingsService(this);
        jukeBoxService = new JukeBoxService(this);
        discService =  new DiscService(this);
        radioService = new RadioService(this);

        midiConverter = new MidiConverter(this);
        nbsConverter = new NBSConverter(this);
        wavConverter = new WavConverter(this);

        environmentUtil = new EnvironmentUtil(this);
        steroNoteUtil = new SteroNoteUtil();

        loadFeatures();

        messageService = isPaperServer && versionService.isNewerOrVersion(1, 18, 2) ? new PaperMessageService(this) : new SpigotMessageService(this);
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
        if(bStatsMetric != null) bStatsMetric.shutdown();
        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-disabled");
    }

    private void loadSettings(CommandSender sender) {
        if(!connectDatabase(sender)) return;
        songService.loadSongs();
        playSettingsService.createDataTables();
        jukeBoxService.createDataTables();
        jukeBoxService.loadJukeboxes(null);
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
        playService.stopSongs();
        radioService.stopRadio();
        songService.unloadSongs();
        playSettingsService.savePlaySettings();

        if(placeholderAPILink != null) placeholderAPILink.unregister();
        if(worldGuardLink != null) worldGuardLink.unregisterFlagHandlers();
    }

    private void setupCommands() {
        getCommand("gmusic").setExecutor(new GMusicCommand(this));
        getCommand("gmusic").setTabCompleter(new GMusicTabComplete(this));
        getCommand("gmusic").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gadminmusic").setExecutor(new GAdminMusicCommand(this));
        getCommand("gadminmusic").setTabCompleter(new GAdminMusicTabComplete(this));
        getCommand("gadminmusic").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
        getCommand("gmusicreload").setExecutor(new GMusicReloadCommand(this));
        getCommand("gmusicreload").setTabCompleter(new EmptyTabComplete());
        getCommand("gmusicreload").setPermissionMessage(messageService.getMessage("Messages.command-permission-error"));
    }

    private void setupEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new JukeBoxEventHandler(this), this);
        getServer().getPluginManager().registerEvents(new DiscEventHandler(this), this);
    }

    private boolean versionCheck() {
        if(versionService.isNewerOrVersion(1, 13)) return true;
        messageService.sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-version", "%Version%", Bukkit.getServer().getVersion());
        updateService.checkForUpdates();
        Bukkit.getPluginManager().disablePlugin(this);
        return false;
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
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            supportsTaskFeature = true;
        } catch(ClassNotFoundException e) { supportsTaskFeature = false; }

        try {
            Class.forName("io.papermc.paper.event.entity.EntityMoveEvent");
            isPaperServer = true;
        } catch(ClassNotFoundException e) { isPaperServer = false; }

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFoliaServer = true;
        } catch(ClassNotFoundException e) { isFoliaServer = false; }

        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardLink = new WorldGuardLink();
            worldGuardLink.registerFlags();
        }
    }

    private void loadPluginDependencies() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if(plugin != null && plugin.isEnabled() && configService.TRUSTED_REGION_ONLY) griefPreventionLink = new GriefPreventionLink(this);
        else griefPreventionLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if(plugin != null && plugin.isEnabled()) {
            placeholderAPILink = new PlaceholderAPILink(this);
            placeholderAPILink.register();
        } else placeholderAPILink = null;

        plugin = Bukkit.getPluginManager().getPlugin("PlotSquared");
        if(plugin != null && plugin.isEnabled() && configService.TRUSTED_REGION_ONLY) {
            plotSquaredLink = new PlotSquaredLink(this);
            if(!plotSquaredLink.isPlotSquaredVersionSupported()) plotSquaredLink = null;
        } else plotSquaredLink = null;

        plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(plugin != null && plugin.isEnabled()) {
            if(worldGuardLink == null) {
                worldGuardLink = new WorldGuardLink();
                worldGuardLink.registerFlags();
            }
            worldGuardLink.registerFlagHandlers();
        } else worldGuardLink = null;
    }

    private void printPluginLinks(CommandSender sender) {
        if(griefPreventionLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("GriefPrevention").getName());
        if(placeholderAPILink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getName());
        if(plotSquaredLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("PlotSquared").getName());
        if(worldGuardLink != null) messageService.sendMessage(sender, "Plugin.plugin-link", "%Link%", Bukkit.getPluginManager().getPlugin("WorldGuard").getName());
    }

    public String getSource() {
        Map<?, ?> map = (new Yaml()).load(getClassLoader().getResourceAsStream("plugin.yml"));
        return map.get("source").toString().toLowerCase();
    }

    private void setupBStatsMetric() {
        bStatsMetric = new BStatsMetric(this, BSTATS_RESOURCE_ID);

        bStatsMetric.addCustomChart(new BStatsMetric.SimplePie("plugin_language", () -> configService.L_LANG));
        bStatsMetric.addCustomChart(new BStatsMetric.SimplePie("plugin_source", this::getSource));
        bStatsMetric.addCustomChart(new BStatsMetric.AdvancedPie("minecraft_version_player_amount", () -> Map.of(versionService.getServerVersion(), Bukkit.getOnlinePlayers().size())));
        bStatsMetric.addCustomChart(new BStatsMetric.SingleLineChart("song_count", () -> songService.getSongs().size()));
    }

}