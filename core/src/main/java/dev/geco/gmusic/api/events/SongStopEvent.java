package dev.geco.gmusic.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.*;

import dev.geco.gmusic.objects.Song;
import dev.geco.gmusic.objects.SongSettings;

public class SongStopEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private Player p;
    private SongSettings ss;
    
    public SongStopEvent(Player Player, SongSettings SongSettings) {
    	
        p = Player;
        ss = SongSettings;
        
    }
    
    public Player getPlayer() { return p; }
    
    public SongSettings getSongSettings() { return ss; }
    
    public Song getSong() { return ss.getSong(); }
    
    @Override
	public HandlerList getHandlers() { return HANDLERS; }
	
	public static HandlerList getHandlerList() { return HANDLERS; }
    
}