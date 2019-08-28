package com.MoofIT.Minecraft.Cenotaph.Listeners;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;

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

		if (b.getType() != Material.CHEST && !Tag.SIGNS.isTagged( b.getType()))
			return;

		TombBlock tBlock = Cenotaph.tombBlockList.get(b.getLocation());
		if (tBlock == null)
			return;

		if (tBlock.isSecured() && !tBlock.getOwnerUUID().equals(event.getPlayer().getUniqueId()) && !p.hasPermission("cenotaph.admin")) {
			plugin.sendMessage(p, "This cenotaph is secured."); //TODO: add a nicer message for denial of access.
			event.setCancelled(true);
			return;
		}
				
		if (CenotaphSettings.noDestroy() && !p.hasPermission("cenotaph.admin")) {
			plugin.sendMessage(p, "You cannot break this cenotaph.");
			event.setCancelled(true);
			return;
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
