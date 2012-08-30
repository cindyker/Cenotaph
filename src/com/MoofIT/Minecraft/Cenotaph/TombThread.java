package com.MoofIT.Minecraft.Cenotaph;

import java.util.Iterator;

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

			//"empty" option checks
			if (plugin.keepUntilEmpty || plugin.removeWhenEmpty) {
				if (tBlock.getBlock().getState() instanceof Chest) {
					boolean isEmpty = true;

					Chest sChest = (Chest)tBlock.getBlock().getState();
					Chest lChest = (tBlock.getLBlock() != null) ? (Chest)tBlock.getLBlock().getState() : null;

					for (ItemStack item : sChest.getInventory().getContents()) {
						if (item != null) isEmpty = false;
						break;
					}
					if (lChest != null && !isEmpty) {
						for (ItemStack item : lChest.getInventory().getContents()) {
							if (item != null) isEmpty = false;
							break;
						}
					}
					if (plugin.keepUntilEmpty) {
						if (!isEmpty) continue;
					}
					if (plugin.removeWhenEmpty) {
						if (isEmpty) {
							plugin.destroyCenotaph(tBlock);
							iter.remove();
						}
					}
				}
			}

			//Security removal check
			if (plugin.securityRemove) {
				Player p = plugin.getServer().getPlayer(tBlock.getOwner());

				if (cTime >= (tBlock.getTime() + plugin.securityTimeout)) {
					if (tBlock.getLwcEnabled() && plugin.lwcPlugin != null) {
						plugin.deactivateLWC(tBlock, false);
						tBlock.setLwcEnabled(false);
						if (p != null)
							plugin.sendMessage(p, "LWC protection disabled on your cenotaph!");
					}
					if (tBlock.getLocketteSign() != null && plugin.LockettePlugin != null) {
						plugin.deactivateLockette(tBlock);
						if (p != null)
							plugin.sendMessage(p, "Lockette protection disabled on your cenotaph!");
					}
				}
			}
			//Block removal check
			if (plugin.cenotaphRemove) {
				if (plugin.levelBasedRemoval) {
					if (cTime > Math.min(tBlock.getTime() + tBlock.getOwnerLevel() * plugin.levelBasedTime, tBlock.getTime() + plugin.removeTime)) {
						plugin.destroyCenotaph(tBlock);
						iter.remove();
					}
				}
				else {
					if (cTime > (tBlock.getTime() + plugin.removeTime)) {
						plugin.destroyCenotaph(tBlock);
						iter.remove();
					}
				}
			}
		}
	}
}
