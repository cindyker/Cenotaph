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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import org.yi.acru.bukkit.Lockette.Lockette;

/*
TODO 2.2 release
	- code refactor
	- improved override messages
	- improved timing messages
	- dynmap integration
TODO 2.3 release
	- vault integration
	- cenotaph payment
	- worldguard integration?
	- towny integration
	- pvp raid options
		- allow ops to restrict # of items looted
		- allow options to add killer to lock, or everyone to lock, or killer group to lock
*/

public class Cenotaph extends JavaPlugin {
	public final CenotaphEntityListener entityListener = new CenotaphEntityListener(this);
	public final CenotaphBlockListener blockListener = new CenotaphBlockListener(this);
	public final CenotaphServerListener serverListener = new CenotaphServerListener(this);
	public final CenotaphPlayerListener playerListener = new CenotaphPlayerListener(this);
	public static Logger log;
	PluginManager pm;

	public LWCPlugin lwcPlugin = null;
	public Lockette LockettePlugin = null;

	public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
	public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
	public static HashMap<String, ArrayList<TombBlock>> playerTombList = new HashMap<String, ArrayList<TombBlock>>();
	public static HashMap<String, EntityDamageEvent> deathCause = new HashMap<String, EntityDamageEvent>();

	public FileConfiguration config;

	/**
	 * Configuration options - Defaults
	 */
	//Core
	public boolean cenotaphSign = true;
	public boolean noDestroy = false;
	public boolean saveCenotaphList = true;
	public boolean noInterfere = true;
	public boolean versionCheck = true;
	public boolean voidCheck = true;
	public boolean creeperProtection = false;
	public String signMessage[] = new String[] {
		"{name}",
		"RIP",
		"{date}",
		"{time}"
	};
	public String dateFormat = "MM/dd/yyyy";
	public String timeFormat = "hh:mm a";
	public List<String> disableInWorlds;

	//Removal
	public boolean destroyQuickLoot = false;
	public boolean cenotaphRemove = false;
	public int removeTime = 3600;
	public boolean removeWhenEmpty = false;
	public boolean keepUntilEmpty = false;
	public boolean levelBasedRemoval = false;
	public int levelBasedTime = 300;

	//Security
	public boolean LocketteEnable = true;
	public boolean lwcEnable = false;
	public boolean securityRemove = false;
	public int securityTimeout = 3600;
	public boolean lwcPublic = false;

	//DeathMessages
	public HashMap<String, Object> deathMessages = new HashMap<String, Object>() {
		public static final long serialVersionUID = 1L;
		{
			put("Monster.Zombie", "a Zombie");
			put("Monster.Skeleton", "a Skeleton");
			put("Monster.Spider", "a Spider");
			put("Monster.Wolf", "a Wolf");
			put("Monster.Creeper", "a Creeper");
			put("Monster.Slime", "a Slime");
			put("Monster.Ghast", "a Ghast");
			put("Monster.PigZombie", "a Pig Zombie");
			put("Monster.Giant", "a Giant");
			put("Monster.Other", "a Monster");
			put("Monster.Blaze", "a Blaze");
			put("Monster.CaveSpider", "a Cave Spider");
			put("Monster.EnderDragon", "a Dragon");
			put("Monster.Enderman", "an Enderman");
			put("Monster.IronGolem", "an Iron Golem");
			put("Monster.MagmaCube", "a Magma Cube");
			put("Monster.Silverfish", "a Siverfish");

	
			put("World.Cactus", "a Cactus");
			put("World.Suffocation", "Suffocation");
			put("World.Fall", "a Fall");
			put("World.Fire", "a Fire");
			put("World.Burning", "Burning");
			put("World.Lava", "Lava");
			put("World.Drowning", "Drowning");
			put("World.Lightning", "Lightning");
	
			put("Explosion.Misc", "an Explosion");
			put("Explosion.TNT", "a TNT Explosion");
	
			put("Misc.Dispenser", "a Dispenser");
			put("Misc.Void", "the Void");
			put("Misc.Other", "Unknown");
		}
	};

	//Config versioning
	public int configVer = 0;
	public final int configCurrent = 12;

	public void onEnable() {
		log = Logger.getLogger("Minecraft");

		log.info("Cenotaph " + getDescription().getVersion() + " is enabled.");

		pm = getServer().getPluginManager();

		pm.registerEvents(entityListener,this);
		pm.registerEvents(blockListener,this);
		pm.registerEvents(playerListener,this);
		pm.registerEvents(serverListener,this);

		lwcPlugin = (LWCPlugin)checkPlugin("LWC");
		LockettePlugin = (Lockette)checkPlugin("Lockette");

		loadConfig();
		for (World w : getServer().getWorlds())
			loadTombList(w.getName());

		if (versionCheck) {
			versionCheck(true);
		}

		// Start removal timer. Run every 5 seconds (20 ticks per second)
		if (securityRemove || cenotaphRemove)
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new TombThread(), 0L, 100L);
	}

	public void loadConfig() {
		this.reloadConfig();
		config = this.getConfig();

		configVer = config.getInt("configVer", configVer);
		if (configVer == 0) {
			log.info("[Cenotaph] Configuration error or no config file found. Generating default config file.");
			saveDefaultConfig();
			this.reloadConfig(); //hack to force good data into configs TODO 2.2: proper defaults
			config = this.getConfig();			
		}
		else if (configVer < configCurrent) {
			log.warning("[Cenotaph] Your config file is out of date! Delete your config and /cenadmin reload to see the new options. Proceeding using set options from config file and defaults for new options..." );
		}

		//Core
		cenotaphSign = config.getBoolean("Core.cenotaphSign", cenotaphSign);
		noDestroy = config.getBoolean("Core.noDestroy", noDestroy);
		saveCenotaphList = config.getBoolean("Core.saveCenotaphList", saveCenotaphList);
		noInterfere = config.getBoolean("Core.noInterfere", noInterfere);
		versionCheck = config.getBoolean("Core.versionCheck", versionCheck);
		voidCheck = config.getBoolean("Core.voidCheck", voidCheck);
		creeperProtection = config.getBoolean("Core.creeperProtection", creeperProtection);
		signMessage = loadSign();
		dateFormat = config.getString("Core.Sign.dateFormat", dateFormat);
		timeFormat = config.getString("Core.Sign.timeFormat", timeFormat);

		try {
			disableInWorlds = config.getStringList("Core.disableInWorlds");
		} catch (NullPointerException e) {
			log.warning("[Cenotaph] Configuration failure while loading disableInWorlds. Using defaults.");
		}		

		//Removal
		destroyQuickLoot = config.getBoolean("Removal.destroyQuickLoot", destroyQuickLoot);
		cenotaphRemove = config.getBoolean("Removal.cenotaphRemove", cenotaphRemove);
		removeTime = config.getInt("Removal.removeTime", removeTime);
		removeWhenEmpty = config.getBoolean("Removal.removeWhenEmpty", removeWhenEmpty);
		keepUntilEmpty = config.getBoolean("Removal.keepUntilEmpty", keepUntilEmpty);
		levelBasedRemoval = config.getBoolean("Removal.levelBasedRemoval", levelBasedRemoval);
		levelBasedTime = config.getInt("Removal.levelBasedTime", levelBasedTime);

		//Security
		LocketteEnable = config.getBoolean("Security.LocketteEnable", LocketteEnable);
		lwcEnable = config.getBoolean("Security.lwcEnable", lwcEnable);
		securityRemove = config.getBoolean("Security.securityRemove", securityRemove);
		securityTimeout = config.getInt("Security.securityTimeout", securityTimeout);
		lwcPublic = config.getBoolean("Security.lwcPublic", lwcPublic);

		//DeathMessages
		try {
			deathMessages = (HashMap<String, Object>)config.getConfigurationSection("DeathMessages").getValues(true);
		} catch (NullPointerException e) {
			log.warning("[Cenotaph] Configuration failure while loading deathMessages. Using defaults.");
		}
	}

	public void loadTombList(String world) {
		if (!saveCenotaphList) return;
		try {
			File fh = new File(this.getDataFolder().getPath(), "tombList-" + world + ".db");
			if (!fh.exists()) return;
			Scanner scanner = new Scanner(fh);
			while (scanner.hasNextLine()) { //TODO handle bad entry cases 
				String line = scanner.nextLine().trim();
				String[] split = line.split(":");
				//block:lblock:sign:owner:level:time:lwc
				Block block = readBlock(split[0]);
				Block lBlock = readBlock(split[1]);
				Block sign = readBlock(split[2]);
				String owner = split[3];
				int level = Integer.valueOf(split[4]);
				long time = Long.valueOf(split[5]);
				boolean lwc = Boolean.valueOf(split[6]);

				if (block == null || owner == null) {
					log.info("[Cenotaph] Invalid entry in database " + fh.getName());
					continue;
				}
				TombBlock tBlock = new TombBlock(block, lBlock, sign, owner, level, time, lwc);
				tombList.offer(tBlock);
				// Used for quick tombStone lookup
				tombBlockList.put(block.getLocation(), tBlock);
				if (lBlock != null) tombBlockList.put(lBlock.getLocation(), tBlock);
				if (sign != null) tombBlockList.put(sign.getLocation(), tBlock);
				ArrayList<TombBlock> pList = playerTombList.get(owner);
				if (pList == null) {
					pList = new ArrayList<TombBlock>();
					playerTombList.put(owner, pList);
				}
				pList.add(tBlock);
			}
			scanner.close();
		} catch (IOException e) {
			log.info("[Cenotaph] Error loading cenotaph list: " + e);
		}
	}

	public void saveCenotaphList(String world) {
		if (!saveCenotaphList) return;
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
				bw.append(tBlock.getOwner());
				bw.append(":");
				bw.append(Integer.toString(tBlock.getOwnerLevel()));
				bw.append(":");				
				bw.append(String.valueOf(tBlock.getTime()));
				bw.append(":");
				bw.append(String.valueOf(tBlock.getLwcEnabled()));

				bw.append(builder.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			log.info("[Cenotaph] Error saving cenotaph list: " + e);
		}
	}

	private String printBlock(Block b) {
		if (b == null) return "";
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

	public void onDisable() {
		for (World w : getServer().getWorlds())
			saveCenotaphList(w.getName());
	}
	private String[] loadSign() {
		String[] msg = signMessage;
		msg[0] = config.getString("Core.Sign.Line1", signMessage[0]);
		msg[1] = config.getString("Core.Sign.Line2", signMessage[1]);
		msg[2] = config.getString("Core.Sign.Line3", signMessage[2]);
		msg[3] = config.getString("Core.Sign.Line4", signMessage[3]);
		return msg;
	}

	/*
	 * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
	 */
	private Plugin checkPlugin(String p) {
		Plugin plugin = pm.getPlugin(p);
		return checkPlugin(plugin);
	}

	public Plugin checkPlugin(Plugin plugin) {
		if (plugin != null && plugin.isEnabled()) {
			log.info("[Cenotaph] Using " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
			return plugin;
		}
		return null;
	}

	public void deactivateLWC(TombBlock tBlock, boolean force) {
		if (!lwcEnable) return;
		if (lwcPlugin == null) return;
		LWC lwc = lwcPlugin.getLWC();

		// Remove the protection on the chest
		Block _block = tBlock.getBlock();
		Protection protection = lwc.findProtection(_block);
		if (protection != null) {
			lwc.getPhysicalDatabase().removeProtection(protection.getId());
			//Set to public instead of removing completely
			if (lwcPublic && !force)
				lwc.getPhysicalDatabase().registerProtection(_block.getTypeId(), Protection.Type.PUBLIC, _block.getWorld().getName(), tBlock.getOwner(), "", _block.getX(), _block.getY(), _block.getZ());
		}

		// Remove the protection on the sign
		_block = tBlock.getSign();
		if (_block != null) {
			protection = lwc.findProtection(_block);
			if (protection != null) {
				protection.remove();
				// Set to public instead of removing completely
				if (lwcPublic && !force)
					lwc.getPhysicalDatabase().registerProtection(_block.getTypeId(), Protection.Type.PUBLIC, _block.getWorld().getName(), tBlock.getOwner(), "", _block.getX(), _block.getY(), _block.getZ());
			}
		}
		tBlock.setLwcEnabled(false);
	}
	public void deactivateLockette(TombBlock tBlock) {
		if (tBlock.getLocketteSign() == null) return;
		tBlock.getLocketteSign().getBlock().setType(Material.AIR);
		tBlock.removeLocketteSign();
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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { //TODO needs major cleanup, move indexing to separate class function
		if (!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenlist")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphlist")) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			ArrayList<TombBlock> pList = playerTombList.get(p.getName());
			if (pList == null) {
				sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			sendMessage(p, "Cenotaph List:");
			int i = 0;
			for (TombBlock tomb : pList) {
				i++;
				if (tomb.getBlock() == null) continue;
				int X = tomb.getBlock().getX();
				int Y = tomb.getBlock().getY();
				int Z = tomb.getBlock().getZ();
				sendMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + X + "," + Y + "," + Z + ")");
			}
			return true;
		} else if (cmd.equalsIgnoreCase("cenfind")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphfind")) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length != 1) return false;
			ArrayList<TombBlock> pList = playerTombList.get(p.getName());
			if (pList == null) {
				sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			int slot = 0;
			try {
				slot = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sendMessage(p, "Invalid cenotaph");
				return true;
			}
			slot -= 1;
			if (slot < 0 || slot >= pList.size()) {
				sendMessage(p, "Invalid cenotaph");
				return true;
			}
			TombBlock tBlock = pList.get(slot);
			double degrees = (getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
			p.setCompassTarget(tBlock.getBlock().getLocation());
			sendMessage(p, "Your cenotaph #" + args[0] + " is to the " + getDirection(degrees) + ". Your compass has been set to point at its location. Use /cenreset to reset it to your spawn point.");
			return true;
		} else if (cmd.equalsIgnoreCase("centime")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphtime")) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length != 1) return false;
			ArrayList<TombBlock> pList = playerTombList.get(p.getName());
			if (pList == null) {
				sendMessage(p, "You have no cenotaphs.");
				return true;
			}
			int slot = 0;
			try {
				slot = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sendMessage(p, "Invalid cenotaph");
				return true;
			}
			slot -= 1;
			if (slot < 0 || slot >= pList.size()) {
				sendMessage(p, "Invalid cenotaph");
				return true;
			}
			long cTime = System.currentTimeMillis() / 1000;
			TombBlock tBlock = pList.get(slot);
			long secTimeLeft = (tBlock.getTime() + securityTimeout) - cTime;
			long remTimeLeft = (tBlock.getTime() + removeTime) - cTime;

			//TODO rework to support shortMessaging
			if (securityRemove && secTimeLeft > 0) sendMessage(p, "Security will be removed from your cenotaph in " + secTimeLeft + " seconds.");

			if (cenotaphRemove & remTimeLeft > 0) sendMessage(p, "Your cenotaph will break in " + remTimeLeft + " seconds");
			if (removeWhenEmpty && keepUntilEmpty) sendMessage(p, "Break override: Your cenotaph will break when it is emptied, but will not break until then.");
			else {
				if (removeWhenEmpty) sendMessage(p, "Break override: Your cenotaph will break when it is emptied.");
				if (keepUntilEmpty) sendMessage(p, "Break override: Your cenotaph will not break until it is empty.");
			}

			return true;
		} else if (cmd.equalsIgnoreCase("cenreset")) {
			if (!p.hasPermission("cenotaph.cmd.cenotaphreset")) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;
		}
		else if (cmd.equalsIgnoreCase("cenadmin")) {
			if (!p.hasPermission("cenotaph.admin")) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			if (args.length == 0) {
				sendMessage(p, "Usage: /cenadmin list"); //TODO 2.2 use name matching
				sendMessage(p, "Usage: /cenadmin list <playerCaseSensitive>");
				sendMessage(p, "Usage: /cenadmin find <playerCaseSensitive> <#>");
				sendMessage(p, "Usage: /cenadmin remove <playerCaseSensitive> <#>");
				sendMessage(p, "Usage: /cenadmin version");
				sendMessage(p, "Usage: /cenadmin reload");
				return true;
			}
			if (args[0].equalsIgnoreCase("list")) {
				if (!p.hasPermission("cenotaph.admin.list")) {
					sendMessage(p, "Permission Denied");
					return true;
				}
				if (args.length < 2) {
					if (playerTombList.keySet().isEmpty()) {
						sendMessage(p, "There are no cenotaphs.");
						return true;
					}
					sendMessage(p, "Players with cenotaphs:");
					for (String player : playerTombList.keySet()) {
						sendMessage(p, player);
					}
					return true;
				}
				ArrayList<TombBlock> pList = playerTombList.get(args[1]);
				if (pList == null) {
					sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				sendMessage(p, "Cenotaph List:");
				int i = 0;
				for (TombBlock tomb : pList) {
					i++;
					if (tomb.getBlock() == null) continue;
					int X = tomb.getBlock().getX();
					int Y = tomb.getBlock().getY();
					int Z = tomb.getBlock().getZ();
					sendMessage(p, " " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + X + "," + Y + "," + Z + ")");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("find")) {
				if (!p.hasPermission("cenotaph.admin.find")) {
					sendMessage(p, "Permission Denied");
					return true;
				}
				ArrayList<TombBlock> pList = playerTombList.get(args[1]);
				if (pList == null) {
					sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				double degrees = (getYawTo(tBlock.getBlock().getLocation(), p.getLocation()) + 270) % 360;
				int X = tBlock.getBlock().getX();
				int Y = tBlock.getBlock().getY();
				int Z = tBlock.getBlock().getZ();
				sendMessage(p, args[1] + "'s cenotaph #" + args[2] + " is at " + X + "," + Y + "," + Z + ", to the " + getDirection(degrees) + ".");
				return true;
			} else if (args[0].equalsIgnoreCase("time")) {
				if (!p.hasPermission("cenotaph.admin.cenotaphtime")) {
					sendMessage(p, "Permission Denied");
					return true;
				}
				if (args.length != 3) return false;
				ArrayList<TombBlock> pList = playerTombList.get(args[1]);
				if (pList == null) {
					sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				long cTime = System.currentTimeMillis() / 1000;
				TombBlock tBlock = pList.get(slot);
				long secTimeLeft = (tBlock.getTime() + securityTimeout) - cTime;
				long remTimeLeft = (tBlock.getTime() + removeTime) - cTime;
				if (securityRemove && secTimeLeft > 0) sendMessage(p, "Security removal: " + secTimeLeft + " seconds.");
				if (cenotaphRemove & remTimeLeft > 0) sendMessage(p, "Cenotaph removal: " + remTimeLeft + " seconds.");
				if (keepUntilEmpty || removeWhenEmpty) sendMessage(p, "Keep until empty:" + keepUntilEmpty + "; remove when empty: " + removeWhenEmpty);
				return true;
			} else if (args[0].equalsIgnoreCase("version")) {
				String message;
				message = versionCheck(false);
				sendMessage(p, message);

				if (configVer == 0) {
					sendMessage(p, "Using default config.");
				}
				else if (configVer < configCurrent) {
					sendMessage(p, "Your config file is out of date.");
				}
				else if (configVer == configCurrent) {
					sendMessage(p, "Your config file is up to date.");
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!p.hasPermission("cenotaph.admin.remove")) {
					sendMessage(p, "Permission Denied");
					return true;
				}
				ArrayList<TombBlock> pList = playerTombList.get(args[1]);
				if (pList == null) {
					sendMessage(p, "No cenotaphs found for " + args[1] + ".");
					return true;
				}
				int slot = 0;
				try {
					slot = Integer.parseInt(args[2]);
				} catch (Exception e) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				slot -= 1;
				if (slot < 0 || slot >= pList.size()) {
					sendMessage(p, "Invalid cenotaph entry.");
					return true;
				}
				TombBlock tBlock = pList.get(slot);
				destroyCenotaph(tBlock);
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!p.hasPermission("cenotaph.admin.reload")) {
					sendMessage(p, "Permission Denied");
					return true;
				}
				loadConfig();
				log.info("[Cenotaph] Configuration reloaded from file.");
				sendMessage(p, "Configuration reloaded from file.");
			} else {
				sendMessage(p, "Usage: /cenadmin list");
				sendMessage(p, "Usage: /cenadmin list <playerCaseSensitive>");
				sendMessage(p, "Usage: /cenadmin find <playerCaseSensitive> <#>");
				sendMessage(p, "Usage: /cenadmin remove <playerCaseSensitive> <#>");
				sendMessage(p, "Usage: /cenadmin version");
				return true;
			}
			return true;
		}
		return false;
	}

	public String versionCheck(Boolean printToLog) {
		String thisVersion = getDescription().getVersion();
		URL url = null;
		try {
			url = new URL("http://www.moofit.com/minecraft/cenotaph.ver?v=" + thisVersion);
			BufferedReader in = null;
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			String newVersion = "";
			String line;
			while ((line = in.readLine()) != null) {
				newVersion += line;
			}
			in.close();
			if (!newVersion.equals(thisVersion)) {
				if (printToLog) log.warning("[Cenotaph] Cenotaph is out of date! This version: " + thisVersion + "; latest version: " + newVersion + ".");
				return "Cenotaph is out of date! This version: " + thisVersion + "; latest version: " + newVersion + ".";
			}
			else {
				if (printToLog) log.info("[Cenotaph] Cenotaph is up to date at version " + thisVersion + ".");
				return "Cenotaph is up to date at version " + thisVersion + ".";
			}
		}
		catch (MalformedURLException ex) {
			if (printToLog) log.warning("[Cenotaph] Error accessing update URL.");
			return "Error accessing update URL.";
		}
		catch (IOException ex) {
			if (printToLog) log.warning("[Cenotaph] Error checking for update.");
			return "Error checking for update.";
		}
	}

	/**
	 * Gets the Yaw from one location to another in relation to North.
	 *
	 */
	public double getYawTo(Location from, Location to) {
			final int distX = to.getBlockX() - from.getBlockX();
			final int distZ = to.getBlockZ() - from.getBlockZ();
			double degrees = Math.toDegrees(Math.atan2(-distX, distZ));
			degrees += 180;
		return degrees;
	}

	/**
	 * Converts a rotation to a cardinal direction name.
	 * Author: sk89q - Original function from CommandBook plugin
	 * @param rot
	 * @return
	 */
	public static String getDirection(double rot) {
		if (0 <= rot && rot < 22.5) {
			return "North";
		} else if (22.5 <= rot && rot < 67.5) {
			return "Northeast";
		} else if (67.5 <= rot && rot < 112.5) {
			return "East";
		} else if (112.5 <= rot && rot < 157.5) {
			return "Southeast";
		} else if (157.5 <= rot && rot < 202.5) {
			return "South";
		} else if (202.5 <= rot && rot < 247.5) {
			return "Southwest";
		} else if (247.5 <= rot && rot < 292.5) {
			return "West";
		} else if (292.5 <= rot && rot < 337.5) {
			return "Northwest";
		} else if (337.5 <= rot && rot < 360.0) {
			return "North";
		} else {
			return null;
		}
	}

	public String convertTime(int s) {
		String formatted = Integer.toString(s);
		if (s >= 86400) {
			formatted = String.format("%d:%d:%02d:%02d", s/86400, (s%86400)/3600, (s%3600)/60, s%60);					
		}
		else if (s >= 3600) {
			formatted = String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
		}
		else if (s > 60) {
			formatted = String.format("%02d:%02d", s/60, s%60);
		}
		return formatted;
	}

	public void sendMessage(Player player, String message) {
		player.sendMessage(ChatColor.GOLD + "[Cenotaph] " + ChatColor.WHITE + message);
	}

	private class TombThread extends Thread {
		public void run() {
			long cTime = System.currentTimeMillis() / 1000;
			for (Iterator<TombBlock> iter = tombList.iterator(); iter.hasNext();) {
				TombBlock tBlock = iter.next();

				//"empty" option checks
				if (keepUntilEmpty || removeWhenEmpty) {
					if (tBlock.getBlock().getState() instanceof Chest) {
						boolean isEmpty = true;

						Chest sChest = (Chest)tBlock.getBlock().getState();
						Chest lChest = (tBlock.getLBlock() != null) ? (Chest)tBlock.getLBlock().getState() : null;

						for (ItemStack item : sChest.getInventory().getContents()) {
							if (item != null) isEmpty = false;
							break;
						}
						if (lChest != null && !isEmpty) {
							for (ItemStack item : lChest.getInventory().getContents()) {
								if (item != null) isEmpty = false;
								break;
							}
						}
						if (keepUntilEmpty) {
							if (!isEmpty) continue;
						}
						if (removeWhenEmpty) {
							if (isEmpty) {
								destroyCenotaph(tBlock);
								iter.remove();
							}
						}
					}
				}

				//Security removal check
				if (securityRemove) {
					Player p = getServer().getPlayer(tBlock.getOwner());

					if (cTime >= (tBlock.getTime() + securityTimeout)) {
						if (tBlock.getLwcEnabled() && lwcPlugin != null) {
							deactivateLWC(tBlock, false);
							tBlock.setLwcEnabled(false);
							if (p != null)
								sendMessage(p, "LWC protection disabled on your cenotaph!");
						}
						if (tBlock.getLocketteSign() != null && LockettePlugin != null) {
							deactivateLockette(tBlock);
							if (p != null)
								sendMessage(p, "Lockette protection disabled on your cenotaph!");
						}
					}
				}
				//Block removal check
				if (cenotaphRemove) {
					if (levelBasedRemoval) {
						if (cTime > Math.min(tBlock.getTime() + tBlock.getOwnerLevel() * levelBasedTime, tBlock.getTime() + removeTime)) {
							destroyCenotaph(tBlock);
							iter.remove();
						}
					}
					else {
						if (cTime > (tBlock.getTime() + removeTime)) {
							destroyCenotaph(tBlock);
							iter.remove();
						}
					}
				}
			}
		}
	}

	public void destroyCenotaph(Location loc) {
		destroyCenotaph(tombBlockList.get(loc));
	}
	public void destroyCenotaph(TombBlock tBlock) {
		if (tBlock.getBlock().getChunk().load() == false) {
			log.severe("[Cenotaph] Error loading world chunk trying to remove cenotaph at " + tBlock.getBlock().getX() + "," + tBlock.getBlock().getY() + "," + tBlock.getBlock().getZ() + " owned by " + tBlock.getOwner() + ".");
			return;
		}
		if (tBlock.getSign() != null) tBlock.getSign().setType(Material.AIR);
		deactivateLockette(tBlock);
		deactivateLWC(tBlock, true);

		tBlock.getBlock().setType(Material.AIR);
		if (tBlock.getLBlock() != null) tBlock.getLBlock().setType(Material.AIR);

		removeTomb(tBlock, true);

		Player p = getServer().getPlayer(tBlock.getOwner());
		if (p != null) sendMessage(p, "Your cenotaph has broken.");
	}

	public HashMap<String, ArrayList<TombBlock>> getCenotaphList() {
		return playerTombList;
	}
}