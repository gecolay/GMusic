package dev.geco.gmusic.manager;

import java.util.ArrayList;
import java.util.List;

import dev.geco.gmusic.main.GMusicMain;

public class CManager {
	
	public boolean CHECK_FOR_UPDATES;
	
	public long JUKEBOX_RANGE;
	
	public long JUKEBOX_MAX_RANGE;
	
	
	public boolean A_SHOW_MESSAGES;
	
	public boolean A_SHOW_ALWAYS_WHILE_PLAYING;
	
	
	public boolean R_ACTIVE;
	
	public boolean R_PLAY_ON_JOIN;
	
	
	public boolean PREVENT_VANILLA_MUSIC;
	
	public boolean USE_ENVIRONMENT_EFFECT;
	
	public List<String> WORLDBLACKLIST = new ArrayList<>();
	
	
	public long P_WAIT_TIME_UNTIL_SHUFFLE;
	
	public long P_WAIT_TIME_UNTIL_REPEAT;
	
	public boolean P_SAVE_ON_QUIT;
	
	public int P_D_PLAYLIST;
	
	public long P_D_VOLUME;
	
	public boolean P_D_JOIN;
	
	public int P_D_PLAYMODE;
	
	public boolean P_D_PARTICLES;
	
	public boolean P_D_REVERSE;
	
	
	public boolean G_DISABLE_RANDOM_SONG;
	
	public boolean G_DISABLE_PLAYLIST;
	
	public boolean G_DISABLE_OPTIONS;
	
	public boolean G_DISABLE_SEARCH;
	
	
	private final GMusicMain GPM;
	
    public CManager(GMusicMain GPluginMain) {
    	GPM = GPluginMain;
    	reload();
    }
	
	public void reload() {
		
		CHECK_FOR_UPDATES = GPM.getConfig().getBoolean("Options.check-for-update", true);
		
		JUKEBOX_RANGE = GPM.getConfig().getLong("Options.jukebox-range", 50);
		JUKEBOX_MAX_RANGE = GPM.getConfig().getLong("Options.jukebox-max-range", 500);
		
		A_SHOW_MESSAGES = GPM.getConfig().getBoolean("Options.ActionBar.show-messages", true);
		A_SHOW_ALWAYS_WHILE_PLAYING = GPM.getConfig().getBoolean("Options.ActionBar.show-always-while-playing", true);
		
		R_ACTIVE = GPM.getConfig().getBoolean("Options.Radio.active", true);
		R_PLAY_ON_JOIN = GPM.getConfig().getBoolean("Options.Radio.play-on-join", false);
		
		PREVENT_VANILLA_MUSIC = GPM.getConfig().getBoolean("Options.prevent-from-vanilla-music", true);
		USE_ENVIRONMENT_EFFECT = GPM.getConfig().getBoolean("Options.use-environment-effect", true);
		WORLDBLACKLIST = GPM.getConfig().getStringList("Options.WorldBlacklist");
		
		P_WAIT_TIME_UNTIL_SHUFFLE = GPM.getConfig().getLong("Options.PlayerSettings.wait-time-until-shuffle", 1000);
		P_WAIT_TIME_UNTIL_REPEAT = GPM.getConfig().getLong("Options.PlayerSettings.wait-time-until-repeat", 1000);
		P_SAVE_ON_QUIT = GPM.getConfig().getBoolean("Options.PlayerSettings.save-on-quit", true);
		P_D_PLAYLIST = GPM.getConfig().getInt("Options.PlayerSettings.Default.playlist", 0);
		P_D_VOLUME = GPM.getConfig().getLong("Options.PlayerSettings.Default.volume", 70);
		P_D_JOIN = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.join", false);
		P_D_PLAYMODE = GPM.getConfig().getInt("Options.PlayerSettings.Default.playmode", 0);
		P_D_PARTICLES = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.particles", false);
		P_D_REVERSE = GPM.getConfig().getBoolean("Options.PlayerSettings.Default.reverse", false);
		
		G_DISABLE_RANDOM_SONG = GPM.getConfig().getBoolean("Options.GUI.disable-random-song", false);
		G_DISABLE_PLAYLIST = GPM.getConfig().getBoolean("Options.GUI.disable-playlist", false);
		G_DISABLE_OPTIONS = GPM.getConfig().getBoolean("Options.GUI.disable-options", false);
		G_DISABLE_SEARCH = GPM.getConfig().getBoolean("Options.GUI.disable-search", false);
		
	}
	
}