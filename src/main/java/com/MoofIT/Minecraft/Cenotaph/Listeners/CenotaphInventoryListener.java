package com.MoofIT.Minecraft.Cenotaph.Listeners;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.Location;
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

		Location loc = event.getSource().getLocation();
		if (loc == null) {
		    event.setCancelled(true);
		    return;
		}
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
