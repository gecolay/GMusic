package dev.geco.gmusic.objects;

import java.util.*;

public class SongSettings {

	private final Song song;

	private Timer timer;

	private long position;

	private boolean paused = false;

	public SongSettings(Song Song, Timer Timer, long Position) {

		song = Song;
		timer = Timer;
		position = Position;
	}

	public Song getSong() { return song; }

	public Timer getTimer() { return timer; }

	public void setTimer(Timer Timer) { timer = Timer; }

	public long getPosition() { return position; }

	public void setPosition(long Position) { position = Position; }

	public boolean isPaused() { return paused; }

	public void setPaused(boolean Paused) { paused = Paused; }

}