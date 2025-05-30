package dev.geco.gmusic.cmd;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class GAMusicCommand implements CommandExecutor {

    private final GMusicMain gMusicMain;

    public GAMusicCommand(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gMusicMain.getPermissionService().hasPermission(sender, "AMusic")) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        ItemStack itemStack = new ItemStack(Material.JUKEBOX);
        itemStack.setAmount(1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(gMusicMain.getJukeBoxService().getJukeBoxKey(), PersistentDataType.BOOLEAN, true);
        itemStack.setItemMeta(itemMeta);

        player.getInventory().addItem(itemStack);

        return true;
    }

}