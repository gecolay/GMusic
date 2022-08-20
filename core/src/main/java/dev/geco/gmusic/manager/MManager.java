package dev.geco.gmusic.manager;

import java.util.*;
import java.util.regex.*;

import org.bukkit.command.*;

import dev.geco.gmusic.main.GMusicMain;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class MManager {
	
	private GMusicMain GCM;
	
    public MManager(GMusicMain GPluginMain) { GCM = GPluginMain; }
    
    private final char C = '&';
    
    private final boolean A = Arrays.stream(net.md_5.bungee.api.ChatColor.class.getMethods()).filter(m -> "of".equals(m.getName())).findFirst().orElse(null) != null;
	
	public String getColoredMessage(String Message) {
		String r = ChatColor.translateAlternateColorCodes(C, Message);
		if(A) {
			Matcher m = Pattern.compile("(#[0-9a-fA-F]{6})").matcher(r);
			while(m.find()) r = r.replace(m.group(), net.md_5.bungee.api.ChatColor.of(m.group()).toString());
		}
		return r;
	}
	
    public void sendMessage(CommandSender S, String Message, Object... ReplaceList) { S.sendMessage(getMessage(Message, ReplaceList)); }
    
    public void sendMessage(CommandSender S, TextComponent... Message) { S.spigot().sendMessage(Message); }
    
    public String getMessage(String Message, Object... ReplaceList) {
    	String m = GCM.getMessages().getString(Message, Message);
    	return getColoredMessage(replace(m, ReplaceList));
    }
    
    public List<String> getMessageList(String Message, Object... ReplaceList) {
    	List<String> m = GCM.getMessages().getStringList(Message), l = new ArrayList<>();
    	for(String i : m) l.add(getColoredMessage(replace(i, ReplaceList)));
    	return l;
    }
    
    public String replace(String Message, Object... ReplaceList) {
    	String m = Message;
    	if(ReplaceList.length > 1) for(int i = 0; i < ReplaceList.length; i += 2) if(ReplaceList[i] != null && ReplaceList[i + 1] != null) m = m.replace(ReplaceList[i].toString(), ReplaceList[i + 1].toString());
    	return m.replace("[P]", GCM.getPrefix());
    }
	
}