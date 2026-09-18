package dev.geco.gmusic.event;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.model.gui.MusicGUI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

	public JukeBoxEventHandler(GMusicMain gMusicMain) {
		this.gMusicMain = gMusicMain;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void playerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if(event.getAction() != Action.RIGHT_CLICK_BLOCK || player.isSneaking()) return;

		Block block = event.getClickedBlock();
		if(block == null || block.getType() != Material.JUKEBOX) return;

		UUID uuid = gMusicMain.getJukeBoxService().getJukeBoxId(block);
		if(uuid == null) return;

		event.setCancelled(true);

		if(!gMusicMain.getPermissionService().hasPermission(player, "JukeBox")) return;

		if(!gMusicMain.getEnvironmentUtil().isEntityInAllowedWorld(player)) return;

		if(!gMusicMain.getEnvironmentUtil().canUseJukeboxInLocation(block.getLocation(), player)) return;

		if(!player.isSneaking()) player.openInventory(MusicGUI.getMusicGUI(uuid).getInventory());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockPlaceEvent(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if(block.getType() != Material.JUKEBOX) return;
		ItemStack itemStack = event.getItemInHand();
		if(!itemStack.getItemMeta().getPersistentDataContainer().has(gMusicMain.getJukeBoxService().getJukeBoxKey())) return;
		gMusicMain.getJukeBoxService().setJukebox(block);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockBreakEvent(BlockBreakEvent event) {
		handleBlockBreak(event.getBlock(), event.getPlayer().getGameMode() != GameMode.CREATIVE);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void blockExplodeEvent(BlockExplodeEvent event) {
		for(Block block : event.blockList()) handleBlockBreak(block, true);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void entityExplodeEvent(EntityExplodeEvent event) {
		for(Block block : event.blockList()) handleBlockBreak(block, true);
	}

	private void handleBlockBreak(Block block, boolean dropJukebox) {
		if(block.getType() != Material.JUKEBOX) return;
		if(gMusicMain.getJukeBoxService().getJukeBoxId(block) == null) return;
		gMusicMain.getJukeBoxService().removeJukebox(block);
		block.setType(Material.AIR);
		if(dropJukebox) block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0, 0.5), gMusicMain.getJukeBoxService().createJukeBoxItem());
	}

}