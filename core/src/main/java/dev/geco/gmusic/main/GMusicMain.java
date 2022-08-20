package dev.geco.gmusic.main;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import dev.geco.gmusic.api.events.*;
import dev.geco.gmusic.cmd.*;
import dev.geco.gmusic.cmd.tab.*;
import dev.geco.gmusic.events.*;
import dev.geco.gmusic.link.*;
import dev.geco.gmusic.manager.*;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.util.*;
import dev.geco.gmusic.util.UtilFormat.*;
import dev.geco.gmusic.values.*;

public class GMusicMain extends JavaPlugin {
	
	private FileConfiguration messages;
	
	public FileConfiguration getMessages() { return messages; }
	
	private CManager cmanager;
	
	public CManager getCManager() { return cmanager; }
	
	private String prefix;
	
	public String getPrefix() { return prefix; }
	
	private Values values;
	
	public Values getValues() { return values; }
	
	private MusicManager musicmanager;
	
	public MusicManager getMusicManager() { return musicmanager; }
	
	private SongManager songmanager;
	
	public SongManager getSongManager() { return songmanager; }
	
	private BoxSongManager boxsongmanager;
	
	public BoxSongManager getBoxSongManager() { return boxsongmanager; }
	
	private RadioManager radiomanager;
	
	public RadioManager getRadioManager() { return radiomanager; }
	
	private NBSManager nbsmanager;
	
	public NBSManager getNBSManager() { return nbsmanager; }
	
	private JukeBoxManager jukeboxmanager;
	
	public JukeBoxManager getJukeBoxManager() { return jukeboxmanager; }
	
	private PlaySettingsManager playsettingsmanager;
	
	public PlaySettingsManager getPlaySettingsManager() { return playsettingsmanager; }
	
	private MidiManager midimanager;
	
	public MidiManager getMidiManager() { return midimanager; }
	
	private PAPILink papilink;
	
	public PAPILink getPAPILink() { return papilink; }
	
	private UManager umanager;
	
	public UManager getUManager() { return umanager; }
	
	private MManager mmanager;
	
	public MManager getMManager() { return mmanager; }
	
	public UtilFormat utilformat;
	
	public UtilFormat getUtilFormat() { return utilformat; }
	
	public UtilMath utilmath;
	
	public UtilMath getUtilMath() { return utilmath; }
	
	public UtilCheck utilcheck;
	
	public UtilCheck getUtilCheck() { return utilcheck; }
	
	public UtilInventory utilinventory;
	
	public UtilInventory getUtilInventory() { return utilinventory; }
	
	public final String NAME = "GMusic";
	
	public final String RESOURCE = "84004";
	
	private static GMusicMain gmusic;
	
	public static GMusicMain getInstance() { return gmusic; }
	
	private void setupSettings() {
		copyLangFiles();
		messages = YamlConfiguration.loadConfiguration(new File("plugins/" + NAME + "/" + Values.LANG_PATH, getConfig().getString("Lang.lang") + Values.YML_FILETYP));
		prefix = messages.getString("Plugin.plugin-prefix");
		getMusicManager().convertSongs();
		getMusicManager().loadMusicSettings();
		getJukeBoxManager().reloadFile();
		getPlaySettingsManager().generatePlaySettings();
		getJukeBoxManager().generateJukeboxes();
		if(getCManager().R_ACTIVE) getRadioManager().playRadio();
	}
	
	private void linkBStats() {
		BStatsLink bstats = new BStatsLink(getInstance(), 4925);
		bstats.addCustomChart(new BStatsLink.SimplePie("plugin_language", new Callable<String>() {
			@Override
			public String call() throws Exception {
				return getConfig().getString("Lang.lang").toLowerCase();
			}
		}));
		bstats.addCustomChart(new BStatsLink.SingleLineChart("loaded_songs", new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getValues().getSongs().size();
			}
		}));
	}
	
	public void onEnable() {
		gmusic = this;
		saveDefaultConfig();
		cmanager = new CManager(getInstance());
		values = new Values();
		musicmanager = new MusicManager(getInstance());
		songmanager = new SongManager(getInstance());
		boxsongmanager = new BoxSongManager(getInstance());
		radiomanager = new RadioManager(getInstance());
		nbsmanager = new NBSManager(getInstance());
		jukeboxmanager = new JukeBoxManager(getInstance());
		playsettingsmanager = new PlaySettingsManager(getInstance());
		midimanager = new MidiManager(getInstance());
		umanager = new UManager(getInstance(), RESOURCE);
		mmanager = new MManager(getInstance());
		utilformat = new UtilFormat();
		utilmath = utilformat.new UtilMath();
		utilcheck = new UtilCheck();
		utilinventory = new UtilInventory();
		getCommand("gmusic").setExecutor(new GMusicCommand(getInstance()));
		getCommand("gmusic").setTabCompleter(new GMusicTabCompleter(getInstance()));
		getCommand("agmusic").setExecutor(new AGMusicCommand(getInstance()));
		getCommand("agmusic").setTabCompleter(new AGMusicTabCompleter(getInstance()));
		getCommand("gmusicreload").setExecutor(new GMusicReloadCommand(getInstance()));
		getServer().getPluginManager().registerEvents(new PlayerEvents(getInstance()), getInstance());
		getServer().getPluginManager().registerEvents(new JukeBoxEvents(getInstance()), getInstance());
		setupSettings();
		linkBStats();
		getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-start");
		loadPluginDepends(Bukkit.getConsoleSender());
		checkForUpdates();
	}
	
	public void onDisable() {
		getMManager().sendMessage(Bukkit.getConsoleSender(), "Plugin.plugin-stop");
		getRadioManager().stopRadio();
		for(PlaySettings p : getValues().getTempJukeBlocks().values()) getValues().removePlaySetting(p.getUUID());
		getPlaySettingsManager().savePlaySettings();
		getValues().clearMusicGUIs();
		getValues().clearInputGUIs();
		for(SongSettings t : getValues().getSongSettings().values()) t.getTimer().cancel();
		getValues().clearSongs();
	}
	
	private void loadPluginDepends(CommandSender s) {
		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getValues().setPAPI(true);
			if(getPAPILink() == null) {
				papilink = new PAPILink(getInstance());
				getPAPILink().register();
			}
			getMManager().sendMessage(s, "Plugin.plugin-link", "%Link%", "PlaceholderAPI");
		} else getValues().setPAPI(false);
	}
	
	public void copyLangFiles() { for(String l : Arrays.asList("de_de", "en_en", "es_es", "fr_fr", "pl_pl", "pt_br", "ru_ru")) if(!new File("plugins/" + NAME + "/" + Values.LANG_PATH + "/" + l + Values.YML_FILETYP).exists()) saveResource(Values.LANG_PATH + "/" + l + Values.YML_FILETYP, false); }
	
	public void reload(CommandSender s) {
		Bukkit.getPluginManager().callEvent(new GPluginReloadEvent(getInstance()));
		getValues().clearMusicGUIs();
		getValues().clearInputGUIs();
		reloadConfig();
		getCManager().reload();
		getRadioManager().stopRadio();
		for(PlaySettings p : getValues().getTempJukeBlocks().values()) getValues().removePlaySetting(p.getUUID());
		getPlaySettingsManager().savePlaySettings();
		setupSettings();
		loadPluginDepends(s);
		checkForUpdates();
	}

	private void checkForUpdates() {

		if(getCManager().CHECK_FOR_UPDATES) {

			getUManager().checkVersion();

			if(!getUManager().isLatestVersion()) {

				String message = getMManager().getMessage("Plugin.plugin-update", "%Name%", NAME, "%NewVersion%", getUManager().getLatestVersion(), "%Version%", getUManager().getPluginVersion(), "%Path%", getDescription().getWebsite());

				for(Player player : Bukkit.getOnlinePlayers()) if(player.hasPermission("GMusic.Update")) player.sendMessage(message);

				Bukkit.getConsoleSender().sendMessage(message);
			}
		}
	}
	
}