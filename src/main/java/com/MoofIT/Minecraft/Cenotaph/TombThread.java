package com.MoofIT.Minecraft.Cenotaph;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TombThread extends Thread {
	private Cenotaph plugin;

	public TombThread(Cenotaph instance) {
		this.plugin = instance;
	}

	public void run() {
		long cTime = System.currentTimeMillis() / 1000;
		for (Iterator<TombBlock> iter = Cenotaph.tombList.iterator(); iter.hasNext();) {
			TombBlock tBlock = iter.next();
			boolean bRemoved = false;

			//"empty" option checks
			if (CenotaphSettings.keepUntilEmpty() || CenotaphSettings.removeWhenEmpty()) {

				//if the Block is not there, remove it from the list.
				if(tBlock==null){
					iter.remove();
					continue;
				}
			//	if(tBlock.getBlock().getState() instanceof Chest)
				Location loc = tBlock.getBlock().getLocation();
				Block b = loc.getWorld().getBlockAt((int)loc.getX(),(int)loc.getY(),(int)loc.getZ());
				if (b.getType() == Material.CHEST) {

					//IF the Chunk isn't loaded, then no one is there, lets not do anything with it right now.
					if(!loc.getChunk().isLoaded())
						continue;

					boolean isEmpty = true;

					Chest sChest = (Chest)tBlock.getBlock().getState();
				    Chest lChest = (tBlock.getLBlock() != null) ? (Chest)tBlock.getLBlock().getState() : null;

					ItemStack[] items =sChest.getInventory().getContents();

					for (ItemStack item : items) {
						if (item != null) isEmpty = false;
						break;
					}
					if (lChest != null && !isEmpty) {
						for (ItemStack item : lChest.getInventory().getContents()) {
							if (item != null) isEmpty = false;
							break;
						}
					}
					if (CenotaphSettings.keepUntilEmpty()) {
						if (!isEmpty) continue;
					}
					if (CenotaphSettings.removeWhenEmpty()) {
						if (isEmpty) {
							plugin.destroyCenotaph(tBlock);
							bRemoved = true;
							iter.remove();
						}
					}
				}
			}

			//Security removal check
			if (CenotaphSettings.securityRemove()) {
				Player p = plugin.getServer().getPlayer(tBlock.getOwnerUUID());

				if (cTime >= (tBlock.getTime() + CenotaphSettings.securityTimeOut())) {
					if (tBlock.getLwcEnabled() && plugin.lwcPlugin != null) {
						plugin.deactivateLWC(tBlock, false);
						tBlock.setLwcEnabled(false);
						if (p != null)
							plugin.sendMessage(p, "LWC protection disabled on your cenotaph!");
					}
					if (tBlock.getLocketteSign() != null && CenotaphSettings.locketteEnable()) {
						plugin.deactivateLockette(tBlock);
						if (p != null)
							plugin.sendMessage(p, "Lockette protection disabled on your cenotaph!");
					}
				}
			}
			//Block removal check
			if (CenotaphSettings.cenotaphRemove()) {
				if (CenotaphSettings.levelBasedRemoval()) {
					if (cTime > Math.min(tBlock.getTime() + tBlock.getOwnerLevel() * CenotaphSettings.levelBasedTime(), tBlock.getTime() + CenotaphSettings.cenotaphRemoveTime())) {
						plugin.destroyCenotaph(tBlock);
						if(!bRemoved)
							iter.remove();
					}
				}
				else {
					if (cTime > (tBlock.getTime() + CenotaphSettings.cenotaphRemoveTime())) {
						plugin.destroyCenotaph(tBlock);
						if(!bRemoved)
							iter.remove();
					}
				}
			}
		}
	}
}
