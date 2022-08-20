package dev.geco.gmusic.objects;

import java.util.*;

public class PlaySettings {
	
	private UUID u;
	
	private int l;
	
	private long v;
	
	private boolean j;
	
	private int m;
	
	private boolean e;
	
	private boolean q;
	
	private boolean t;
	
	private long r;
	
	private String c;
	
	private List<Song> f = new ArrayList<Song>();
	
	public PlaySettings(UUID UUID, int PlayList, long Volume, boolean PlayOnJoin, int PlayMode, boolean ShowingParticles, boolean ReverseMode, boolean Toggle, long Range, String CurrentSong, List<Song> Favorites) {
		
		l = PlayList;
		u = UUID;
		v = Volume;
		j = PlayOnJoin;
		m = PlayMode;
		e = ShowingParticles;
		q = ReverseMode;
		t = Toggle;
		r = Range;
		c = CurrentSong;
		f = Favorites;
		
	}
	
	public UUID getUUID() { return u; }
	
	public int getPlayList() { return l; }
	
	public void setPlayList(int PlayList) { l = PlayList; }
	
	public long getVolume() { return v; }
	
	public float getFixedVolume() { return (float) (v * 2) / 100; }
	
	public void setVolume(long Volume) { v = Volume; }
	
	public boolean isPlayOnJoin() { return j; }
	
	public void setPlayOnJoin(boolean PlayOnJoin) { j = PlayOnJoin; }
	
	public int getPlayMode() { return m; }
	
	public void setPlayMode(int PlayMode) { m = PlayMode; }
	
	public boolean isShowingParticles() { return e; }
	
	public void setShowingParticles(boolean ShowingParticles) { e = ShowingParticles; }
	
	public boolean isReverseMode() { return q; }
	
	public void setReverseMode(boolean ReverseMode) { q = ReverseMode; }
	
	public boolean isToggleMode() { return t; }
	
	public void setToggleMode(boolean ToggleMode) { t = ToggleMode; }
	
	public long getRange() { return r; }
	
	public void setRange(long Range) { r = Range; }
	
	public String getCurrentSong() { return c; }
	
	public void setCurrentSong(String CurrentSong) { c = CurrentSong; }
	
	public List<Song> getFavorites() { return f; }
	
	public void addFavoriteSong(Song S) { f.add(S); }
	
	public void removeFavoriteSong(Song S) { f.remove(S); }
	
}