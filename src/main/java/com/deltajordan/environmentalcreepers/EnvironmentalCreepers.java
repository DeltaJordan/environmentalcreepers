package com.deltajordan.environmentalcreepers;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.deltajordan.environmentalcreepers.config.Configs;
import com.deltajordan.environmentalcreepers.events.ExplosionEventHandler;
import com.deltajordan.environmentalcreepers.events.WorldLoadHandler;
import com.deltajordan.environmentalcreepers.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = EnvironmentalCreepers.MODID,
    version = Tags.VERSION,
    name = "Environmental Creepers",
    dependencies = "required-after:gtnhlib;",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*")
public class EnvironmentalCreepers {

    public static final String MODID = "environmentalcreepers";
    public static final Logger logger = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.deltajordan.environmentalcreepers.proxy.CommonProxy",
        serverSide = "com.deltajordan.environmentalcreepers.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        try {
            Configs.registerConfig();
        } catch (Exception ex) {
            logger.error("Failed to load configuration.");
        }

        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        MinecraftForge.EVENT_BUS.register(new WorldLoadHandler());
    }

    public static void logInfo(String message, Object... params) {
        if (Configs.General.verbose_logging) {
            logger.info(message, params);
        }
    }
}
