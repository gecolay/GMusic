package dev.geco.gmusic.event;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.PlayListMode;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.PlayState;
import dev.geco.gmusic.model.PlayType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerEventHandler implements Listener {

    private static final String RESOURCE_PACK_URL = "https://github.com/gecolay/GMusic/raw/main/resources/resource_pack/note_block_extended_octave_range.zip";

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

        if(!gMusicMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

        PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(playerUuid, PlayType.DEFAULT);
        if(gMusicMain.getConfigService().R_PLAY_ON_JOIN) playSettings.setPlayListMode(PlayListMode.RADIO);
        if(playSettings.getPlayListMode() == PlayListMode.RADIO) gMusicMain.getRadioService().addRadioPlayer(player);

        PlayState playState = gMusicMain.getPlayService().getPlayState(playerUuid);
        if(playState != null && !playState.isPaused()) gMusicMain.getPlayService().playSong(player, playState.getSong(), -playState.getTickPosition());
    }

    @EventHandler
    public void playerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(playerUuid, PlayType.DEFAULT);

        if(!gMusicMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) {
            gMusicMain.getPlayService().stopSong(player);
            gMusicMain.getRadioService().removeRadioPlayer(player);
            return;
        }

        if(playSettings.getPlayListMode() == PlayListMode.RADIO) gMusicMain.getRadioService().addRadioPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        gMusicMain.getRadioService().removeRadioPlayer(player);

        if(gMusicMain.getConfigService().PS_SAVE_ON_QUIT) {
            PlayState playState = gMusicMain.getPlayService().getPlayState(playerUuid);
            if(playState != null) gMusicMain.getPlayService().savePlayState(playerUuid, playState);

            PlaySettings playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(playerUuid, PlayType.DEFAULT);
            gMusicMain.getPlaySettingsService().savePlaySettings(playerUuid, playSettings);
        }

        gMusicMain.getPlayService().removePlayState(playerUuid);
        gMusicMain.getPlaySettingsService().removePlaySettingsCache(playerUuid);
    }

}