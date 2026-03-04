package dev.geco.gmusic.service;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.PlayMode;
import dev.geco.gmusic.model.PlaySettings;
import dev.geco.gmusic.model.Song;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class DiscService {

	private final GMusicMain gMusicMain;
	private final NamespacedKey discKey;

	public DiscService(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
		discKey = new NamespacedKey(gMusicMain, GMusicMain.NAME + "_disc");
	}

	public @NotNull NamespacedKey getDiscKey() { return discKey; }

	public @NotNull ItemStack createDiscItem(@NotNull Song song) {
		ItemStack itemStack = new ItemStack(song.getDiscMaterial());
		itemStack.setAmount(1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(gMusicMain.getMessageService().getMessage(
			"Items.disc-title",
			"%Song%", song.getId(),
			"%SongTitle%", song.getTitle(),
			"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
			"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
		));
		itemMeta.setLore(List.of(gMusicMain.getMessageService().getMessage(
			"Items.disc-description",
			"%Song%", song.getId(),
			"%SongTitle%", song.getTitle(),
			"%Author%", song.getAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-author") : song.getAuthor(),
			"%OriginalAuthor%", song.getOriginalAuthor().isEmpty() ? gMusicMain.getMessageService().getMessage("MusicGUI.disc-empty-original-author") : song.getOriginalAuthor()
		)));
		itemMeta.getPersistentDataContainer().set(discKey, PersistentDataType.STRING, song.getId());
		itemMeta.addItemFlags(ItemFlag.values());
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public @NotNull ItemStack createDiscPlaceholderItem(@NotNull String songId, @NotNull UUID jukeboxUuid) {
		ItemStack itemStack = new ItemStack(Material.STICK);
		itemStack.setAmount(1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(jukeboxUuid.toString());
		itemMeta.getPersistentDataContainer().set(discKey, PersistentDataType.STRING, songId);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public void generateDiscPlaySettings(@NotNull UUID uuid) {
		PlaySettings playSettings = gMusicMain.getPlaySettingsService().generateDefaultPlaySettings(uuid);
		playSettings.setRange(gMusicMain.getConfigService().JUKEBOX_RANGE);
		playSettings.setPlayMode(PlayMode.DEFAULT);
		playSettings.setShowParticles(true);
	}

}