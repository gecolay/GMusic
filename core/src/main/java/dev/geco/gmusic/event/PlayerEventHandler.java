package dev.geco.gmusic.event;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GSong;
import dev.geco.gmusic.object.GPlayListMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerEventHandler implements Listener {

    private static final String RESOURCE_PACK_URL = "https://github.com/Gecolay/GMusic/raw/main/resources/resource_pack/note_block_extended_octave_range.zip";

    private final GMusicMain gMusicMain;

    public PlayerEventHandler(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        gMusicMain.getUpdateService().checkForUpdates(player);

        if(gMusicMain.getConfigService().S_EXTENDED_RANGE && gMusicMain.getConfigService().S_FORCE_RESOURCES) player.setResourcePack(RESOURCE_PACK_URL, "null", true);

        GPlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(playerUuid);

        if(gMusicMain.getConfigService().R_PLAY_ON_JOIN) playSettings.setPlayListMode(GPlayListMode.RADIO);

        if(playSettings.getPlayListMode() == GPlayListMode.RADIO) gMusicMain.getRadioService().addRadioPlayer(player);
        else if(playSettings.isPlayOnJoin()) {
            if(gMusicMain.getPlayService().hasPlayingSong(playerUuid)) gMusicMain.getPlayService().resumeSong(player);
            else {
                GSong song = playSettings.getCurrentSong() != null ? gMusicMain.getSongService().getSongById(playSettings.getCurrentSong()) : null;
                gMusicMain.getPlayService().playSong(player, song != null ? song : gMusicMain.getPlayService().getRandomSong(playerUuid));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        gMusicMain.getRadioService().removeRadioPlayer(player);

        if(gMusicMain.getConfigService().PS_SAVE_ON_QUIT) gMusicMain.getPlaySettingsService().savePlaySettings(player.getUniqueId(), gMusicMain.getPlaySettingsService().getPlaySettings(player.getUniqueId()));
        gMusicMain.getPlaySettingsService().removePlaySettingsCache(player.getUniqueId());
    }

}