package dev.geco.gmusic.api.event;

import org.jetbrains.annotations.*;

import org.bukkit.event.*;
import org.bukkit.event.server.*;

import dev.geco.gmusic.GMusicMain;

public class GMusicReloadEvent extends PluginEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GMusicMain GPM;

    public GMusicReloadEvent(GMusicMain GPluginMain) {
        super(GPluginMain);
        GPM = GPluginMain;
    }

    public @NotNull GMusicMain getPlugin() { return GPM; }

    public @NotNull HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }

}