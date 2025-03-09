package dev.geco.gmusic.event;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GMusicGUI;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GSong;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerEventHandler implements Listener {

    private final GMusicMain gMusicMain;

    public PlayerEventHandler(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        gMusicMain.getUpdateService().checkForUpdates(player);

        if(gMusicMain.getConfigService().S_EXTENDED_RANGE && gMusicMain.getConfigService().S_FORCE_RESOURCES) {
            player.setResourcePack("https://github.com/Gecolay/GMusic/raw/main/resources/resource_pack/note_block_extended_octave_range.zip", "null", true);
        }

        GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(playerUuid);

        if(gMusicMain.getConfigService().R_PLAY_ON_JOIN) playSettings.setPlayList(2);

        if(playSettings.getPlayList() == 2) gMusicMain.getRadioService().addRadioPlayer(player);
        else if(playSettings.isPlayOnJoin()) {
            if(gMusicMain.getPlaySongService().hasPlayingSong(playerUuid)) gMusicMain.getPlaySongService().resumeSong(player);
            else {
                GSong song = playSettings.getCurrentSong() == null ? gMusicMain.getPlaySongService().getRandomSong(playerUuid) : gMusicMain.getSongService().getSongById(playSettings.getCurrentSong());
                gMusicMain.getPlaySongService().playSong(player, song != null ? song : gMusicMain.getPlaySongService().getRandomSong(playerUuid));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        gMusicMain.getRadioService().removeRadioPlayer(player);

        if(gMusicMain.getConfigService().PS_SAVE_ON_QUIT) gMusicMain.getPlaySettingsService().setPlaySettings(player.getUniqueId(), gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId()));
        gMusicMain.getPlaySettingsService().removePlaySettingsCache(player.getUniqueId());

        GMusicGUI musicGUI = gMusicMain.getSongService().getMusicGUIs().get(player.getUniqueId());
        if(musicGUI != null) musicGUI.close(true);

        gMusicMain.getSongService().getMusicGUIs().remove(player.getUniqueId());
    }

}