package com.MoofIT.Minecraft.Cenotaph.PluginHandlers;

/**
 * Cenotaph
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.If not, see <http://www.gnu.org/licenses/>.
 *
 * This class is based on a similar class in GriefPrevention.
 */

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;

import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.entity.Player;

import com.MoofIT.Minecraft.Cenotaph.Cenotaph;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardWrapper
{
	static WorldGuardPlugin worldGuard = (WorldGuardPlugin) Cenotaph.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	
    public static boolean canBuild(Player creatingPlayer)
    {
        //If they are Op they can build anywhere!
        if( creatingPlayer.isOp() ){
            return true;
        }        

        WorldGuardPlatform plat = WorldGuard.getInstance().getPlatform();

        com.sk89q.worldedit.entity.Player wPlayer = worldGuard.wrapPlayer(creatingPlayer);

        RegionManager manager = plat.getRegionContainer().get(wPlayer.getWorld());

        if(manager != null)
        {
            LocalPlayer localPlayer = worldGuard.wrapPlayer(creatingPlayer);

            ApplicableRegionSet testRegion = plat.getRegionContainer().createQuery().getApplicableRegions(localPlayer.getLocation());

            for (ProtectedRegion r : testRegion.getRegions()) {

                if (!manager.getApplicableRegions(r).testState(localPlayer, Flags.BUILD)) {
                    Cenotaph.log.info("Player "+localPlayer.getName()+" is in Region "+ r.getId() +" where they can't build. No Cenotaph will be made.");
                    return false;
                }
            }
            return true;
        }

        return true;
    }
}