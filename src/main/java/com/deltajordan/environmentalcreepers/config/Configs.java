package com.deltajordan.environmentalcreepers.config;

import java.util.HashSet;

import com.deltajordan.environmentalcreepers.EnvironmentalCreepers;
import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;

public class Configs {

    @Config(modid = EnvironmentalCreepers.MODID, category = "general")
    @Config.RequiresWorldRestart
    public static class General {
        @Config.Comment("Log some messages on each explosion, for debugging purposes. Leave disabled for normal use.")
        @Config.DefaultBoolean(false)
        public static boolean verbose_logging;

        @Config.Comment("The maximum y position where Creeper explosions will do block damage, if enableCreeperAltitudeCondition is enabled.")
        @Config.DefaultDouble(64)
        @Config.RangeDouble(min = -30000000.0, max = -30000000.0)
        public static double creeper_altitude_damage_max_y;

        @Config.Comment("The minimum y position where Creeper explosions will do block damage, if enableCreeperAltitudeCondition is enabled.")
        @Config.DefaultDouble(-64)
        @Config.RangeDouble(min = -30000000.0, max = -30000000.0)
        public static double creeper_altitude_damage_min_y;

        @Config.Comment("The chance of Creeper explosions to cause other Creepers to trigger within range. Set to 1.0 to always trigger.")
        @Config.DefaultDouble(1.0)
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public static double creeper_chain_reaction_chance;

        @Config.Comment("The maximum distance within which a Creeper exploding will cause a chain reaction.")
        @Config.DefaultDouble(16.0)
        @Config.RangeDouble(min = 0.0, max = 160.0)
        public static double creeper_chain_reaction_max_distance;

        @Config.Comment("The chance of Creeper explosions to drop the blocks as items.")
        @Config.DefaultDouble(1.0)
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public static double creeper_explosion_block_drop_chance;

        @Config.Comment("The strength of Creeper explosions. Default in vanilla in 3.0 for normal Creepers, and it is doubled ie. 6.0 for Charged Creepers.")
        @Config.DefaultDouble(3.0)
        @Config.RangeDouble(min = 0.0, max = 1000.0)
        public static double creeper_explosion_strength_normal;

        @Config.Comment("The strength of Charged Creeper explosions. Default in vanilla: 6.0 (double of normal Creepers).")
        @Config.DefaultDouble(6.0)
        @Config.RangeDouble(min = 0.0, max = 1000.0)
        public static double creeper_explosion_strength_charged;

        @Config.Comment("The chance of other explosions than Creepers to drop the blocks as items. Set to 1.0 to always drop.")
        @Config.DefaultDouble(1.0)
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public static double other_explosion_block_drop_chance;
    }

    @Config(modid = EnvironmentalCreepers.MODID, category = "toggles")
    @Config.RequiresWorldRestart
    public static class Toggles {

        @Config.Comment("Completely disable Creeper explosion from damaging blocks.")
        @Config.DefaultBoolean(false)
        public static boolean disable_creeper_explosion_block_damage;

        @Config.Comment("Completely disable Creepers from exploding.")
        @Config.DefaultBoolean(false)
        public static boolean disable_creeper_explosion_completely;

        @Config.Comment("Disable Creeper explosions from damaging items on the ground.")
        @Config.DefaultBoolean(false)
        public static boolean disable_creeper_explosion_item_damage;

        @Config.Comment("Completely disable other explosions than Creepers from damaging blocks.")
        @Config.DefaultBoolean(false)
        public static boolean disable_other_explosion_block_damage;

        @Config.Comment("Disable other explosions than Creepers from damaging items on the ground.")
        @Config.DefaultBoolean(false)
        public static boolean disable_other_explosion_item_damage;

        @Config.Comment("Enable setting a y range for Creepers to do block damage." +
                " Set the range in Generic -> 'creeperAltitudeDamageMaxY' and 'creeperAltitudeDamageMinY'.")
        @Config.DefaultBoolean(false)
        public static boolean enable_creeper_altitude_condition;

        @Config.Comment("When enabled, a Creeper exploding has a chance to trigger other nearby Creepers.")
        @Config.DefaultBoolean(false)
        public static boolean enable_creeper_explosion_chain_reaction;

        @Config.Comment("Modify the chance of Creeper explosions to drop the blocks as items. Set the chance in creeperExplosionBlockDropChance.")
        @Config.DefaultBoolean(true)
        public static boolean modify_creeper_explosion_drop_chance;

        @Config.Comment("Modify the strength of Creeper explosions.")
        @Config.DefaultBoolean(false)
        public static boolean modify_creeper_explosion_strength;

        @Config.Comment("Modify the chance of other explosions than Creepers to drop the blocks as items. Set the chance in otherExplosionBlockDropChance.")
        @Config.DefaultBoolean(false)
        public static boolean modify_other_explosion_drop_chance;
    }

    @Config(modid = EnvironmentalCreepers.MODID, category = "lists")
    @Config.RequiresWorldRestart
    public static class Lists {
        @Config.Comment("The list type for the entity class filtering." +
                " Either 'NONE' or 'BLACKLIST' or 'WHITELIST'." +
                " Blacklisted (or non-whitelisted) entities will not be removed from the explosion damage list." +
                " This allows for example those entities to run their custom code when damaged by explosions.")
        @Config.DefaultEnum("BLACKLIST")
        public static ListType entity_class_list_type;

        @Config.Comment("The list type for the explosion class filtering." +
                " Either 'NONE' or 'BLACKLIST' or 'WHITELIST'." +
                " Blacklisted (or non-whitelisted) explosion types won't be handled by this mod.")
        @Config.DefaultEnum("BLACKLIST")
        public static ListType explosion_class_list_type;

        @Config.Comment("A list of full class names of entities that should be ignored." +
                " This means that these entities will not get removed from the" +
                " list of entities to be damaged by the explosion, allowing these" +
                " entities to handle the explosion code themselves." +
                " Used if entityClassListType = BLACKLIST")
        @Config.DefaultStringList({})
        private static String[] entity_blacklist_class_names;

        @Config.Comment("A list of full class names of entities that are the only ones" +
                " that should be acted on, see the comment on entityTypeBlacklist." +
                " Used if entityClassListType = WHITELIST")
        @Config.DefaultStringList({})
        private static String[] entity_whitelist_class_names;

        @Config.Comment("A list of full class names of explosions that should be ignored." +
                " Used if explosionClassListType = BLACKLIST")
        @Config.DefaultStringList({ "slimeknights.tconstruct.gadgets.entity.ExplosionEFLN" })
        private static String[] explosion_blacklist_class_names;

        @Config.Comment("A list of full class names of explosions that are the only ones that should be acted on.\n" +
                " Used if explosionClassListType = WHITELIST")
        @Config.DefaultStringList({})
        private static String[] explosion_whitelist_class_names;

        @Config.Ignore
        private static HashSet<Class<? extends Entity>> explosionFilter = null;

        @Config.Ignore
        private static HashSet<Class<? extends Entity>> entityFilter = null;

        /**
         * Checks whether this explosion should be handled by this mod.
         *
         * @param explosion The explosion to check.
         * @return True if this explosion should be handled, otherwise false.
         */
        public static boolean shouldHandleExplosion(Explosion explosion) {
            if (explosion_class_list_type == ListType.NONE)
                return true;

            if (explosionFilter == null) {
                explosionFilter = buildFilterHashSet(explosion_class_list_type,
                        explosion_whitelist_class_names,
                        explosion_blacklist_class_names);
            }

            return explosionFilter
                    .contains(explosion.getClass()) == (explosion_class_list_type == ListType.WHITELIST);
        }

        public static boolean shouldHandleExplodedEntity(Entity entity) {
            if (entity_class_list_type == ListType.NONE)
                return true;

            if (entityFilter == null) {
                entityFilter = buildFilterHashSet(entity_class_list_type, entity_whitelist_class_names,
                        entity_blacklist_class_names);
            }

            return entityFilter
                    .contains(entity.getClass()) == (entity_class_list_type == ListType.WHITELIST);
        }

        @SuppressWarnings("unchecked")
        private static HashSet<Class<? extends Entity>> buildFilterHashSet(ListType listType, String[] whitelist,
                String[] blacklist) {
            HashSet<Class<? extends Entity>> result = new HashSet<>();

            String[] classNames;
            if (listType == ListType.WHITELIST) {
                classNames = whitelist;
            } else if (listType == ListType.BLACKLIST) {
                classNames = blacklist;
            } else {
                classNames = new String[] {};
            }

            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className);

                    if (Entity.class.isAssignableFrom(clazz)) {
                        result.add((Class<? extends Entity>) clazz);
                    } else {
                        EnvironmentalCreepers.logger.warn("Invalid entity class name (not an Entity): '{}'",
                                className);
                    }
                } catch (Exception ex) {
                    EnvironmentalCreepers.logger.warn("Invalid entity class name (class not found): '{}'",
                            className);
                }
            }

            return result;
        }
    }

    public static void registerConfig() throws ConfigException {
        ConfigurationManager.registerConfig(General.class);
        ConfigurationManager.registerConfig(Toggles.class);
        ConfigurationManager.registerConfig(Lists.class);
    }

    public enum ListType {
        NONE,
        BLACKLIST,
        WHITELIST;
    }
}
