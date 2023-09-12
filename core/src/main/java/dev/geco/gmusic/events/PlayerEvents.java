package dev.geco.gmusic.events;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gmusic.GMusicMain;

public class PlayerEvents implements Listener {

    private final GMusicMain GPM;

    public PlayerEvents(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler
    public void PJoiE(PlayerJoinEvent Event) { GPM.getUManager().loginCheckForUpdates(Event.getPlayer()); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        GPM.getPlaySettingsManager().setPlaySettings(player.getUniqueId(), GPM.getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
    }

}