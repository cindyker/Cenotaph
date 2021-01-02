package com.MoofIT.Minecraft.Cenotaph;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.MoofIT.Minecraft.Cenotaph.Config.Lang;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.HolographicDisplays;

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
				CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("you_have_no_cenotaphs"));
				return true;
			}
			CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("cenotaph_list"));
			int i = 0;
			for (TombBlock tomb : pList) {
				i++;
				if (tomb.getBlock() == null) continue;
				String message;
				message = " " + i + " - " + Lang.string("world") + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")";
				if (CenotaphSettings.securityEnable()) {
					if (!CenotaphSettings.securityRemove())
						message = message + ChatColor.GREEN + Lang.string("locked");
					else if (CenotaphSettings.securityRemove() && (tomb.securityTimeLeft() > 0 ))
						message = message + ChatColor.GREEN + Lang.string("locked", ChatColor.RED + CenotaphUtil.convertTime(tomb.securityTimeLeft()) + ChatColor.GREEN);
					else if (CenotaphSettings.securityRemove() && (!tomb.isSecured()))
						message = message + ChatColor.RED + Lang.string("unlocked");
				}
				CenotaphMessaging.sendPrefixedPlayerMessage(p, message);
			}
			return true;

		} else if (cmd.equalsIgnoreCase("cenfind")) {
			TombBlock tBlock = CenotaphUtil.getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("invalid_cenotaph_entry_verbose"));
				return true;
			}
			double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("course", degrees, CenotaphUtil.getDirection(degrees)));
			return true;
			
		} else if (cmd.equalsIgnoreCase("ceninfo")) {			
			TombBlock tBlock = CenotaphUtil.getBlockByIndex(p.getName(), args.length == 0 ? "last" : args[0]);
			if (tBlock == null) {
				CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("invalid_cenotaph_entry_verbose"));
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
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("missing_subcommand"));
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin list");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin list <player>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin version");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin deletehologram");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin reload");
				return;
			}
			if (args.length > 1) {
				try {					
					playerName = plugin.getServer().getPlayer(args[1]).getName();
				} catch (NullPointerException e) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("player_not_found", args[1]));
					return;
				}
			}
			
			if (args[0].equalsIgnoreCase("list")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.list")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				if (args.length == 1)
					return;
				if (args.length < 2) {
					if (CenotaphDatabase.playerTombList.keySet().isEmpty()) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("there_are_no_cenotaphs"));
						return;
					}
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("players_with_cenotaphs"));
					for (String player : CenotaphDatabase.playerTombList.keySet()) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, player);
					}
					return;
				}
				ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("no_cenotaphs_found_for_x", playerName));
					return;
				}
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("cenotaph_list"));
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					CenotaphMessaging.sendPrefixedAdminMessage(sender, " " + i + " - " + Lang.string("world") + tomb.getBlock().getWorld().getName() + " @(" + tomb.getBlock().getX() + "," + tomb.getBlock().getY() + "," + tomb.getBlock().getZ() + ")");
				}
				return;
				
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.find")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				if (args.length == 1)
					return;
				TombBlock tBlock = CenotaphUtil.getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("invalid_cenotaph_entry_verbose"));
					return;
				}

				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				if (!isConsole) {
					double degrees = (CenotaphUtil.getYawTo(tBlock.getBlock().getLocation(), ((Player) sender).getLocation()) + 270) % 360;
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("admin_cen_find_with_dir", X, Y, Z, CenotaphUtil.getDirection(degrees)));
					return;
				} else {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("admin_cen_find", X, Y, Z));
				}
				
			} else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("time")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.time")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				if (args.length == 1)
					return;

				TombBlock tBlock = CenotaphUtil.getBlockByIndex(playerName, args.length < 3 ? "last" : args[2]);
				if (tBlock == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("invalid_cenotaph_entry_verbose"));
					return;
				}

				CenotaphMessaging.sendPrefixedAdminMessage(sender, CenotaphMessaging.centimeMsg(tBlock));
				return;
				
			} else if (args[0].equalsIgnoreCase("version")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.version")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				String message;
				message = plugin.getVersion();
				CenotaphMessaging.sendPrefixedAdminMessage(sender, message);

			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.remove")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				if (args.length == 1)
					return;
				ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(playerName);
				if (pList == null) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("no_cenotaphs_found_for_x", playerName));
					return;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("invalid_cenotaph_entry"));
					return;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("invalid_cenotaph_entry"));
					return;
				}
				TombBlock tBlock = pList.get(slot);
				CenotaphDatabase.destroyCenotaph(tBlock);
				
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!isConsole)
					if (!sender.hasPermission("cenotaph.admin.reload")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				plugin.loadSettings();
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("config_reloaded"));
				return;
			} else if (args[0].equalsIgnoreCase("deletehologram")) {
				if (isConsole) {
					CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("command_cannot_be_used_from_the_console"));
					return;
				} else {
					if (!sender.hasPermission("cenotaph.admin.deletehologram")) {
						CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("permission_denied"));
						return;
					}
				}
				HolographicDisplays.deleteHolo(Bukkit.getServer().getPlayer(sender.getName()), Bukkit.getServer().getPlayer(sender.getName()).getLocation());				
			} else {
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("invalid_command"));
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin list");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin list <player>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin info <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin find <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin remove <player> <#>");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin version");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin deletehologram");
				CenotaphMessaging.sendPrefixedAdminMessage(sender, Lang.string("usage") + "/cenadmin reload");
				return;
			}
			return;
		}
		return;		
	}
}

