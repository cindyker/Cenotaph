package com.MoofIT.Minecraft.Cenotaph.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;

public class CenotaphPlayerListener implements Listener {
	private Cenotaph plugin;

	public CenotaphPlayerListener(Cenotaph instance) {
		this.plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Block b = event.getClickedBlock();
		if (!b.getType().equals(Material.CHEST))
			return;
		TombBlock tBlock = Cenotaph.tombBlockList.get(b.getLocation());
		if (tBlock == null)
			return;
		if (tBlock.isSecured() && !tBlock.getOwnerUUID().equals(event.getPlayer().getUniqueId())) {
			plugin.sendMessage(event.getPlayer(), "This cenotaph is secured."); //TODO: add a nicer message for denial of access.
			event.setCancelled(true);
			return;
		}		
		// We'll do quickloot on rightclick of chest if we're going to destroy it anyways
		if (!event.getPlayer().hasPermission("cenotaph.quickloot"))
			return;		
		if (!tBlock.getOwnerUUID().equals(event.getPlayer().getUniqueId()))
			return;
		Chest sChest = (Chest)tBlock.getBlock().getState();
		Chest lChest = (tBlock.getLBlock() != null) ? (Chest)tBlock.getLBlock().getState() : null;

		ItemStack[] items = sChest.getInventory().getContents();
		boolean overflow = false;
		for (int cSlot = 0; cSlot < items.length; cSlot++) {
			ItemStack item = items[cSlot];
			if (item == null) continue;
			if (item.getType() == Material.AIR) continue;
			int slot = event.getPlayer().getInventory().firstEmpty();
			if (slot == -1) {
				overflow = true;
				break;
			}
			event.getPlayer().getInventory().setItem(slot, item);
			sChest.getInventory().clear(cSlot);
		}
		if (lChest != null) {
			items = lChest.getInventory().getContents();
			for (int cSlot = 0; cSlot < items.length; cSlot++) {
				ItemStack item = items[cSlot];
				if (item == null) continue;
				if (item.getType() == Material.AIR) continue;
				int slot = event.getPlayer().getInventory().firstEmpty();
				if (slot == -1) {
					overflow = true;
					break;
				}
				event.getPlayer().getInventory().setItem(slot, item);
				lChest.getInventory().clear(cSlot);
			}
		}

		if (!overflow) {
			// We're quicklooting, so no need to resume this interaction
			event.setUseInteractedBlock(Result.DENY);
			event.setUseItemInHand(Result.DENY); //TODO: Minor bug here - if you're holding a sign, it'll still pop up
			event.setCancelled(true);

			if (CenotaphSettings.destroyQuickloot()) {
				plugin.destroyCenotaph(tBlock);
			}
		}

		// Manually update inventory for the time being.
		event.getPlayer().updateInventory();
	}
}
