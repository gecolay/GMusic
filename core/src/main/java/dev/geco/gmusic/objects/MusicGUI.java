package dev.geco.gmusic.objects;

import java.util.*;

import net.kyori.adventure.text.Component;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.server.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import dev.geco.gmusic.api.event.*;
import dev.geco.gmusic.GMusicMain;

public class MusicGUI {

	private final GMusicMain GPM = GMusicMain.getInstance();
	private final UUID uuid;
	private final MenuType type;
	private final Inventory inventory;
	private final Listener listener;
	private int st = 0;
	private int page;
	private final PlaySettings playSettings;

	private static final long VOLUME_STEPS = 10;
	private static final long RANGE_STEPS = 1;
	private static final long SHIFT_RANGE_STEPS = 10;

	Plugin plugin = Bukkit.getPluginManager().getPlugin(GPM.NAME);
	NamespacedKey localizedNameKey = new NamespacedKey(plugin, "LocalizedName");

	public MusicGUI(UUID UUID, MenuType Type) {

		uuid = UUID;

		type = Type;

		playSettings = GPM.getPlaySettingsManager().getPlaySettings(uuid);

		inventory = Bukkit.createInventory(new InventoryHolder() {

			@Override
			public @NotNull Inventory getInventory() { return inventory; }

		}, 6 * 9, GPM.getMManager().getMessage("MusicGUI.title"));

		setPage(1);

		setDefaultBar();

		listener = new Listener() {

			@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
			public void ICliE(InventoryClickEvent Event) {
				if(Event.getInventory().equals(inventory)) {
					ClickType click = Event.getClick();
					if(GPM.getSVManager().getInventoryFromView(Event.getView(), "getTopInventory").equals(Event.getClickedInventory())) {
						ItemStack itemStack = Event.getCurrentItem();
						if(itemStack != null && !(itemStack.getPersistentDataContainer().isEmpty())) {

							String clickName = itemStack.getPersistentDataContainer().get(localizedNameKey, PersistentDataType.STRING);
							if(clickName.startsWith("!")) setPage(Integer.parseInt(clickName.replace("!", "")));
							else if(clickName.startsWith(".")) setOptionsBar();
							else if(clickName.startsWith("+")) {
								switch(clickName.replace("+", "")) {
									case "v":
										switch(click) {
											case MIDDLE:
												playSettings.setVolume(GPM.getCManager().PS_D_VOLUME);
												break;
											case LEFT:
												playSettings.setVolume(playSettings.getVolume() - VOLUME_STEPS < 0 ? 0 : playSettings.getVolume() - VOLUME_STEPS);
												break;
											case RIGHT:
												playSettings.setVolume(playSettings.getVolume() + VOLUME_STEPS > 100 ? 100 : playSettings.getVolume() + VOLUME_STEPS);
												break;
											default:
												break;
										}
										break;
									case "j":
										playSettings.setPlayOnJoin(click == ClickType.MIDDLE ? GPM.getCManager().PS_D_JOIN : !playSettings.isPlayOnJoin());
										break;
									case "s":
										switch(click) {
											case MIDDLE:
												playSettings.setPlayMode(GPM.getCManager().PS_D_PLAY_MODE);
												break;
											case LEFT:
												playSettings.setPlayMode(playSettings.getPlayMode() - 1 < 0 ? 2 : playSettings.getPlayMode() - 1);
												break;
											case RIGHT:
												playSettings.setPlayMode(playSettings.getPlayMode() + 1 > 2 ? 0 : playSettings.getPlayMode() + 1);
												break;
											default:
												break;
										}
										break;
									case "e":
										playSettings.setShowParticles(click == ClickType.MIDDLE ? GPM.getCManager().PS_D_PARTICLES : !playSettings.isShowingParticles());
										break;
									case "q":
										playSettings.setReverseMode(click == ClickType.MIDDLE ? GPM.getCManager().PS_D_REVERSE : !playSettings.isReverseMode());
										break;
									case "r":
										switch(click) {
											case MIDDLE:
												playSettings.setRange(GPM.getCManager().JUKEBOX_RANGE);
												break;
											case LEFT:
												playSettings.setRange(playSettings.getRange() - RANGE_STEPS < 1 ? 1 : playSettings.getRange() - RANGE_STEPS);
												break;
											case SHIFT_LEFT:
												playSettings.setRange(playSettings.getRange() - SHIFT_RANGE_STEPS < 1 ? 1 : playSettings.getRange() - SHIFT_RANGE_STEPS);
												break;
											case RIGHT:
												playSettings.setRange(playSettings.getRange() + RANGE_STEPS > GPM.getCManager().MAX_JUKEBOX_RANGE ? GPM.getCManager().MAX_JUKEBOX_RANGE : playSettings.getRange() + RANGE_STEPS);
												break;
											case SHIFT_RIGHT:
												playSettings.setRange(playSettings.getRange() + SHIFT_RANGE_STEPS > GPM.getCManager().MAX_JUKEBOX_RANGE ? GPM.getCManager().MAX_JUKEBOX_RANGE : playSettings.getRange() + SHIFT_RANGE_STEPS);
												break;
											default:
												break;
										}
										break;
								}
								setOptionsBar();
							} else if(clickName.startsWith("-")) {
								setDefaultBar();
							} else if(clickName.startsWith(",")) {
								setPlaylistBar();
							} else if(clickName.startsWith("%")) {
								int cpl = playSettings.getPlayList();
								switch(click) {
									case MIDDLE:
										playSettings.setPlayList(GPM.getCManager().PS_D_PLAYLIST);
										break;
									case LEFT:
										playSettings.setPlayList(cpl - 1 < 0 ? 2 : cpl - 1);
										break;
									case RIGHT:
										playSettings.setPlayList(cpl + 1 > 2 ? 0 : cpl + 1);
										break;
									default:
										break;
								}
								if(type == MenuType.DEFAULT) {
									Player t = Bukkit.getPlayer(uuid);
									if(playSettings.getPlayList() == 2) GPM.getRadioManager().addRadioPlayer(t);
									else GPM.getRadioManager().removeRadioPlayer(t);
									if(playSettings.getPlayList() == 2) GPM.getPlaySongManager().stopSong(t);
									else if(cpl != 2 && GPM.getPlaySongManager().hasPlayingSong(uuid) && playSettings.getPlayList() == 1 && !playSettings.getFavorites().contains(GPM.getPlaySongManager().getPlayingSong(uuid))) GPM.getPlaySongManager().stopSong(t);
								} else {
									/*if(playSettings.getPlayList() == 2) {
										playSettings.setReverseMode(false);
										playSettings.setPlayOnJoin(false);
										GPM.getRadioManager().addRadioJukeBox(uuid);
									} else GPM.getRadioManager().removeRadioJukeBox(uuid);*/
									if(playSettings.getPlayList() == 2 || cpl == 2) GPM.getBoxSongManager().stopBoxSong(UUID);
									else if(GPM.getPlaySongManager().hasPlayingSong(uuid) && playSettings.getPlayList() == 1 && !playSettings.getFavorites().contains(GPM.getPlaySongManager().getPlayingSong(uuid))) GPM.getBoxSongManager().stopBoxSong(UUID);
								}
								setPage(1);
								setPlaylistBar();
							} else if(clickName.startsWith("=")) {
								String id = clickName.substring(1);
								Song S = GPM.getSongManager().getSongById(id);
								if(click == ClickType.MIDDLE) {
									if(playSettings.getFavorites().contains(S)) playSettings.removeFavoriteSong(S);
									else playSettings.addFavoriteSong(S);
									setPage(page);
								} else {
									if(type == MenuType.DEFAULT) GPM.getPlaySongManager().playSong(Bukkit.getPlayer(uuid), S);
									else GPM.getBoxSongManager().playBoxSong(UUID, S);
									setPauseResumeBar();
								}
							} else if(clickName.startsWith("/")) {
								if(type == MenuType.DEFAULT) {
									Player t = Bukkit.getPlayer(uuid);
									switch(clickName.replace("/", "")) {
										case "s":
											GPM.getPlaySongManager().stopSong(t);
											break;
										case "r":
											GPM.getPlaySongManager().resumeSong(t);
											break;
										case "p":
											GPM.getPlaySongManager().pauseSong(t);
											break;
										case "q":
											GPM.getPlaySongManager().playSong(t, GPM.getPlaySongManager().getRandomSong(uuid));
											break;
										case "l":
											GPM.getPlaySongManager().playSong(t, GPM.getPlaySongManager().getNextSong(t));
											break;
										default:
											break;
									}
								} else {
									switch(clickName.replace("/", "")) {
										case "s":
											GPM.getBoxSongManager().stopBoxSong(UUID);
											break;
										case "r":
											GPM.getBoxSongManager().resumeBoxSong(UUID);
											break;
										case "p":
											GPM.getBoxSongManager().pauseBoxSong(UUID);
											break;
										case "q":
											GPM.getBoxSongManager().playBoxSong(UUID, GPM.getPlaySongManager().getRandomSong(uuid));
											break;
										case "l":
											GPM.getBoxSongManager().playBoxSong(UUID, GPM.getBoxSongManager().getNextSong(UUID));
											break;
										default:
											break;
									}
								}
								setPauseResumeBar();
							}

						}

						Event.setCancelled(true);
					} else if(Event.getView().getBottomInventory().equals(Event.getClickedInventory())) {
						switch(click) {
							case SHIFT_RIGHT:
							case SHIFT_LEFT:
								Event.setCancelled(true);
								break;
							default:
								break;
						}
					}
				}
			}

			@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
			public void IDraE(InventoryDragEvent Event) {
				if(Event.getInventory().equals(inventory)) {
					for(int slot : Event.getRawSlots()) {
						if(slot < inventory.getSize()) {
							Event.setCancelled(true);
							return;
						}
					}
				}
			}

			@EventHandler
			public void ICloE(InventoryCloseEvent Event) { if(Event.getInventory().equals(inventory)) close(false); }

			@EventHandler (ignoreCancelled = true)
			public void IOpeE(InventoryOpenEvent Event) { if(Event.getInventory().equals(inventory)) setPauseResumeBar(); }

			@EventHandler
			public void GMusR(GMusicReloadEvent Event) { if(Event.getPlugin().equals(GPM)) close(true); }

			@EventHandler
			public void PDisE(PluginDisableEvent Event) { if(Event.getPlugin().equals(GPM)) close(true); }
		};

		GPM.getSongManager().putMusicGUI(uuid, this);

		Bukkit.getPluginManager().registerEvents(listener, GPM);
	}

	public void close(boolean Force) {
		if(Force) for(HumanEntity entity : new ArrayList<>(inventory.getViewers())) entity.closeInventory();
		HandlerList.unregisterAll(listener);
		GPM.getSongManager().getMusicGUIs().remove(uuid);
	}

	public int getMenuState() { return st; }

	private void clearBar() { for(int z = 45; z < 52; z++) inventory.setItem(z, null); }

	public void setDefaultBar() {

		st = 0;

		clearBar();

		ItemStack lp;
		ItemMeta lpm;

		if(playSettings.getPlayList() != 2) {
			lp = new ItemStack(Material.BARRIER);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-stop")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "/s");
			lp.setItemMeta(lpm);
			inventory.setItem(46, lp);

			if(!GPM.getCManager().G_DISABLE_RANDOM_SONG) {
				lp = new ItemStack(Material.ENDER_PEARL);
				lpm = lp.getItemMeta();
				lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-random")));
				lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "/q");
				lp.setItemMeta(lpm);
				inventory.setItem(47, lp);
			}
		}

		if(!GPM.getCManager().G_DISABLE_PLAYLIST) {
			lp = new ItemStack(Material.ENDER_CHEST);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-playlist")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, ",");
			lp.setItemMeta(lpm);
			inventory.setItem(49, lp);
		}

		if(!GPM.getCManager().G_DISABLE_OPTIONS) {
			lp = new ItemStack(Material.HOPPER);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, ".");
			lp.setItemMeta(lpm);
			inventory.setItem(50, lp);
		}

		setPauseResumeBar();
	}

	public void setPauseResumeBar() {

		if(st != 0 || playSettings.getPlayList() == 2) return;

		SongSettings songSettings = GPM.getPlaySongManager().getSongSettings(uuid);

		if(songSettings != null) {
			ItemStack lp = new ItemStack(Material.END_CRYSTAL);
			ItemMeta lpm = lp.getItemMeta();
			if(songSettings.isPaused()) {
				lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-resume")));
				lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "/r");
			} else {
				lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-pause")));
				lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "/p");
			}
			lp.setItemMeta(lpm);
			inventory.setItem(45, lp);
		} else inventory.setItem(45, null);
	}

	public void setOptionsBar() {

		st = 1;

		clearBar();

		ItemStack lp = new ItemStack(Material.CHEST);
		ItemMeta lpm = lp.getItemMeta();
		lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-back")));
		lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "-");
		lp.setItemMeta(lpm);
		inventory.setItem(45, lp);
		lp = new ItemStack(Material.MAGMA_CREAM);
		lpm = lp.getItemMeta();
		lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + playSettings.getVolume())));
		lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+v");
		lp.setItemMeta(lpm);
		inventory.setItem(46, lp);
		lp = new ItemStack(Material.FIREWORK_ROCKET);
		lpm = lp.getItemMeta();
		lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options-particle", "%Particle%", GPM.getMManager().getMessage(playSettings.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false"))));
		lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+e");
		lp.setItemMeta(lpm);
		inventory.setItem(47, lp);

		if(playSettings.getPlayList() != 2) {
			lp = new ItemStack(Material.DIAMOND);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options-join", "%Join%", GPM.getMManager().getMessage(playSettings.isPlayOnJoin() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false"))));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+j");
			lp.setItemMeta(lpm);
			inventory.setItem(48, lp);
			lp = new ItemStack(Material.BLAZE_POWDER);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage(playSettings.getPlayMode() == 0 ? "MusicGUI.music-options-playmode-once" : playSettings.getPlayMode() == 1 ? "MusicGUI.music-options-playmode-shuffle" : "MusicGUI.music-options-playmode-repeat")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+s");
			lp.setItemMeta(lpm);
			inventory.setItem(49, lp);
			lp = new ItemStack(Material.TOTEM_OF_UNDYING);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options-reverse", "%Reverse%", GPM.getMManager().getMessage(playSettings.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false"))));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+q");
			lp.setItemMeta(lpm);
			inventory.setItem(50, lp);
		}

		if(type == MenuType.JUKEBOX || type == MenuType.FULLJUKEBOX) {
			lp = new ItemStack(Material.REDSTONE);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-options-range", "%Range%", "" + playSettings.getRange())));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "+r");
			lp.setItemMeta(lpm);
			inventory.setItem(51, lp);
		}
	}

	public void setPlaylistBar() {

		st = 2;

		clearBar();

		ItemStack lp = new ItemStack(Material.CHEST);
		ItemMeta lpm = lp.getItemMeta();
		lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-back")));
		lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "-");
		lp.setItemMeta(lpm);
		inventory.setItem(45, lp);

		if(playSettings.getPlayList() != 2) {
			lp = new ItemStack(Material.FEATHER);
			lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.music-playlist-skip")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "/l");
			lp.setItemMeta(lpm);
			inventory.setItem(47, lp);
		}

		lp = new ItemStack(Material.NOTE_BLOCK);
		lpm = lp.getItemMeta();
		lpm.displayName(Component.text(GPM.getMManager().getMessage(playSettings.getPlayList() == 0 ? "MusicGUI.music-playlist-type-default" : playSettings.getPlayList() == 1 ? "MusicGUI.music-playlist-type-favorites" : "MusicGUI.music-playlist-type-radio")));
		lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "%");
		lp.setItemMeta(lpm);
		inventory.setItem(49, lp);
	}

	public void setPage(int Page) {

		page = Page;

		List<Song> songs = new ArrayList<>();

		if(playSettings.getPlayList() != 2) songs = playSettings.getPlayList() == 1 ? playSettings.getFavorites() : GPM.getSongManager().getSongs();

		if(page > getMaxPageSize(songs)) page = getMaxPageSize(songs);

		for(int z = 0; z < 45; z++) inventory.setItem(z, null);

		if(!songs.isEmpty()) {
			for(int z = (page - 1) * 45; z < 45 * page && z < songs.size(); z++) {
				Song s1 = songs.get(z);
				ItemStack is = new ItemStack(s1.getMaterial());
				ItemMeta im = is.getItemMeta();
				im.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.disc-title", "%Title%", s1.getTitle(), "%Author%", s1.getAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-author") : s1.getAuthor(), "%OAuthor%", s1.getOriginalAuthor().isEmpty() ? GPM.getMManager().getMessage("MusicGUI.disc-empty-oauthor") : s1.getOriginalAuthor())));
				ArrayList<Component> dl = new ArrayList<>();
				for(String d : s1.getDescription()) dl.add(Component.text(GPM.getMManager().toFormattedMessage("&6" + d)));
				if(playSettings.getFavorites().contains(s1)) dl.add(Component.text(GPM.getMManager().getMessage("MusicGUI.disc-favorite")));
				im.lore(dl);
				im.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "=" + s1.getId());
				im.addItemFlags(ItemFlag.values());
				is.setItemMeta(im);
				inventory.setItem(z % 45, is);
			}
		}

		if(page > 1) {
			ItemStack lp = new ItemStack(Material.ARROW);
			ItemMeta lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.last-page")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "!" + (page - 1));
			lp.setItemMeta(lpm);
			inventory.setItem(52, lp);
		} else {
			ItemStack lp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta lpm = lp.getItemMeta();
			lpm.displayName(Component.text(" "));
			lp.setItemMeta(lpm);
			inventory.setItem(52, lp);
		}

		if(page < getMaxPageSize(songs)) {
			ItemStack lp = new ItemStack(Material.ARROW);
			ItemMeta lpm = lp.getItemMeta();
			lpm.displayName(Component.text(GPM.getMManager().getMessage("MusicGUI.next-page")));
			lpm.getPersistentDataContainer().set(localizedNameKey, PersistentDataType.STRING, "!" + (page + 1));
			lp.setItemMeta(lpm);
			inventory.setItem(53, lp);
		} else {
			ItemStack lp = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta lpm = lp.getItemMeta();
			lpm.displayName(Component.text(" "));
			lp.setItemMeta(lpm);
			inventory.setItem(53, lp);
		}
	}

	private int getMaxPageSize(List<Song> Songs) {
		int i = Songs.size();
		return (i / 45) + (i % 45 == 0 ? 0 : 1);
	}

	public UUID getOwner() { return uuid; }

	public MenuType getMenuType() { return type; }

	public PlaySettings getPlaySettings() { return playSettings; }

	public Inventory getInventory() { return inventory; }

	public enum MenuType {

		DEFAULT,
		JUKEBOX,
		FULLJUKEBOX;
	}

}