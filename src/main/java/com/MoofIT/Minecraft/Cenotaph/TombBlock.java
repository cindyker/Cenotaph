package com.MoofIT.Minecraft.Cenotaph;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class TombBlock {
	private Block block;
	private Block lBlock;
	private Block sign;
	private long time;
	private int ownerLevel;
	private UUID ownerUUID;
	
	public TombBlock(Block block, Block lBlock, Block sign, long time, int ownerLevel, UUID ownerUUID) {
		this.block = block;
		this.lBlock = lBlock;
		this.sign = sign;
		this.time = time;
		this.ownerLevel = ownerLevel;
		this.ownerUUID = ownerUUID;
	}

	public long getTime() {
		return time;
	}
	public Block getBlock() {
		return block;
	}
	public Block getLBlock() {
		return lBlock;
	}
	public Block getSign() {
		return sign;
	}
	public String getOwner() {
		return Bukkit.getOfflinePlayer(ownerUUID).getName();
	}
	public UUID getOwnerUUID() {
		return ownerUUID;
	}
	int getOwnerLevel() {
		return ownerLevel;
	}
	public boolean isSecured() {
		if (!CenotaphSettings.securityEnable()) 
			return false;
		if (!CenotaphSettings.securityRemove())
			return true;
		
		if (securityTimeLeft() > 0 )
			return true;
		else
			return false;		
	}
	public int securityTimeLeft() {
		long cTime = System.currentTimeMillis() / 1000;
		return (int) (CenotaphSettings.securityTimeOut() - (cTime - getTime()));
	}
	public int removalTimeLeft() {
		int time = (CenotaphSettings.levelBasedRemoval() ? Math.min(ownerLevel + 1 * CenotaphSettings.levelBasedTime(), CenotaphSettings.cenotaphRemoveTime()) : CenotaphSettings.cenotaphRemoveTime());
		long cTime = System.currentTimeMillis() / 1000;
		return (int) (time - (cTime - getTime()));
	}

}
