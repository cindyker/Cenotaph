package com.MoofIT.Minecraft.Cenotaph;

/**
 * Cenotaph - A Dead Man's Chest plugin for Bukkit
 * By Jim Drey (Southpaw018) <moof@moofit.com>
 * Original Copyright (C) 2011 Steven "Drakia" Scott <Drakia@Gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphBlockListener;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphEntityListener;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphPlayerListener;
import net.milkbowl.vault.economy.Economy;

/*
TODO
	--GENERAL
	- dynmap: display break time and security info
	- option to force low priority for player death events
	- block replacement strategy: agressive vs. passive

	--ECONOMY
	- life purchasing
	- keep compass option
	- keep chest and sign option

	--REGION PROTECTION
	- towny integration
	- factions integration

	--PVP
	- allow ops to restrict # of items looted
	- allow options to add killer to lock, or everyone to lock, or killer group to lock
*/

public class Cenotaph extends JavaPlugin {
	public final CenotaphEntityListener entityListener = new CenotaphEntityListener(this);
	public final CenotaphBlockListener blockListener = new CenotaphBlockListener(this);
	public final CenotaphPlayerListener playerListener = new CenotaphPlayerListener(this);
	public final CenotaphCommand commandExec = new CenotaphCommand(this);
	public final DynmapThread dynThread = new DynmapThread(this);
	public static Logger log;
	public static Cenotaph plugin;
	
	PluginManager pm;

	public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
	public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
	public static HashMap<String, ArrayList<TombBlock>> playerTombList = new HashMap<String, ArrayList<TombBlock>>();
	public static HashMap<String, EntityDamageEvent> deathCause = new HashMap<String, EntityDamageEvent>();
	
	//This is still required because of how the DynmapThread is implemented. 
	//TODO: Figure out a different way of showing dynmap markers that doesn't required casting variable to a config that doesn't save them.
	public FileConfiguration config;

	public DynmapAPI dynmap = null;
	public static boolean economyEnabled = false;
	public static boolean dynmapEnabled = false;
	public static boolean worldguardEnabled = false;
	private String version = "2.0.0";
	public static Economy econ = null;	

	@Override
	public void onEnable() {
		log = Logger.getLogger("Minecraft");
		version = this.getDescription().getVersion();
		plugin = this;
		pm = getServer().getPluginManager();

		pm.registerEvents(entityListener,this);
		pm.registerEvents(blockListener,this);
		pm.registerEvents(playerListener,this);
				
		if (!loadSettings()) {
			log.info("Cenotaph config.yml couldn't load.");
			onDisable();
		}

		economyEnabled = setupEconomy();
		dynmapEnabled = setupDynmap();
		worldguardEnabled = setupWorldGuard();		
		
		for (World w : getServer().getWorlds())
			loadTombList(w.getName());

		// Start removal timer. Run every 5 seconds (20 ticks per second)
		if (CenotaphSettings.cenotaphRemove())
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new TombThread(this), 0L, 100L);
		
		log.info("Cenotaph " + getDescription().getVersion() + " is enabled.");
	}
	
	public String getVersion() {
		return version;
	}
	
    boolean loadSettings() {
        
        try {
            CenotaphSettings.loadConfig(this.getDataFolder() + File.separator + "config.yml", getVersion());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
        return true;
    }

    private boolean setupEconomy() {
    	if (pm.isPluginEnabled("Vault")) {
	        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (economyProvider != null) {
	            econ = economyProvider.getProvider();
	            log.info("Cenotaph hooked into Vault.");
	        }
	        return (econ != null);
        } else {
        	return false;
        }
    }
    private boolean setupDynmap() {
    	if (pm.isPluginEnabled("Dynmap")) {
    		if (!CenotaphSettings.dynmapEnable())
    			return false;
    		else {
    			dynThread.activate((DynmapAPI) pm.getPlugin("dynmap"));
    			log.info("Cenotaph hooked into DynMap.");
    			return true;
    		}
    	} 
    	return false;
    }
    private boolean setupWorldGuard() {
    	if (pm.isPluginEnabled("WorldGuard")) {
    		if (!CenotaphSettings.worldguardEnable())
    			return false;
    		else {
    			log.info("Cenotaph hooked into WorldGuard.");
    			return true;
    		}
    	}
    	return false;
    }
    

	public void loadTombList(String world) {
		if (!CenotaphSettings.saveCenotaphList()) return;
		try {
			File fh = new File(this.getDataFolder().getPath(), "tombList-" + world + ".db");
			if (!fh.exists()) return;
			Scanner scanner = new Scanner(fh);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] split = line.split(":");
				Block block, lBlock, sign = null;
				long time;
				int level;
				UUID ownerUUID;				
				
				//Try and load old databases first.
				if (split.length > 6) {
					//block:lblock:sign:owner:level:time:lwc:locketteSign
					block = readBlock(split[0]);
					lBlock = readBlock(split[1]);
					sign = readBlock(split[2]);									
					level = Integer.valueOf(split[4]);
					time = Long.valueOf(split[5]);
					if (split.length == 8) {
						ownerUUID = null;
						continue;
					} else {
						ownerUUID = UUID.fromString(split[8]);
					}
				//Must be a new database Cenotaph 5.3+
				} else {
					//block:lblock:sign:time:ownerlevel:ownerUUID
					block = readBlock(split[0]);
					lBlock = readBlock(split[1]);
					sign = readBlock(split[2]);
					time = Long.valueOf(split[3]);
					level = Integer.valueOf(split[4]);
					ownerUUID = UUID.fromString(split[5]);
				}
				if (block == null ) {
					log.info("[Cenotaph] Invalid entry in database " + fh.getName());
					continue;
				}
				
				TombBlock tBlock = new TombBlock(block, lBlock, sign, time, level, ownerUUID);
				tombList.offer(tBlock);
				// Used for quick tombStone lookup
				tombBlockList.put(block.getLocation(), tBlock);
				if (lBlock != null) tombBlockList.put(lBlock.getLocation(), tBlock);
				if (sign != null) tombBlockList.put(sign.getLocation(), tBlock);
				ArrayList<TombBlock> pList = playerTombList.get(Bukkit.getOfflinePlayer(ownerUUID).getName());
				if (pList == null) {
					pList = new ArrayList<TombBlock>();
					playerTombList.put(Bukkit.getOfflinePlayer(ownerUUID).getName(), pList);
				}
				pList.add(tBlock);
			}
			scanner.close();
		} catch (IOException e) {
			log.info("[Cenotaph] Error loading cenotaph list: " + e);
		}
	}

	public void saveCenotaphList(String world) {
		if (!CenotaphSettings.saveCenotaphList()) return;
		try {
			File fh = new File(this.getDataFolder().getPath(), "tombList-" + world + ".db");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fh));
			for (Iterator<TombBlock> iter = tombList.iterator(); iter.hasNext();) {
				TombBlock tBlock = iter.next();
				// Skip not this world
				if (!tBlock.getBlock().getWorld().getName().equalsIgnoreCase(world)) continue;

				StringBuilder builder = new StringBuilder();

				bw.append(printBlock(tBlock.getBlock()));
				bw.append(":");
				bw.append(printBlock(tBlock.getLBlock()));
				bw.append(":");
				bw.append(printBlock(tBlock.getSign()));
				bw.append(":");
				bw.append(String.valueOf(tBlock.getTime()));
				bw.append(":");
				bw.append(Integer.toString(tBlock.getOwnerLevel()));
				bw.append(":");
				bw.append(String.valueOf(tBlock.getOwnerUUID()));
				bw.append(builder.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			log.info("[Cenotaph] Error saving cenotaph list: " + e);
		}
	}

	private String printBlock(Block b) {
		if (b == null) return null;
		return b.getWorld().getName() + "," + b.getX() + "," + b.getY() + "," + b.getZ();
	}

	private Block readBlock(String b) {
		if (b.length() == 0) return null;
		String[] split = b.split(",");
		//world,x,y,z
		World world = getServer().getWorld(split[0]);
		if (world == null) return null;
		return world.getBlockAt(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
	}
	
	@Override
	public void onDisable() {
		for (World w : getServer().getWorlds()) saveCenotaphList(w.getName());
		if (CenotaphSettings.dynmapEnable() && dynmap != null) dynThread.cenotaphLayer.cleanup();
		getServer().getScheduler().cancelTasks(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return commandExec.onCommand(sender, command, label, args);
	}

	public void removeTomb(TombBlock tBlock, boolean removeList) {
		if (tBlock == null) return;

		tombBlockList.remove(tBlock.getBlock().getLocation());
		if (tBlock.getLBlock() != null) tombBlockList.remove(tBlock.getLBlock().getLocation());
		if (tBlock.getSign() != null) tombBlockList.remove(tBlock.getSign().getLocation());

		// Remove just this tomb from tombList
		ArrayList<TombBlock> tList = playerTombList.get(tBlock.getOwner());
		if (tList != null) {
			tList.remove(tBlock);
			if (tList.size() == 0) {
				playerTombList.remove(tBlock.getOwner());
			}
		}

		if (removeList)
			tombList.remove(tBlock);

		if (tBlock.getBlock() != null)
			saveCenotaphList(tBlock.getBlock().getWorld().getName());
	}

	public void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.GOLD + "[Cenotaph] " + ChatColor.WHITE + message);
	}

	public void destroyCenotaph(Location loc) {
		destroyCenotaph(tombBlockList.get(loc));
	}
	public void destroyCenotaph(TombBlock tBlock) {
		if (tBlock.getBlock().getChunk().load() == false) {
			log.severe("[Cenotaph] Error loading world chunk trying to remove cenotaph at " + tBlock.getBlock().getX() + "," + tBlock.getBlock().getY() + "," + tBlock.getBlock().getZ() + " owned by " + tBlock.getOwner() + ".");
			return;
		}

		tBlock.getBlock().setType(Material.AIR);
		if (tBlock.getLBlock() != null) tBlock.getLBlock().setType(Material.AIR);
		if (tBlock.getSign() != null) tBlock.getSign().setType(Material.AIR);

		removeTomb(tBlock, true);

		Player p = null;
		if (tBlock.getOwnerUUID() != null)
			p = getServer().getPlayer(tBlock.getOwnerUUID());
		if (p != null) sendMessage(p, "Your cenotaph has broken.");
	}

	public HashMap<String, ArrayList<TombBlock>> getCenotaphList() {
		return playerTombList;
	}
}
