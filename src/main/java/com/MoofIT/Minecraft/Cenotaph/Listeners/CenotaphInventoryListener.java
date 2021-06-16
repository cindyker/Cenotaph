package com.MoofIT.Minecraft.Cenotaph.Listeners;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphDatabase;

public class CenotaphInventoryListener implements Listener {
	@SuppressWarnings("unused")
	private Cenotaph plugin;

	public CenotaphInventoryListener(Cenotaph instance) {
		this.plugin = instance;
	}
	
	private Predicate<Entity> isHopperMinecart() {
		return e -> e.getType().equals(EntityType.MINECART_HOPPER);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHopperMoveItem(InventoryMoveItemEvent event) {
		if (!event.getSource().getType().equals(InventoryType.CHEST))
			return;

		/*
		 * Spigot doesn't have a Composter InventoryType, so Composters being 
		 * siphoned by a hopper report a Chest source Inventory, bypassing the
		 * above if statement. If left unchecked, the loc will end up being null,
		 * throwing an NPE. The following two lines take care of this until
		 * Spigot can add a Composter InventoryType.
		 */
		Location destLoc = event.getDestination().getLocation(); 
		if (destLoc.getWorld().getBlockAt(destLoc).getRelative(0, 1, 0).getType().equals(Material.COMPOSTER))
			return;
		
		Location loc = event.getSource().getLocation();
		// Double chests return .5 locations in between the chests. Only one will ever get floored.
		loc.setX(loc.getBlockX());
		loc.setZ(loc.getBlockZ());
		
		if (CenotaphDatabase.tombBlockList.containsKey(loc)) {
			event.setCancelled(true);
			// Kill the hopper/rail.
			event.getDestination().getLocation().getBlock().breakNaturally();
			// Kill the hoppercart.
			Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 2, 1.5, 2, isHopperMinecart());
			for (Entity hoppercart : entities)
				hoppercart.remove();
		}
	}
}
