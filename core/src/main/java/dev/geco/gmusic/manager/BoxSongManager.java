package dev.geco.gmusic.manager;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.geco.gmusic.api.events.*;
import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BoxSongManager {
	
	private final GMusicMain GPM;
	
	private final Random r = new Random();
	
    public BoxSongManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
    public void playBoxSong(UUID U, Song S) { playBoxSong(U, S, 0); }
    
    private void playBoxSong(UUID U, Song S, long Delay) {
    	
    	if(S == null) return;
		
    	SongSettings os = GPM.getValues().getSongSettings().get(U);
    	
    	if(os != null) os.getTimer().cancel();
    	
		Timer t = new Timer();
		
		PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
		
		SongSettings ss = new SongSettings(S, t, ps.isReverseMode() ? S.getLength() + Delay : 0 - Delay);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				Bukkit.getPluginManager().callEvent(new BoxSongPlayEvent(U, ss));
				
			}
			
		}.runTask(GPM);
		
		GPM.getValues().putSongSettings(U, ss);
		
		ps.setCurrentSong(S.getId());
		
		if(GPM.getCManager().A_SHOW_MESSAGES) {
    		TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-play", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));
    		for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
    	}
		
		playBoxTimer(U, S, t);
    	
    }
    
    private void playBoxTimer(UUID U, Song S, Timer T) {
    	
    	SongSettings ss = GPM.getValues().getSongSettings().get(U);
    	
    	Location L = GPM.getValues().getJukeBlocks().get(U);
    	
    	if(L == null) return;
    	
    	TextComponent anp = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));
    	
    	T.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				long z = ss.getPosition();
				
				PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
				
				if(ps != null) {
					
					List<NotePart> lnp = S.getContent().get(z);
					
					HashMap<Player, Double> pl = GPM.getJukeBoxManager().getPlayersInRange(L, ps.getRange());
					
					if(lnp != null && ps.getVolume() > 0 && pl.size() > 0) {
						
						if(ps.isShowingParticles()) {
							Location PL = L.clone().add(r.nextDouble() - 0.5, 1, r.nextDouble() - 0.5);
							for(Player P : pl.keySet()) P.spawnParticle(Particle.NOTE, PL, 0, r.nextDouble(), r.nextDouble(), r.nextDouble(), 1);
						}
						
						for(NotePart np : lnp) {
							
							for(Player P : pl.keySet()) {
								
								if(np.getSound() != null) {
									
									float v = np.isVariableVolume() ? (float) ((pl.get(P) - ps.getRange()) * ps.getFixedVolume() / (double) -ps.getRange()) : np.getVolume();
									
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
						
						if(ps.getPlayMode() == 2 && ps.getPlayList() != 2) {
							
							z = ps.isReverseMode() ? S.getLength() + GPM.getCManager().P_WAIT_TIME_UNTIL_REPEAT : -GPM.getCManager().P_WAIT_TIME_UNTIL_REPEAT;
							
							ss.setPosition(z);
							
						} else {
							
							T.cancel();
							
							if(ps.getPlayMode() == 1 && ps.getPlayList() != 2) playBoxSong(U, GPM.getSongManager().getShuffleSong(U, S), GPM.getCManager().P_WAIT_TIME_UNTIL_SHUFFLE);
							else {
								
								GPM.getValues().removeSongSettings(U);
								
								MusicGUI m = GPM.getValues().getMusicGUIs().get(U);
								
								if(m != null) m.setPauseResumeBar();
								
							}
							
						}
						
					} else {
						
						ss.setPosition(ps.isReverseMode() ? z - 1 : z + 1);
						
						if(GPM.getCManager().A_SHOW_ALWAYS_WHILE_PLAYING) for(Player P : pl.keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);
						
					}
					
				} else T.cancel();
				
			}
			
		}, 0, 1);
    	
    }
    
    public void skipBoxSong(UUID U) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(U);
    	
    	playBoxSong(U, t != null ? GPM.getSongManager().getShuffleSong(U, t.getSong()) : GPM.getSongManager().getRandomSong(U));
    	
    }
    
    public void stopBoxSong(UUID U) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(U);
    	
    	if(t != null) {
    		
    		BoxSongStopEvent bsse = new BoxSongStopEvent(U, t);
    		
    		Bukkit.getPluginManager().callEvent(bsse);
    		
    		t.getTimer().cancel();
			
    		GPM.getValues().removeSongSettings(U);
    		
    		PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
    		
    		ps.setCurrentSong(null);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
    			TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-stop"));
    			for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
    		}
    		
    	}
    	
    }
    
    public void pauseBoxSong(UUID U) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(U);
    	
    	if(t != null) {
    		
    		BoxSongPauseEvent bspe = new BoxSongPauseEvent(U, t);
    		
    		Bukkit.getPluginManager().callEvent(bspe);
    		
    		t.getTimer().cancel();
    		
    		t.setPaused(true);
    		
    		PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
    			TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-pause"));
    			for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
    		}
    		
    	}
    	
    }
    
    public void resumeBoxSong(UUID U) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(U);
    	
    	if(t != null) {
    		
    		BoxSongResumeEvent bsre = new BoxSongResumeEvent(U, t);
    		
    		Bukkit.getPluginManager().callEvent(bsre);
    		
    		t.setTimer(new Timer());
    		
    		t.setPaused(false);
    		
    		PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES && ps != null) {
    			TextComponent anpc = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-resume"));
    			for(Player P : GPM.getJukeBoxManager().getPlayersInRange(GPM.getValues().getJukeBlocks().get(U), ps.getRange()).keySet()) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anpc);
    		}
    		
    		playBoxTimer(U, t.getSong(), t.getTimer());
    		
    	}
    	
    }
    
}