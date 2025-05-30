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
import org.bukkit.event.inventory.InventoryOpenEvent;
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

public class GMusicGUI {

	private final GMusicMain gMusicMain = GMusicMain.getInstance();
	private final NamespacedKey songKey = new NamespacedKey(gMusicMain, GMusicMain.NAME + "_song");
	private static final HashMap<UUID, GMusicGUI> musicGUIS = new HashMap<>();
	private static final long VOLUME_STEPS = 10;
	private static final long RANGE_STEPS = 1;
	private static final long SHIFT_RANGE_STEPS = 10;
	private final UUID uuid;
	private final MenuType type;
	private final Inventory inventory;
	private final Listener listener;
	private int state = 0;
	private int page;
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
			public void ICliE(InventoryClickEvent Event) {
				if(!Event.getInventory().equals(inventory)) return;
				ClickType click = Event.getClick();
				if(gMusicMain.getVersionManager().executeMethod(Event.getView(), "getBottomInventory").equals(Event.getClickedInventory())) {
					switch(click) {
						case SHIFT_RIGHT:
						case SHIFT_LEFT:
							Event.setCancelled(true);
							break;
					}
					return;
				}
				if(!gMusicMain.getVersionManager().executeMethod(Event.getView(), "getTopInventory").equals(Event.getClickedInventory())) return;
				Event.setCancelled(true);
				ItemStack itemStack = Event.getCurrentItem();
				switch(Event.getRawSlot()) {
					case 46 -> {
						Player target = Bukkit.getPlayer(uuid);
						if (target == null) return;
						gMusicMain.getPlayService().stopSong(target);
					}
					case 47 -> {
						if (gMusicMain.getConfigService().G_DISABLE_RANDOM_SONG) return;
						Player target = Bukkit.getPlayer(uuid);
						if (target == null) return;
						gMusicMain.getPlayService().playSong(target, gMusicMain.getPlayService().getRandomSong(uuid));
					}
					case 49 -> setOptionsBar();
					case 52 -> setPage(page - 1);
					case 53 -> setPage(page + 1);
					default -> {

					}
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
			public void inventoryOpenEvent(InventoryOpenEvent Event) { if(Event.getInventory().equals(inventory)) setPauseResumeBar(); }

			@EventHandler (ignoreCancelled = true)
			public void gMusicReloadEvent(GMusicReloadEvent Event) { if(Event.getPlugin().equals(gMusicMain)) close(true); }

			@EventHandler
			public void pluginDisableEvent(PluginDisableEvent Event) { if(Event.getPlugin().equals(gMusicMain)) close(true); }
		};

		Bukkit.getPluginManager().registerEvents(listener, gMusicMain);
	}

	public static GMusicGUI getMusicGUI(UUID uuid) { return musicGUIS.get(uuid); }

	public static void unregisterMusicGUI(UUID uuid) {
		GMusicGUI musicGUI = getMusicGUI(uuid);
		if(musicGUI != null) musicGUI.unregister();
		musicGUIS.remove(uuid);
	}

	public void close(boolean Force) {
		if(Force) for(HumanEntity entity : new ArrayList<>(inventory.getViewers())) entity.closeInventory();
		if(type == MenuType.JUKEBOX) return;
		unregisterMusicGUI(uuid);
	}

	public void unregister() {
		HandlerList.unregisterAll(listener);
	}

	private void clearBar() { for(int slot = 45; slot < 52; slot++) inventory.setItem(slot, null); }

	public void setDefaultBar() {
		state = 0;

		clearBar();

		ItemStack itemStack;
		ItemMeta itemMeta;

		if(playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			itemStack = new ItemStack(Material.BARRIER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-stop"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(46, itemStack);

			if(!gMusicMain.getConfigService().G_DISABLE_RANDOM_SONG) {
				itemStack = new ItemStack(Material.ENDER_PEARL);
				itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-random"));
				itemStack.setItemMeta(itemMeta);
				inventory.setItem(47, itemStack);
			}
		}

		if(!gMusicMain.getConfigService().G_DISABLE_PLAYLIST) {
			itemStack = new ItemStack(Material.ENDER_CHEST);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-playlist"));
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

		setPauseResumeBar();
	}

	public void setPauseResumeBar() {
		if(state != 0 || playSettings.getPlayListMode() == GPlayListMode.RADIO) return;

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
		} else inventory.setItem(45, null);
	}

	public void setOptionsBar() {
		state = 1;

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
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playSettings.getPlayMode() == GPlayMode.DEFAULT ? "MusicGUI.music-options-playmode-once" : playSettings.getPlayMode() == GPlayMode.SHUFFLE ? "MusicGUI.music-options-playmode-shuffle" : "MusicGUI.music-options-playmode-repeat"));
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

	public void setPlaylistBar() {
		state = 2;

		clearBar();

		ItemStack itemStack = new ItemStack(Material.CHEST);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-back"));
		itemStack.setItemMeta(itemMeta);
		inventory.setItem(45, itemStack);

		if(playSettings.getPlayListMode() != GPlayListMode.RADIO) {
			itemStack = new ItemStack(Material.FEATHER);
			itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage("MusicGUI.music-playlist-skip"));
			itemStack.setItemMeta(itemMeta);
			inventory.setItem(47, itemStack);
		}

		itemStack = new ItemStack(Material.NOTE_BLOCK);
		itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(playSettings.getPlayListMode() == GPlayListMode.DEFAULT ? "MusicGUI.music-playlist-type-default" : playSettings.getPlayListMode() == GPlayListMode.FAVORITES ? "MusicGUI.music-playlist-type-favorites" : "MusicGUI.music-playlist-type-radio"));
		itemStack.setItemMeta(itemMeta);
		inventory.setItem(49, itemStack);
	}

	public void setPage(int Page) {
		page = Page;

		List<GSong> songs = new ArrayList<>();

		if(playSettings.getPlayListMode() != GPlayListMode.RADIO) songs = playSettings.getPlayListMode() == GPlayListMode.FAVORITES ? playSettings.getFavorites() : gMusicMain.getSongService().getSongs();

		if(page > getMaxPageSize(songs.size())) page = getMaxPageSize(songs.size());
		if(page < 1) page = 1;

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