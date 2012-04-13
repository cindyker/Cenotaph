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
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { //TODO needs major cleanup, move indexing to separate class function
		if (!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenlist")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphlist")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
			if (pList == null) {
				plugin.sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			plugin.sendMessage(p, "Cenotaph List:");
			int i = 0;
			for (TombBlock tomb : pList) {
				i++;
				if (tomb.getBlock() == null) continue;
				int X = tomb.getBlock().getX();
				int Y = tomb.getBlock().getY();
				int Z = tomb.getBlock().getZ();
				plugin.sendMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + X + "," + Y + "," + Z + ")");
			}
			return true;
		} else if (cmd.equalsIgnoreCase("cenfind")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphfind")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length != 1) return false;
			ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
			if (pList == null) {
				plugin.sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			int slot = 0;
			try {
				slot = Integer.parseInt(args[0]);
			} catch (Exception e) {
				plugin.sendMessage(p, "Invalid cenotaph");
				return true;
			}
			slot -= 1;
			if (slot < 0 || slot >= pList.size()) {
				plugin.sendMessage(p, "Invalid cenotaph");
				return true;
			}
			TombBlock tBlock = pList.get(slot);
			double degrees = (plugin.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			plugin.sendMessage(p, "Your cenotaph #" + args[0] + " is to the " + Cenotaph.getDirection(degrees) + ". Your compass has been set to point at its location. Use /cenreset to reset it to your spawn point.");
			return true;
		} else if (cmd.equalsIgnoreCase("ceninfo")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphtime")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length != 1) return false;
			ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
			if (pList == null) {
				plugin.sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			int slot = 0;
			try {
				slot = Integer.parseInt(args[0]);
			} catch (Exception e) {
				plugin.sendMessage(p, "Invalid cenotaph");
				return true;
			}
			slot -= 1;
			if (slot < 0 || slot >= pList.size()) {
				plugin.sendMessage(p, "Invalid cenotaph");
				return true;
			}

			long cTime = System.currentTimeMillis() / 1000;
			TombBlock tBlock = pList.get(slot);

			int breakTime = (plugin.levelBasedRemoval ? Math.min(tBlock.getOwnerLevel() + 1 * plugin.levelBasedTime,plugin.removeTime) : plugin.removeTime); 
			int secTimeLeft = (int)((tBlock.getTime() + plugin.securityTimeout) - cTime);
			int remTimeLeft = (int)((tBlock.getTime() + breakTime) - cTime);

			String msg = ChatColor.YELLOW + "Security: " + ChatColor.WHITE;
			if (tBlock.getLwcEnabled()) msg += "LWC ";
			else if (tBlock.getLocketteSign() != null) msg += "Lockette ";
			else msg += "None ";
			if (plugin.securityRemove) msg += ChatColor.YELLOW + "SecTime: " + ChatColor.WHITE + (plugin.securityTimeout < breakTime && plugin.cenotaphRemove && !plugin.keepUntilEmpty ? plugin.convertTime(secTimeLeft) : "Inf" ) + " ";
			msg += ChatColor.YELLOW + "BreakTime: " + ChatColor.WHITE + (plugin.cenotaphRemove ? plugin.convertTime(remTimeLeft) : "Inf") + " ";
			if (plugin.removeWhenEmpty || plugin.keepUntilEmpty) {
				msg += ChatColor.YELLOW + "BreakOverride: " + ChatColor.WHITE;
				if (plugin.removeWhenEmpty) msg += "Break on empty";
				if (plugin.removeWhenEmpty && plugin.keepUntilEmpty) msg += " & ";
				if (plugin.keepUntilEmpty) msg += "Keep until empty";			
			}
			
			plugin.sendMessage(p, msg);
			return true;
		} else if (cmd.equalsIgnoreCase("cenreset")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphreset")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;
		}
		else if (cmd.equalsIgnoreCase("cenadmin")) {
			if (!p.hasPermission("cenotaph.admin")) {
				plugin.sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length == 0) {
				plugin.sendMessage(p, "Usage: /cenadmin list"); //TODO 2.2 use name matching
				plugin.sendMessage(p, "Usage: /cenadmin list <playerCaseSensitive>");
				plugin.sendMessage(p, "Usage: /cenadmin find <playerCaseSensitive> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin remove <playerCaseSensitive> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin version");
				plugin.sendMessage(p, "Usage: /cenadmin reload");
				return true;
			}
			if (args[0].equalsIgnoreCase("list")) {
				if (!p.hasPermission("cenotaph.admin.list")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				if (args.length < 2) {
					if (Cenotaph.playerTombList.keySet().isEmpty()) {
						plugin.sendMessage(p, "There are no cenotaphs.");
						return true;
					}
					plugin.sendMessage(p, "Players with cenotaphs:");
					for (String player : Cenotaph.playerTombList.keySet()) {
						plugin.sendMessage(p, player);
					}
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(args[1]);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				plugin.sendMessage(p, "Cenotaph List:");
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					int X = tomb.getBlock().getX();
					int Y = tomb.getBlock().getY();
					int Z = tomb.getBlock().getZ();
					plugin.sendMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + X + "," + Y + "," + Z + ")");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!p.hasPermission("cenotaph.admin.find")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(args[1]);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				double degrees = (plugin.getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				plugin.sendMessage(p, args[1] + "'s cenotaph #" + args[2] + " is at " + X + "," + Y + "," + Z + ", to the " + Cenotaph.getDirection(degrees) + ".");
				return true;
			} else if (args[0].equalsIgnoreCase("time")) {
				if (!p.hasPermission("cenotaph.admin.cenotaphtime")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				if (args.length != 3) return false;
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(args[1]);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				long cTime = System.currentTimeMillis() / 1000;
				TombBlock tBlock = pList.get(slot);
				long secTimeLeft = (tBlock.getTime() + plugin.securityTimeout) - cTime;
				long remTimeLeft = (tBlock.getTime() + plugin.removeTime) - cTime;
				if (plugin.securityRemove && secTimeLeft > 0) plugin.sendMessage(p, "Security removal: " + secTimeLeft + " seconds.");
				if (plugin.cenotaphRemove & remTimeLeft > 0) plugin.sendMessage(p, "Cenotaph removal: " + remTimeLeft + " seconds.");
				if (plugin.keepUntilEmpty || plugin.removeWhenEmpty) plugin.sendMessage(p, "Keep until empty:" + plugin.keepUntilEmpty + "; remove when empty: " + plugin.removeWhenEmpty);
				return true;
			} else if (args[0].equalsIgnoreCase("version")) {
				String message;
				message = plugin.versionCheck(false);
				plugin.sendMessage(p, message);

				if (plugin.configVer == 0) {
					plugin.sendMessage(p, "Using default config.");
				}
				else if (plugin.configVer < plugin.configCurrent) {
					plugin.sendMessage(p, "Your config file is out of date.");
				}
				else if (plugin.configVer == plugin.configCurrent) {
					plugin.sendMessage(p, "Your config file is up to date.");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!p.hasPermission("cenotaph.admin.remove")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(args[1]);
				if (pList == null) {
					plugin.sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					plugin.sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				plugin.destroyCenotaph(tBlock);
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!p.hasPermission("cenotaph.admin.reload")) {
					plugin.sendMessage(p, "Permission Denied");
					return true;
				}
				plugin.loadConfig();
				Cenotaph.log.info("[Cenotaph] Configuration reloaded from file.");
				plugin.sendMessage(p, "Configuration reloaded from file.");
			} else {
				plugin.sendMessage(p, "Usage: /cenadmin list");
				plugin.sendMessage(p, "Usage: /cenadmin list <playerCaseSensitive>");
				plugin.sendMessage(p, "Usage: /cenadmin find <playerCaseSensitive> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin remove <playerCaseSensitive> <#>");
				plugin.sendMessage(p, "Usage: /cenadmin version");
				return true;
			}
			return true;
		}
		return false;
	}
}
