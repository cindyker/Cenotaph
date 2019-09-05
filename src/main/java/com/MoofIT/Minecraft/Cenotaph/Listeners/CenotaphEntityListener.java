package com.MoofIT.Minecraft.Cenotaph.Listeners;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphDatabase;
import com.MoofIT.Minecraft.Cenotaph.CenotaphMessaging;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.CenotaphUtil;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.HolographicDisplays;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.WorldGuardWrapper;
import net.milkbowl.vault.economy.EconomyResponse;

public class CenotaphEntityListener implements Listener {
	@SuppressWarnings("unused")
	private Cenotaph plugin;

	public CenotaphEntityListener(Cenotaph instance) {
		this.plugin = instance;		
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getEntity() instanceof Player))return;

		Player player = (Player)event.getEntity();
		// Add them to the list if they're about to die
		if (player.getHealth() - event.getDamage() <= 0) {
			CenotaphDatabase.deathCause.put(player.getName(), event);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) 
			return;
		for (Block block : event.blockList()) {
			if (CenotaphUtil.isTombBlock(block)) {			
				if (CenotaphUtil.getTombBlock(block).isSecured())
					event.setCancelled(true);
				else if (CenotaphSettings.explosionProtection())
					event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplodeMonitor(EntityExplodeEvent event) {
		if (event.isCancelled()) 
			return;
		for (Block block : event.blockList()) {
			if (CenotaphUtil.isTombBlock(block))
				if (!event.isCancelled())
					CenotaphDatabase.removeTomb(CenotaphUtil.getTombBlock(block), true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player p = (Player)event.getEntity();
		World world = p.getWorld();

		if (!p.hasPermission("cenotaph.use")) return;

		if (event.getDrops().size() == 0) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Inv empty.");
			return;
		}

		if (CenotaphSettings.disabledWorlds().contains(world.getName())) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p,"Cenotaph disabled in " + world.getName() + ". Inv dropped.");
			return;
		}

		// Get the current player location.
		Location loc = p.getLocation();
		Block block = p.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		//Don't create the chest if it or its sign would be in the void
		if (CenotaphSettings.voidCheck() && ((CenotaphSettings.cenotaphSign() && block.getY() > p.getWorld().getMaxHeight() - 1) || (!CenotaphSettings.cenotaphSign() && block.getY() > p.getWorld().getMaxHeight()) || p.getLocation().getY() < 1)) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Chest would be in the Void. Inv dropped.");
			return;
		}

		//WorldGuard support, see if the player could build where they've died. Disallow a cenotaph if they cannot build.
		if (Cenotaph.worldguardEnabled){
			if (!WorldGuardWrapper.canBuild(p)){
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "In a WorldGuard protected area. Inv dropped.");
				return;
			}
		}
		
		if (Cenotaph.economyEnabled) {
			//Check balance to see if they can pay for their cenotaph.
			if (!p.hasPermission("cenotaph.nocost") && CenotaphSettings.cenotaphCost() > 0){
				if (Cenotaph.econ.getBalance(p) < CenotaphSettings.cenotaphCost()){
					CenotaphMessaging.sendPrefixedPlayerMessage(p, "Not enough money! Inv dropped.");
					return;
				}
			}
		}

		// Check if the player has a chest.
		int pChestCount = 0;
		int pSignCount = 0;
		for (ItemStack item : event.getDrops()) {
			if (item == null) continue;
			if (item.getType() == Material.CHEST) pChestCount += item.getAmount();
			for(Material mat: Tag.SIGNS.getValues()) {
				if(item.getType() == mat)	
					pSignCount += item.getAmount();	
			}
		}

		if (pChestCount == 0 && !p.hasPermission("cenotaph.freechest")) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "No chest! Inv dropped.");
			return;
		}

		boolean smallChest = event.getDrops().size() < 28;
		// Check if we can replace the block.
		block = CenotaphUtil.findPlace(block, smallChest);
		if ( block == null ) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "No room to place chest. Inv dropped.");
			return;
		}

		int removeChestCount = 1;
		int removeSignCount = 0;

		// Do the check for a large chest block here so we can check for interference
		Block lBlock = CenotaphUtil.findLarge(block);

		// Set the current block to a chest, init some variables for later use.
		block.setType(Material.CHEST);
		// We're running into issues with 1.3 where we can't cast to a Chest :(
		BlockState state = block.getState();
		if (!(state instanceof Chest)) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "Could not access chest. Inv dropped.");
			return;
		}
		Chest sChest = (Chest)state;
		Chest lChest = null;
		int slot = 0;
		int maxSlot = sChest.getInventory().getSize();
		BlockFace relativeFace = BlockFace.NORTH;
		// Check if they need a large chest.
		if (event.getDrops().size() > maxSlot) {
			// If they are allowed spawn a large chest to catch their entire inventory.
			if (lBlock != null && p.hasPermission("cenotaph.large")) {
				removeChestCount = 2;
				// Check if the player has enough chests
				if (pChestCount >= removeChestCount || p.hasPermission("cenotaph.freechest")) {
					CenotaphUtil.createLargeChest(block, lBlock, relativeFace);
					lChest = (Chest)lBlock.getState();
					maxSlot = maxSlot * 2;
				} else {
					removeChestCount = 1;
				}
			}
		}

		// Don't remove any chests if they get a free one.
		if (p.hasPermission("cenotaph.freechest"))
			removeChestCount = 0;

		// We are either going to make a hologram with the information on it, or we're going to make a sign (if a sign can be placed.)
		Block sBlock = null;
		if (Cenotaph.hologramsEnabled)
			HolographicDisplays.createHolo(block, p);
		// Check if we have signs enabled, if the player can use signs, and if the player has a sign or gets a free sign
		else if (CenotaphSettings.cenotaphSign() && p.hasPermission("cenotaph.sign") &&
			(pSignCount > 0 || p.hasPermission("cenotaph.freesign"))) {
			// Find a place to put the sign, then place the sign.
			sBlock = sChest.getWorld().getBlockAt(sChest.getX(), sChest.getY() + 1, sChest.getZ());
			if (CenotaphUtil.canReplace(sBlock.getType())) {
				CenotaphUtil.createSign(sBlock, p, relativeFace);
				removeSignCount += 1;
			} else if (lChest != null) {
				sBlock = lChest.getWorld().getBlockAt(lChest.getX(), lChest.getY() + 1, lChest.getZ());
				if (CenotaphUtil.canReplace(sBlock.getType())) {
					CenotaphUtil.createSign(sBlock, p, relativeFace.getOppositeFace());
					removeSignCount += 1;
				}
			}
		}
		// Don't remove a sign if they get a free one
		if (p.hasPermission("cenotaph.freesign"))
			removeSignCount -= 1;

		// Create a TombBlock for this tombstone
		TombBlock tBlock = new TombBlock(sChest.getBlock(), (lChest != null) ? lChest.getBlock() : null, sBlock, (System.currentTimeMillis() / 1000), p.getLevel() + 1, p.getUniqueId());

		// Add tombstone to list
		CenotaphDatabase.tombList.offer(tBlock);

		// Add tombstone blocks to tombBlockList
		CenotaphDatabase.tombBlockList.put(tBlock.getBlock().getLocation(), tBlock);
		if (tBlock.getLBlock() != null) CenotaphDatabase.tombBlockList.put(tBlock.getLBlock().getLocation(), tBlock);
		if (tBlock.getSign() != null) CenotaphDatabase.tombBlockList.put(tBlock.getSign().getLocation(), tBlock);

		// Add tombstone to player lookup list
		ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(p.getName());
		if (pList == null) {
			pList = new ArrayList<TombBlock>();
			CenotaphDatabase.playerTombList.put(p.getName(), pList);
		}
		pList.add(tBlock);

		CenotaphDatabase.saveCenotaphList(p.getWorld().getName());
		if (Cenotaph.hologramsEnabled)
			HolographicDisplays.saveHolograms();

		// Next get the players inventory using the getDrops() method.
		for (Iterator<ItemStack> iter = event.getDrops().listIterator(); iter.hasNext();) {
			ItemStack item = iter.next();
			if (item == null) continue;
			// Take the chest(s)
			if (removeChestCount > 0 && item.getType() == Material.CHEST) {
				if (item.getAmount() >= removeChestCount) {
					item.setAmount(item.getAmount() - removeChestCount);
					removeChestCount = 0;
				} else {
					removeChestCount -= item.getAmount();
					item.setAmount(0);
				}
				if (item.getAmount() == 0) {
					iter.remove();
					continue;
				}
			}
		
			for(Material mat: Tag.SIGNS.getValues()) {
				if(item.getType() == mat)
					pSignCount += item.getAmount();	
			}

			// Take a sign
			if (removeSignCount > 0 && Tag.SIGNS.isTagged( item.getType() )) {
				item.setAmount(item.getAmount() - 1);
				removeSignCount -= 1;
				if (item.getAmount() == 0) {
					iter.remove();
					continue;
				}
			}

			// Add items to chest if not full.
			if (slot < maxSlot) {
				if (slot >= sChest.getInventory().getSize()) {
					if (lChest == null) continue;
					lChest.getInventory().setItem(slot % sChest.getInventory().getSize(), item);
				} else {
					sChest.getInventory().setItem(slot, item);
				}
				iter.remove();
				slot++;
			} else if (removeChestCount == 0) break;
		}

		String msg = "Inv stored. ";
		if (event.getDrops().size() > 0) msg += ChatColor.YELLOW + "Overflow: " + ChatColor.WHITE + event.getDrops().size() + " ";
		msg += CenotaphMessaging.centimeMsg(tBlock);
		CenotaphMessaging.sendPrefixedPlayerMessage(p, msg);
		
		//Subtract money
		if (!p.hasPermission("cenotaph.nocost") && CenotaphSettings.cenotaphCost() > 0){
			EconomyResponse r = Cenotaph.econ.withdrawPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), CenotaphSettings.cenotaphCost());
			if (r.transactionSuccess()){
				CenotaphMessaging.sendPrefixedPlayerMessage(p, CenotaphSettings.cenotaphCost() + " " + Cenotaph.econ.currencyNamePlural() + " has been taken from your account.");
			}
		}
	}
}
