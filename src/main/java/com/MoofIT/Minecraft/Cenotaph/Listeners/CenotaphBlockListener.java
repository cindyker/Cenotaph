package com.MoofIT.Minecraft.Cenotaph.Listeners;

import org.bukkit.Material;
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
import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphMessaging;
import com.MoofIT.Minecraft.Cenotaph.CenotaphUtil;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;
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

		if (b.getType() != Material.CHEST && b.getType() != Material.SIGN)
			return;

		if (!CenotaphUtil.isTombBlock(b))
			return;
		TombBlock tBlock = CenotaphUtil.getTombBlock(b);
		
		if (tBlock.isSecured() && !tBlock.getOwnerUUID().equals(event.getPlayer().getUniqueId()) && !p.hasPermission("cenotaph.admin")) {
			CenotaphMessaging.sendActionBarPlayerMessage(p, "This cenotaph is secured.");
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
		if (event.isCancelled()) 
			return;
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			else if (CenotaphUtil.getTombBlock(block).isSecured())
				event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPistonRetractMonitor(BlockPistonRetractEvent event) {
		if (event.isCancelled()) 
			return;
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
		}
	}

	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled()) 
			return;
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			else if (CenotaphUtil.getTombBlock(block).isSecured())
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPistonExtendMonitor(BlockPistonExtendEvent event) {
		if (event.isCancelled()) 
			return;
		List<Block> blockList = event.getBlocks();
		for (Block block : blockList) {
			if (!CenotaphUtil.isTombBlock(block))
				return;
			plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) 
			return;
		Block block = event.getBlock();
		if (!CenotaphUtil.isTombBlock(block))
			return;
		else if (CenotaphUtil.getTombBlock(block).isSecured())
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockIgniteMonitor(BlockIgniteEvent event) {
		if (event.isCancelled()) 
			return;
		Block block = event.getBlock();
		if (!CenotaphUtil.isTombBlock(block))
			return;
		plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) 
			return;
		Block block = event.getBlock();		
		if (!CenotaphUtil.isTombBlock(block))
			return;
		else if (CenotaphUtil.getTombBlock(block).isSecured())
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBurnMonitor(BlockBurnEvent event) {
		if (event.isCancelled()) 
			return;
		Block block = event.getBlock();		
		if (!CenotaphUtil.isTombBlock(block))
			return;
		plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
	}
}
