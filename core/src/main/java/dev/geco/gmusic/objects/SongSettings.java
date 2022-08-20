package dev.geco.gmusic.objects;

import java.util.*;

public class SongSettings {
	
	private Song s;
	
	private Timer t;
	
	private long p;
	
	private boolean q = false;
	
	public SongSettings(Song S, Timer T, long P) {
		
		s = S;
		t = T;
		p = P;
		
	}
	
	public Song getSong() { return s; }
	
	public Timer getTimer() { return t; }
	
	public void setTimer(Timer T) { t = T; }
	
	public long getPosition() { return p; }
	
	public void setPosition(long P) { p = P; }
	
	public boolean isPaused() { return q; }
	
	public void setPaused(boolean Paused) { q = Paused; }
	
}