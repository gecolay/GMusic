package dev.geco.gmusic.service.message;

import dev.geco.gmusic.GMusicMain;
import dev.geco.gmusic.service.MessageService;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotMessageService extends MessageService {

    public SpigotMessageService(GMusicMain gMusicMain) {
        super(gMusicMain);
    }

    public String toFormattedMessage(String text, Object... rawReplaceList) { return org.bukkit.ChatColor.translateAlternateColorCodes(AMPERSAND_CHAR, replaceHexColorsDirectly(replaceText(text, rawReplaceList).replace("<lang:key.sneak>", "Sneak"))); }

    public void sendMessage(@NotNull CommandSender target, String message, Object... replaceList) {
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        target.sendMessage(translatedMessage);
    }

    public void sendActionBarMessage(@NotNull Player target, String message, Object... replaceList) {
        String translatedMessage = getTranslatedMessage(message, getLanguageForTarget(target), replaceList);
        if(translatedMessage.isEmpty()) return;
        target.spigot().sendMessage(ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(translatedMessage));
    }

}