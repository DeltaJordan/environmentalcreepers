package com.deltajordan.environmentalcreepers.events;

import com.deltajordan.environmentalcreepers.config.Configs;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;

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
