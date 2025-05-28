package com.deltajordan.environmentalcreepers.events;

import net.minecraftforge.event.world.WorldEvent;

import com.deltajordan.environmentalcreepers.config.Configs;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class WorldLoadHandler extends Event {

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (Configs.Toggles.disable_creeper_explosion_completely) {
            CreeperEventHandler.INSTANCE.register();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        CreeperEventHandler.INSTANCE.unregister();
    }
}
