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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) { 
			parseCenAdminCommand(sender, command, label, args);
			return true; 
		}
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenlist")) {
			if (args.length == 1)
				return false;
			ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(p.getName());
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
			TombBlock tBlock = CenotaphUtil.getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}
			double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Course: " + degrees + " (" + CenotaphUtil.getDirection(degrees) + "). Your compass has been set to point that direction. Use /cenreset to reset it to your spawn point.");
			return true;
			
		} else if (cmd.equalsIgnoreCase("ceninfo")) {			
			TombBlock tBlock = CenotaphUtil.getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
				return true;
			}
			CenotaphMessaging.sendPrefixedPlayerMessage(p, CenotaphMessaging.centimeMsg(tBlock));
			return true;
			
		} else if (cmd.equalsIgnoreCase("cenreset")) {
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;

		} else if (cmd.equalsIgnoreCase("cenadmin")) {
			parseCenAdminCommand(sender, command, label, args);
			return true;

		}return false;
	}

	private void parseCenAdminCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName();
		boolean isConsole = !(sender instanceof Player);
		
		if (cmd.equalsIgnoreCase("cenadmin")) {
			String playerName = null;
			
			if (args.length == 0) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Missing Subcommand:");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin list");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin list <player>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin version");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin reload");
				return;
			}
			if (args.length > 1) {
				try {					
					playerName = plugin.getServer().getPlayer(args[1]).getName();
				} catch (NullPointerException e) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Player " + args[1] + " not found.");
					return;
				}
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.list")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				if (args.length == 1)
					return;
				if (args.length < 2) {
					if (CenotaphDatabase.playerTombList.keySet().isEmpty()) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "There are no cenotaphs.");
						return;
					}
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Players with cenotaphs:");
					for (String player : CenotaphDatabase.playerTombList.keySet()) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, player);
					}
					return;
				}
				ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "No cenotaphs found for " + playerName + ".");
					return;
				}
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Cenotaph List:");
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					CenotaphMessaging.sendPrefixedAdminMessage(sender, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")");
				}
				return;
				
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.find")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				if (args.length == 1)
					return;
				TombBlock tBlock = CenotaphUtil.getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return;
				}

				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				if (!isConsole) {
					double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), ((Player) sender).getLocation()) + 270) % 360;
					CenotaphMessaging.sendPrefixedAdminMessage(sender,"Location:" + X + "," + Y + "," + Z + ", to the " + CenotaphUtil.getDirection(degrees) + ".");
					return;
				} else {
					CenotaphMessaging.sendPrefixedAdminMessage(sender,"Location:" + X + "," + Y + "," + Z);
				}
				
			} else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("time")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.time")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				if (args.length == 1)
					return;

				TombBlock tBlock = CenotaphUtil.getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Invalid cenotaph entry or no cenotaphs active. Check with /cenlist.");
					return;
				}

				CenotaphMessaging.sendPrefixedAdminMessage(sender, CenotaphMessaging.centimeMsg(tBlock));
				return;
				
			} else if (args[0].equalsIgnoreCase("version")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.version")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				String message;
				message = plugin.getVersion();
				CenotaphMessaging.sendPrefixedAdminMessage(sender, message);

			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.remove")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				if (args.length == 1)
					return;
				ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "No cenotaphs found for " + playerName + ".");
					return;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Invalid cenotaph entry.");
					return;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, "Invalid cenotaph entry.");
					return;
				}
				TombBlock tBlock = pList.get(slot);
				CenotaphDatabase.destroyCenotaph(tBlock);
				
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.reload")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, "Permission Denied");
						return;
					}
				plugin.loadSettings();
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Configuration reloaded from file.");
				return;
			} else {
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Invalid command");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin list");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin list <player>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin version");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, "Usage: /cenadmin reload");
				return;
			}
			return;
		}
		return;		
	}
}

