package com.MoofIT.Minecraft.Cenotaph;

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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;

import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardWrapper
{
    private static WorldGuardPlugin worldGuard = null;

    public WorldGuardWrapper() throws ClassNotFoundException
    {
        this.worldGuard = (WorldGuardPlugin)Cenotaph.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
    }

    public static boolean canBuild(Player creatingPlayer)
    {

        if (worldGuard == null)
        {
            Cenotaph.log.info("WorldGuard is out of date and not enabled. Please update or remove WorldGuard.");
            return true;
        }

        //If they are Op they can build anywhere!
        if( creatingPlayer.isOp() ){
            Cenotaph.log.info("Player is op. Can build anywhere.");
            return true;
        }

        WorldGuardPlatform p = WorldGuard.getInstance().getPlatform();

        RegionManager manager = p.getRegionContainer().get(p.getWorldByName(creatingPlayer.getWorld().getName()));


        if(manager != null)
        {

            Vector vector = new Vector();
            vector.add(creatingPlayer.getLocation().getX(),creatingPlayer.getLocation().getY()-1,creatingPlayer.getLocation().getZ());
            ApplicableRegionSet testregion = manager.getApplicableRegions(vector);

            LocalPlayer localPlayer = worldGuard.wrapPlayer(creatingPlayer);

            for (ProtectedRegion r : testregion.getRegions()) {

                if (!manager.getApplicableRegions(r).testState(localPlayer, Flags.BUILD)) {
                    Cenotaph.log.info("Player "+localPlayer.getName()+" is in a Region where they can't build. No Cenotaph will be made.");
                    return false;
                }
            }
            return true;
        }

        return true;
    }
}