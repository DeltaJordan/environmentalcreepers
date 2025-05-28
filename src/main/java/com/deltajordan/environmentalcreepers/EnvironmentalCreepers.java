package com.deltajordan.environmentalcreepers;

import java.util.Map;

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
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(
    modid = EnvironmentalCreepers.MODID,
    version = Tags.VERSION,
    name = "Environmental Creepers",
    acceptedMinecraftVersions = "[1.7.10]")
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

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> map, Side side) {
        return side != Side.CLIENT || map.containsKey(MODID) && map.get(MODID)
            .equals(Tags.VERSION);
    }

    public static void logInfo(String message, Object... params) {
        if (Configs.General.verbose_logging) {
            logger.info(message, params);
        }
    }
}
