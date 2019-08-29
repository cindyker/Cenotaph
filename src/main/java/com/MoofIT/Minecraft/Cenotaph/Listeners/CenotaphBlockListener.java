package com.MoofIT.Minecraft.Cenotaph.Listeners;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphMessaging;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.CenotaphUtil;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;

import java.util.Iterator;
import java.util.List;

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

		if (!CenotaphUtil.isTombBlock(b))
			return;
		TombBlock tBlock = CenotaphUtil.getTombBlock(b);
		
		if (tBlock.isSecured() && !tBlock.getOwnerUUID().equals(event.getPlayer().getUniqueId()) && !p.hasPermission("cenotaph.admin")) {
			CenotaphMessaging.sendActionBarPlayerMessage(p, "This cenotaph is secured.");
			event.setCancelled(true);
			return;
		}
				
		if (CenotaphSettings.noDestroy() && !p.hasPermission("cenotaph.admin")) {
			CenotaphMessaging.sendActionBarPlayerMessage(p, "You cannot break this cenotaph.");
			event.setCancelled(true);
			return;
		}

		Player owner = null;
		if (tBlock.getOwnerUUID() != null)
			owner = plugin.getServer().getPlayer(tBlock.getOwnerUUID());
		plugin.removeTomb(tBlock, true);
		if (owner != null) CenotaphMessaging.sendPrefixedPlayerMessage(owner, "Your cenotaph has been destroyed by " + p.getName() + "!");
	}

	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event) {
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			else if (CenotaphUtil.getTombBlock(block).isSecured())
				event.setCancelled(true);
			else
				// Someone's destroying/moving part of a cenotaph so we'll just take it out of the database.
				plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
		}
	}
	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			else if (CenotaphUtil.getTombBlock(block).isSecured())
				event.setCancelled(true);
			else
				// Someone's destroying/moving part of a cenotaph so we'll just take it out of the database.
				plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		Block block = event.getBlock();
		if (!CenotaphUtil.isTombBlock(block))
			return;
		else if (CenotaphUtil.getTombBlock(block).isSecured())
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();		
		if (!CenotaphUtil.isTombBlock(block))
			return;
		else if (CenotaphUtil.getTombBlock(block).isSecured())
			event.setCancelled(true);
		else
			plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
	}
	


	// TODO: decide if this is doing anything.
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
			if( CenotaphSettings.explosionProtection()) {
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
