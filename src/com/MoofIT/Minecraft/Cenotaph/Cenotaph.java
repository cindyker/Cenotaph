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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.yi.acru.bukkit.Lockette.Lockette;

public class Cenotaph extends JavaPlugin {
	private final eListener entityListener = new eListener();
	private final bListener blockListener = new bListener();
	private final sListener serverListener = new sListener();
	private final pListener playerListener = new pListener();
	public static Logger log;
	PluginManager pm;

	private Permissions permissions = null;
	private LWCPlugin lwcPlugin = null;
	private Lockette LockettePlugin = null;

	private ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
	private HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
	private HashMap<String, ArrayList<TombBlock>> playerTombList = new HashMap<String, ArrayList<TombBlock>>();
	private HashMap<String, EntityDamageEvent> deathCause = new HashMap<String, EntityDamageEvent>();
	private Configuration config;
	private Cenotaph plugin;

	/**
	 * Configuration options - Defaults
	 */
	//Core
	private boolean logEvents = false;
	private boolean cenotaphSign = true;
	private boolean noDestroy = false;
	private boolean pMessage = true;
	private boolean saveCenotaphList = true;
	private boolean noInterfere = true;
	private boolean versionCheck = true;
	private boolean voidCheck = true;
	private boolean creeperProtection = false;
	private String signMessage[] = new String[] {
		"{name}",
		"RIP",
		"{date}",
		"{time}"
	};

	//Removal
	private boolean destroyQuickLoot = false;
	private boolean cenotaphRemove = false;
	private int removeTime = 18000;

	//Security
	private boolean LocketteEnable = true;
	private boolean lwcEnable = false;
	private boolean lwcRemove = false;
	private int lwcTime = 3600;
	private boolean lwcPublic = false;

	private int configVer = 0;
	private final int configCurrent = 7;

	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		log = Logger.getLogger("Minecraft");
		config = this.getConfiguration();

		String thisVersion = pdfFile.getVersion();
		log.info(pdfFile.getName() + " v." + thisVersion + " is enabled.");

		pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);

		permissions = (Permissions)checkPlugin("Permissions");
		lwcPlugin = (LWCPlugin)checkPlugin("LWC");
		LockettePlugin = (Lockette)checkPlugin("Lockette");
		plugin = this;

		loadConfig();
		for (World w : getServer().getWorlds())
			loadTombList(w.getName());

		if (versionCheck) {
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
					log.warning("[Cenotaph] Cenotaph is out of date! This version: " + thisVersion + "; latest version: " + newVersion);
				}
				else {
					log.info("[Cenotaph] Cenotaph is up to date.");
				}
			}
			catch (MalformedURLException ex) {
				log.warning("[Cenotaph] Error accessing update URL.");
			}
			catch (IOException ex) {
				log.warning("[Cenotaph] Error checking for update.");
			}
		}

		// Start removal timer. Run every 30 seconds (20 ticks per second)
		if (lwcRemove || cenotaphRemove)
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new TombThread(), 0L, 100L);
	}

	public void loadConfig() {
		config.load();

		configVer = config.getInt("configVer", configVer);
		if (configVer == 0) {
			try {
				log.info("[Cenotaph] Configuration error or no config file found. Downloading default config file...");
				if (!new File(getDataFolder().toString()).exists()) {
					new File(getDataFolder().toString()).mkdir();
				}
				URL config = new URL("https://raw.github.com/Southpaw018/Cenotaph/master/config.yml");
				ReadableByteChannel rbc = Channels.newChannel(config.openStream());
				FileOutputStream fos = new FileOutputStream(this.getDataFolder().getPath() + "/config.yml");
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			} catch (MalformedURLException ex) {
				log.warning("[Cenotaph] Error accessing default config file URL: " + ex);
			} catch (FileNotFoundException ex) {
				log.warning("[Cenotaph] Error accessing default config file URL: " + ex);
			} catch (IOException ex) {
				log.warning("[Cenotaph] Error downloading default config file: " + ex);
			}
			
		}
		else if (configVer < configCurrent) {
			log.warning("[Cenotaph] Your config file is out of date! Delete your config and reload to see the new options. Proceeding using set options from config file and defaults for new options..." );
		}
		
		//Core
		logEvents = config.getBoolean("Core.logEvents", logEvents);
		cenotaphSign = config.getBoolean("Core.cenotaphSign", cenotaphSign);
		noDestroy = config.getBoolean("Core.noDestroy", noDestroy);
		pMessage = config.getBoolean("Core.playerMessage", pMessage);
		saveCenotaphList = config.getBoolean("Core.saveCenotaphList", saveCenotaphList);
		noInterfere = config.getBoolean("Core.noInterfere", noInterfere);
		versionCheck = config.getBoolean("Core.versionCheck", versionCheck);
		voidCheck = config.getBoolean("Core.voidCheck", voidCheck);
		creeperProtection = config.getBoolean("Core.creeperProtection", creeperProtection);
		signMessage = loadSign();

		//Removal
		destroyQuickLoot = config.getBoolean("Removal.destroyQuickLoot", destroyQuickLoot);
		cenotaphRemove = config.getBoolean("Removal.cenotaphRemove", cenotaphRemove);
		removeTime = config.getInt("Removal.removeTime", removeTime);

		//Security
		LocketteEnable = config.getBoolean("Security.LocketteEnable", LocketteEnable);
		lwcEnable = config.getBoolean("Security.lwcEnable", lwcEnable);
		lwcRemove = config.getBoolean("Security.lwcRemove", lwcRemove);
		lwcTime = config.getInt("Security.lwcTimeout", lwcTime);
		lwcPublic = config.getBoolean("Security.lwcPublic", lwcPublic);
	}

	public void loadTombList(String world) {
		if (!saveCenotaphList) return;
		try {
			File fh = new File(this.getDataFolder().getPath(), "tombList-" + world + ".db");
			if (!fh.exists()) return;
			Scanner scanner = new Scanner(fh);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] split = line.split(":");
				//block:lblock:sign:time:name:lwc
				Block block = readBlock(split[0]);
				Block lBlock = readBlock(split[1]);
				Block sign = readBlock(split[2]);
				String owner = split[3];
				long time = Long.valueOf(split[4]);
				boolean lwc = Boolean.valueOf(split[5]);
				if (block == null || owner == null) {
					log.info("[Cenotaph] Invalid entry in database " + fh.getName());
					continue;
				}
				TombBlock tBlock = new TombBlock(block, lBlock, sign, owner, time, lwc);
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
			Cenotaph.log.info("[Cenotaph] Error loading cenotaph list: " + e);
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
				bw.append(String.valueOf(tBlock.getTime()));
				bw.append(":");
				bw.append(String.valueOf(tBlock.getLwcEnabled()));

				bw.append(builder.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			Cenotaph.log.info("[Cenotaph] Error saving cenotaph list: " + e);
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
		log.info("[Cenotaph] msg[0] = " + msg[0]);
		log.info("[Cenotaph] msg[1] = " + msg[1]);
		log.info("[Cenotaph] msg[2] = " + msg[2]);
		log.info("[Cenotaph] msg[3] = " + msg[3]);
		return msg;
	}


	/*
	 * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
	 */
	private Plugin checkPlugin(String p) {
		Plugin plugin = pm.getPlugin(p);
		return checkPlugin(plugin);
	}

	private Plugin checkPlugin(Plugin plugin) {
		if (plugin != null && plugin.isEnabled()) {
			log.info("[Cenotaph] Using " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
			return plugin;
		}
		return null;
	}

	private Boolean activateLWC(Player player, TombBlock tBlock) {
		if (!lwcEnable) return false;
		if (lwcPlugin == null) return false;
		LWC lwc = lwcPlugin.getLWC();

		// Register the chest + sign as private
		Block block = tBlock.getBlock();
		Block sign = tBlock.getSign();
		lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), ProtectionTypes.PRIVATE, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
		if (sign != null)
			lwc.getPhysicalDatabase().registerProtection(sign.getTypeId(), ProtectionTypes.PRIVATE, block.getWorld().getName(), player.getName(), "", sign.getX(), sign.getY(), sign.getZ());

		tBlock.setLwcEnabled(true);
		return true;
	}

	private Boolean protectWithLockette(Player player, TombBlock tBlock) { //REF Lockette protection here
		if (!LocketteEnable) return false;
		if (LockettePlugin == null) return false;

		Block signBlock = null;

		signBlock = findPlace(tBlock.getBlock(),true);
		if (signBlock == null) {
			sendMessage(player, "No room for Lockette sign! Chest unsecured!");
			return false;
		}

		signBlock.setType(Material.AIR); //hack to prevent oddness with signs popping out of the ground as of Bukkit 818
		signBlock.setType(Material.WALL_SIGN);

		String facing = getDirection((getYawTo(signBlock.getLocation(),tBlock.getBlock().getLocation()) + 270) % 360);
		if (facing == "East")
			signBlock.setData((byte)0x02);
		else if (facing == "West")
			signBlock.setData((byte)0x03);
		else if (facing == "North")
			signBlock.setData((byte)0x04);
		else if (facing == "South")
			signBlock.setData((byte)0x05);
		else {
			sendMessage(player, "Error placing Lockette sign! Chest unsecured!");
			return false;
		}

		BlockState signBlockState = null;
		signBlockState = signBlock.getState();
		final Sign sign = (Sign)signBlockState;
		
		String name = player.getName();
		if (name.length() > 15) name = name.substring(0, 15);
		sign.setLine(0, "[Private]");
		sign.setLine(1, name);
		getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				sign.update();
			}
		});
		tBlock.setLocketteSign(sign);
		return true;
	}

	private void deactivateLWC(TombBlock tBlock, boolean force) {
		if (!lwcEnable) return;
		if (lwcPlugin == null) return;
		LWC lwc = lwcPlugin.getLWC();

		// Remove the protection on the chest
		Block _block = tBlock.getBlock();
		Protection protection = lwc.findProtection(_block);
		if (protection != null) {
			lwc.getPhysicalDatabase().unregisterProtection(protection.getId());
			//Set to public instead of removing completely
			if (lwcPublic && !force)
				lwc.getPhysicalDatabase().registerProtection(_block.getTypeId(), ProtectionTypes.PUBLIC, _block.getWorld().getName(), tBlock.getOwner(), "", _block.getX(), _block.getY(), _block.getZ());
		}

		// Remove the protection on the sign
		_block = tBlock.getSign();
		if (_block != null) {
			protection = lwc.findProtection(_block);
			if (protection != null) {
				lwc.getPhysicalDatabase().unregisterProtection(protection.getId());
				// Set to public instead of removing completely
				if (lwcPublic && !force)
					lwc.getPhysicalDatabase().registerProtection(_block.getTypeId(), ProtectionTypes.PUBLIC, _block.getWorld().getName(), tBlock.getOwner(), "", _block.getX(), _block.getY(), _block.getZ());
			}
		}
		tBlock.setLwcEnabled(false);
	}

	private void removeTomb(TombBlock tBlock, boolean removeList) {
		if (tBlock == null) return;

		tombBlockList.remove(tBlock.getBlock().getLocation());
		if (tBlock.getLBlock() != null) tombBlockList.remove(tBlock.getLBlock().getLocation());
		if (tBlock.getSign() != null) tombBlockList.remove(tBlock.getSign().getLocation());

		playerTombList.remove(tBlock.getOwner());

		if (removeList)
			tombList.remove(tBlock);

		if (tBlock.getBlock() != null)
			saveCenotaphList(tBlock.getBlock().getWorld().getName());
	}

	/*
	 * Check whether the player has the given permissions.
	 */
	public boolean hasPerm(Player player, String perm, boolean def) {
		if (permissions != null) {
			return permissions.getHandler().has(player, perm);
		} else {
			return def;
		}
	}

	public void sendMessage(Player p, String msg) {
		if (!pMessage) return;
		p.sendMessage("[Cenotaph] " + msg);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) return false;
		Player p = (Player)sender;
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cenotaphlist")) {
			if (!hasPerm(p, "cenotaph.cmd.cenotaphlist", p.isOp())) {
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
				sendMessage(p, "  " + i + " - World: " + tomb.getBlock().getWorld().getName() + " @(" + X + "," + Y + "," + Z + ")");
			}
			return true;
		} else if (cmd.equalsIgnoreCase("cenotaphfind")) {
			if (!hasPerm(p, "cenotaph.cmd.cenotaphfind", p.isOp())) {
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
		} else if (cmd.equalsIgnoreCase("cenotaphreset")) {
			if (!hasPerm(p, "cenotaph.cmd.cenotaphreset", p.isOp())) {
				sendMessage(p, "Permission Denied");
				return true;
			}
			p.setCompassTarget(p.getWorld().getSpawnLocation());
			return true;
		}
		return false;
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
	private static String getDirection(double rot) {
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

	/**
	 * 
	 * Print a message to terminal if logEvents is enabled
	 * @param msg
	 * @return
	 * 
	 */
	private void logEvent(String msg) {
		if (!logEvents) return;
		log.info("[Cenotaph] " + msg);
	}

	private class bListener extends BlockListener {
		@Override
		public void onBlockBreak(BlockBreakEvent event) {
			Block b = event.getBlock();
			Player p = event.getPlayer();

			if (b.getType() == Material.WALL_SIGN)
			{
				org.bukkit.material.Sign sign = (org.bukkit.material.Sign)b.getState().getData();
				TombBlock tBlock = tombBlockList.get(b.getFace(sign.getAttachedFace()).getLocation());
				if (tBlock == null) return;

				if (tBlock.getLocketteSign() != null) {
					tBlock.getLocketteSign().getBlock().setType(Material.AIR);
				}
				return;
			}

			if (b.getType() != Material.CHEST && b.getType() != Material.SIGN_POST) return;

			TombBlock tBlock = tombBlockList.get(b.getLocation());
			if (tBlock == null) return;

			if (noDestroy && !hasPerm(p, "cenotaph.admin", p.isOp())) {
				logEvent(p.getName() + " tried to destroy cenotaph at " + b.getLocation());
				sendMessage(p, "Cenotaph unable to be destroyed");
				event.setCancelled(true);
				return;
			}

			if (lwcPlugin != null && lwcEnable && tBlock.getLwcEnabled()) {
				if (tBlock.getOwner().equals(p.getName()) || hasPerm(p, "cenotaph.admin", p.isOp())) {
					deactivateLWC(tBlock, true);
				} else {
					event.setCancelled(true);
					return;
				}
			}
			logEvent(p.getName() + " destroyed cenotaph at " + b.getLocation());
			removeTomb(tBlock, true);
		}
	}

	private class pListener extends PlayerListener {
		@Override
		public void onPlayerInteract(PlayerInteractEvent event) {
			if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
			Block b = event.getClickedBlock();
			if (b.getType() != Material.SIGN_POST && b.getType() != Material.CHEST) return;
			// We'll do quickloot on rightclick of chest if we're going to destroy it anyways
			if (b.getType() == Material.CHEST && (!destroyQuickLoot || !noDestroy)) return;
			if (!hasPerm(event.getPlayer(), "cenotaph.quickloot", true)) return;

			TombBlock tBlock = tombBlockList.get(b.getLocation());
			if (tBlock == null || !(tBlock.getBlock().getState() instanceof Chest)) return;

			if (!tBlock.getOwner().equals(event.getPlayer().getName())) return;

			Chest sChest = (Chest)tBlock.getBlock().getState();
			Chest lChest = (tBlock.getLBlock() != null) ? (Chest)tBlock.getLBlock().getState() : null;

			ItemStack[] items = sChest.getInventory().getContents();
			boolean overflow = false;
			for (int cSlot = 0; cSlot < items.length; cSlot++) {
				ItemStack item = items[cSlot];
				if (item == null) continue;
				if (item.getType() == Material.AIR) continue;
				int slot = event.getPlayer().getInventory().firstEmpty();
				if (slot == -1) {
					overflow = true;
					break;
				}
				event.getPlayer().getInventory().setItem(slot, item);
				sChest.getInventory().clear(cSlot);
			}
			if (lChest != null) {
				items = lChest.getInventory().getContents();
				for (int cSlot = 0; cSlot < items.length; cSlot++) {
					ItemStack item = items[cSlot];
					if (item == null) continue;
					if (item.getType() == Material.AIR) continue;
					int slot = event.getPlayer().getInventory().firstEmpty();
					if (slot == -1) {
						overflow = true;
						break;
					}
					event.getPlayer().getInventory().setItem(slot, item);
					lChest.getInventory().clear(cSlot);
				}
			}

			if (!overflow) {
				// We're quicklooting, so no need to resume this interaction
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY); //TODO: Minor bug here - if you're holding a sign, it'll still pop up
				event.setCancelled(true);

				// Deactivate LWC
				deactivateLWC(tBlock, true);
				removeTomb(tBlock, true);

				if (destroyQuickLoot) {
					if (tBlock.getSign() != null) tBlock.getSign().setType(Material.AIR);
					if (tBlock.getLocketteSign() != null) tBlock.getLocketteSign().getBlock().setType(Material.AIR);
					tBlock.getBlock().setType(Material.AIR);
					if (tBlock.getLBlock() != null) tBlock.getLBlock().setType(Material.AIR);

				}
			}

			// Manually update inventory for the time being.
			event.getPlayer().updateInventory();
			sendMessage(event.getPlayer(), "Cenotaph quicklooted!");
			logEvent(event.getPlayer() + " quicklooted cenotaph at " + tBlock.getBlock().getLocation());
		}
	}

	public class eListener extends EntityListener
	{
		@Override
		public void onEntityDamage(EntityDamageEvent event) {
			if (event.isCancelled()) return;
			if (!(event.getEntity() instanceof Player))return;

			Player player = (Player)event.getEntity();
			// Add them to the list if they're about to die
			if (player.getHealth() - event.getDamage() <= 0) {
				deathCause.put(player.getName(), event);
			}
		}

		@Override
		public void onEntityExplode(EntityExplodeEvent event)
		{
			if (event.isCancelled()) return;
			if (!creeperProtection) return;
			for (Block block : event.blockList()) {
				TombBlock tBlock = tombBlockList.get(block.getLocation());
				if (tBlock != null) {
					event.setCancelled(true);
				}
			}
		}

		@Override
		public void onEntityDeath(EntityDeathEvent event)
		{
			if (!(event.getEntity() instanceof Player)) return;
			Player p = (Player)event.getEntity();

			if (!hasPerm(p, "cenotaph.use", true)) return;

			logEvent(p.getName() + " died.");

			if (event.getDrops().size() == 0) {
				sendMessage(p, "Inventory Empty.");
				logEvent(p.getName() + " inventory empty.");
				return;
			}

			// Get the current player location.
			Location loc = p.getLocation();
			Block block = p.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

			// If we run into something we don't want to destroy, go one up.
			if (	block.getType() == Material.STEP || 
					block.getType() == Material.TORCH ||
					block.getType() == Material.REDSTONE_WIRE || 
					block.getType() == Material.RAILS || 
					block.getType() == Material.STONE_PLATE || 
					block.getType() == Material.WOOD_PLATE ||
					block.getType() == Material.REDSTONE_TORCH_ON ||
					block.getType() == Material.REDSTONE_TORCH_OFF ||
					block.getType() == Material.CAKE_BLOCK) {
				block = p.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
			}

			//Don't create the chest if it or its sign would be in the void
			if (voidCheck && ((cenotaphSign && block.getY() > 126) || (!cenotaphSign && block.getY() > 127) || p.getLocation().getY() < 1)) {
				sendMessage(p, "Your Cenotaph would be in the Void. Inventory dropped");
				logEvent(p.getName() + " died in the Void.");
				return;
			}

			// Check if the player has a chest.
			int pChestCount = 0;
			int pSignCount = 0;
			for (ItemStack item : event.getDrops()) {
				if (item == null) continue;
				if (item.getType() == Material.CHEST) pChestCount += item.getAmount();
				if (item.getType() == Material.SIGN) pSignCount += item.getAmount();
			}

			if (pChestCount == 0 && !hasPerm(p, "cenotaph.freechest", p.isOp())) {
				sendMessage(p, "No chest found in inventory. Inventory dropped");
				logEvent(p.getName() + " No chest in inventory.");
				return;
			}

			// Check if we can replace the block.
			block = findPlace(block,false);
			if ( block == null ) {
				sendMessage(p, "Could not find room for chest. Inventory dropped");
				logEvent(p.getName() + " Could not find room for chest.");
				return;
			}

			// Check if there is a nearby chest
			if (noInterfere && checkChest(block)) {
				sendMessage(p, "There is a chest interfering with your cenotaph. Inventory dropped");
				logEvent(p.getName() + " Chest interfered with cenotaph creation.");
				return;
			}

			int removeChestCount = 1;
			int removeSign = 0;

			// Do the check for a large chest block here so we can check for interference
			Block lBlock = findLarge(block);

			// Set the current block to a chest, init some variables for later use.
			block.setType(Material.CHEST);
			// We're running into issues with 1.3 where we can't cast to a Chest :(
			BlockState state = block.getState();
			if (!(state instanceof Chest)) {
				sendMessage(p, "Could not access chest. Inventory dropped.");
				logEvent(p.getName() + " Could not access chest.");
				return;
			}
			Chest sChest = (Chest)state;
			Chest lChest = null;
			int slot = 0;
			int maxSlot = sChest.getInventory().getSize();

			// Check if they need a large chest.
			if (event.getDrops().size() > maxSlot) {
				// If they are allowed spawn a large chest to catch their entire inventory.
				if (lBlock != null && hasPerm(p, "cenotaph.large", p.isOp())) {
					removeChestCount = 2;
					// Check if the player has enough chests
					if (pChestCount >= removeChestCount || hasPerm(p, "cenotaph.freechest", p.isOp())) {
						lBlock.setType(Material.CHEST);
						lChest = (Chest)lBlock.getState();
						maxSlot = maxSlot * 2;
					} else {
						removeChestCount = 1;
					}
				}
			}

			// Don't remove any chests if they get a free one.
			if (hasPerm(p, "cenotaph.freechest", p.isOp()))
				removeChestCount = 0;

			// Check if we have signs enabled, if the player can use signs, and if the player has a sign or gets a free sign
			Block sBlock = null;
			if (cenotaphSign && hasPerm(p, "cenotaph.sign", true) && 
				(pSignCount > 0 || hasPerm(p, "cenotaph.freesign", p.isOp()))) {
				// Find a place to put the sign, then place the sign.
				sBlock = sChest.getWorld().getBlockAt(sChest.getX(), sChest.getY() + 1, sChest.getZ());
				if (canReplace(sBlock.getType())) {
					createSign(sBlock, p);
					removeSign = 1;
				} else if (lChest != null) {
					sBlock = lChest.getWorld().getBlockAt(lChest.getX(), lChest.getY() + 1, lChest.getZ());
					if (canReplace(sBlock.getType())) {
						createSign(sBlock, p);
						removeSign = 1;
					}
				}
			}
			// Don't remove a sign if they get a free one
			if (hasPerm(p, "cenotaph.freesign", p.isOp()))
				removeSign = 0;

			// Create a TombBlock for this tombstone
			TombBlock tBlock = new TombBlock(sChest.getBlock(), (lChest != null) ? lChest.getBlock() : null, sBlock, p.getName(), (System.currentTimeMillis() / 1000));

			// Protect the chest/sign if LWC is installed.
			Boolean prot = false;
			Boolean protLWC = false;
			if (hasPerm(p, "cenotaph.lwc", true))
				prot = activateLWC(p, tBlock);
			tBlock.setLwcEnabled(prot);
			if (prot) protLWC = true;

			// Protect the chest with Lockette if installed, enabled, and unprotected.
			if (hasPerm(p, "cenotaph.lockette", true))
				prot = protectWithLockette(p, tBlock);


			// Add tombstone to list
			tombList.offer(tBlock);

			// Add tombstone blocks to tombBlockList
			tombBlockList.put(tBlock.getBlock().getLocation(), tBlock);
			if (tBlock.getLBlock() != null) tombBlockList.put(tBlock.getLBlock().getLocation(), tBlock);
			if (tBlock.getSign() != null) tombBlockList.put(tBlock.getSign().getLocation(), tBlock);

			// Add tombstone to player lookup list
			ArrayList<TombBlock> pList = playerTombList.get(p.getName());
			if (pList == null) {
				pList = new ArrayList<TombBlock>();
				playerTombList.put(p.getName(), pList);
			}
			pList.add(tBlock);

			saveCenotaphList(p.getWorld().getName());

			// Next get the players inventory using the getDrops() method.
			for (Iterator<ItemStack> iter = event.getDrops().listIterator(); iter.hasNext();) {
				ItemStack item = iter.next();
				if (item == null) continue;
				// Take the chest(s)
				if (removeChestCount > 0 && item.getType() == Material.CHEST) {
					if (item.getAmount() >= removeChestCount) {
						item.setAmount(item.getAmount() - removeChestCount);
						removeChestCount = 0;
					} else {
						removeChestCount -= item.getAmount();
						item.setAmount(0);
					}
					if (item.getAmount() == 0) {
						iter.remove();
						continue;
					}
				}

				// Take a sign
				if (removeSign > 0 && item.getType() == Material.SIGN){
					item.setAmount(item.getAmount() - 1);
					removeSign = 0;
					if (item.getAmount() == 0) {
						iter.remove();
						continue;
					}
				}

				// Add items to chest if not full.
				if (slot < maxSlot) {
					if (slot >= sChest.getInventory().getSize()) {
						if (lChest == null) continue;
						lChest.getInventory().setItem(slot % sChest.getInventory().getSize(), item);
					} else {
						sChest.getInventory().setItem(slot, item);
					}
					iter.remove();
					slot++;
				} else if (removeChestCount == 0) break;
			}

			// Tell the player how many items went into chest.
			String msg = "Inventory stored in chest. ";
			if (event.getDrops().size() > 0)
				msg += event.getDrops().size() + " items wouldn't fit in chest.";
			sendMessage(p, msg);
			logEvent(p.getName() + " " + msg);
			if (prot && protLWC) {
				sendMessage(p, "Chest protected with LWC. " + lwcTime + "s before chest is unprotected.");
				logEvent(p.getName() + " Chest protected with LWC. " + lwcTime + "s before chest is unprotected.");
			}
			if (prot && !protLWC) {
				sendMessage(p, "Chest protected with Lockette.");
				logEvent(p.getName() + " Chest protected with Lockette.");
			}
			if (cenotaphRemove) {
				sendMessage(p, "Chest will be automatically removed in " + removeTime + "s");
				logEvent(p.getName() + " Chest will be automatically removed in " + removeTime + "s");
			}
		}

		private void createSign(Block signBlock, Player p) {
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String time = new SimpleDateFormat("hh:mm a").format(new Date());
			String name = p.getName();
			String reason = "Unknown";

			EntityDamageEvent dmg = deathCause.get(name);
			if (dmg != null) {
				deathCause.remove(name);
				reason = getCause(dmg);
			}

			signBlock.setType(Material.SIGN_POST);
			final Sign sign = (Sign)signBlock.getState();

			for (int x = 0; x < 4; x++) {
				String line = signMessage[x];
				line = line.replace("{name}", name);
				line = line.replace("{date}", date);
				line = line.replace("{time}", time);
				line = line.replace("{reason}", reason);
				log.info("[Cenotaph] Death sign var check: line post-replace = " + line);

				if (line.length() > 15) line = line.substring(0, 15);
				sign.setLine(x, line);
			}

			getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					sign.update();
				}
			});
		}

		private String getCause(EntityDamageEvent dmg) {
		     switch (dmg.getCause()) {
		     case ENTITY_ATTACK:
		     {
			     EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)dmg;
			     Entity e = event.getDamager();
			     if (e == null) {
			    	 return "a Dispenser";
			     } else if (e instanceof Player) {
			    	 return ((Player) e).getDisplayName();
			     } else if (e instanceof PigZombie) {
			    	 return "a Pig Zombie";
			     } else if (e instanceof Giant) {
			    	 return "a Giant";
			     } else if (e instanceof Zombie) {
			    	 return "a Zombie";
			     } else if (e instanceof Skeleton) {
			    	 return "a Skeleton";
			     } else if (e instanceof Spider) {
			    	 return "a Spider";
			     } else if (e instanceof Creeper) {
			    	 return "a Creeper";
			     } else if (e instanceof Ghast) {
			    	 return "a Ghast";
			     } else if (e instanceof Slime) {
			    	 return "a Slime";
			     } else if (e instanceof Wolf) {
			    	 return "a Wolf";
			     } else {
			    	 return "a Monster";
			     }
		     }
		     case CONTACT:
		    	 return "a Cactus";
		     case SUFFOCATION:
		    	 return "Suffocation";
		     case FALL:
		    	 return "a Fall";
		     case FIRE:
		    	 return "a Fire";
		     case FIRE_TICK:
		    	 return "Burning";
		     case LAVA:
		    	 return "Lava";
		     case DROWNING:
		    	 return "Drowning";
		     case BLOCK_EXPLOSION:
		    	 return "an Explosion";
		     case ENTITY_EXPLOSION:
		     {
			     try {
						EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)dmg;
						Entity e = event.getDamager();
						if (e instanceof TNTPrimed) return "a TNT Explosion";
						else if (e instanceof Fireball) return "a Ghast";
						else return "a Creeper";
			     } catch (Exception e) {
			    	 return "an Explosion";
			     }
		     }
		     case VOID:
		    	 return "the Void";
		     case LIGHTNING:
		    	 return "Lightning";
		     default:
		    	 return "Unknown";
		     }
		}

		Block findLarge(Block base) {
			// Check all 4 sides for air.
			Block exp;
			exp = base.getWorld().getBlockAt(base.getX() - 1, base.getY(), base.getZ());
			if (canReplace(exp.getType()) && (!noInterfere || !checkChest(exp))) return exp;
			exp = base.getWorld().getBlockAt(base.getX(), base.getY(), base.getZ() - 1);
			if (canReplace(exp.getType()) && (!noInterfere || !checkChest(exp))) return exp;
			exp = base.getWorld().getBlockAt(base.getX() + 1, base.getY(), base.getZ());
			if (canReplace(exp.getType()) && (!noInterfere || !checkChest(exp))) return exp;
			exp = base.getWorld().getBlockAt(base.getX(), base.getY(), base.getZ() + 1);
			if (canReplace(exp.getType()) && (!noInterfere || !checkChest(exp))) return exp;
			return null;
		}

		boolean checkChest(Block base) {
			// Check all 4 sides for a chest.
			Block exp;
			exp = base.getWorld().getBlockAt(base.getX() - 1, base.getY(), base.getZ());
			if (exp.getType() == Material.CHEST) return true;
			exp = base.getWorld().getBlockAt(base.getX(), base.getY(), base.getZ() - 1);
			if (exp.getType() == Material.CHEST) return true;
			exp = base.getWorld().getBlockAt(base.getX() + 1, base.getY(), base.getZ());
			if (exp.getType() == Material.CHEST) return true;
			exp = base.getWorld().getBlockAt(base.getX(), base.getY(), base.getZ() + 1);
			if (exp.getType() == Material.CHEST) return true;
			return false;
		}
	}


	/**
	 * Find a block near the base block to place the tombstone
	 * @param base
	 * @return
	 */
	Block findPlace(Block base, Boolean CardinalSearch) {
		if (canReplace(base.getType())) return base;
		int baseX = base.getX();
		int baseY = base.getY();
		int baseZ = base.getZ();
		World w = base.getWorld();

		if (CardinalSearch) {
			Block b;
			b = w.getBlockAt(baseX - 1, baseY, baseZ);
			if (canReplace(b.getType())) return b;
			b = w.getBlockAt(baseX + 1, baseY, baseZ);
			if (canReplace(b.getType())) return b;
			b = w.getBlockAt(baseX, baseY, baseZ - 1);
			if (canReplace(b.getType())) return b;
			b = w.getBlockAt(baseX, baseY, baseZ + 1);
			if (canReplace(b.getType())) return b;
			b = w.getBlockAt(baseX, baseY, baseZ);
			if (canReplace(b.getType())) return b;

			return null;
		}

		for (int x = baseX - 1; x < baseX + 1; x++) {
			for (int z = baseZ - 1; z < baseZ + 1; z++) {
				Block b = w.getBlockAt(x, baseY, z);
				if (canReplace(b.getType())) return b;
			}
		}

		return null;
	}

	Boolean canReplace(Material mat) {
		return (mat == Material.AIR || 
				mat == Material.SAPLING || 
				mat == Material.WATER || 
				mat == Material.STATIONARY_WATER || 
				mat == Material.LAVA || 
				mat == Material.STATIONARY_LAVA || 
				mat == Material.YELLOW_FLOWER || 
				mat == Material.RED_ROSE || 
				mat == Material.BROWN_MUSHROOM || 
				mat == Material.RED_MUSHROOM || 
				mat == Material.FIRE || 
				mat == Material.CROPS || 
				mat == Material.SNOW || 
				mat == Material.SUGAR_CANE ||
				mat == Material.GRAVEL ||
				mat == Material.SAND);
	}

	private class sListener extends ServerListener {
		@Override
		public void onPluginEnable(PluginEnableEvent event) {
			if (lwcPlugin == null) {
				if (event.getPlugin().getDescription().getName().equalsIgnoreCase("LWC")) {
					lwcPlugin = (LWCPlugin)checkPlugin(event.getPlugin());
				}
			}
			if (permissions == null) {
				if (event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")) {
					permissions = (Permissions)checkPlugin(event.getPlugin());
				}
			}
			if (LockettePlugin == null) {
				if (event.getPlugin().getDescription().getName().equalsIgnoreCase("Lockette")) {
					LockettePlugin = (Lockette)checkPlugin(event.getPlugin());
				}
			}
		}

		@Override
		public void onPluginDisable(PluginDisableEvent event) {
			if (event.getPlugin() == lwcPlugin) {
				log.info("[Cenotaph] LWC plugin lost.");
				lwcPlugin = null;
			}
			if (event.getPlugin() == permissions) {
				log.info("[Cenotaph] Permissions plugin lost.");
				permissions = null;
			}
			if (event.getPlugin() == LockettePlugin) {
				log.info("[Cenotaph] Lockette plugin lost.");
				permissions = null;
			}
		}
	}

	private class TombThread extends Thread {
		public void run() {
			long cTime = System.currentTimeMillis() / 1000;
			for (Iterator<TombBlock> iter = tombList.iterator(); iter.hasNext();) {
				TombBlock tBlock = iter.next();

				if (lwcRemove && tBlock.getLwcEnabled() && lwcPlugin != null) {
					if (cTime > (tBlock.getTime() + lwcTime)) {
						// Remove the protection on the block
						deactivateLWC(tBlock, false);
						tBlock.setLwcEnabled(false);
						Player p = getServer().getPlayer(tBlock.getOwner());
						if (p != null)
							sendMessage(p, "LWC Protection disabled on your cenotaph!");
					}
				}

				// Remove block, drop items on ground (One last free-for-all)
				if (cenotaphRemove && cTime > (tBlock.getTime() + removeTime)) {
					tBlock.getBlock().getWorld().loadChunk(tBlock.getBlock().getChunk());
					if (tBlock.getLwcEnabled()) {
						deactivateLWC(tBlock, true);
					}
					if (tBlock.getLocketteSign() != null)
						tBlock.getLocketteSign().getBlock().setType(Material.AIR);
					if (tBlock.getSign() != null)
						tBlock.getSign().setType(Material.AIR);
					tBlock.getBlock().setType(Material.AIR);
					if (tBlock.getLBlock() != null)
						tBlock.getLBlock().setType(Material.AIR);

					// Remove from tombList
					iter.remove();
					removeTomb(tBlock, false);

					Player p = getServer().getPlayer(tBlock.getOwner());
					if (p != null)
						sendMessage(p, "Your cenotaph has been destroyed!");
				}
			}
		}
	}

	private class TombBlock {
		private Block block;
		private Block lBlock;
		private Block sign;
		private Sign LocketteSign;
		private long time;
		private String owner;
		private boolean lwcEnabled = false;
		TombBlock(Block block, Block lBlock, Block sign, String owner, long time) {
			this.block = block;
			this.lBlock = lBlock;
			this.sign = sign;
			this.owner = owner;
			this.time = time;
		}
		TombBlock(Block block, Block lBlock, Block sign, String owner, long time, boolean lwc) {
			this.block = block;
			this.lBlock = lBlock;
			this.sign = sign;
			this.owner = owner;
			this.time = time;
			this.lwcEnabled = lwc;
		}
		long getTime() {
			return time;
		}
		Block getBlock() {
			return block;
		}
		Block getLBlock() {
			return lBlock;
		}
		Block getSign() {
			return sign;
		}
		Sign getLocketteSign() {
			return LocketteSign;
		}
		String getOwner() {
			return owner;
		}
		boolean getLwcEnabled() {
			return lwcEnabled;
		}
		void setLwcEnabled(boolean val) {
			lwcEnabled = val;
		}
		void setLocketteSign(Sign sign) {
			this.LocketteSign = sign;
		}
	}
}
