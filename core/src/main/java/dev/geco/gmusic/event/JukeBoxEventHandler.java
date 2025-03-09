package dev.geco.gmusic.event;

import org.bukkit.event.*;

import dev.geco.gmusic.GMusicMain;

public class JukeBoxEventHandler implements Listener {

	private final GMusicMain GPM;

	public JukeBoxEventHandler(GMusicMain GPluginMain) { GPM = GPluginMain; }

	/*@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void PIntE(PlayerInteractEvent Event) {

		Player player = Event.getPlayer();

		if(Event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Block block = Event.getClickedBlock();
		if(block == null ||block.getType() != Material.JUKEBOX) return;
		if(!(player.hasPermission(GPM.NAME + ".AMusic.UseJukeBox") || player.hasPermission(GPM.NAME + ".AMusic.*"))) return;
		Jukebox jukebox = (Jukebox) block.getState();

		if(block.hasMetadata(GPM.NAME + "_JB")) {
			if(!player.isSneaking() || Event.getItem() == null) {
				player.openInventory(GPM.getSongManager().getMusicGUIs().get((UUID) block.getMetadata(GPM.NAME + "_JB").get(0).value()).getInventory());
				Event.setCancelled(true);
				Event.setUseItemInHand(Result.DENY);
				Event.setUseInteractedBlock(Result.DENY);
			} else if(!Event.isBlockInHand()) Event.setUseInteractedBlock(Result.DENY);
		} else if((jukebox.getRecord() == null || jukebox.getRecord().getType() == Material.AIR) && Event.getItem() != null && Event.getItem().hasItemMeta() && Event.getItem().getItemMeta().hasLocalizedName()) {

			String ln = Event.getItem().getItemMeta().getLocalizedName();
			ItemStack I = GPM.getValues().getDiscItems().keySet().stream().filter(d -> d.getItemMeta().getLocalizedName().equals(ln)).findFirst().orElse(null);

			if(I != null) {

				jukebox.setRecord(Event.getItem());
				jukebox.update(false, false);

				Sound s = Sound.valueOf(Event.getItem().getType().name());

				if(player.getGameMode() != GameMode.CREATIVE) {
					ItemStack s1 = Event.getItem();
					s1.setAmount(s1.getAmount() - 1);
					if(Event.getHand() == EquipmentSlot.HAND) {
						player.getInventory().setItemInMainHand(s1);
					} else if(Event.getHand() == EquipmentSlot.OFF_HAND) {
						player.getInventory().setItemInOffHand(s1);
					}
				}

				List<Player> cm = new ArrayList<>();
				for(Entity pcm : block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 70, 70, 70)) {
					if(pcm instanceof Player) cm.add((Player) pcm);
				}

				GPM.getTManager().runDelayed(() -> {
					for(Player t : cm) {
						t.stopSound(s, SoundCategory.RECORDS);
						//t.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
					}
				}, false, 0);

				UUID un = UUID.randomUUID();
				PlaySettings ps = GPM.getPlaySettingsManager().getPlaySettings(un);
				ps.setPlayMode(0);
				ps.setPlayList(0);
				GPM.getValues().putPlaySetting(un, ps);
				GPM.getValues().putJukeBlock(un, block.getLocation().add(0.5, 0.5, 0.5));
				GPM.getBoxSongManager().playBoxSong(un, GPM.getValues().getDiscItems().get(I));
				GPM.getValues().putTempJukeBlock(block, ps);

				Event.setCancelled(true);
				Event.setUseItemInHand(Result.DENY);
				Event.setUseInteractedBlock(Result.DENY);
			}
		} else GPM.getJukeBoxManager().removeTempJukebox(block);
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void BPlaE(BlockPlaceEvent e) {
		Block b = e.getBlock();
		if(b.getType() == Material.JUKEBOX) {
			ItemStack is = e.getItemInHand();
			if(is.getItemMeta().hasLocalizedName() && GPM.getMusicManager().getJukeBox().getItemMeta().getLocalizedName().equals(is.getItemMeta().getLocalizedName())) GPM.getJukeBoxManager().setNewJukebox(b);
		}
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void BBreE(BlockBreakEvent e) {
		Block b = e.getBlock();
		if(b.getType() == Material.JUKEBOX) {
			if(b.hasMetadata(GPM.NAME + "_JB")) {
				b.setType(Material.AIR);
				b.getWorld().dropItemNaturally(b.getLocation(), GPM.getMusicManager().getJukeBox());
				GPM.getJukeBoxManager().removeJukebox(b);
			} else GPM.getJukeBoxManager().removeTempJukebox(b);
		}
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void BExpE(BlockExplodeEvent e) {
		for(Block b : e.blockList()) {
			if(b.getType() == Material.JUKEBOX) {
				if(b.hasMetadata(GPM.NAME + "_JB")) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), GPM.getMusicManager().getJukeBox());
					GPM.getJukeBoxManager().removeJukebox(b);
				} else GPM.getJukeBoxManager().removeTempJukebox(b);
			}
		}
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void EExpE(EntityExplodeEvent e) {
		for(Block b : e.blockList()) {
			if(b.getType() == Material.JUKEBOX) {
				if(b.hasMetadata(GPM.NAME + "_JB")) {
					b.setType(Material.AIR);
					b.getWorld().dropItemNaturally(b.getLocation(), GPM.getMusicManager().getJukeBox());
					GPM.getJukeBoxManager().removeJukebox(b);
				} else GPM.getJukeBoxManager().removeTempJukebox(b);
			}
		}
	}*/

}