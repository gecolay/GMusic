package dev.geco.gmusic.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;

public class GMusicReloadCommand implements CommandExecutor {

    private final GMusicMain GPM;

    public GMusicReloadCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        if(!(Sender instanceof Player || Sender instanceof ConsoleCommandSender || Sender instanceof RemoteConsoleCommandSender)) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-sender-error");
            return true;
        }

        if(!GPM.getPManager().hasPermission(Sender, "Reload")) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-permission-error");
            return true;
        }

        GPM.reload(Sender);

        GPM.getMManager().sendMessage(Sender, "Plugin.plugin-reload");
        return true;
    }

}