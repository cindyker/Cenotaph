package com.MoofIT.Minecraft.Cenotaph.PluginHandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphDatabase;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.CenotaphUtil;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class HolographicDisplays {
	
	public static ConcurrentLinkedQueue<Hologram> holograms = new ConcurrentLinkedQueue<Hologram>();
	
	public static void createHolo(Block block, Player p) {
		String date = new SimpleDateFormat(CenotaphSettings.dateFormat()).format(new Date());
		String time = new SimpleDateFormat(CenotaphSettings.timeFormat()).format(new Date());
		String name = p.getName();
		String reason = "Unknown";

		EntityDamageEvent dmg = CenotaphDatabase.deathCause.get(name);
		if (dmg != null) {
			CenotaphDatabase.deathCause.remove(name);
			reason = CenotaphUtil.getCause(dmg);
		}

		Hologram holo = HologramsAPI.createHologram(Cenotaph.plugin, block.getRelative(BlockFace.UP,2).getLocation());
		for (int x = 0; x < 4; x++) {
			String line = CenotaphUtil.signMessage[x];
			line = line.replace("{name}", name);
			line = line.replace("{date}", date);
			line = line.replace("{time}", time);
			line = line.replace("{reason}", reason);
			holo.appendTextLine(line);					
		}				 
		HolographicDisplays.holograms.add(holo);
		
	}
	
	public static void saveHolograms() {
		try {
			File fh = new File(Cenotaph.plugin.getDataFolder().getPath(), "holograms.db");
			BufferedWriter bw = new BufferedWriter(new FileWriter(fh));
			for (Iterator<Hologram> iter = holograms.iterator(); iter.hasNext();) {
				Hologram holo = iter.next();
				StringBuilder builder = new StringBuilder();
				bw.append(holo.getWorld().getName()+","+holo.getX()+","+holo.getY()+","+holo.getZ());
				bw.append("|");
				bw.append(holo.getLine(0).toString());
				bw.append("|");
				bw.append(holo.getLine(1).toString());
				bw.append("|");
				bw.append(holo.getLine(2).toString());
				bw.append("|");
				bw.append(holo.getLine(3).toString());
				bw.append(builder.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			Cenotaph.log.info("[Cenotaph] Error saving hologram list: " + e);
		}
	}
	
	public static void loadHolograms() { 
		try {
			File fh = new File(Cenotaph.plugin.getDataFolder().getPath(), "holograms.db");
			if (!fh.exists()) return;
			Scanner scanner = new Scanner(fh);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] split = line.split("\\|");
				String[] loc = split[0].split(",");
				World world = Bukkit.getWorld(loc[0]);
				Double x = Double.valueOf(loc[1]);
				Double y = Double.valueOf(loc[2]);
				Double z = Double.valueOf(loc[3]);
				Location location = new Location(world, x, y, z);
				Hologram holo = HologramsAPI.createHologram(Cenotaph.plugin, location);
				holo.appendTextLine(holoLinetoString(split[1]));
				holo.appendTextLine(holoLinetoString(split[2]));
				holo.appendTextLine(holoLinetoString(split[3]));
				holo.appendTextLine(holoLinetoString(split[4]));
				holograms.offer(holo);
			}
			scanner.close();
		} catch (IOException e) {
			Cenotaph.log.info("[Cenotaph] Error loading hologram list: " + e);
		}
	}
	
	private static String holoLinetoString (String string) {
		// Cuts off 'CraftTextLine [text=' from beginning and ']' on the end.
		return string.substring(20, string.length()-1);
	}

}
