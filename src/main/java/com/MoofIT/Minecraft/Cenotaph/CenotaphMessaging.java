package com.MoofIT.Minecraft.Cenotaph;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class CenotaphMessaging {

	
	/**
	 * Sends a prefixed message to a player
	 * [Cenotaph] - in Gold
	 * Message in White
	 *  
	 * @param player
	 * @param message
	 */
	public static void sendPrefixedPlayerMessage(Player player, String message) {
		player.sendMessage(ChatColor.GOLD + "[Cenotaph] " + ChatColor.WHITE + message);
	}
	
	public static String centimeMsg(TombBlock tBlock) {
		String msg = null;
		if (CenotaphSettings.securityEnable()) { 
			msg = ChatColor.YELLOW + "Security: " + ChatColor.WHITE + (!tBlock.isSecured() ? "Unsecured " : "Secured"); 	
			if (tBlock.isSecured())
				msg += ChatColor.YELLOW + " SecTime: " + ChatColor.WHITE + (CenotaphSettings.securityRemove() ? CenotaphUtil.convertTime(tBlock.securityTimeLeft()) + " " : "Inf ");
		}
		if (CenotaphSettings.cenotaphRemove() && !CenotaphSettings.keepUntilEmpty()) 
			msg += ChatColor.YELLOW + "BreakTime: " + ChatColor.WHITE + (CenotaphSettings.cenotaphRemove() ? CenotaphUtil.convertTime(tBlock.removalTimeLeft()) : "Inf") + " ";
		if (CenotaphSettings.removeWhenEmpty() || CenotaphSettings.keepUntilEmpty()) {
			msg += ChatColor.YELLOW + "BreakOverride: " + ChatColor.WHITE;
			if (CenotaphSettings.removeWhenEmpty()) msg += "Break on empty";
			if (CenotaphSettings.removeWhenEmpty() && CenotaphSettings.keepUntilEmpty()) msg += " & ";
			if (CenotaphSettings.keepUntilEmpty()) msg += "Keep until empty";
		}
		return msg;
	}
	
	/** 
	 * Attempts to send an ActionBar message to the player if the server is using Spigot.
	 * Falls back to standard sendPrefixedPlayerMessage if Spigot is not being used.
	 * @param player
	 * @param message
	 */
	public static void sendActionBarPlayerMessage(Player player, String message) {
		if (!Cenotaph.isSpigot)
			sendPrefixedPlayerMessage(player, message);
		else 
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}
}
