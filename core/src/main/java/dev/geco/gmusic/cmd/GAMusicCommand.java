package dev.geco.gmusic.cmd;

import org.jetbrains.annotations.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import dev.geco.gmusic.GMusicMain;

public class GAMusicCommand implements CommandExecutor {

    private final GMusicMain GPM;

    public GAMusicCommand(GMusicMain GPluginMain) { GPM = GPluginMain; }

    @Override
    public boolean onCommand(@NotNull CommandSender Sender, @NotNull Command Command, @NotNull String Label, String[] Args) {

        if(!(Sender instanceof Player)) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-sender-error");
            return true;
        }

        Player player = (Player) Sender;

        if(!GPM.getPManager().hasPermission(Sender, "AMusic")) {

            GPM.getMManager().sendMessage(Sender, "Messages.command-permission-error");
            return true;
        }

        return true;
    }

}