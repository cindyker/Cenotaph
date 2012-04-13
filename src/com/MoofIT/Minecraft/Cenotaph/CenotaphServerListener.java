package com.MoofIT.Minecraft.Cenotaph;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.yi.acru.bukkit.Lockette.Lockette;

import com.griefcraft.lwc.LWCPlugin;

public class CenotaphServerListener implements Listener {
	private Cenotaph plugin;

	public CenotaphServerListener(Cenotaph instance) {
		this.plugin = instance;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if (plugin.lwcPlugin == null) {
			if (event.getPlugin().getDescription().getName().equalsIgnoreCase("LWC")) {
				plugin.lwcPlugin = (LWCPlugin)plugin.checkPlugin(event.getPlugin());
			}
		}
		if (plugin.LockettePlugin == null) {
			if (event.getPlugin().getDescription().getName().equalsIgnoreCase("Lockette")) {
				plugin.LockettePlugin = (Lockette)plugin.checkPlugin(event.getPlugin());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin() == plugin.lwcPlugin) {
			Cenotaph.log.info("[Cenotaph] LWC plugin lost.");
			plugin.lwcPlugin = null;
		}
		if (event.getPlugin() == plugin.LockettePlugin) {
			Cenotaph.log.info("[Cenotaph] Lockette plugin lost.");
			plugin.LockettePlugin = null;
		}
	}
}