package dev.geco.gmusic.manager;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.geco.gmusic.api.events.*;
import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class SongManager {
	
	private final GMusicMain GPM;
	
	private final Random r = new Random();
	
    public SongManager(GMusicMain GPluginMain) { GPM = GPluginMain; }
	
    public void playSong(Player P, Song S) { playSong(P, S, 0);}
    
    private void playSong(Player P, Song S, long Delay) {
    	
    	if(S == null) return;
		
    	PlaySettings ps = GPM.getValues().getPlaySettings().get(P.getUniqueId());
    	
    	if(ps.getPlayList() == 2) return;
    	
    	SongSettings os = GPM.getValues().getSongSettings().get(P.getUniqueId());
    	
    	if(os != null) os.getTimer().cancel();
    	
		Timer t = new Timer();
		
		SongSettings ss = new SongSettings(S, t, ps.isReverseMode() ? S.getLength() + Delay : 0 - Delay);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				Bukkit.getPluginManager().callEvent(new SongPlayEvent(P, ss));
				
			}
			
		}.runTask(GPM);
		
		GPM.getValues().putSongSettings(P.getUniqueId(), ss);
		
		ps.setCurrentSong(S.getId());
		
		if(GPM.getCManager().A_SHOW_MESSAGES) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-play", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor())));
		
		playTimer(P, S, t);
    	
    }
    
    public Song getSongById(String Song) { return GPM.getValues().getSongs().parallelStream().filter(so -> so.getId().equalsIgnoreCase(Song)).findFirst().orElse(null); }
    
    public Song getRandomSong(UUID U) {
    	PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
    	List<Song> s = ps.getPlayList() == 1 ? ps.getFavorites() : GPM.getValues().getSongs();
    	return s.size() > 0 ? s.get(r.nextInt(s.size())) : null;
    }
    
    public Song getShuffleSong(UUID U, Song S) {
    	PlaySettings ps = GPM.getValues().getPlaySettings().get(U);
    	List<Song> s = ps.getPlayList() == 1 ? ps.getFavorites() : GPM.getValues().getSongs();
    	return s.size() > 0 ? s.indexOf(S) + 1 == s.size() ? s.get(0) : s.get(s.indexOf(S) + 1) : null;
    }
    
    public List<Song> getSongsBySearch(List<Song> S, String Title) { return S.parallelStream().filter(s -> s.getTitle().toLowerCase().contains(Title.toLowerCase())).collect(Collectors.toList()); }
    
    private void playTimer(Player P, Song S, Timer T) {
    	
    	UUID u = P.getUniqueId();
    	
    	SongSettings ss = GPM.getValues().getSongSettings().get(u);
    	
    	TextComponent anp = new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-now-playing", "%Title%", S.getTitle(), "%Author%", S.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : S.getAuthor(), "%OAuthor%", S.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : S.getOriginalAuthor()));
    	
    	T.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				long z = ss.getPosition();
				
				PlaySettings ps = GPM.getValues().getPlaySettings().get(u);
				
				if(ps != null) {
					
					List<NotePart> lnp = S.getContent().get(z);
					
					if(lnp != null && ps.getVolume() > 0) {
						
						if(ps.isShowingParticles()) P.spawnParticle(Particle.NOTE, P.getEyeLocation().clone().add(r.nextDouble() - 0.5, 0.3, r.nextDouble() - 0.5), 0, r.nextDouble(), r.nextDouble(), r.nextDouble(), 1);
						
						for(NotePart np : lnp) {
							
							if(np.getSound() != null) {
								
								float v = np.isVariableVolume() ? ps.getFixedVolume() : np.getVolume();
								
								Location L = np.getDistance() == 0 ? P.getLocation() : GPM.getUtilMath().convertToStero(P.getLocation(), np.getDistance());
								
								if(!GPM.getCManager().USE_ENVIRONMENT_EFFECT) P.playSound(L, np.getSound(), S.getCategory(), v, np.getPitch());
								else {
									
									if(GPM.getUtilCheck().isPlayerSwimming(P)) P.playSound(L, np.getSound(), S.getCategory(), v > 0.4f ? v - 0.3f : v, np.getPitch() - 0.15f);
									else P.playSound(L, np.getSound(), S.getCategory(), v, np.getPitch());
									
								}
								
							} else if(np.getStopSound() != null) P.stopSound(np.getStopSound(), S.getCategory());
							
						}
						
					}
					
					if(z == (ps.isReverseMode() ? 0 : S.getLength())) {
						
						if(ps.getPlayMode() == 2) {
							
							z = ps.isReverseMode() ? S.getLength() + GPM.getCManager().P_WAIT_TIME_UNTIL_REPEAT : -GPM.getCManager().P_WAIT_TIME_UNTIL_REPEAT;
							
							ss.setPosition(z);
							
						} else {
							
							T.cancel();
							
							if(ps.getPlayMode() == 1) playSong(P, getShuffleSong(u, S), GPM.getCManager().P_WAIT_TIME_UNTIL_SHUFFLE);
							else {
								
								GPM.getValues().removeSongSettings(u);
								
								MusicGUI m = GPM.getValues().getMusicGUIs().get(u);
								
								if(m != null) m.setPauseResumeBar();
								
							}
							
						}
						
					} else {
						
						ss.setPosition(ps.isReverseMode() ? z - 1 : z + 1);
						
						if(GPM.getCManager().A_SHOW_ALWAYS_WHILE_PLAYING) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, anp);
						
					}
					
				} else T.cancel();
				
			}
			
		}, 0, 1);
    	
    }
    
    public boolean hasPlayingSong(UUID U) { return GPM.getValues().getSongSettings().get(U) != null; }
    
    public Song getPlayingSong(UUID U) { return GPM.getValues().getSongSettings().get(U).getSong(); }
    
    public void skipSong(Player P) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(P.getUniqueId());
    	
    	playSong(P, t != null ? getShuffleSong(P.getUniqueId(), t.getSong()) : getRandomSong(P.getUniqueId()));
    	
    }
    
    public void stopSong(Player P) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(P.getUniqueId());
    	
    	if(t != null) {
    		
    		SongStopEvent sse = new SongStopEvent(P, t);
    		
    		Bukkit.getPluginManager().callEvent(sse);
    		
    		t.getTimer().cancel();
			
    		GPM.getValues().removeSongSettings(P.getUniqueId());
    		
    		PlaySettings ps = GPM.getValues().getPlaySettings().get(P.getUniqueId());
    		
    		ps.setCurrentSong(null);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-stop")));
    		
    	}
    	
    }
    
    public void pauseSong(Player P) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(P.getUniqueId());
    	
    	if(t != null) {
    		
    		SongPauseEvent spe = new SongPauseEvent(P, t);
    		
    		Bukkit.getPluginManager().callEvent(spe);
    		
    		t.getTimer().cancel();
    		
    		t.setPaused(true);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-pause")));
    		
    	}
    	
    }
    
    public void resumeSong(Player P) {
    	
    	SongSettings t = GPM.getValues().getSongSettings().get(P.getUniqueId());
    	
    	if(t != null) {
    		
    		SongResumeEvent sre = new SongResumeEvent(P, t);
    		
    		Bukkit.getPluginManager().callEvent(sre);
    		
    		t.setTimer(new Timer());
    		
    		t.setPaused(false);
    		
    		if(GPM.getCManager().A_SHOW_MESSAGES) P.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(GPM.getMManager().getMessage("Messages.actionbar-resume")));
    		
    		playTimer(P, t.getSong(), t.getTimer());
    		
    	}
    	
    }
    
}