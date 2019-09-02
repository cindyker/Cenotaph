package com.MoofIT.Minecraft.Cenotaph;


import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.fusesource.jansi.Ansi;

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

	public static void sendPrefixedAdminMessage(CommandSender sender, String string) {
		if (sender instanceof Player)
			sendPrefixedPlayerMessage((Player) sender, string);
		else 
			Bukkit.getConsoleSender().sendMessage("[Cenotaph] " + string);
	}

	public static void sendEnabledMessage(String hooked) {
		if (hooked.length() > 0) {
		    hooked = "  Cenotaph Hooked into: " + hooked.substring(0, hooked.length() - 2) + ".";
		    sendInfoConsoleMessage(hooked);
		}		
		sendInfoConsoleMessage("Cenotaph " + Cenotaph.plugin.getDescription().getVersion() 
		        + " by " + Cenotaph.plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "") 
		        + " is enabled.");
		Cenotaph.log.info(" ");
	}
	
	public static void sendSevereConsoleMessage(String message) {
	    String lineSeparator =  Ansi.ansi().fg(Ansi.Color.WHITE) + System.lineSeparator() + Ansi.ansi().fg(Ansi.Color.RED).toString()+ " ";
	    String error = " " + WordUtils.wrap(Ansi.ansi().fg(Ansi.Color.RED).toString() + "[Cenotaph] " + message + Ansi.ansi().fg(Ansi.Color.WHITE), 42, lineSeparator, true);
	    for (String line : error.split(System.lineSeparator()))
            Cenotaph.log.severe(line);	    
	}
	
	public static void sendInfoConsoleMessage(String message) {
	    String split = "  " + WordUtils.wrap(message, 41, System.lineSeparator() + "  ", true); 
	    for (String line : split.split(System.lineSeparator()))
            Cenotaph.log.info(line);
	}
	
	public static void sendSweetAsciiArt() {
		Cenotaph.log.info(" ");
		Cenotaph.log.info("  C              __.....__               C");
		Cenotaph.log.info("               .'         ':,             ");
		Cenotaph.log.info("  E           /  __  _  __  \\\\           E");
		Cenotaph.log.info("              | |_)) || |_))||            ");
		Cenotaph.log.info("  N           | | \\\\ || |   ||           N");
		Cenotaph.log.info("              |             ||   _,       ");
		Cenotaph.log.info("  O           |             ||.-(_{}     O");
		Cenotaph.log.info("              |             |/    `       ");
		Cenotaph.log.info("  T           |        ,_ (\\;|/)         T");
		Cenotaph.log.info("            \\\\|       {}_)-,||`         ");
		Cenotaph.log.info("  A         \\\\;/,,;;;;;;;,\\\\|//,         A");
		Cenotaph.log.info("           .;;;;;;;;;;;;;;;;,             ");
		Cenotaph.log.info("  P       \\,;;;;;;;;;;;;;;;;,//          P");
		Cenotaph.log.info("         \\\\;;;;;;;;;;;;;;;;,//          ");
		Cenotaph.log.info("  H     ,\\';;;;;;;;;;;;;;;;'             H");
		Cenotaph.log.info("       jgs;;;;;;;;;;;'''`                 ");
		Cenotaph.log.info(" ");		
	}
}
