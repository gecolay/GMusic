package dev.geco.gmusic.manager;

import java.io.*;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import dev.geco.gmusic.values.Values;

public class PlaySettingsManager {
	
	private final GMusicMain GPM;
	
	private File PlayData;
	
	private FileConfiguration PlayD;
	
    public PlaySettingsManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
    
    public void generatePlaySettings() {
    	
    	reloadFile();
    	
    	GPM.getValues().clearRadioPlayers();
    	
    	for(Player p : Bukkit.getOnlinePlayers()) {
    		PlaySettings ps = getPlaySettings(p.getUniqueId());
    		GPM.getValues().putPlaySetting(p.getUniqueId(), ps);
    		if(ps.getPlayList() == 2) GPM.getValues().addRadioPlayer(p);
    	}
    	
    }
    
    public void savePlaySettings() {
    	
    	for(UUID p : GPM.getValues().getPlaySettings().keySet()) setPlaySettings(p, GPM.getValues().getPlaySettings().get(p));
    	
    	GPM.getValues().clearPlaySettings();
    	
    }
    
    public PlaySettings getPlaySettings(UUID U) {
    	
    	String uu = "PS." + U.toString();
    	
    	int l = PlayD.getString(uu + ".L") != null ? PlayD.getInt(uu + ".L") : GPM.getCManager().P_D_PLAYLIST;
    	long v = PlayD.getString(uu + ".V") != null ? PlayD.getLong(uu + ".V") : GPM.getCManager().P_D_VOLUME;
    	boolean j = PlayD.getString(uu + ".J") != null ? PlayD.getBoolean(uu + ".J") : GPM.getCManager().P_D_JOIN;
    	int m = PlayD.getString(uu + ".M") != null ? PlayD.getInt(uu + ".M") : GPM.getCManager().P_D_PLAYMODE;
    	boolean e = PlayD.getString(uu + ".E") != null ? PlayD.getBoolean(uu + ".E") : GPM.getCManager().P_D_PARTICLES;
    	boolean q = PlayD.getString(uu + ".Q") != null ? PlayD.getBoolean(uu + ".Q") : GPM.getCManager().P_D_REVERSE;
    	boolean t = PlayD.getString(uu + ".T") != null ? PlayD.getBoolean(uu + ".T") : false;
    	long r = PlayD.getString(uu + ".R") != null ? PlayD.getLong(uu + ".R") : GPM.getCManager().JUKEBOX_RANGE;
    	String c = PlayD.getString(uu + ".C", null);
    	List<Song> f = new ArrayList<Song>();
    	for(String S : PlayD.getStringList(uu + ".F").size() > 0 ? PlayD.getStringList(uu + ".F") : new ArrayList<String>()) f.add(GPM.getSongManager().getSongById(S));
    	
    	return new PlaySettings(U, l, v, j, m, e, q, t, r, c, f);
    	
    }
    
    public void setPlaySettings(UUID U, PlaySettings PS) {
    	
    	String uu = "PS." + U.toString();
    	
    	if(PS == null) PlayD.set(uu, null);
    	else {
    		
    		PlayD.set(uu + ".L", PS.getPlayList() == GPM.getCManager().P_D_PLAYLIST ? null : PS.getPlayList());
    		PlayD.set(uu + ".V", PS.getVolume() == GPM.getCManager().P_D_VOLUME ? null : PS.getVolume());
        	PlayD.set(uu + ".J", PS.isPlayOnJoin() == GPM.getCManager().P_D_JOIN ? null : PS.isPlayOnJoin());
        	PlayD.set(uu + ".M", PS.getPlayMode() == GPM.getCManager().P_D_PLAYMODE ? null : PS.getPlayMode());
        	PlayD.set(uu + ".E", PS.isShowingParticles() == GPM.getCManager().P_D_PARTICLES ? null : PS.isShowingParticles());
        	PlayD.set(uu + ".Q", PS.isReverseMode() == GPM.getCManager().P_D_REVERSE ? null : PS.isReverseMode());
        	PlayD.set(uu + ".T", PS.isToggleMode() == false ? null : PS.isToggleMode());
        	PlayD.set(uu + ".R", PS.getRange() == GPM.getCManager().JUKEBOX_RANGE ? null : PS.getRange());
        	PlayD.set(uu + ".C", PS.getCurrentSong() == null ? null : PS.getCurrentSong());
        	List<String> f = new ArrayList<String>();
        	for(Song S : PS.getFavorites()) f.add(S.getId());
        	PlayD.set(uu + ".F", PS.getFavorites().size() == 0 ? null : f);
    		
    	}
    	
    	saveFile();
    	
    }
    
    public void reloadFile() {
    	PlayData = new File("plugins/" + GPM.NAME, Values.DATA_PATH + "/" + Values.PLAY_FILE + Values.DATA_FILETYP);
    	PlayD = YamlConfiguration.loadConfiguration(PlayData);
    }
    
    private void saveFile() { try { PlayD.save(PlayData); } catch (IOException e) { } }
    
}