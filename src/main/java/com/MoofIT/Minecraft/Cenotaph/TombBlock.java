package com.MoofIT.Minecraft.Cenotaph;

import java.util.UUID;

import org.bukkit.block.Block;

public class TombBlock {
	private Block block;
	private Block lBlock;
	private Block sign;
	private Block locketteSign;
	private long time;
	private String owner;
	private int ownerLevel;
	private boolean lwcEnabled = false;
	private UUID ownerUUID;

	TombBlock(Block block, Block lBlock, Block sign, String owner, int ownerLevel, long time, UUID ownerUUID) {
		this.block = block;
		this.lBlock = lBlock;
		this.sign = sign;
		this.owner = owner;
		this.ownerUUID = ownerUUID;
		this.ownerLevel = ownerLevel;
		this.time = time;
		
	}
	TombBlock(Block block, Block lBlock, Block sign, String owner,  int ownerLevel, long time, boolean lwc, Block locketteSign, UUID ownerUUID) {
		this.block = block;
		this.lBlock = lBlock;
		this.sign = sign;
		this.owner = owner;
		this.ownerUUID = ownerUUID;
		this.ownerLevel = ownerLevel;
		this.time = time;
		this.lwcEnabled = lwc;
		this.locketteSign = locketteSign;
	}

	long getTime() {
		return time;
	}
	Block getBlock() {
		return block;
	}
	Block getLBlock() {
		return lBlock;
	}
	Block getSign() {
		return sign;
	}
	Block getLocketteSign() {
		return locketteSign;
	}
	String getOwner() {
		return owner;
	}
	UUID getOwnerUUID() {
		return ownerUUID;
	}
	int getOwnerLevel() {
		return ownerLevel;
	}
	boolean getLwcEnabled() {
		return lwcEnabled;
	}
	void setLwcEnabled(boolean val) {
		lwcEnabled = val;
	}
	void setLocketteSign(Block signBlock) {
		this.locketteSign = signBlock;
	}
	void removeLocketteSign() {
		this.locketteSign = null;
	}
}
