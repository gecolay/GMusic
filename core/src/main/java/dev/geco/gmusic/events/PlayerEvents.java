package dev.geco.gmusic.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.MusicGUI;
import dev.geco.gmusic.objects.PlaySettings;
import dev.geco.gmusic.objects.Song;

public class PlayerEvents implements Listener {
	
	private final GMusicMain GPM;
	
    public PlayerEvents(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void PJoiE(PlayerJoinEvent e) {
		
		Player p = e.getPlayer();
		
		UUID u = p.getUniqueId();

		if(GPM.getCManager().CHECK_FOR_UPDATES && !GPM.getUManager().isLatestVersion()) {
			String me = GPM.getMManager().getMessage("Plugin.plugin-update", "%Name%", GPM.NAME, "%NewVersion%", GPM.getUManager().getLatestVersion(), "%Version%", GPM.getUManager().getPluginVersion(), "%Path%", GPM.getDescription().getWebsite());
			if(p.hasPermission(GPM.NAME + ".Update") || p.hasPermission(GPM.NAME + ".*")) p.sendMessage(me);
		}
		
		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(u);
		
		GPM.getValues().putPlaySetting(u, ps);
		
		if(GPM.getCManager().R_PLAY_ON_JOIN) ps.setPlayList(2);
		
		if(ps.getPlayList() == 2) GPM.getValues().addRadioPlayer(p);
		else {
			if(ps.isPlayOnJoin()) {
				
				if(GPM.getSongManager().hasPlayingSong(u)) GPM.getSongManager().resumeSong(p);
				else {
					Song s = ps.getCurrentSong() == null ? GPM.getSongManager().getRandomSong(u) : GPM.getSongManager().getSongById(ps.getCurrentSong());
					GPM.getSongManager().playSong(p, s != null ? s : GPM.getSongManager().getRandomSong(u));
				}
				
			}
		}
		
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void PQuiE(PlayerQuitEvent e) {
		
		Player p = e.getPlayer();
		
		UUID u = p.getUniqueId();
		
		MusicGUI m = GPM.getValues().getMusicGUIs().get(u);
		
		PlaySettings z = GPM.getValues().getPlaySettings().get(u);
		
		if(z.getPlayList() == 2) GPM.getValues().removeRadioPlayer(p);
		
		if(m != null) {
			
			if((GPM.getCManager().P_SAVE_ON_QUIT && z != null && z.isPlayOnJoin() || !GPM.getCManager().P_SAVE_ON_QUIT && GPM.getCManager().P_D_JOIN)) GPM.getSongManager().pauseSong(p);
			else {
				
				m.destroy();
				
				GPM.getValues().removeMusicGUI(u);
				
			}
			
		}
		
		if(GPM.getCManager().P_SAVE_ON_QUIT) {
			
			GPM.getPlaySettingsManager().setPlaySettings(u, z);
			
			GPM.getValues().removePlaySetting(u);
			
		}
		
	}
	
}