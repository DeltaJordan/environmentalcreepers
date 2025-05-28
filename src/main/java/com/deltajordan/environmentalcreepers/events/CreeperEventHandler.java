package com.deltajordan.environmentalcreepers.events;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

import com.deltajordan.environmentalcreepers.EnvironmentalCreepers;
import com.deltajordan.environmentalcreepers.config.Configs;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CreeperEventHandler extends Event {

    public static final CreeperEventHandler INSTANCE = new CreeperEventHandler();

    private boolean registered;

    public void register() {
        if (this.registered == false) {
            EnvironmentalCreepers.logInfo("Registering CreeperEventHandler");
            MinecraftForge.EVENT_BUS.register(this);
            this.registered = true;
        }
    }

    public void unregister() {
        if (this.registered) {
            EnvironmentalCreepers.logInfo("Unregistering CreeperEventHandler");
            MinecraftForge.EVENT_BUS.unregister(this);
            this.registered = false;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (Configs.Toggles.disable_creeper_explosion_completely && event.entity instanceof EntityCreeper creeper) {
            int state = creeper.getCreeperState();

            if (state > 0) {
                try {
                    int timeSinceIgnited = (int) ObfuscationReflectionHelper
                        .getPrivateValue(EntityCreeper.class, creeper, "timeSinceIgnited");
                    int fuseTime = (int) ObfuscationReflectionHelper
                        .getPrivateValue(EntityCreeper.class, creeper, "fuseTime");

                    if (timeSinceIgnited >= (fuseTime - state - 1)) {
                        ObfuscationReflectionHelper
                            .setPrivateValue(EntityCreeper.class, creeper, fuseTime - state - 1, "timeSinceIgnited");
                    }
                } catch (Exception e) {
                    EnvironmentalCreepers.logger
                        .warn("CreeperEventHandler.onLivingUpdate(): Exception while trying to reflect Creeper fields");
                }
            }
        }
    }
}
