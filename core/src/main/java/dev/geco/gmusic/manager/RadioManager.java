package dev.geco.gmusic.manager;

import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;

import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class RadioManager {
	
	private final GMusicMain GPM;
	
	private final Random r = new Random();
	
	private final UUID u = UUID.randomUUID();
	
    public RadioManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
    
    public void playRadio() {
    	
		PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(u);
		GPM.getValues().putPlaySetting(u, ps);
		
		playRadioSong(GPM.getSongManager().getRandomSong(u), 0);
    	
    }
    
    public void playRadioSong(Song S, long Delay) {
    	
    	if(S == null) return;
    	
    	SongSettings t1 = GPM.getValues().getSongSettings().get(u);
    	
    	if(t1 != null) t1.getTimer().cancel();
    	
    	PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(u);
    	
    	Timer t = new Timer();
		
    	SongSettings ss = new SongSettings(S, t, ps.isReverseMode() ? S.getLength() + Delay : 0 - Delay);
		
		GPM.getValues().putSongSettings(u, ss);
		
		ps.setCurrentSong(S.getId());
		
		if(GPM.getCManager().A_SHOW_MESSAGES) {
    		TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-play", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));
    		for(Player P : GPM.getValues().getRadioPlayers()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
    	}
		
		for(UUID U : GPM.getValues().getRadioJukeBoxes()) GPM.getBoxSongManager().playBoxSong(U, S);
		
		playTimer(S, t);
    	
    }
    
    private void playTimer(Song S, Timer T) {
    	
    	SongSettings ss = GPM.getValues().getSongSettings().get(u);
    	
    	TextComponent anp = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));
    	
    	T.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				long z = ss.getPosition();
				
				PlaySettings ps = GPM.getValues().getPlaySettings().get(u);
				
				if(ps != null) {
					
					List<NotePart> lnp = S.getContent().get(z);
					
					List<Player> pl = new ArrayList<>();
					GPM.getValues().getRadioPlayers().forEach(i -> pl.add(i));	
					
					if(lnp != null && ps.getVolume() > 0 && pl.size() > 0) {
						
						for(Player P : pl) {
							PlaySettings ps1 = GPM.getValues().getPlaySettings().get(P.getUniqueId());
							if(ps1.isShowingParticles()) P.spawnParticle(Particle.NOTE, P.getEyeLocation().clone().add(r.nextDouble() - 0.5, 0.3, r.nextDouble() - 0.5), 0, r.nextDouble(), r.nextDouble(), r.nextDouble(), 1);
						}
						
						for(NotePart np : lnp) {
							
							for(Player P : pl) {
								
								if(np.getSound() != null) {
									
									PlaySettings ps1 = GPM.getValues().getPlaySettings().get(P.getUniqueId());
									
									float v = np.isVariableVolume() ? ps1.getFixedVolume() : np.getVolume();
									
									Location L = np.getDistance() == 0 ? P.getLocation() : GPM.getUtilMath().convertToStero(P.getLocation(), np.getDistance());
									
									if(!GPM.getCManager().USE_ENVIRONMENT_EFFECT) P.playSound(L, np.getSound(), S.getCategory(), v, np.getPitch());
									else {
										
										if(GPM.getUtilCheck().isPlayerSwimming(P)) P.playSound(L, np.getSound(), S.getCategory(), v > 0.4f ? v - 0.3f : v, np.getPitch() - 0.15f);
										else P.playSound(L, np.getSound(), S.getCategory(), v, np.getPitch());
										
									}
									
								} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), S.getCategory());
								
							}
							
						}
						
					}
					
					if(z == (ps.isReverseMode() ? 0 : S.getLength())) {
						
						T.cancel();
						
						playRadioSong(GPM.getSongManager().getShuffleSong(u, S), GPM.getCManager().P_WAIT_TIME_UNTIL_SHUFFLE);
						
					} else {
						
						ss.setPosition(ps.isReverseMode() ? z - 1 : z + 1);
						
						if(GPM.getCManager().A_SHOW_ALWAYS_WHILE_PLAYING) for(Player P : pl) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);
						
					}
					
				} else T.cancel();
				
			}
			
		}, 0, 1);
    	
    }
    
    public void stopRadio() {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(u);
    	
    	if(t != null) {
    		
    		t.getTimer().cancel();
			
    		GPM.getValues().removeSongSettings(u);
    		GPM.getValues().removePlaySetting(u);
    		
    	}
    	
    }
    
}