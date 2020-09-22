package com.MoofIT.Minecraft.Cenotaph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.BlockProjectileSource;

import com.MoofIT.Minecraft.Cenotaph.Config.Lang;
import com.MoofIT.Minecraft.Cenotaph.PluginHandlers.WorldGuardWrapper;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

public class CenotaphUtil {
	
	public static boolean isTombBlock(Block block) {
		TombBlock tBlock = CenotaphDatabase.tombBlockList.get(block.getLocation());
		if (tBlock == null)
			return false;
		else 
			return true;
	}
	
	public static TombBlock getTombBlock(Block block) {
		TombBlock tBlock = CenotaphDatabase.tombBlockList.get(block.getLocation());
		if (tBlock == null)
			return null;
		else 
			return tBlock;
	}
	
	public static TombBlock getBlockByIndex(String playerName,String index) {
		ArrayList<TombBlock> pList = CenotaphDatabase.playerTombList.get(playerName);
		int slot = 0;

		if (pList == null) return null;

		try {
			slot = Integer.parseInt(index);
		} catch (NumberFormatException e) {
			slot = pList.size();
		}
		slot -= 1;

		if (slot < 0 || slot >= pList.size()) return null;

		return pList.get(slot);
	}
	
	public static String[] signMessage = {
		CenotaphSettings.signLine1(),
		CenotaphSettings.signLine2(),
		CenotaphSettings.signLine3(),
		CenotaphSettings.signLine4()
	};

	/**
	 * Converts a rotation to a cardinal direction name.
	 * Author: sk89q - Original function from CommandBook plugin
	 * @param rot
	 * @return
	 */
	public static String getDirection(double rot) {
		if (0 <= rot && rot < 22.5) {
			return Lang.string("north");
		} else if (22.5 <= rot && rot < 67.5) {
			return Lang.string("northeast");
		} else if (67.5 <= rot && rot < 112.5) {
			return Lang.string("east");
		} else if (112.5 <= rot && rot < 157.5) {
			return Lang.string("southeast");
		} else if (157.5 <= rot && rot < 202.5) {
			return Lang.string("south");
		} else if (202.5 <= rot && rot < 247.5) {
			return Lang.string("southwest");
		} else if (247.5 <= rot && rot < 292.5) {
			return Lang.string("west");
		} else if (292.5 <= rot && rot < 337.5) {
			return Lang.string("northwest");
		} else if (337.5 <= rot && rot < 360.0) {
			return Lang.string("north");
		} else {
			return null;
		}
	}
	
	public static String convertTime(int s) {
		String formatted = Integer.toString(s);
		if (s >= 86400) {
			formatted = String.format("%dd %d:%02d:%02d", s/86400, (s%86400)/3600, (s%3600)/60, s%60);
		}
		else if (s >= 3600) {
			formatted = String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
		}
		else if (s > 60) {
			formatted = String.format("%02d:%02d", s/60, s%60);
		}
		else if (s == 60) {
			formatted = "01:00";
		}
		else if (s < 60) {
			formatted = String.format("%02d:%02d", 00, s%60);
		}
		return formatted;
	}
	
	/**
	 * Gets the Yaw from one location to another in relation to North.
	 *
	 */
	public static double getYawTo(Location from, Location to) {
		final int distX = to.getBlockX() - from.getBlockX();
		final int distZ = to.getBlockZ() - from.getBlockZ();
		double degrees = Math.toDegrees(Math.atan2(-distX, distZ));
		degrees += 180;
		return degrees;
	}

	public static String getCause(EntityDamageEvent dmg) {
		try {
			switch (dmg.getCause()) {
				case PROJECTILE:
					Projectile p = (Projectile)getDamager((EntityDamageByEntityEvent) dmg);
					if (p.getShooter() instanceof Player) {
						return ((Player) p.getShooter()).getDisplayName();
					} else if (p.getShooter() instanceof BlockProjectileSource){
						return Lang.string("dispenser");
					} else if (p.getShooter() instanceof Entity) {
						return ((Entity)p.getShooter()).getType().name();
					} else {
						return p.getName();
					}
				case ENTITY_ATTACK:
				{
					Entity e = getDamager((EntityDamageByEntityEvent) dmg);
					if (e instanceof Player) {
						return ((Player) e).getDisplayName();
					} else {
						return e.getType().name();
					}
				}
				case ENTITY_SWEEP_ATTACK:
				{
					Entity e = getDamager((EntityDamageByEntityEvent) dmg);
					if (e instanceof Player) {
						return ((Player) e).getDisplayName();
					} else {
						return e.getType().name();
					}
				}
				case ENTITY_EXPLOSION:
				{
					try {
						Entity e = getDamager((EntityDamageByEntityEvent) dmg);
						if (e instanceof TNTPrimed) return Lang.string("tnt");
						else if (e instanceof Fireball) return Lang.string("ghast");
						else return Lang.string("creeper");
					} catch (Exception e) {
						return Lang.string("explosion");
					}
				}
				case BLOCK_EXPLOSION:
					return Lang.string("explosion");
				case CONTACT:
					return Lang.string("cactus");
				case DRAGON_BREATH:
					return Lang.string("dragonbreath");
				case DROWNING:
					return Lang.string("drowning");
				case FALL:
					return Lang.string("fall");
				case FALLING_BLOCK:
					return Lang.string("anvil");
				case FLY_INTO_WALL:
					return Lang.string("flyingintowall");
				case FIRE:
					return Lang.string("fire");
				case FIRE_TICK:
					return Lang.string("burning");
				case HOT_FLOOR:
					return Lang.string("hotfloor");
				case LAVA:
					return Lang.string("lava");
				case LIGHTNING:
					return Lang.string("lightning");
				case MAGIC:
					return Lang.string("magic");
				case POISON:
					return Lang.string("poison");
				case STARVATION:
					return Lang.string("starvation");
				case SUFFOCATION:
					return Lang.string("suffocation");
				case SUICIDE:
					return Lang.string("suicide");
				case THORNS:
					return Lang.string("thorns");
				case VOID:
					return Lang.string("void");
				case WITHER:
					return Lang.string("withereffect");
				default:
					return Lang.string("unknown");
			}
		} catch (NullPointerException e) {
			Cenotaph.log.severe("[Cenotaph] Error processing death cause: " + dmg.getCause().toString());
			e.printStackTrace();
			return ChatColor.RED + "ERROR" + ChatColor.BLACK;
		}
	}

	public static Entity getDamager(EntityDamageByEntityEvent dmg) {
		return dmg.getDamager();		
	}

	/**
	 * Find a block near the base block to place the tombstone
	 * @param base
	 * @param smallChest 
	 * @return
	 */
	public static Block findPlace(Block base, boolean smallChest) {
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

	public static Boolean canReplace(Material mat) {
		return (mat == Material.AIR ||
				mat == Material.CAVE_AIR ||
				mat == Material.BUBBLE_COLUMN ||
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
				(Tag.SMALL_FLOWERS.isTagged(mat) && mat != Material.WITHER_ROSE) ||
				Tag.SAPLINGS.isTagged(mat)
				);
	}

	public static Block findLarge(Block base) {
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
	
	public static void createSign(Block signBlock, Player p, BlockFace bf) {
		String date = new SimpleDateFormat(CenotaphSettings.dateFormat()).format(new Date());
		String time = new SimpleDateFormat(CenotaphSettings.timeFormat()).format(new Date());
		String name = p.getName();
		String reason = Lang.string("unknown");

		EntityDamageEvent dmg = CenotaphDatabase.deathCause.get(name);
		if (dmg != null) {
			CenotaphDatabase.deathCause.remove(name);
			reason = CenotaphUtil.getCause(dmg);
		}

		signBlock.setType(Material.OAK_SIGN);
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

		Cenotaph.plugin.getServer().getScheduler().scheduleSyncDelayedTask(Cenotaph.plugin, new Runnable() {
			public void run() {
				sign.update();
			}
		});
	}

	public static void createLargeChest(Block block, Block lBlock, BlockFace relativeFace) {
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
		
	}

	/**
	 * Tests to make sure a player can build where their cenotaph will be formed.
	 * WorldGuard and Towny are supported, both must be enabled in the config.
	 * 
	 * @param player - Player that died.
	 * @param loc - Location of the death.
	 * @return true if they can build in configured protection plugins.
	 */
	public static boolean testRegionForBuildRights(Player player, Location loc) {
		//WorldGuard support, see if the player could build where they've died. Disallow a cenotaph if they cannot build.
		if (Cenotaph.worldguardEnabled) {
			if (!WorldGuardWrapper.canBuild(player)) {
				CenotaphMessaging.sendPrefixedPlayerMessage(player, Lang.string("worldguard_area"));
				return false;
			}
		}

		//Towny support, see if the player could build where they've died. Disallow a cenotaph if they cannot build.
		if (Cenotaph.townyEnabled) {
			if (!PlayerCacheUtil.getCachePermission(player, loc, Material.CHEST, ActionType.BUILD)) {
				CenotaphMessaging.sendPrefixedPlayerMessage(player, Lang.string("towny_area"));
				return false;
			}		
		}
		return true;
	}

}
