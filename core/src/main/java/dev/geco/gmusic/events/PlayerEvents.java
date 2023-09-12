package dev.geco.gmusic.events;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gmusic.GMusicMain;

public class PlayerEvents implements Listener {

    private final GMusicMain GPM;

    public PlayerEvents(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler
    public void PJoiE(PlayerJoinEvent Event) {

        GPM.getUManager().loginCheckForUpdates(Event.getPlayer());

        if(GPM.getCManager().S_EXTENDED_RANGE && GPM.getCManager().S_FORCE_RESOURCES) {

            Event.getPlayer().setResourcePack("https://github.com/Gecolay/GMusic/raw/main/src/resources/resource_pack/note_block_extended_octave_range.zip", "null", true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        GPM.getPlaySettingsManager().setPlaySettings(player.getUniqueId(), GPM.getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
    }

}