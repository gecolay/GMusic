package dev.geco.gmusic.api.event;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.event.HandlerList;
import org.bukkit.event.server.PluginEvent;
import org.jetbrains.annotations.NotNull;

public class GMusicLoadedEvent extends PluginEvent {

    private final GMusicMain gMusicMain;
    private static final HandlerList handlers = new HandlerList();

    public GMusicLoadedEvent(@NotNull GMusicMain gMusicMain) {
        super(gMusicMain);
        this.gMusicMain = gMusicMain;
    }

    @Override
    public @NotNull GMusicMain getPlugin() { return gMusicMain; }

    @Override
    public @NotNull HandlerList getHandlers() { return handlers; }

    public static @NotNull HandlerList getHandlerList() { return handlers; }

}