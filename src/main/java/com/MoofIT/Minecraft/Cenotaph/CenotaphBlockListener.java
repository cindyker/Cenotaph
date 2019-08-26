package com.MoofIT.Minecraft.Cenotaph;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;

public class CenotaphBlockListener implements Listener {
	private Cenotaph plugin;

	public CenotaphBlockListener(Cenotaph instance) {
		this.plugin = instance;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		Player p = event.getPlayer();
		

		
		// Handles wall_signs which are used for lockette/deadbolt locks on Tombstones.
		if ( Tag.WALL_SIGNS.isTagged( b.getType() ) )
		{
			TombBlock tBlock = null;
			BlockData blockData = b.getBlockData();
			Directional directional = (Directional) blockData;
			Block attachedBlock = b.getRelative(directional.getFacing().getOppositeFace());

			if (attachedBlock != null)
				tBlock = Cenotaph.tombBlockList.get(attachedBlock.getLocation());
			
			if (tBlock == null) return;

			if (tBlock.getLocketteSign() != null) {
				Sign sign = (Sign)b.getState();
				event.setCancelled(true);
				sign.update();
				return;
			}
		}

		if (b.getType() != Material.CHEST && Tag.SIGNS.isTagged( b.getType())) return;

		TombBlock tBlock = Cenotaph.tombBlockList.get(b.getLocation());
		if (tBlock == null) return;

		if (CenotaphSettings.noDestroy() && !p.hasPermission("cenotaph.admin")) {
			plugin.sendMessage(p, "You cannot break this cenotaph..");
			event.setCancelled(true);
			return;
		}

		if (plugin.lwcPlugin != null && CenotaphSettings.lwcEnable() && tBlock.getLwcEnabled()) {
			if (tBlock.getOwnerUUID().equals(p.getUniqueId()) || p.hasPermission("cenotaph.admin")) {
				plugin.deactivateLWC(tBlock, true);
			} else {
				event.setCancelled(true);
				return;
			}
		}
		
		if (CenotaphSettings.locketteEnable() && tBlock.getLocketteSign() != null) {
			if (!tBlock.getOwnerUUID().equals(p.getUniqueId()) && !p.hasPermission("cenotaph.admin")) {
				event.setCancelled(true);
				plugin.sendMessage(p, "Cannot interfere with a locked Cenotaph.");
				return;
			}
		}		
		Player owner = null;
		if (tBlock.getOwnerUUID() != null)
			owner = plugin.getServer().getPlayer(tBlock.getOwnerUUID());
		plugin.removeTomb(tBlock, true);
		if (owner != null) plugin.sendMessage(owner, "Your cenotaph has been destroyed by " + p.getName() + "!");
	}


	//Handle Explosions...
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {
		Iterator<Block> iter = event.blockList().iterator();
		while (iter.hasNext()) {
			Block block = iter.next();

			if (Tag.WALL_SIGNS.isTagged( block.getType())) {
				TombBlock tBlock = null;
				BlockData blockData = block.getBlockData();				
				Directional directional = (Directional) blockData;
				Block attachedBlock = block.getRelative(directional.getFacing().getOppositeFace());

				if (attachedBlock != null)
					tBlock = Cenotaph.tombBlockList.get(attachedBlock.getLocation());
				if (tBlock == null) {
					continue;
				}
				iter.remove();
			}

			if (block.getType() != Material.CHEST && Tag.SIGNS.isTagged( block.getType()) ) continue;

			TombBlock tBlock = Cenotaph.tombBlockList.get(block.getLocation());

			if (tBlock == null) continue;
			//plugin.getLogger().info("Found Cenotaph in an explosion!");
			//its an cenotaph block.. prevent TNT.
			if( CenotaphSettings.tntProtection()) {
			//	plugin.getLogger().info("Protecting Cenotaph from the explosion!");
				iter.remove();
			}
			else if (event.isCancelled()) {
			//	plugin.getLogger().info("Removing Cenotaph from the list");
				plugin.removeTomb(tBlock, true);
			} else return;
		}
	}
}
