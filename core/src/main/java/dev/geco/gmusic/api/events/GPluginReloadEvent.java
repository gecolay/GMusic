package dev.geco.gmusic.api.events;

import org.bukkit.event.*;
import org.bukkit.plugin.Plugin;

public class GPluginReloadEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Plugin GCM;
	
	public GPluginReloadEvent(Plugin GPluginMain) { GCM = GPluginMain; }
	
	public Plugin getPlugin() { return GCM; }
	
	@Override
	public HandlerList getHandlers() { return HANDLERS; }
	
	public static HandlerList getHandlerList() { return HANDLERS; }
	
}