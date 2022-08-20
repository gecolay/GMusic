package dev.geco.gmusic.api.events;

import java.util.UUID;

import org.bukkit.event.*;

import dev.geco.gmusic.objects.Song;
import dev.geco.gmusic.objects.SongSettings;

public class BoxSongPlayEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private UUID u;
    private SongSettings ss;
    
    public BoxSongPlayEvent(UUID UUID, SongSettings SongSettings) {
    	
        u = UUID;
        ss = SongSettings;
        
    }
    
    public UUID getUUID() { return u; }
    
    public SongSettings getSongSettings() { return ss; }
    
    public Song getSong() { return ss.getSong(); }
    
    @Override
	public HandlerList getHandlers() { return HANDLERS; }
	
	public static HandlerList getHandlerList() { return HANDLERS; }
    
}