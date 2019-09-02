package com.MoofIT.Minecraft.Cenotaph.Listeners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.MoofIT.Minecraft.Cenotaph.CenotaphMessaging;
import com.MoofIT.Minecraft.Cenotaph.CenotaphSettings;
import com.MoofIT.Minecraft.Cenotaph.CenotaphUtil;
import com.MoofIT.Minecraft.Cenotaph.TombBlock;
import com.MoofIT.Minecraft.Cenotaph.WorldGuardWrapper;
import net.milkbowl.vault.economy.EconomyResponse;

public class CenotaphEntityListener implements Listener {
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
			Cenotaph.deathCause.put(player.getName(), event);
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
					plugin.removeTomb(CenotaphUtil.getTombBlock(block), true);
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

		//WorldGuard support
		if (Cenotaph.worldguardEnabled){
			if (!WorldGuardWrapper.canBuild(p)){
				CenotaphMessaging.sendPrefixedPlayerMessage(p, "In a WorldGuard protected area. Inv dropped.");
				return;
			}
		}
		
		if (Cenotaph.economyEnabled) {
			//Check balance
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
			if (item.getType() == Material.SIGN)
				pSignCount += item.getAmount();
		}

		if (pChestCount == 0 && !p.hasPermission("cenotaph.freechest")) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "No chest! Inv dropped.");
			return;
		}

		boolean smallChest = event.getDrops().size() < 28;
		// Check if we can replace the block.
		block = findPlace(block, smallChest);
		if ( block == null ) {
			CenotaphMessaging.sendPrefixedPlayerMessage(p, "No room to place chest. Inv dropped.");
			return;
		}

		int removeChestCount = 1;
		int removeSignCount = 0;

		// Do the check for a large chest block here so we can check for interference
		Block lBlock = findLarge(block);

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
					lBlock.setType(Material.CHEST);
					// This fun stuff is required post-1.13 when they made chests not snap together. 
					org.bukkit.block.data.type.Chest blockChestData = (org.bukkit.block.data.type.Chest) block.getBlockData();
					org.bukkit.block.data.type.Chest lBlockChestData = (org.bukkit.block.data.type.Chest) lBlock.getBlockData();
					relativeFace = block.getFace(lBlock);
					if (relativeFace.equals(BlockFace.WEST)) {
						blockChestData.setFacing(BlockFace.SOUTH);
						lBlockChestData.setFacing(BlockFace.SOUTH);
						blockChestData.setType(Type.LEFT);
						lBlockChestData.setType(Type.RIGHT);
					} else if (relativeFace.equals(BlockFace.EAST)) {
						//Chests face North by default so Eastwards lBlock doesn't need the chest faced.
						blockChestData.setType(Type.LEFT);
						lBlockChestData.setType(Type.RIGHT);
					} else if (relativeFace.equals(BlockFace.SOUTH)) {
						blockChestData.setFacing(BlockFace.EAST);
						lBlockChestData.setFacing(BlockFace.EAST);
						blockChestData.setType(Type.LEFT);
						lBlockChestData.setType(Type.RIGHT);
					} else if (relativeFace.equals(BlockFace.NORTH)) {
						blockChestData.setFacing(BlockFace.WEST);
						lBlockChestData.setFacing(BlockFace.WEST);
						blockChestData.setType(Type.LEFT);
						lBlockChestData.setType(Type.RIGHT);							
					}
					block.setBlockData(blockChestData,true);
					lBlock.setBlockData(lBlockChestData,true);
					
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

		// Check if we have signs enabled, if the player can use signs, and if the player has a sign or gets a free sign
		Block sBlock = null;
		if (CenotaphSettings.cenotaphSign() && p.hasPermission("cenotaph.sign") &&
			(pSignCount > 0 || p.hasPermission("cenotaph.freesign"))) {
			// Find a place to put the sign, then place the sign.
			sBlock = sChest.getWorld().getBlockAt(sChest.getX(), sChest.getY() + 1, sChest.getZ());
			if (canReplace(sBlock.getType())) {
				createSign(sBlock, p, relativeFace);
				removeSignCount += 1;
			} else if (lChest != null) {
				sBlock = lChest.getWorld().getBlockAt(lChest.getX(), lChest.getY() + 1, lChest.getZ());
				if (canReplace(sBlock.getType())) {
					createSign(sBlock, p, relativeFace);
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
		Cenotaph.tombList.offer(tBlock);

		// Add tombstone blocks to tombBlockList
		Cenotaph.tombBlockList.put(tBlock.getBlock().getLocation(), tBlock);
		if (tBlock.getLBlock() != null) Cenotaph.tombBlockList.put(tBlock.getLBlock().getLocation(), tBlock);
		if (tBlock.getSign() != null) Cenotaph.tombBlockList.put(tBlock.getSign().getLocation(), tBlock);

		// Add tombstone to player lookup list
		ArrayList<TombBlock> pList = Cenotaph.playerTombList.get(p.getName());
		if (pList == null) {
			pList = new ArrayList<TombBlock>();
			Cenotaph.playerTombList.put(p.getName(), pList);
		}
		pList.add(tBlock);

		plugin.saveCenotaphList(p.getWorld().getName());

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
		
			if(item.getType() == Material.SIGN)
				pSignCount += item.getAmount();

			// Take a sign
			if (removeSignCount > 0 && item.getType() == Material.SIGN) {
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

	private void createSign(Block signBlock, Player p, BlockFace bf) {
		String date = new SimpleDateFormat(CenotaphSettings.dateFormat()).format(new Date());
		String time = new SimpleDateFormat(CenotaphSettings.timeFormat()).format(new Date());
		String name = p.getName();
		String reason = "Unknown";

		EntityDamageEvent dmg = Cenotaph.deathCause.get(name);
		if (dmg != null) {
			Cenotaph.deathCause.remove(name);
			reason = getCause(dmg);
		}

		signBlock.setType(Material.SIGN);
		//Lets make the sign appear to look downwards towards the foot of the long chests.
		org.bukkit.block.data.type.Sign sBlockData = (org.bukkit.block.data.type.Sign) signBlock.getBlockData();
		sBlockData.setRotation(bf);
		signBlock.setBlockData(sBlockData);
		
		final Sign sign = (Sign)signBlock.getState();

		for (int x = 0; x < 4; x++) {
			String line = CenotaphUtil.signMessage[x];
			line = line.replace("{name}", name);
			line = line.replace("{date}", date);
			line = line.replace("{time}", time);
			line = line.replace("{reason}", reason);

			if (line.length() > 15) line = line.substring(0, 15);
			sign.setLine(x, line);
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				sign.update();
			}
		});
	}

	private String getCause(EntityDamageEvent dmg) {
		try {
			switch (dmg.getCause()) {
				case ENTITY_ATTACK:
				{
					EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)dmg;
					Entity e = event.getDamager();
					if (e == null) {
						return "Dispenser";
					} else if (e instanceof Player) {
						return ((Player) e).getDisplayName();
					} else {
						return e.getName();
					}
				}
				case CONTACT:
					return "Cactus";
				case SUFFOCATION:
					return "Suffocation";
				case FALL:
					return "Fall";
				case FIRE:
					return "Fire";
				case FIRE_TICK:
					return "Burning";
				case LAVA:
					return "Lava";
				case DROWNING:
					return "Drowning";
				case BLOCK_EXPLOSION:
					return "Explosion";
				case ENTITY_EXPLOSION:
				{
					try {
						EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)dmg;
						Entity e = event.getDamager();
						if (e instanceof TNTPrimed) return "TNT";
						else if (e instanceof Fireball) return "Ghast";
						else return "Creeper";
					} catch (Exception e) {
						return "Explosion";
					}
				}
				case VOID:
					return "The Void";
				case LIGHTNING:
					return "Lightning";
				default:
					return "Unknown";
			}
		} catch (NullPointerException e) {
			Cenotaph.log.severe("[Cenotaph] Error processing death cause: " + dmg.getCause().toString());
			e.printStackTrace();
			return ChatColor.RED + "ERROR" + ChatColor.BLACK;
		}
	}

	/**
	 * Find a block near the base block to place the tombstone
	 * @param base
	 * @param smallChest 
	 * @return
	 */
	Block findPlace(Block base, boolean smallChest) {
		if (canReplace(base.getType()) && smallChest) return base;
		int baseX = base.getX();
		int baseY = base.getY();
		int baseZ = base.getZ();
		World w = base.getWorld();

		for (int x = baseX - 1; x < baseX + 1; x++) {
			for (int z = baseZ - 1; z < baseZ + 1; z++) {
				Block b = w.getBlockAt(x, baseY, z);
				if (canReplace(b.getType()) && smallChest) 
					return b;
				else if (canReplace(b.getType())) { 
					// When there ought to be a double chest we should test to see if there's space for a double chest and force a oneBlockUpCheck for it.
					// Previously if you died in a 1x1x1 hole only a small chest would form, causing overflow.
					if (canReplace(b.getRelative(BlockFace.NORTH).getType())) return b;
					else if (canReplace(b.getRelative(BlockFace.EAST).getType())) return b;
					else if (canReplace(b.getRelative(BlockFace.WEST).getType())) return b;
					else if (canReplace(b.getRelative(BlockFace.SOUTH).getType())) return b;
				}
			}
		}
		if(CenotaphSettings.oneBlockUpCheck()) {
			//Check block one up, in case of Carpeting/
			for (int x = baseX - 1; x < baseX + 1; x++) {
				for (int z = baseZ - 1; z < baseZ + 1; z++) {
					Block b = w.getBlockAt(x, baseY + 1, z);
					if (canReplace(b.getType())) return b;
				}
			}
		}
		return null;
	}

	Boolean canReplace(Material mat) {
		return (mat == Material.AIR ||
				mat == Material.WATER ||
				mat == Material.LAVA ||
				mat == Material.COBWEB || 
				mat == Material.SUNFLOWER ||
				mat == Material.LILAC ||
				mat == Material.PEONY ||
				mat == Material.ROSE_BUSH ||
				mat == Material.BROWN_MUSHROOM ||
				mat == Material.RED_MUSHROOM ||
				mat == Material.FIRE ||
				mat == Material.SNOW ||
				mat == Material.SUGAR_CANE ||
				mat == Material.GRAVEL ||
				mat == Material.SAND ||
				mat == Material.GRASS ||
				mat == Material.TALL_GRASS ||
				(mat.createBlockData() instanceof Ageable) ||
				mat == Material.DANDELION ||
				mat == Material.AZURE_BLUET ||
				mat == Material.BLUE_ORCHID ||
				mat == Material.FERN ||
				mat == Material.ALLIUM ||
				mat == Material.OXEYE_DAISY ||
				mat == Material.POPPY ||
				mat == Material.ORANGE_TULIP ||
				mat == Material.PINK_TULIP ||
				mat == Material.RED_TULIP ||
				mat == Material.WHITE_TULIP ||
				mat == Material.ACACIA_SAPLING ||
				mat == Material.BIRCH_SAPLING ||
				mat == Material.JUNGLE_SAPLING ||
				mat == Material.DARK_OAK_SAPLING ||
				mat == Material.OAK_SAPLING ||
				mat == Material.SPRUCE_SAPLING
				);
	}

	Block findLarge(Block base) {
		// Check all 4 sides for air.
		Block exp;
		exp = base.getRelative(BlockFace.NORTH);
		if (canReplace(exp.getType())) return exp;
		exp = base.getRelative(BlockFace.EAST);
		if (canReplace(exp.getType())) return exp;
		exp = base.getRelative(BlockFace.SOUTH);
		if (canReplace(exp.getType())) return exp;
		exp = base.getRelative(BlockFace.WEST);
		if (canReplace(exp.getType())) return exp;
		return null;
	}


}
