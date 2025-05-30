package dev.geco.gmusic.object.gui;

import dev.geco.gmusic.api.event.GMusicReloadEvent;
import dev.geco.gmusic.object.GPlayListMode;
import dev.geco.gmusic.object.GPlayMode;
import dev.geco.gmusic.object.GPlaySettings;
import dev.geco.gmusic.object.GPlayState;
import dev.geco.gmusic.object.GSong;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;

import dev.geco.gmusic.GMusicMain;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class GMusicGUI {

	private final GMusicMain gMusicMain = GMusicMain.getInstance();
	private final NamespacedKey songKey = new NamespacedKey(gMusicMain, GMusicMain.NAME + "_song");
	private static final HashMap<UUID, GMusicGUI> musicGUIS = new HashMap<>();
	private static final int VOLUME_STEPS = 10;
	private static final int SHIFT_VOLUME_STEPS = 1;
	private static final long RANGE_STEPS = 1;
	private static final long SHIFT_RANGE_STEPS = 10;
	private final UUID uuid;
	private final MenuType type;
	private final Inventory inventory;
	private final Listener listener;
	private boolean optionState = false;
	private int page = 1;
	private boolean searchMode = false;
	private String searchKey = null;
	private final GPlaySettings playSettings;

	public GMusicGUI(UUID uuid, MenuType type) {
		this.uuid = uuid;
		this.type = type;

		musicGUIS.put(uuid, this);

		playSettings = gMusicMain.getPlaySettingsService().getPlaySettings(uuid);
		inventory = Bukkit.createInventory(new InventoryHolder() {

			@Override
			public @NotNull Inventory getInventory() { return inventory; }

		}, 6 * 9, gMusicMain.getMessageService().getMessage("MusicGUI.title"));

		setPage(1);

		setDefaultBar();

		listener = new Listener() {

			@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
			public void ICliE(InventoryClickEvent event) {
				if(!event.getInventory().equals(inventory)) return;
				ClickType click = event.getClick();
				if(gMusicMain.getVersionManager().executeMethod(event.getView(), "getBottomInventory").equals(event.getClickedInventory())) {
					switch(click) {
						case SHIFT_RIGHT:
						case SHIFT_LEFT:
							event.setCancelled(true);
							break;
					}
					return;
				}
				if(!gMusicMain.getVersionManager().executeMethod(event.getView(), "getTopInventory").equals(event.getClickedInventory())) return;
				event.setCancelled(true);
				ItemStack itemStack = event.getCurrentItem();
				if(itemStack == null) return;
				ItemMeta itemMeta = itemStack.getItemMeta();
				HumanEntity clicker = event.getWhoClicked();
				int slot = event.getRawSlot();
				switch(slot) {
					case 45 -> {
						if(!optionState) {
							GPlayState songSettings = gMusicMain.getPlayService().getPlayState(uuid);
							Player target = Bukkit.getPlayer(uuid);
							if(songSettings == null || target == null) return;
							if(songSettings.isPaused()) gMusicMain.getPlayService().resumeSong(target);
							else gMusicMain.getPlayService().pauseSong(target);
						} else {
							setDefaultBar();
						}
					}
					case 46 -> {
						if(!optionState) {
							Player target = Bukkit.getPlayer(uuid);
							if(target == null) return;
							gMusicMain.getPlayService().stopSong(target);
						} else {
							int volumn = playSettings.getVolume();
							int step = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ? SHIFT_VOLUME_STEPS : VOLUME_STEPS;
							int newVolumn = click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_VOLUME : (click == ClickType.RIGHT ? Math.max(volumn - step, 0) : Math.min(volumn + step, 100));
							playSettings.setVolume(newVolumn);
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + newVolumn));
						}
					}
					case 47 -> {
						if(!optionState) {
							Player target = Bukkit.getPlayer(uuid);
							if(target == null) return;
							gMusicMain.getPlayService().playSong(target, gMusicMain.getPlayService().getNextSong(target));
						} else {
							playSettings.setShowParticles(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PARTICLES : !playSettings.isShowingParticles());
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-particle", "%Particle%", gMusicMain.getMessageService().getMessage(playSettings.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
						}
					}
					case 48 -> {
						if(playSettings.getPlayListMode() == GPlayListMode.RADIO) return;
						if(!optionState) {
							if(gMusicMain.getConfigService().G_DISABLE_RANDOM_SONG) return;
							Player target = Bukkit.getPlayer(uuid);
							if(target == null) return;
							gMusicMain.getPlayService().playSong(target, gMusicMain.getPlayService().getRandomSong(uuid));
						} else {
							playSettings.setPlayOnJoin(click == ClickType.MIDDLE ? gMusicMain.getConfigService().R_PLAY_ON_JOIN : !playSettings.isPlayOnJoin());
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-join", "%Join%", gMusicMain.getMessageService().getMessage(playSettings.isPlayOnJoin() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
						}
					}
					case 49 -> {
						if(!optionState) {
							int playListModeId = playSettings.getPlayListMode().getId();
							GPlayListMode playListMode = GPlayListMode.byId(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PLAYLIST_MODE : (click == ClickType.RIGHT ? (playListModeId - 1 < 0 ? GPlayListMode.values().length - 1 : playListModeId - 1) : (playListModeId + 1 > GPlayListMode.values().length - 1 ? 0 : playListModeId + 1)));
							Player target = Bukkit.getPlayer(uuid);
							playSettings.setPlayListMode(playListMode);
							if(playListMode.getId() != playListModeId && target != null) {
								setPage(1);
								gMusicMain.getPlayService().stopSong(target);
							}
							if(playListMode == GPlayListMode.RADIO) {
								gMusicMain.getRadioService().addRadioPlayer(target);
							} else {
								gMusicMain.getRadioService().removeRadioPlayer(target);
							}
							setDefaultBar();
						} else {
							if(playSettings.getPlayListMode() == GPlayListMode.RADIO) return;
							int playModeId = playSettings.getPlayMode().getId();
							GPlayMode playMode = GPlayMode.byId(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_PLAY_MODE : (click == ClickType.RIGHT ? (playModeId - 1 < 0 ? GPlayMode.values().length - 1 : playModeId - 1) : (playModeId + 1 > GPlayMode.values().length - 1 ? 0 : playModeId + 1)));
							playSettings.setPlayMode(playMode);
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playMode == GPlayMode.DEFAULT ? "MusicGUI.music-options-play-mode-once" : playMode == GPlayMode.SHUFFLE ? "MusicGUI.music-options-play-mode-shuffle" : "MusicGUI.music-options-play-mode-repeat"));
						}
					}
					case 50 -> {
						if(!optionState) {
							setOptionsBar();
						} else {
							if(playSettings.getPlayListMode() == GPlayListMode.RADIO) return;
							playSettings.setReverseMode(click == ClickType.MIDDLE ? gMusicMain.getConfigService().PS_D_REVERSE : !playSettings.isReverseMode());
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-reverse", "%Reverse%", gMusicMain.getMessageService().getMessage(playSettings.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
						}
					}
					case 51 -> {
						if(!optionState) {
							if(playSettings.getPlayListMode() == GPlayListMode.RADIO) return;
							IGMusicInputGUI inputGUI = getInputGUIInstance((input) -> {
								searchKey = input;
								setPage(1);
								setDefaultBar();
								clicker.openInventory(inventory);
								searchMode = false;
								return true;
							}, ItemMeta::getDisplayName);
							ItemStack nameItem = new ItemStack(Material.NAME_TAG);
							ItemMeta nameItemMeta = nameItem.getItemMeta();
							nameItemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-search-menu-field"));
							nameItem.setItemMeta(nameItemMeta);
							searchKey = null;
							searchMode = true;
							inputGUI.open(clicker, gMusicMain.getMessageService().getMessage("MusicGUI.music-search-menu-title"), nameItem);
						} else {
							if(type != MenuType.JUKEBOX) return;
							long range = playSettings.getRange();
							long step = click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ? SHIFT_RANGE_STEPS : RANGE_STEPS;
							long newRange = click == ClickType.MIDDLE ? gMusicMain.getConfigService().JUKEBOX_RANGE : (click == ClickType.RIGHT ? Math.max(range - step, 0) : Math.min(range + step, gMusicMain.getConfigService().MAX_JUKEBOX_RANGE));
							playSettings.setRange(newRange);
							itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-range", "%Range%", "" + newRange));
						}
					}
					case 52 -> setPage(page - 1);
					case 53 -> setPage(page + 1);
					default -> {
						if(slot < 0 || slot > 44) return;
						String songId = itemMeta.getPersistentDataContainer().get(songKey, PersistentDataType.STRING);
						Player target = Bukkit.getPlayer(uuid);
						if(target == null || songId == null) return;
						GSong song = gMusicMain.getSongService().getSongById(songId);
						if(click == ClickType.MIDDLE) {
							if(playSettings.getFavorites().contains(song)) playSettings.getFavorites().remove(song);
							else playSettings.getFavorites().add(song);
							return;
						}
						gMusicMain.getPlayService().playSong(target, gMusicMain.getSongService().getSongById(songId));
					}
				}
				setPauseResumeBar();
				itemStack.setItemMeta(itemMeta);
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

	public static GMusicGUI getMusicGUI(UUID uuid) { return musicGUIS.get(uuid); }

	public void close(boolean force) {
		if(force) for(HumanEntity entity : new ArrayList<>(inventory.getViewers())) entity.closeInventory();
		if(!force && (searchMode || type == MenuType.JUKEBOX)) return;
		musicGUIS.remove(uuid);
		HandlerList.unregisterAll(listener);
	}

	private IGMusicInputGUI getInputGUIInstance(IGMusicInputGUI.InputCallback call, IGMusicInputGUI.ValidateCallback validateCall) {
		try {
			Class<?> inputGUIClass = Class.forName(gMusicMain.getVersionManager().getPackagePath() + ".object.gui.GMusicInputGUI");
			return (IGMusicInputGUI) inputGUIClass.getConstructor(IGMusicInputGUI.InputCallback.class, IGMusicInputGUI.ValidateCallback.class).newInstance(call, validateCall);
		} catch(Throwable e) { gMusicMain.getLogger().log(Level.SEVERE, "Could not get input gui instance", e); }
		return null;
	}

	private void clearBar() { for(int slot = 45; slot < 52; slot++) inventory.setItem(slot, null); }

	public void setDefaultBar() {
		optionState = false;

		clearBar();

		ItemStack itemStack;
		ItemMeta itemMeta;

		if(!gMusicMain.getConfigService().G_DISABLE_RANDOM_SONG && playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			itemStack = new ItemStack(Material.ENDER_PEARL);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-random"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(48, itemStack);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_PLAYLIST) {
			itemStack = new ItemStack(Material.NOTE_BLOCK);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playSettings.getPlayListMode() == GPlayListMode.DEFAULT ? "MusicGUI.music-playlist-mode-default" : playSettings.getPlayListMode() == GPlayListMode.FAVORITES ? "MusicGUI.music-playlist-mode-favorites" : "MusicGUI.music-playlist-mode-radio"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(49, itemStack);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_OPTIONS) {
			itemStack = new ItemStack(Material.HOPPER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(50, itemStack);
		}

		if(!gMusicMain.getConfigService().G_DISABLE_SEARCH && playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			itemStack = new ItemStack(Material.OAK_SIGN);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(searchKey == null || searchKey.isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.music-search-none") : gMusicMain.getMessageService().getMessage("MusicGUI.music-search", "%Search%", searchKey));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(51, itemStack);
		}

		setPauseResumeBar();
	}

	public void setPauseResumeBar() {
		if(optionState || playSettings.getPlayListMode() == GPlayListMode.RADIO) return;

		GPlayState songSettings = gMusicMain.getPlayService().getPlayState(uuid);

		if(songSettings != null) {
			ItemStack itemStack = new ItemStack(Material.END_CRYSTAL);
			ItemMeta itemMeta = itemStack.getItemMeta();
			if(songSettings.isPaused()) {
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-resume"));
			} else {
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-pause"));
			}
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(45, itemStack);

			itemStack = new ItemStack(Material.BARRIER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-stop"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(46, itemStack);

			itemStack = new ItemStack(Material.FEATHER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-skip"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(47, itemStack);

			return;
		}

		inventory.setItem(45, null);
		inventory.setItem(46, null);
		inventory.setItem(47, null);
	}

	public void setOptionsBar() {
		optionState = true;

		clearBar();

		ItemStack itemStack = new ItemStack(Material.CHEST);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-back"));
		itemStack.setItemMeta(itemMeta);
		inventory.setItem(45, itemStack);
		itemStack = new ItemStack(Material.MAGMA_CREAM);
		itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-volume", "%Volume%", "" + playSettings.getVolume()));
		itemStack.setItemMeta(itemMeta);
		inventory.setItem(46, itemStack);
		itemStack = new ItemStack(Material.FIREWORK_ROCKET);
		itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-particle", "%Particle%", gMusicMain.getMessageService().getMessage(playSettings.isShowingParticles() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
		itemMeta.addItemFlags(ItemFlag.values());
		itemStack.setItemMeta(itemMeta);
		inventory.setItem(47, itemStack);

		if(playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			itemStack = new ItemStack(Material.DIAMOND);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-join", "%Join%", gMusicMain.getMessageService().getMessage(playSettings.isPlayOnJoin() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(48, itemStack);
			itemStack = new ItemStack(Material.BLAZE_POWDER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playSettings.getPlayMode() == GPlayMode.DEFAULT ? "MusicGUI.music-options-play-mode-once" : playSettings.getPlayMode() == GPlayMode.SHUFFLE ? "MusicGUI.music-options-play-mode-shuffle" : "MusicGUI.music-options-play-mode-repeat"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(49, itemStack);
			itemStack = new ItemStack(Material.TOTEM_OF_UNDYING);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-reverse", "%Reverse%", gMusicMain.getMessageService().getMessage(playSettings.isReverseMode() ? "MusicGUI.music-options-true" : "MusicGUI.music-options-false")));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(50, itemStack);
		}

		if(type == MenuType.JUKEBOX) {
			itemStack = new ItemStack(Material.REDSTONE);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-options-range", "%Range%", "" + playSettings.getRange()));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(51, itemStack);
		}
	}

	public void setPage(int newPage) {
		List<GSong> songs = new ArrayList<>();

		if(playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			songs = playSettings.getPlayListMode() == GPlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();
			if(searchKey != null && !searchKey.isEmpty()) songs = gMusicMain.getSongService().filterSongsBySearch(songs, searchKey);
		}

		if(newPage > getMaxPageSize(songs.size())) newPage = getMaxPageSize(songs.size());
		if(newPage < 1) newPage = 1;

		page = newPage;

		for(int slot = 0; slot < 45; slot++) inventory.setItem(slot, null);

		if(!songs.isEmpty()) {
			for(int songPosition = (page - 1) * 45; songPosition < 45 * page && songPosition < songs.size(); songPosition++) {
				GSong song = songs.get(songPosition);
				ItemStack itemStack = new ItemStack(song.getDiscMaterial());
				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(
						"MusicGUI.disc-title",
						"%Title%", song.getTitle(),
						"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
						"%OAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-oauthor") : song.getOriginalAuthor()
				));
				List<String> description = new ArrayList<>();
				for(String descriptionRow : song.getDescription()) description.add(gMusicMain.getMessageService().toFormattedMessage("&6" + descriptionRow));
				if(playSettings.getFavorites().contains(song)) description.add(gMusicMain.getMessageService().getMessage("MusicGUI.disc-favorite"));
				itemMeta.setLore(description);
				itemMeta.getPersistentDataContainer().set(songKey, PersistentDataType.STRING, song.getId());
				itemMeta.addItemFlags(ItemFlag.values());
				itemStack.setItemMeta(itemMeta);
				inventory.setItem(songPosition % 45, itemStack);
			}
		}

		if(page > 1) {
			ItemStack itemStack = new ItemStack(Material.ARROW);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.last-page"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(52, itemStack);
		} else {
			ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(" ");
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(52, itemStack);
		}

		if(page < getMaxPageSize(songs.size())) {
			ItemStack itemStack = new ItemStack(Material.ARROW);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.next-page"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(53, itemStack);
		} else {
			ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(" ");
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(53, itemStack);
		}
	}

	private int getMaxPageSize(int songCount) { return (songCount / 45) + (songCount % 45 == 0 ? 0 : 1); }

	public UUID getOwner() { return uuid; }

	public MenuType getMenuType() { return type; }

	public GPlaySettings getPlaySettings() { return playSettings; }

	public Inventory getInventory() { return inventory; }

	public enum MenuType {

		DEFAULT,
		JUKEBOX
    }

}