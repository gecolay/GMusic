package dev.geco.gmusic.model.gui;

import dev.geco.gmusic.api.event.GMusicReloadEvent;
import dev.geco.gmusic.model.PlayListMode;
import dev.geco.gmusic.model.PlayMode;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.PlayState;
import dev.geco.gmusic.model.PlayType;
import dev.geco.gmusic.model.Song;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class MusicGUI {

	private final GMusicMain gMusicMain = GMusicMain.getInstance();
	private final HashMap<Integer, Song> pageSongs = new HashMap<>();
	private static final HashMap<UUID, MusicGUI> musicGUIS = new HashMap<>();
	private static final int VOLUME_STEPS = 10;
	private static final int SHIFT_VOLUME_STEPS = 1;
	private static final long RANGE_STEPS = 1;
	private static final long SHIFT_RANGE_STEPS = 10;
	private static final int DESCRIPTION_MAX_LINE_LENGTH = 80;
	private final UUID uuid;
	private final PlayType playType;
	private final Inventory inventory;
	private final Listener listener;
	private boolean optionState = false;
	private int page = 1;
	private boolean searchMode = false;
	private String searchKey = null;
	private final PlaySettings playSettings;
	private final Map<Integer, ClickHandler> buttons = new HashMap<>();

	public MusicGUI(@NotNull UUID uuid, @NotNull PlayType playType) {
		this.uuid = uuid;
		this.playType = playType;

		musicGUIS.put(uuid, this);

		playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid, playType);
		inventory = Bukkit.createInventory(new InventoryHolder() {

			@Override
			public @NotNull Inventory getInventory() { return inventory; }

		}, 6 * 9, gMusicMain.getMessageService().getMessage(playType == PlayType.RADIO ? "MusicGUI.radio-title" : "MusicGUI.title"));

		setPage(1);

		setDefaultBar();

		listener = new Listener() {

			@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
			public void ICliE(InventoryClickEvent event) {
				if(!event.getInventory().equals(inventory)) return;
				ClickType click = event.getClick();
				if(gMusicMain.getVersionService().executeMethod(event.getView(), "getBottomInventory").equals(event.getClickedInventory())) {
					switch(click) {
						case SHIFT_RIGHT:
						case SHIFT_LEFT:
							event.setCancelled(true);
							break;
					}
					return;
				}
				if(!gMusicMain.getVersionService().executeMethod(event.getView(), "getTopInventory").equals(event.getClickedInventory())) return;
				event.setCancelled(true);
				ItemStack itemStack = event.getCurrentItem();
				if(itemStack == null) return;
				ItemMeta itemMeta = itemStack.getItemMeta();
				HumanEntity clicker = event.getWhoClicked();
				int slot = event.getRawSlot();
				if(slot < 0) {
					return;
				} else if(slot < 45) {
					Song song = pageSongs.get(slot);
					if(song == null) return;
					if(click == ClickType.MIDDLE) {
						if(playSettings.getFavorites().contains(song)) playSettings.getFavorites().remove(song);
						else playSettings.getFavorites().add(song);
						setPage(page);
						return;
					}
					switch(playType) {
						case DEFAULT -> {
							Player target = Bukkit.getPlayer(uuid);
							if(target == null) return;
							gMusicMain.getPlayService().playSong(target, song);
						}
						case JUKEBOX -> gMusicMain.getJukeBoxService().playBoxSong(uuid, song);
						case RADIO -> gMusicMain.getRadioService().playSong(song);
					}
					setPauseResumeBar();
				} else if(slot < 52) {
					ClickHandler handler = buttons.get(slot);
					if(handler != null) handler.onClick(itemMeta, click, clicker);
					itemStack.setItemMeta(itemMeta);
				} else if(slot == 52) {
					setPage(page - 1);
					setPauseResumeBar();
				} else if(slot == 53) {
					setPage(page + 1);
					setPauseResumeBar();
				}
			}

			@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
			public void inventoryDragEvent(InventoryDragEvent event) {
				if(!event.getInventory().equals(inventory)) return;
				for(int slot : event.getRawSlots()) {
					if(slot >= inventory.getSize()) continue;
					event.setCancelled(true);
					return;
				}
			}

			@EventHandler
			public void inventoryCloseEvent(InventoryCloseEvent Event) { if(Event.getInventory().equals(inventory)) close(false); }

			@EventHandler (ignoreCancelled = true)
			public void gMusicReloadEvent(GMusicReloadEvent Event) { if(Event.getPlugin().equals(gMusicMain)) close(true); }

			@EventHandler
			public void pluginDisableEvent(PluginDisableEvent Event) { if(Event.getPlugin().equals(gMusicMain)) close(true); }
		};

		Bukkit.getPluginManager().registerEvents(listener, gMusicMain);
	}

	public static @Nullable MusicGUI getMusicGUI(@NotNull UUID uuid) { return musicGUIS.get(uuid); }

	public void close(boolean force) {
		if(force) for(HumanEntity entity : new ArrayList<>(inventory.getViewers())) entity.closeInventory();
		if(!force && (searchMode || playType == PlayType.JUKEBOX || playType == PlayType.RADIO)) return;
		musicGUIS.remove(uuid);
		HandlerList.unregisterAll(listener);
	}

	private @Nullable MusicInputGUI getInputGUIInstance(@NotNull MusicInputGUI.InputCallback call, @NotNull MusicInputGUI.ValidateCallback validateCall) {
		try {
			Class<?> inputGUIClass = Class.forName(gMusicMain.getVersionService().getPackagePath() + ".model.gui.MusicInputGUI");
			return (MusicInputGUI) inputGUIClass.getConstructor(MusicInputGUI.InputCallback.class, MusicInputGUI.ValidateCallback.class).newInstance(call, validateCall);
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not get input gui instance", e); }
		return null;
	}

	private void clearBar() {
		for(int slot = 45; slot < 52; slot++) setButton(slot, null);
	}

	public void setDefaultBar() {
		optionState = false;

		clearBar();

		ItemStack item;

		List<Song> songs = new ArrayList<>();
		List<Song> filteredSongs = songs;

		if(playSettings.getPlayListMode() != PlayListMode.RADIO) {
			songs = playSettings.getPlayListMode() == PlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
			filteredSongs = songs;
			if(searchKey != null && !searchKey.isEmpty()) filteredSongs = gMusicMain.getSongService().filterSongsBySearch(songs, searchKey);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_RANDOM_SONG && playSettings.getPlayListMode() != PlayListMode.RADIO && !filteredSongs.isEmpty()) {
			item = makeItem(Material.ENDER_PEARL, gMusicMain.getMessageService().getMessage("MusicGUI.music-random"));
			Button button = new Button(item, (itemMeta, click, clicker) -> {
				switch(playType) {
					case DEFAULT -> {
						Player target = Bukkit.getPlayer(uuid);
						if(target == null) return;
						gMusicMain.getPlayService().playSong(target, gMusicMain.getPlayService().getRandomSong(uuid, playType));
					}
					case JUKEBOX -> {
						gMusicMain.getJukeBoxService().playBoxSong(uuid, gMusicMain.getPlayService().getRandomSong(uuid, playType));
					}
					case RADIO -> {
						gMusicMain.getRadioService().playSong(gMusicMain.getPlayService().getRandomSong(uuid, playType));
					}
				}
			});
			setButton(48, button);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_PLAYLIST && playType != PlayType.RADIO) {
			item = makeItem(Material.NOTE_BLOCK, gMusicMain.getMessageService().getMessage(playSettings.getPlayListMode() == PlayListMode.DEFAULT ? "MusicGUI.music-playlist-mode-default" : playSettings.getPlayListMode() == PlayListMode.FAVORITES ? "MusicGUI.music-playlist-mode-favorites" : "MusicGUI.music-playlist-mode-radio"));
			Button button = new Button(item, (itemMeta, click, clicker) -> {
				int playListModeId = playSettings.getPlayListMode().getId();
				PlayListMode playListMode = PlayListMode.byId(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PLAYLIST_MODE : (click == ClickType.RIGHT ? (playListModeId - 1 < 0 ? PlayListMode.values().length - 1 : playListModeId - 1) : (playListModeId + 1 > PlayListMode.values().length - 1 ? 0 : playListModeId + 1)));
				playSettings.setPlayListMode(playListMode);
				switch(playType) {
					case DEFAULT -> {
						Player target = Bukkit.getPlayer(uuid);
						if(playListMode.getId() != playListModeId && target != null) {
							setPage(1);
							gMusicMain.getPlayService().stopSong(target);
						}
						if(playListMode == PlayListMode.RADIO) {
							gMusicMain.getRadioService().addRadioPlayer(target);
						} else {
							gMusicMain.getRadioService().removeRadioPlayer(target);
						}
					}
					case JUKEBOX -> {
						if(playListMode.getId() != playListModeId) {
							setPage(1);
							gMusicMain.getJukeBoxService().stopBoxSong(uuid);
						}
						if(playListMode == PlayListMode.RADIO) {
							gMusicMain.getRadioService().addRadioJukeBox(uuid, gMusicMain.getJukeBoxService().getJukeBoxBlock(uuid));
						} else {
							gMusicMain.getRadioService().removeRadioJukeBox(uuid);
						}
					}
				}
				setDefaultBar();
			});
			setButton(49, button);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_OPTIONS) {
			item = makeItem(Material.HOPPER, gMusicMain.getMessageService().getMessage("MusicGUI.music-options"));
			Button button = new Button(item, (itemMeta, click, clicker) -> {
				setOptionsBar();
			});
			setButton(50, button);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_SEARCH && playSettings.getPlayListMode() != PlayListMode.RADIO && !songs.isEmpty() && gMusicMain.getVersionService().isAvailable()) {
			item = makeItem(Material.OAK_SIGN, searchKey == null || searchKey.isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.music-search-none") : gMusicMain.getMessageService().getMessage("MusicGUI.music-search", "%Search%", searchKey));
			Button button = new Button(item, (itemMeta, click, clicker) -> {
				if(click == ClickType.LEFT) {
					MusicInputGUI inputGUI = getInputGUIInstance((input) -> {
						searchKey = input;
						setPage(1);
						setDefaultBar();
						clicker.openInventory(inventory);
						searchMode = false;
						return true;
					}, ItemMeta::getDisplayName);
					ItemStack nameItem = makeItem(Material.NAME_TAG, gMusicMain.getMessageService().getMessage("MusicGUI.music-search-menu-field"));
					searchKey = null;
					searchMode = true;
					inputGUI.open(clicker, gMusicMain.getMessageService().getMessage("MusicGUI.music-search-menu-title"), nameItem);
				} else if(searchKey != null) {
					searchKey = null;
					setPage(1);
					setDefaultBar();
				}
			});
			setButton(51, button);
		}

		setPauseResumeBar();
	}

	public void setPauseResumeBar() {
		if(optionState || playSettings.getPlayListMode() == PlayListMode.RADIO) return;

		PlayState songSettings = gMusicMain.getPlayService().getPlayState(uuid);

		ItemStack item;

		if(songSettings != null) {
			item = makeItem(Material.END_CRYSTAL, songSettings.isPaused() ? gMusicMain.getMessageService().getMessage("MusicGUI.music-resume") : gMusicMain.getMessageService().getMessage("MusicGUI.music-pause"));
			Button button = new Button(item, (itemMeta, click, clicker) -> {
				PlayState currentSongSettings = gMusicMain.getPlayService().getPlayState(uuid);
				if(currentSongSettings == null) return;
				switch(playType) {
					case DEFAULT -> {
						Player target = Bukkit.getPlayer(uuid);
						if(target == null) return;
						if(currentSongSettings.isPaused()) gMusicMain.getPlayService().resumeSong(target);
						else gMusicMain.getPlayService().pauseSong(target);
					}
					case JUKEBOX -> {
						if(currentSongSettings.isPaused()) gMusicMain.getJukeBoxService().resumeBoxSong(uuid);
						else gMusicMain.getJukeBoxService().pauseBoxSong(uuid);
					}
					case RADIO -> {
						if(currentSongSettings.isPaused()) gMusicMain.getRadioService().resumeSong();
						else gMusicMain.getRadioService().pauseSong();
					}
				}
			});
			setButton(45, button);

			item = makeItem(Material.BARRIER, gMusicMain.getMessageService().getMessage("MusicGUI.music-stop"));
			button = new Button(item, (itemMeta, click, clicker) -> {
				switch(playType) {
					case DEFAULT -> {
						Player target = Bukkit.getPlayer(uuid);
						if(target == null) return;
						gMusicMain.getPlayService().stopSong(target);
					}
					case JUKEBOX -> {
						gMusicMain.getJukeBoxService().stopBoxSong(uuid);
					}
					case RADIO -> {
						gMusicMain.getRadioService().stopSong();
					}
				}
			});
			setButton(46, button);

			item = makeItem(Material.FEATHER, gMusicMain.getMessageService().getMessage("MusicGUI.music-skip"));
			button = new Button(item, (itemMeta, click, clicker) -> {
				switch(playType) {
					case DEFAULT -> {
						Player target = Bukkit.getPlayer(uuid);
						if(target == null) return;
						gMusicMain.getPlayService().playSong(target, gMusicMain.getPlayService().getNextSong(target));
					}
					case JUKEBOX -> {
						gMusicMain.getJukeBoxService().playBoxSong(uuid, gMusicMain.getJukeBoxService().getNextSong(uuid));
					}
					case RADIO -> {
						gMusicMain.getRadioService().playSong(gMusicMain.getRadioService().getNextSong());
					}
				}
			});
			setButton(47, button);

			return;
		}

		setButton(45, null);
		setButton(46, null);
		setButton(47, null);
	}

	public void setOptionsBar() {
		optionState = true;

		clearBar();

		ItemStack item;
		Button button;
		List<Button> buttonsToAdd = new ArrayList<>();

		item = makeItem(Material.CHEST, gMusicMain.getMessageService().getMessage("MusicGUI.music-back"));
		button = new Button(item, (itemMeta, click, clicker) -> {
			setDefaultBar();
		});
		buttonsToAdd.add(button);

		if(playType != PlayType.RADIO) {
			item = makeItem(Material.MAGMA_CREAM, gMusicMain.getMessageService().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + playSettings.getVolume()));
			button = new Button(item, (itemMeta, click, clicker) -> {
                int volumn = playSettings.getVolume();
                int step = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ? SHIFT_VOLUME_STEPS : VOLUME_STEPS;
				int newVolumn = click == ClickType.MIDDLE ? (playType == PlayType.JUKEBOX ? gMusicMain.getConfigService().J_VOLUME : gMusicMain.getConfigService().PS_D_VOLUME) : (click == ClickType.RIGHT ? Math.max(volumn - step, 0) : Math.min(volumn + step, 100));
                playSettings.setVolume(newVolumn);
                itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + newVolumn));
            });
			buttonsToAdd.add(button);

			item = makeItem(Material.FIREWORK_ROCKET, gMusicMain.getMessageService().getMessage("MusicGUI.music-options-particle", "%Particle%", gMusicMain.getMessageService().getMessage(playSettings.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			button = new Button(item, (itemMeta, click, clicker) -> {
                playSettings.setShowParticles(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PARTICLES : !playSettings.isShowingParticles());
                itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-particle", "%Particle%", gMusicMain.getMessageService().getMessage(playSettings.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
            });
			buttonsToAdd.add(button);
		}

		if(playSettings.getPlayListMode() != PlayListMode.RADIO) {
			item = makeItem(Material.BLAZE_POWDER, gMusicMain.getMessageService().getMessage(playSettings.getPlayMode() == PlayMode.DEFAULT ? "MusicGUI.music-options-play-mode-once" : playSettings.getPlayMode() == PlayMode.SHUFFLE ? "MusicGUI.music-options-play-mode-shuffle" : "MusicGUI.music-options-play-mode-repeat"));
			button = new Button(item, (itemMeta, click, clicker) -> {
				int playModeId = playSettings.getPlayMode().getId();
				PlayMode playMode = PlayMode.byId(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PLAY_MODE : (click == ClickType.RIGHT ? (playModeId - 1 < 0 ? PlayMode.values().length - 1 : playModeId - 1) : (playModeId + 1 > PlayMode.values().length - 1 ? 0 : playModeId + 1)));
				playSettings.setPlayMode(playMode);
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playMode == PlayMode.DEFAULT ? "MusicGUI.music-options-play-mode-once" : playMode == PlayMode.SHUFFLE ? "MusicGUI.music-options-play-mode-shuffle" : "MusicGUI.music-options-play-mode-repeat"));
			});
			buttonsToAdd.add(button);

			item = makeItem(Material.TOTEM_OF_UNDYING, gMusicMain.getMessageService().getMessage("MusicGUI.music-options-reverse", "%Reverse%", gMusicMain.getMessageService().getMessage(playSettings.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			button = new Button(item, (itemMeta, click, clicker) -> {
				if(playSettings.getPlayListMode() == PlayListMode.RADIO) return;
				playSettings.setReverseMode(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_REVERSE : !playSettings.isReverseMode());
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-reverse", "%Reverse%", gMusicMain.getMessageService().getMessage(playSettings.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			});
			buttonsToAdd.add(button);
		}

		if(playType == PlayType.JUKEBOX) {
			item = makeItem(Material.REDSTONE, gMusicMain.getMessageService().getMessage("MusicGUI.music-options-range", "%Range%", "" + playSettings.getRange()));
			button = new Button(item, (itemMeta, click, clicker) -> {
				long range = playSettings.getRange();
				long step = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ? SHIFT_RANGE_STEPS : RANGE_STEPS;
				long newRange = click == ClickType.MIDDLE ? gMusicMain.getConfigService().J_RANGE : (click == ClickType.RIGHT ? Math.max(range - step, 0) : Math.min(range + step, gMusicMain.getConfigService().J_MAX_RANGE));
				playSettings.setRange(newRange);
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-range", "%Range%", "" + newRange));
			});
			buttonsToAdd.add(button);
		}

		if(!(playType == PlayType.JUKEBOX && gMusicMain.getConfigService().J_LOCATIONAL_SOUNDS)) {
			item = makeItem(Material.EMERALD, gMusicMain.getMessageService().getMessage("MusicGUI.music-options-stereo", "%Stereo%", gMusicMain.getMessageService().getMessage(playSettings.isStereo() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			button = new Button(item, (itemMeta, click, clicker) -> {
				playSettings.setStereo(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_STEREO : !playSettings.isStereo());
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-stereo", "%Stereo%", gMusicMain.getMessageService().getMessage(playSettings.isStereo() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			});
			buttonsToAdd.add(button);
		}

		for(int i = 0; i < buttonsToAdd.size(); i++) {
			int slot = 45 + i;
			if(slot >= 52) break;
			button = buttonsToAdd.get(i);
			setButton(slot, button);
		}
	}

	public void setPage(int newPage) {
		List<Song> songs = new ArrayList<>();

		if(playSettings.getPlayListMode() != PlayListMode.RADIO) {
			songs = playSettings.getPlayListMode() == PlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
			if(searchKey != null && !searchKey.isEmpty()) songs = gMusicMain.getSongService().filterSongsBySearch(songs, searchKey);
			songs.sort(Comparator.comparing(Song::getTitle));
		}

		if(newPage > getMaxPageSize(songs.size())) newPage = getMaxPageSize(songs.size());
		if(newPage < 1) newPage = 1;

		page = newPage;

		for(int slot = 0; slot < 45; slot++) inventory.setItem(slot, null);

		pageSongs.clear();

		if(!songs.isEmpty()) {
			for(int songPosition = (page - 1) * 45; songPosition < 45 * page && songPosition < songs.size(); songPosition++) {
				Song song = songs.get(songPosition);
				ItemStack itemStack = new ItemStack(song.getDiscMaterial());
				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(
						"MusicGUI.disc-title",
						"%Song%", song.getId(),
						"%SongTitle%", song.getTitle(),
						"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
						"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
				));
				List<String> description = new ArrayList<>();
				for(String descriptionRow : song.getDescription()) {
					for(String descriptionLine : ChatPaginator.wordWrap(descriptionRow, DESCRIPTION_MAX_LINE_LENGTH)) {
						description.add(gMusicMain.getMessageService().toFormattedMessage("&6" + descriptionLine));
					}
				}
				if(playSettings.getFavorites().contains(song)) description.add(gMusicMain.getMessageService().getMessage("MusicGUI.disc-favorite"));
				itemMeta.setLore(description);
				pageSongs.put(songPosition % 45, song);
				itemMeta.addItemFlags(ItemFlag.values());
				itemStack.setItemMeta(itemMeta);
				inventory.setItem(songPosition % 45, itemStack);
			}
		}

		if(page > 1) {
			ItemStack itemStack = makeItem(Material.ARROW, gMusicMain.getMessageService().getMessage("MusicGUI.last-page"));
			inventory.setItem(52, itemStack);
		} else {
			ItemStack itemStack = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
			inventory.setItem(52, itemStack);
		}

		if(page < getMaxPageSize(songs.size())) {
			ItemStack itemStack = makeItem(Material.ARROW, gMusicMain.getMessageService().getMessage("MusicGUI.next-page"));
			inventory.setItem(53, itemStack);
		} else {
			ItemStack itemStack = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ");
			inventory.setItem(53, itemStack);
		}
	}

	private int getMaxPageSize(int songCount) { return (songCount / 45) + (songCount % 45 == 0 ? 0 : 1); }

	public @NotNull UUID getOwner() { return uuid; }

	public @NotNull PlayType getPlayType() { return playType; }

	public @NotNull PlaySettings getPlaySettings() { return playSettings; }

	public @NotNull Inventory getInventory() { return inventory; }

	private static ItemStack makeItem(Material material, String displayName) {
		ItemStack itemStack = new ItemStack(material);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(displayName);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	private record Button(ItemStack item, ClickHandler handler) {}

	@FunctionalInterface
	private interface ClickHandler {
		void onClick(ItemMeta itemMeta, ClickType click, HumanEntity clicker);
	}

	private void setButton(int slot, @Nullable Button button) {
		if(button == null) {
			inventory.setItem(slot, makeItem(Material.BLACK_STAINED_GLASS_PANE, " "));
			buttons.remove(slot);
		} else {
			inventory.setItem(slot, button.item);
			buttons.put(slot, button.handler);
		}
	}

}