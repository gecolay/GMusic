package dev.geco.gmusic.cmd;

import dev.geco.gmusic.GMusicMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GMusicReloadCommand implements CommandExecutor {

    private final GMusicMain gMusicMain;

    public GMusicReloadCommand(GMusicMain gMusicMain) {
        this.gMusicMain = gMusicMain;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player || sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-sender-error");
            return true;
        }

        if(!gMusicMain.getPermissionService().hasPermission(sender, "Reload")) {
            gMusicMain.getMessageService().sendMessage(sender, "Messages.command-permission-error");
            return true;
        }

        gMusicMain.reload(sender);

        gMusicMain.getMessageService().sendMessage(sender, "Plugin.plugin-reload");
        return true;
    }

}