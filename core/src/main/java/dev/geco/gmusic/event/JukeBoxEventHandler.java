package dev.geco.gmusic.event;

import dev.geco.gmusic.object.gui.GMusicGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class JukeBoxEventHandler implements Listener {

	private final GMusicMain gMusicMain;
	private final NamespacedKey jukeBoxKey;

	public JukeBoxEventHandler(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
		jukeBoxKey = new NamespacedKey(gMusicMain, GMusicMain.NAME + "_juke_box");
	}

	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void playerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Block block = event.getClickedBlock();
		if(block == null || block.getType() != Material.JUKEBOX) return;

		UUID uuid = gMusicMain.getJukeBoxService().getJukeBoxId(block);
		if(uuid == null) return;

		event.setCancelled(true);

		if(!gMusicMain.getPermissionService().hasPermission(player, "AMusic.UseJukeBox", "AMusic.*")) return;

		if(!player.isSneaking()) player.openInventory(GMusicGUI.getMusicGUI(uuid).getInventory());
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockPlaceEvent(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if(block.getType() != Material.JUKEBOX) return;
		ItemStack itemStack = event.getItemInHand();
		if(!itemStack.getItemMeta().getPersistentDataContainer().has(jukeBoxKey)) return;
		gMusicMain.getJukeBoxService().setJukebox(block);
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockBreakEvent(BlockBreakEvent event) {
		handleBlockBreak(event.getBlock());
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockExplodeEvent(BlockExplodeEvent event) {
		for(Block block : event.blockList()) handleBlockBreak(block);
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void entityExplodeEvent(EntityExplodeEvent event) {
		for(Block block : event.blockList()) handleBlockBreak(block);
	}

	private void handleBlockBreak(Block block) {
		if(block.getType() != Material.JUKEBOX) return;
		if(gMusicMain.getJukeBoxService().getJukeBoxId(block) == null) return;
		gMusicMain.getJukeBoxService().removeJukebox(block);
	}

}