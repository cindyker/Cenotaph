package com.MoofIT.Minecraft.Cenotaph;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CenotaphCommand implements CommandExecutor {
	private Cenotaph plugin;

	public CenotaphCommand(Cenotaph instance) {
		this.plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenlist")) {
			if (args.length == 1)
				return false;
			ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
			if (pList == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "You have no cenotaphs.");
				return true;
			}
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Cenotaph List:");
			int i = 0;
			for (TombBlock tomb : pList) {
				i++;
				if (tomb.getBlock() == null) continue;
				String message;
				message = " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")";
				if (CenotaphSettings.securityEnable()) {
					if (!CenotaphSettings.securityRemove())
						message = message + ChatColor.GREEN + " [Locked]";
					else if (CenotaphSettings.securityRemove() && (tomb.securityTimeLeft() > 0 ))
						message = message + ChatColor.GREEN + " [Locked " + ChatColor.RED + CenotaphUtil.convertTime(tomb.securityTimeLeft()) + ChatColor.GREEN + "]";
					else if (CenotaphSettings.securityRemove() && (!tomb.isSecured()))
						message = message + ChatColor.RED + " [Unlocked]";
				}
				CenotaphMessaging.sendPrefixedPlayerMessage(p, message);
			}
			return true;

		} else if (cmd.equalsIgnoreCase("cenfind")) {
			TombBlock tBlock = getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}
			double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Course: " + degrees + " (" + CenotaphUtil.getDirection(degrees) + "). Your compass has been set to point that direction. Use /cenreset to reset it to your spawn point.");
			return true;
			
		} else if (cmd.equalsIgnoreCase("ceninfo")) {			
			TombBlock tBlock = getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}
			CenotaphMessaging.sendPrefixedPlayerMessage(p, CenotaphMessaging.centimeMsg(tBlock));
			return true;
			
		} else if (cmd.equalsIgnoreCase("cenreset")) {
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;
		}
		
		else if (cmd.equalsIgnoreCase("cenadmin")) {
			String playerName = null;
			
			if (args.length == 0) {
				if (!p.hasPermission("cenotaph.admin")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Missing Subcommand:");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin list");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin list <player>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin version");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin reload");
				return true;
			}
			if (args.length > 1) {
				try {					
					playerName = plugin.getServer().getPlayer(args[1]).getName();
				} catch (NullPointerException e) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Player " + args[1] + " not found.");
					return true;
				}
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				if (!p.hasPermission("cenotaph.admin.list")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				if (args.length == 1)
					return false;
				if (args.length < 2) {
					if (Cenotaph.playerTombList.keySet().isEmpty()) {
						CenotaphMessaging.sendPrefixedPlayerMessage(p, "There are no cenotaphs.");
						return true;
					}
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Players with cenotaphs:");
					for (String player : Cenotaph.playerTombList.keySet()) {
						CenotaphMessaging.sendPrefixedPlayerMessage(p, player);
					}
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "No cenotaphs found for " + playerName + ".");
					return true;
				}
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Cenotaph List:");
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					CenotaphMessaging.sendPrefixedPlayerMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")");
				}
				return true;
				
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!p.hasPermission("cenotaph.admin.find")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				if (args.length == 1)
					return false;
				TombBlock tBlock = getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return true;
				}

				double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				CenotaphMessaging.sendPrefixedPlayerMessage(p,"Location:" + X + "," + Y + "," + Z + ", to the " + CenotaphUtil.getDirection(degrees) + ".");
				return true;
				
			} else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("time")) {
				if (!p.hasPermission("cenotaph.admin.time")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				if (args.length == 1)
					return false;

				TombBlock tBlock = getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return true;
				}

				CenotaphMessaging.sendPrefixedPlayerMessage(p, CenotaphMessaging.centimeMsg(tBlock));
				return true;
				
			} else if (args[0].equalsIgnoreCase("version")) {
				if (!p.hasPermission("cenotaph.admin.version")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				String message;
				message = plugin.getVersion();
				CenotaphMessaging.sendPrefixedPlayerMessage(p, message);

			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!p.hasPermission("cenotaph.admin.remove")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				if (args.length == 1)
					return false;
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "No cenotaphs found for " + playerName + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				plugin.destroyCenotaph(tBlock);
				
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!p.hasPermission("cenotaph.admin.reload")) {
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Permission Denied");
					return true;
				}
				plugin.loadSettings();
				Cenotaph.log.info("[Cenotaph] Configuration reloaded from file.");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Configuration reloaded from file.");
			} else {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid command");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin list");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin list <player>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin version");
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Usage: /cenadmin reload");
				return true;
			}
			return true;
		}
		return false;
	}

	private TombBlock getBlockByIndex(String playerName,String index) {
		ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(playerName);
		int slot = 0;

		if (pList == null) return null;

		try {
			slot = Integer.parseInt(index);
		} catch (NumberFormatException e) {
			slot = pList.size();
		}
		slot -= 1;

		if (slot < 0 || slot >= pList.size()) return null;

		return pList.get(slot);
	}
}

