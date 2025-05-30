package dev.geco.gmusic.mcv.v1_21_5.object.gui;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.api.event.GMusicReloadEvent;
import dev.geco.gmusic.object.gui.IGMusicInputGUI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GMusicInputGUI implements IGMusicInputGUI {

    protected final GMusicMain gMusicMain = GMusicMain.getInstance();
    private Inventory inventory;
    private final Listener listener;
    private String input;

    public GMusicInputGUI(InputCallback call, ValidateCallback validateCall) {
        listener = new Listener() {

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void inventoryClickEvent(InventoryClickEvent event) {
                if(!event.getInventory().equals(inventory)) return;
                event.setCancelled(true);
                if(event.getRawSlot() != 2) return;
                if(event.getCurrentItem() == null) return;
                boolean success = call.call(input);
                if(!success) return;
                close(true);
            }

            @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
            public void PAnvE(PrepareAnvilEvent Event) {
                if(!Event.getInventory().equals(inventory)) return;
                org.bukkit.inventory.ItemStack result = Event.getResult();
                if(result == null || !result.hasItemMeta()) return;
                ItemMeta itemMeta = result.getItemMeta();
                if(itemMeta == null) return;
                String newInput = itemMeta.getDisplayName();
                if(validateCall != null) {
                    newInput = validateCall.call(itemMeta);
                    if(newInput == null) {
                        Event.setResult(null);
                        return;
                    }
                }
                input = newInput;
                result.setItemMeta(itemMeta);
                Event.setResult(result);
            }

            @EventHandler
            public void inventoryCloseEvent(InventoryCloseEvent event) { if(event.getInventory().equals(inventory)) close(false); }

            @EventHandler (ignoreCancelled = true)
            public void gMusicReloadEvent(GMusicReloadEvent event) { if(event.getPlugin().equals(gMusicMain)) close(true); }

            @EventHandler
            public void pluginDisableEvent(PluginDisableEvent event) { if(event.getPlugin().equals(gMusicMain)) close(true); }
        };

        Bukkit.getPluginManager().registerEvents(listener, gMusicMain);
    }

    @Override
    public void open(LivingEntity entity, String title, ItemStack inputItem) {
        if(!(entity instanceof Player)) return;

        ServerPlayer player = ((CraftPlayer) entity).getHandle();
        int containerId = player.nextContainerCounter();
        AnvilMenu anvilMenu = new AnvilMenu(containerId, player.getInventory(), ContainerLevelAccess.create(player.level(), player.blockPosition()));

        anvilMenu.checkReachable = false;
        anvilMenu.maximumRepairCost = 0;
        Component titleComponent = CraftChatMessage.fromString(title)[0];
        anvilMenu.setTitle(titleComponent);

        if(inputItem.hasItemMeta() && inputItem.getItemMeta() != null) {
            input = inputItem.getItemMeta().getDisplayName();
            inventory = (Inventory) gMusicMain.getVersionManager().executeMethod(gMusicMain.getVersionManager().executeMethod(anvilMenu, "getBukkitView"), "getTopInventory");
            inventory.setItem(0, inputItem);
        }

        ClientboundOpenScreenPacket clientboundOpenScreenPacket = new ClientboundOpenScreenPacket(containerId, net.minecraft.world.inventory.MenuType.ANVIL, titleComponent);
        player.connection.send(clientboundOpenScreenPacket);

        player.containerMenu = anvilMenu;
        player.initMenu(anvilMenu);
    }

    @Override
    public void close(boolean force) {
        HandlerList.unregisterAll(listener);
        if(inventory == null) return;
        inventory.clear();
        if(force) for(HumanEntity entity : new ArrayList<>(inventory.getViewers())) entity.closeInventory();
    }

}