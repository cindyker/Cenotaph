package com.MoofIT.Minecraft.Cenotaph;

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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.MoofIT.Minecraft.Cenotaph.Config.Lang;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.HolographicDisplays;
import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class CenotaphDatabase {
	public static ConcurrentLinkedQueue<TombBlock> tombList = new ConcurrentLinkedQueue<TombBlock>();
	public static HashMap<Location, TombBlock> tombBlockList = new HashMap<Location, TombBlock>();
	public static HashMap<String, ArrayList<TombBlock>> playerTombList = new HashMap<String, ArrayList<TombBlock>>();
	public static HashMap<String, EntityDamageEvent> deathCause = new HashMap<String, EntityDamageEvent>();
	
	public static void loadTombList(String world) {
		if (!CenotaphSettings.saveCenotaphList()) return;
		try {
			File fh = new File(Cenotaph.plugin.getDataFolder().getPath(), "tombList-" + world + ".db");
			if (!fh.exists()) return;
			Scanner scanner = new Scanner(fh);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] split = line.split(":");
				Block block = null;
				Block lBlock = null;
				Block sign = null;
				long time = 0;
				int level = 0;
				UUID ownerUUID = null;
				boolean secured = false;
				
				// Try and load older Cenotaph database Cenotaph 5.3-5.7.
				if (split.length == 6){
					//block:lblock:sign:time:ownerlevel:ownerUUID
					block = readBlock(split[0]);
					lBlock = readBlock(split[1]);
					sign = readBlock(split[2]);
					time = Long.valueOf(split[3]);
					level = Integer.valueOf(split[4]);
					ownerUUID = UUID.fromString(split[5]);
					secured = CenotaphSettings.securityEnable();
					
				// Must be Cenotaph 5.8+
				} else if (split.length == 7) {
					//block:lblock:sign:time:ownerlevel:ownerUUID:secured
					block = readBlock(split[0]);
					lBlock = readBlock(split[1]);
					sign = readBlock(split[2]);
					time = Long.valueOf(split[3]);
					level = Integer.valueOf(split[4]);
					ownerUUID = UUID.fromString(split[5]);
					secured = Boolean.valueOf(split[6]);
				}
				
				if (block == null ) {
					Cenotaph.log.info("[Cenotaph] Invalid entry in database " + fh.getName());
					continue;
				}
				
				TombBlock tBlock = new TombBlock(block, lBlock, sign, time, level, ownerUUID, secured);
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
			Cenotaph.log.info("[Cenotaph] Error loading cenotaph list: " + e);
		}
	}

	public static void saveCenotaphList(String world) {
		if (!CenotaphSettings.saveCenotaphList()) return;
		try {
			File fh = new File(Cenotaph.plugin.getDataFolder().getPath(), "tombList-" + world + ".db");
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
				bw.append(":");
				bw.append(String.valueOf(tBlock.isSecured()));
				bw.append(builder.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			Cenotaph.log.info("[Cenotaph] Error saving cenotaph list: " + e);
		}
	}

	private static String printBlock(Block b) {
		if (b == null) return null;
		return b.getWorld().getName() + "," + b.getX() + "," + b.getY() + "," + b.getZ();
	}

	private static Block readBlock(String b) {
		if (b.length() == 0) return null;
		String[] split = b.split(",");
		//world,x,y,z
		World world = Cenotaph.plugin.getServer().getWorld(split[0]);
		if (world == null) return null;
		return world.getBlockAt(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
	}

	public static void removeTomb(TombBlock tBlock, boolean removeList) {
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

	public void destroyCenotaph(Location loc) {
		destroyCenotaph(tombBlockList.get(loc));
	}
	public static void destroyCenotaph(TombBlock tBlock) {
		if (tBlock.getBlock().getChunk().load() == false) {
			Cenotaph.log.severe("[Cenotaph] Error loading world chunk trying to remove cenotaph at " + tBlock.getBlock().getX() + "," + tBlock.getBlock().getY() + "," + tBlock.getBlock().getZ() + " owned by " + tBlock.getOwner() + ".");
			return;
		}

		tBlock.getBlock().setType(Material.AIR);
		if (tBlock.getLBlock() != null) tBlock.getLBlock().setType(Material.AIR);
		if (tBlock.getSign() != null) tBlock.getSign().setType(Material.AIR);
		if (Cenotaph.hologramsEnabled) {
			for (Hologram holo : HolographicDisplays.holograms) {
				if (holo.getLocation().equals(tBlock.getBlock().getLocation().add(0.5, 2.0, 0.5))) {
					holo.delete();
					HolographicDisplays.holograms.remove(holo);
				}
			}
				
		}

		removeTomb(tBlock, true);

		Player p = null;
		if (tBlock.getOwnerUUID() != null)
			p = Cenotaph.plugin.getServer().getPlayer(tBlock.getOwnerUUID());
		if (p != null) CenotaphMessaging.sendPrefixedPlayerMessage(p, Lang.string("your_cenotaph_has_broken"));
	}

	public static HashMap<String, ArrayList<TombBlock>> getCenotaphList() {
		return playerTombList;
	}

}
