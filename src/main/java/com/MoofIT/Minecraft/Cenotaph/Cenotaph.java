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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import com.MoofIT.Minecraft.Cenotaph.Config.Lang;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphBlockListener;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphEntityListener;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphInventoryListener;
import com.MoofIT.Minecraft.Cenotaph.Listeners.CenotaphPlayerListener;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.DynmapThread;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.HolographicDisplays;
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
	public final CenotaphInventoryListener inventoryListener = new CenotaphInventoryListener(this);
	public final CenotaphCommand commandExec = new CenotaphCommand(this);
	public final DynmapThread dynThread = new DynmapThread(this);
	public static Logger log;
	public static Cenotaph plugin;

	PluginManager pm;

	public static boolean economyEnabled = false;
	public static boolean dynmapEnabled = false;
	public static boolean worldguardEnabled = false;
	public static boolean townyEnabled = false;
	public static boolean hologramsEnabled = false;
	private String version = "2.0.0";
	public static Economy econ = null;
	public static boolean isSpigot = false;
	public static boolean slimefunEnabled = false;
	public static Map<World, Integer> worldDepths = new HashMap<>();
	private String hooked = "";

	@Override
	public void onEnable() {
		log = Logger.getLogger("Minecraft");
		version = this.getDescription().getVersion();
		plugin = this;
		pm = getServer().getPluginManager();

		pm.registerEvents(entityListener,this);
		pm.registerEvents(blockListener,this);
		pm.registerEvents(playerListener,this);
		pm.registerEvents(inventoryListener, this);
		
		if (!loadSettings()) {
			CenotaphMessaging.sendSevereConsoleMessage("Cenotaph config.yml couldn't load.");
			onDisable();
		}

		if (!versionCheck()) {
			CenotaphMessaging.sendSevereConsoleMessage("Your previous Cenotaph version cannot be upgraded. Update to any version 5.3-5.7 and then update to this " + version);
			onDisable();	
		}

		if (CenotaphSettings.enableAscii())
			CenotaphMessaging.sendSweetAsciiArt();

		isSpigot = isSpigot();
		economyEnabled = setupEconomy();
		dynmapEnabled = setupDynmap();
		worldguardEnabled = setupWorldGuard();
		townyEnabled = setupTowny();
		hologramsEnabled = setupHolograms();
		slimefunEnabled = setupSlimefun();

		for (World w : getServer().getWorlds()) {
			CenotaphDatabase.loadTombList(w.getName());
			setWorldDepth(w);
		}

		// Start removal timer. Run every 5 seconds (20 ticks per second)
		if (CenotaphSettings.cenotaphRemove())
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new TombThread(this), 0L, 100L);
		
		CenotaphMessaging.sendEnabledMessage(hooked);		
	}
	
	/*
	 * Nothing below Cenotaph 5.3 should be loaded.
	 */
	private boolean versionCheck() {
		String lastRunVersion = CenotaphSettings.getLastRunVersion(version);
		String[] numbers = lastRunVersion.split("\\.");
		int one = Integer.valueOf(numbers[0]);
		int two = Integer.valueOf(numbers[1]);
		if (one < 6) {
			if (one != 5)
				return false;
			if (two <=2)
				return false;
		}
		return true;
	}

	public String getVersion() {
		return version;
	}

	public static boolean isSpigot() {
		try {
			Class.forName("org.bukkit.entity.Player$Spigot");
			return true;
		} catch (Throwable tr) {
			return false;
		}
	}
	
	boolean loadSettings() {
        
        try {
            CenotaphSettings.loadConfig(this.getDataFolder() + File.separator + "config.yml", getVersion());
            Lang.loadLanguage(this.getDataFolder().getPath(), "english.yml");
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
	            hooked += econ.getName() + " via Vault, ";
	        }
	        return (econ != null);
        } else if (CenotaphSettings.cenotaphCost() > 0)
            CenotaphMessaging.sendSevereConsoleMessage("Unable to find Vault. Cenotaph cost will be ignored!");
        return false;        
    }
    private boolean setupDynmap() {
    	if (pm.isPluginEnabled("dynmap")) {
    		if (!CenotaphSettings.dynmapEnable())
    			return false;
    		else {
    			dynThread.activate((DynmapAPI) pm.getPlugin("dynmap"));
    			hooked += "Dynmap, ";
    			return true;
    		}
    	} else if (CenotaphSettings.dynmapEnable())
    	    CenotaphMessaging.sendSevereConsoleMessage("Unabled to find Dynmap. Dynmap not hooked!");
    	return false;
    }
    private boolean setupWorldGuard() {
    	if (pm.isPluginEnabled("WorldGuard")) {
    		if (!CenotaphSettings.worldguardEnable())
    			return false;
    		else {
    			hooked += "WorldGuard, ";
    			return true;
    		}
    	} else if (CenotaphSettings.worldguardEnable())
    	    CenotaphMessaging.sendSevereConsoleMessage("Unabled to find WorldGuard. WorldGuard not hooked!");
    	return false;
    }
    private boolean setupTowny() {
    	if (pm.isPluginEnabled("Towny")) {
    		if (!CenotaphSettings.townyEnable())
    			return false;
    		else {
    			hooked += "Towny, ";
    			return true;
    		}
    	} else if (CenotaphSettings.townyEnable())
    		CenotaphMessaging.sendSevereConsoleMessage("Unable to find Towny. Towny not hooked!");
    	return false;
    }
    private boolean setupHolograms() {
    	if (pm.isPluginEnabled("HolographicDisplays")) {
    		if (!CenotaphSettings.hologramsEnable())
    			return false;
    		else {
    			hooked += "HolographicDisplays, ";
    			HolographicDisplays.loadHolograms();
    			return true;
    		}
    	} else if (CenotaphSettings.hologramsEnable())
    	    CenotaphMessaging.sendSevereConsoleMessage("Unabled to find HolographicDisplays. Holograms will not be used!");
    	return false;
    }
    private boolean setupSlimefun() {
    	if (pm.isPluginEnabled("Slimefun")) {
    		hooked += "SlimeFun, ";
    		return true;
    	}
    	return false;
    }

	public static void setWorldDepth(World w) {
		try {
			setWorldDepth(w, w.getMinHeight());
		} catch (NoSuchMethodError e) {
			setWorldDepth(w, 0);
		}
	}

	private static void setWorldDepth(World w, int minHeight) {
		worldDepths.put(w, minHeight);
	}

	public static boolean hasSetWorldDepth(World w) {
		return worldDepths.containsKey(w);
	}

	public static int getWorldDepth(World w) {
		return worldDepths.get(w);
	}

	@Override
	public void onDisable() {
		for (World w : getServer().getWorlds()) CenotaphDatabase.saveCenotaphList(w.getName());
		if (hologramsEnabled) HolographicDisplays.saveHolograms();
		if (dynmapEnabled) dynThread.cenotaphLayer.cleanup();
		getServer().getScheduler().cancelTasks(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return commandExec.onCommand(sender, command, label, args);
	}
}
