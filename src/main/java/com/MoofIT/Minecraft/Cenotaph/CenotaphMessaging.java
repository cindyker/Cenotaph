package com.MoofIT.Minecraft.Cenotaph;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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

	public static void sendPrefixedAdminMessage(CommandSender sender, String string) {
		if (sender instanceof Player)
			sendPrefixedPlayerMessage((Player) sender, string);
		else 
			Bukkit.getConsoleSender().sendMessage("[Cenotaph] " + string);
	}

	public static void sendEnabledGraphic(String hooked) {

		if (CenotaphSettings.enableAscii()) {
			Cenotaph.log.info(" ");
			Cenotaph.log.info("  C             __.....__               C");
			Cenotaph.log.info("              .'         ':,             ");
			Cenotaph.log.info("  E          /  __  _  __  \\\\           E");
			Cenotaph.log.info("             | |_)) || |_))||            ");
			Cenotaph.log.info("  N          | | \\\\ || |   ||           N");
			Cenotaph.log.info("             |             ||   _,       ");
			Cenotaph.log.info("  O          |             ||.-(_{}     O");
			Cenotaph.log.info("             |             |/    `       ");
			Cenotaph.log.info("  T          |        ,_ (\\;|/)         T");
			Cenotaph.log.info("           \\\\|       {}_)-,||`         ");
			Cenotaph.log.info("  A        \\\\;/,,;;;;;;;,\\\\|//,         A");
			Cenotaph.log.info("          .;;;;;;;;;;;;;;;;,             ");
			Cenotaph.log.info("  P      \\,;;;;;;;;;;;;;;;;,//          P");
			Cenotaph.log.info("        \\\\;;;;;;;;;;;;;;;;,//          ");
			Cenotaph.log.info("  H    ,\\';;;;;;;;;;;;;;;;'             H");
			Cenotaph.log.info("      jgs;;;;;;;;;;;'''`                 ");
			Cenotaph.log.info(" ");
		}
		if (hooked.length() > 0)
			Cenotaph.log.info("  Cenotaph Hooked into: " + hooked.substring(0, hooked.length() - 2)); // Cut off trailing ", " from the hooked string.
		Cenotaph.log.info("  Cenotaph " + Cenotaph.plugin.getDescription().getVersion() + " by " + Cenotaph.plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", "") + " is enabled.");

	}
}
