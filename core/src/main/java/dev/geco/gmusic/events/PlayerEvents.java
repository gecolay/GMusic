package dev.geco.gmusic.events;

import java.util.*;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.objects.*;

public class PlayerEvents implements Listener {

    private final GMusicMain GPM;

    public PlayerEvents(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @EventHandler
    public void PJoiE(PlayerJoinEvent Event) {

        Player player = Event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        GPM.getUManager().loginCheckForUpdates(player);

        if(GPM.getCManager().S_EXTENDED_RANGE && GPM.getCManager().S_FORCE_RESOURCES) {

            player.setResourcePack("https://github.com/Gecolay/GMusic/raw/main/src/resource_pack/note_block_extended_octave_range.zip", "null", true);
        }

        PlaySettings playSettings = GPM.getPlaySettingsManager().getPlaySettings(playerUuid);

        if(GPM.getCManager().R_PLAY_ON_JOIN) playSettings.setPlayList(2);

        if(playSettings.getPlayList() == 2) GPM.getRadioManager().addRadioPlayer(player);
        else if(playSettings.isPlayOnJoin()) {
            if(GPM.getPlaySongManager().hasPlayingSong(playerUuid)) GPM.getPlaySongManager().resumeSong(player);
            else {
                Song song = playSettings.getCurrentSong() == null ? GPM.getPlaySongManager().getRandomSong(playerUuid) : GPM.getSongManager().getSongById(playSettings.getCurrentSong());
                GPM.getPlaySongManager().playSong(player, song != null ? song : GPM.getPlaySongManager().getRandomSong(playerUuid));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PQuiE(PlayerQuitEvent Event) {

        Player player = Event.getPlayer();

        GPM.getRadioManager().removeRadioPlayer(player);

        if(GPM.getCManager().PS_SAVE_ON_QUIT) GPM.getPlaySettingsManager().setPlaySettings(player.getUniqueId(), GPM.getPlaySettingsManager().getPlaySettings(player.getUniqueId()));
        GPM.getPlaySettingsManager().removePlaySettingsCache(player.getUniqueId());

        MusicGUI musicGUI = GPM.getSongManager().getMusicGUIs().get(player.getUniqueId());
        if(musicGUI != null) musicGUI.close(true);
        GPM.getSongManager().getMusicGUIs().remove(player.getUniqueId());
    }

}