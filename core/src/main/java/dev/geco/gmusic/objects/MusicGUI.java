package dev.geco.gmusic.objects;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import dev.geco.gmusic.api.events.GPluginReloadEvent;
import dev.geco.gmusic.main.GMusicMain;
import dev.geco.gmusic.objects.SearchGUI.*;
import dev.geco.gmusic.values.Values;

public class MusicGUI {
	
	private final UUID u;
	
	private MenuType q;
	
	private final Inventory i;
	
	private final GMusicMain GPM;
	
	private final Listener l;
	
	private String s = "";
	
	private int st = 0;
	
	private int p;
	
	private PlaySettings ps;
	
	public MusicGUI(UUID U, MenuType Type, GMusicMain GPluginMain) {
		
		u = U;
		
		q = Type;
		
		GPM = GPluginMain;
		
		ps = GPM.getValues().getPlaySettings().get(u);
		
		i = Bukkit.createInventory(new InventoryHolder() {
			
			@Override
			public Inventory getInventory() {
				return i;
			}
			
		}, 6 * 9, GPM.getMManager().getMessage("MusicGUI.title"));
		
		setPage(1);
		
		setDefaultBar();
		
		l = new Listener() {
			
			@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
			public void ICliE(InventoryClickEvent e) {
				
				if(e.getInventory().equals(i)) {
					
					ItemStack is = e.getCurrentItem();
					
					if(is != null && is.hasItemMeta() && is.getItemMeta().hasLocalizedName()) {
						
						String z = is.getItemMeta().getLocalizedName();
						
						ClickType f = e.getClick();
						
						if(z.startsWith("!")) setPage(Integer.parseInt(z.replace("!", "")));
						else if(z.startsWith(".")) setOptionsBar();
						else if(z.startsWith("+")) {
							switch(z.replace("+", "")) {
							case "v":
								switch(f) {
								case MIDDLE:
									ps.setVolume(GPM.getCManager().P_D_VOLUME);
									break;
								case LEFT:
									ps.setVolume(ps.getVolume() - Values.VOLUME_STEPS < 0 ? 0 : ps.getVolume() - Values.VOLUME_STEPS);
									break;
								case RIGHT:
									ps.setVolume(ps.getVolume() + Values.VOLUME_STEPS > 100 ? 100 : ps.getVolume() + Values.VOLUME_STEPS);
									break;
								default:
									break;
								}
								break;
							case "j":
								ps.setPlayOnJoin(f == ClickType.MIDDLE ? GPM.getCManager().P_D_JOIN : !ps.isPlayOnJoin());
								break;
							case "s":
								switch(f) {
								case MIDDLE:
									ps.setPlayMode(GPM.getCManager().P_D_PLAYMODE);
									break;
								case LEFT:
									ps.setPlayMode(ps.getPlayMode() - 1 < 0 ? 2 : ps.getPlayMode() - 1);
									break;
								case RIGHT:
									ps.setPlayMode(ps.getPlayMode() + 1 > 2 ? 0 : ps.getPlayMode() + 1);
									break;
								default:
									break;
								}
								break;
							case "e":
								ps.setShowingParticles(f == ClickType.MIDDLE ? GPM.getCManager().P_D_PARTICLES : !ps.isShowingParticles());
								break;
							case "q":
								ps.setReverseMode(f == ClickType.MIDDLE ? GPM.getCManager().P_D_REVERSE : !ps.isReverseMode());
								break;
							case "r":
								switch(f) {
								case MIDDLE:
									ps.setRange(GPM.getCManager().JUKEBOX_RANGE);
									break;
								case LEFT:
									ps.setRange(ps.getRange() - Values.RANGE_STEPS < 1 ? 1 : ps.getRange() - Values.RANGE_STEPS);
									break;
								case SHIFT_LEFT:
									ps.setRange(ps.getRange() - Values.SHIFT_RANGE_STEPS < 1 ? 1 : ps.getRange() - Values.SHIFT_RANGE_STEPS);
									break;
								case RIGHT:
									ps.setRange(ps.getRange() + Values.RANGE_STEPS > GPM.getCManager().JUKEBOX_MAX_RANGE ? GPM.getCManager().JUKEBOX_MAX_RANGE : ps.getRange() + Values.RANGE_STEPS);
									break;
								case SHIFT_RIGHT:
									ps.setRange(ps.getRange() + Values.SHIFT_RANGE_STEPS > GPM.getCManager().JUKEBOX_MAX_RANGE ? GPM.getCManager().JUKEBOX_MAX_RANGE : ps.getRange() + Values.SHIFT_RANGE_STEPS);
									break;
								default:
									break;
								}
								break;
							}
							setOptionsBar();
						} else if(z.startsWith("-")) setDefaultBar();
						else if(z.startsWith("?")) {
							if(f == ClickType.LEFT) {
								Player p = (Player) e.getWhoClicked();
								SearchGUI igui = new SearchGUI(p, new AnvilClickEventHandler() {
									public void onAnvilClick(AnvilClickEvent e) {
										s = e.getText();
										setPage(1);
										setDefaultBar();
										new BukkitRunnable() {
											@Override
											public void run() { p.openInventory(i); }
										}.runTaskLater(GPM, 0);
									}
								}, GPM);
								igui.openInventory();
							} else if((f == ClickType.RIGHT || f == ClickType.MIDDLE) && !s.equals("")) {
								s = "";
								setPage(1);
								setDefaultBar();
							}
						} else if(z.startsWith(",")) {
							setPlaylistBar();
						} else if(z.startsWith("%")) {
							int cpl = ps.getPlayList();
							switch(f) {
							case MIDDLE:
								ps.setPlayList(GPM.getCManager().P_D_PLAYLIST);
								break;
							case LEFT:
								ps.setPlayList(cpl - 1 < 0 ? 2 : cpl - 1);
								break;
							case RIGHT:
								ps.setPlayList(cpl + 1 > 2 ? 0 : cpl + 1);
								break;
							default:
								break;
							}
							if(q == MenuType.DEFAULT) {
								Player t = Bukkit.getPlayer(u);
								if(ps.getPlayList() == 2) GPM.getValues().addRadioPlayer(t);
								else GPM.getValues().removeRadioPlayer(t);
								if(ps.getPlayList() == 2) GPM.getSongManager().stopSong(t);
								else if(cpl != 2 && GPM.getSongManager().hasPlayingSong(u) && ps.getPlayList() == 1 && !ps.getFavorites().contains(GPM.getSongManager().getPlayingSong(u))) GPM.getSongManager().stopSong(t);
							} else {
								if(ps.getPlayList() == 2) {
									ps.setReverseMode(false);
									ps.setPlayOnJoin(false);
									GPM.getValues().addRadioJukeBox(u);
								} else GPM.getValues().removeRadioJukeBox(u);
								if(ps.getPlayList() == 2 || cpl == 2) GPM.getBoxSongManager().stopBoxSong(U);
								else if(GPM.getSongManager().hasPlayingSong(u) && ps.getPlayList() == 1 && !ps.getFavorites().contains(GPM.getSongManager().getPlayingSong(u))) GPM.getBoxSongManager().stopBoxSong(U);
							}
							setPage(1);
							setPlaylistBar();
						} else if(z.startsWith("=")) {
							String id = z.substring(1);
							Song S = GPM.getValues().getSongs().stream().filter(so -> so.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
							if(f == ClickType.MIDDLE) {
								if(ps.getFavorites().contains(S)) ps.removeFavoriteSong(S);
								else ps.addFavoriteSong(S);
								setPage(p);
							} else {
								if(q == MenuType.DEFAULT) GPM.getSongManager().playSong(Bukkit.getPlayer(u), S);
								else GPM.getBoxSongManager().playBoxSong(U, S);
								setPauseResumeBar();
							}
						} else if(z.startsWith("/")) {
							if(q == MenuType.DEFAULT) {
								Player t = Bukkit.getPlayer(u);
								switch(z.replace("/", "")) {
								case "s":
									GPM.getSongManager().stopSong(t);
									break;
								case "r":
									GPM.getSongManager().resumeSong(t);
									break;
								case "p":
									GPM.getSongManager().pauseSong(t);
									break;
								case "q":
									GPM.getSongManager().playSong(t, GPM.getSongManager().getRandomSong(u));
									break;
								case "l":
									GPM.getSongManager().skipSong(t);
									break;
								default:
									break;
								}
							} else {
								switch(z.replace("/", "")) {
								case "s":
									GPM.getBoxSongManager().stopBoxSong(U);
									break;
								case "r":
									GPM.getBoxSongManager().resumeBoxSong(U);
									break;
								case "p":
									GPM.getBoxSongManager().pauseBoxSong(U);
									break;
								case "q":
									GPM.getBoxSongManager().playBoxSong(U, GPM.getSongManager().getRandomSong(u));
									break;
								case "l":
									GPM.getBoxSongManager().skipBoxSong(U);
									break;
								default:
									break;
								}
							}
							setPauseResumeBar();
						}
						
					}
					
					e.setCancelled(true);
					e.setResult(Result.DENY);
					
				}
				
			}
			
			@EventHandler (ignoreCancelled = true)
			public void IDraE(InventoryDragEvent e) {
				
				if(e.getInventory().equals(i)) e.setCancelled(true);
				
			}
			
			@EventHandler (ignoreCancelled = true)
			public void IOpeE(InventoryOpenEvent e) {
				
				if(e.getInventory().equals(i)) setPauseResumeBar();
				
			}
			
			@EventHandler
			public void PDisE(PluginDisableEvent e) {
				
				if(GPM.equals(e.getPlugin())) destroy();
				
			}
			
			@EventHandler
			public void GPluRE(GPluginReloadEvent e) {
				
				if(GPM.equals(e.getPlugin())) destroy();
				
			}
			
		};
		
		GPM.getValues().putMusicGUI(u, this);
		
		Bukkit.getPluginManager().registerEvents(l, GPM);
		
	}
	
	public void destroy() {
		
		List<HumanEntity> r = new ArrayList<>();
		r.addAll(i.getViewers());
		for(HumanEntity i : r) i.closeInventory();
		
		HandlerList.unregisterAll(l);
		
	}
	
	public int getMenuState() { return st; }
	
	public String getSearch() { return s; }
	
	private void clearBar() { for(int z = 45; z < 52; z++) i.setItem(z, null); }
	
	public void setDefaultBar() {
		
		st = 0;
		
		clearBar();
		
		ItemStack lp = null;
		
		ItemMeta lpm = null;
		
		if(ps.getPlayList() != 2) {
			
			lp = new ItemStack(Material.BARRIER);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-stop"));
			
			lpm.setLocalizedName("/s");
			
			lp.setItemMeta(lpm);
			
			i.setItem(46, lp);
			
			if(!GPM.getCManager().G_DISABLE_RANDOM_SONG) {
				
				lp = new ItemStack(Material.ENDER_PEARL);
				
				lpm = lp.getItemMeta();
				
				lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-random"));
				
				lpm.setLocalizedName("/q");
				
				lp.setItemMeta(lpm);
				
				i.setItem(47, lp);
				
			}
			
			if(!GPM.getCManager().G_DISABLE_SEARCH) {
				
				try { lp = new ItemStack(Material.OAK_SIGN); } catch(NoSuchFieldError e) { lp = new ItemStack(Material.valueOf("SIGN")); }
				
				lpm = lp.getItemMeta();
				
				lpm.setDisplayName(GPM.getMManager().getMessage(s.equals("") ? "MusicGUI.music-search-none" : "MusicGUI.music-search", "%Search%", s));
				
				lpm.setLocalizedName("?");
				
				lp.setItemMeta(lpm);
				
				i.setItem(51, lp);
				
			}
			
		}
		
		if(!GPM.getCManager().G_DISABLE_PLAYLIST) {
			
			lp = new ItemStack(Material.ENDER_CHEST);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-playlist"));
			
			lpm.setLocalizedName(",");
			
			lp.setItemMeta(lpm);
			
			i.setItem(49, lp);
			
		}
		
		if(!GPM.getCManager().G_DISABLE_OPTIONS) {
			
			lp = new ItemStack(Material.HOPPER);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options"));
			
			lpm.setLocalizedName(".");
			
			lp.setItemMeta(lpm);
			
			i.setItem(50, lp);
			
		}
		
		setPauseResumeBar();
		
	}
	
	public void setPauseResumeBar() {
		
		if(st != 0 || ps.getPlayList() == 2) return;
		
		SongSettings t = GPM.getValues().getSongSettings().get(u);
		
		if(t != null) {
			
			ItemStack lp = new ItemStack(Material.END_CRYSTAL);
			
			ItemMeta lpm = lp.getItemMeta();
			
			if(t.isPaused()) {
				
				lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-resume"));
				
				lpm.setLocalizedName("/r");
				
			} else {
				
				lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-pause"));
				
				lpm.setLocalizedName("/p");
				
			}
			
			lp.setItemMeta(lpm);
			
			i.setItem(45, lp);
			
		} else i.setItem(45, null);
		
	}
	
	public void setOptionsBar() {
		
		st = 1;
		
		clearBar();
		
		ItemStack lp = new ItemStack(Material.CHEST);
		
		ItemMeta lpm = lp.getItemMeta();
		
		lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-back"));
		
		lpm.setLocalizedName("-");
		
		lp.setItemMeta(lpm);
		
		i.setItem(45, lp);
		
		lp = new ItemStack(Material.MAGMA_CREAM);
		
		lpm = lp.getItemMeta();
		
		lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + ps.getVolume()));
		
		lpm.setLocalizedName("+v");
		
		lp.setItemMeta(lpm);
		
		i.setItem(46, lp);
		
		lp = new ItemStack(Material.FIREWORK_ROCKET);
		
		lpm = lp.getItemMeta();
		
		lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options-particle", "%Particle%", GPM.getMManager().getMessage(ps.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
		
		lpm.setLocalizedName("+e");
		
		lp.setItemMeta(lpm);
		
		i.setItem(47, lp);
		
		if(ps.getPlayList() != 2) {
			
			lp = new ItemStack(Material.DIAMOND);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options-join", "%Join%", GPM.getMManager().getMessage(ps.isPlayOnJoin() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			
			lpm.setLocalizedName("+j");
			
			lp.setItemMeta(lpm);
			
			i.setItem(48, lp);
			
			lp = new ItemStack(Material.BLAZE_POWDER);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage(ps.getPlayMode() == 0 ? "MusicGUI.music-options-playmode-once" : ps.getPlayMode() == 1 ? "MusicGUI.music-options-playmode-shuffle" : "MusicGUI.music-options-playmode-repeat"));
			
			lpm.setLocalizedName("+s");
			
			lp.setItemMeta(lpm);
			
			i.setItem(49, lp);
			
			lp = new ItemStack(Material.TOTEM_OF_UNDYING);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options-reverse", "%Reverse%", GPM.getMManager().getMessage(ps.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			
			lpm.setLocalizedName("+q");
			
			lp.setItemMeta(lpm);
			
			i.setItem(50, lp);
			
		}
		
		if(q == MenuType.JUKEBOX || q == MenuType.FULLJUKEBOX) {
			
			lp = new ItemStack(Material.REDSTONE);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-options-range", "%Range%", "" + ps.getRange()));
			
			lpm.setLocalizedName("+r");
			
			lp.setItemMeta(lpm);
			
			i.setItem(51, lp);
			
		}
		
	}
	
	public void setPlaylistBar() {
		
		st = 2;
		
		clearBar();
		
		ItemStack lp = new ItemStack(Material.CHEST);
		
		ItemMeta lpm = lp.getItemMeta();
		
		lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-back"));
		
		lpm.setLocalizedName("-");
		
		lp.setItemMeta(lpm);
		
		i.setItem(45, lp);
		
		if(ps.getPlayList() != 2) {
			
			lp = new ItemStack(Material.FEATHER);
			
			lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.music-playlist-skip"));
			
			lpm.setLocalizedName("/l");
			
			lp.setItemMeta(lpm);
			
			i.setItem(47, lp);
			
		}
		
		lp = new ItemStack(Material.NOTE_BLOCK);
		
		lpm = lp.getItemMeta();
		
		lpm.setDisplayName(GPM.getMManager().getMessage(ps.getPlayList() == 0 ? "MusicGUI.music-playlist-type-default" : ps.getPlayList() == 1 ? "MusicGUI.music-playlist-type-favorites" : "MusicGUI.music-playlist-type-radio"));
		
		lpm.setLocalizedName("%");
		
		lp.setItemMeta(lpm);
		
		i.setItem(49, lp);
		
	}
	
	public void setPage(int Page) {
		
		p = Page;
		
		List<Song> S = new ArrayList<Song>();
		
		if(ps.getPlayList() != 2) {
			
			S = ps.getPlayList() == 1 ? ps.getFavorites() : GPM.getValues().getSongs();
			
			if(!s.equals("")) S = GPM.getSongManager().getSongsBySearch(S, s);
			
		}
		
		if(p > getMaxPageSize(S)) p = getMaxPageSize(S);
		
		for(int z = 0; z < 45; z++) i.setItem(z, null);
		
		if(S.size() > 0) {
			
			for(int z = (p - 1) * 45; z < 45 * p && z < S.size(); z++) {
				
				Song s1 = S.get(z);
				
				ItemStack is = new ItemStack(s1.getMaterial());
				
				ItemMeta im = is.getItemMeta();
				
				im.setDisplayName(GPM.getMManager().getMessage("MusicGUI.disc-title", "%Title%", s1.getTitle(), "%Author%", s1.getAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : s1.getAuthor(), "%OAuthor%", s1.getOriginalAuthor().equals("") ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : s1.getOriginalAuthor()));
				
				List<String> dl = new ArrayList<>();
				
				for(String d : s1.getDescription()) dl.add(GPM.getMManager().getColoredMessage("&6" + d));
				
				if(ps.getFavorites().contains(s1)) dl.add(GPM.getMManager().getMessage("MusicGUI.disc-favorite"));
				
				im.setLore(dl);
				
				im.setLocalizedName("=" + s1.getId());
				
				im.addItemFlags(ItemFlag.values());
				
				is.setItemMeta(im);
				
				i.setItem(z % 45, is);
				
			}
			
		}
		
		if(p > 1) {
			
			ItemStack lp = new ItemStack(Material.ARROW);
			
			ItemMeta lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.last-page"));
			
			lpm.setLocalizedName("!" + (p - 1));
			
			lp.setItemMeta(lpm);
			
			i.setItem(52, lp);
			
		} else {
			
			ItemStack lp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			
			ItemMeta lpm = lp.getItemMeta();
			
			lpm.setDisplayName(" ");
			
			lp.setItemMeta(lpm);
			
			i.setItem(52, lp);
			
		}
		
		if(p < getMaxPageSize(S)) {
			
			ItemStack lp = new ItemStack(Material.ARROW);
			
			ItemMeta lpm = lp.getItemMeta();
			
			lpm.setDisplayName(GPM.getMManager().getMessage("MusicGUI.next-page"));
			
			lpm.setLocalizedName("!" + (p + 1));
			
			lp.setItemMeta(lpm);
			
			i.setItem(53, lp);
			
		} else {
			
			ItemStack lp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			
			ItemMeta lpm = lp.getItemMeta();
			
			lpm.setDisplayName(" ");
			
			lp.setItemMeta(lpm);
			
			i.setItem(53, lp);
			
		}
		
	}
	
	private int getMaxPageSize(List<Song> S) {
    	
    	int i = S.size();
    	
    	return (i / 45) + (i % 45 == 0 ? 0 : 1);
    	
    }
	
	public UUID getOwner() { return u; }
	
	public MenuType getMenuType() { return q; }
	
	public PlaySettings getPlaySettings() { return ps; }
	
	public Inventory getInventory() { return i; }
	
	
	public enum MenuType {
		
		DEFAULT,
		JUKEBOX,
		FULLJUKEBOX;
		
	}
	
}