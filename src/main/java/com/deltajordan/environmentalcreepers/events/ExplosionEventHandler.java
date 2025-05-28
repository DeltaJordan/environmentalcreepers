package com.deltajordan.environmentalcreepers.events;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.deltajordan.environmentalcreepers.EnvironmentalCreepers;
import com.deltajordan.environmentalcreepers.config.Configs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.ExplosionEvent;

public class ExplosionEventHandler {

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
        Explosion explosion = event.explosion;

        if (!Configs.Lists.shouldHandleExplosion(explosion)) {
            if (Configs.General.verbose_logging) {
                EnvironmentalCreepers.logInfo("Explosion (blocked by white- or blacklist): class: {}, position: {}",
                        explosion.getClass().getName(), getExplosionPosition(explosion));
            }

            return;
        }

        if (Configs.General.verbose_logging) {
            EnvironmentalCreepers.logInfo("Explosion: class: {}, position: {}", explosion.getClass().getName(),
                    getExplosionPosition(explosion));
        }

        if (explosion.getExplosivePlacedBy() instanceof EntityCreeper) {
            if (Configs.Toggles.modify_creeper_explosion_drop_chance
                    && Configs.Toggles.disable_creeper_explosion_block_damage == false) {
                this.replaceExplosion(event, true);
            }
        } else {
            if (Configs.Toggles.modify_other_explosion_drop_chance
                    && Configs.Toggles.disable_other_explosion_block_damage == false) {
                this.replaceExplosion(event, false);
            }
        }
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.explosion;

        if (!Configs.Lists.shouldHandleExplosion(explosion)) {
            return;
        }

        if (explosion.getExplosivePlacedBy() instanceof EntityCreeper) {
            if (Configs.Toggles.disable_creeper_explosion_item_damage) {
                this.removeItemEntities(event.getAffectedEntities(), true);
            }

            if (Configs.Toggles.disable_creeper_explosion_block_damage ||
                    (Configs.Toggles.enable_creeper_altitude_condition &&
                            (explosion.explosionY < Configs.General.creeper_altitude_damage_min_y ||
                                    explosion.explosionY > Configs.General.creeper_altitude_damage_max_y))) {
                EnvironmentalCreepers
                        .logInfo("ExplosionEventHandler - clearAffectedBlockPositions() - Type: 'Creeper'");
                explosion.affectedBlockPositions.clear();
            }

            if (Configs.Toggles.enable_creeper_explosion_chain_reaction) {
                this.causeCreeperChainReaction(event.world, getExplosionPosition(explosion));
            }
        } else {
            if (Configs.Toggles.disable_other_explosion_item_damage) {
                this.removeItemEntities(event.getAffectedEntities(), false);
            }

            if (Configs.Toggles.disable_other_explosion_block_damage) {
                EnvironmentalCreepers.logInfo("ExplosionEventHandler - clearAffectedBlockPositions() - Type: 'Other'");
                explosion.affectedBlockPositions.clear();
            }
        }
    }

    private void removeItemEntities(List<Entity> list, boolean isCreeper) {
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.removeItemEntities() - Type: '{}'",
                isCreeper ? "Creeper" : "Other");
        Iterator<Entity> iter = list.iterator();

        while (iter.hasNext()) {
            Entity entity = iter.next();

            if (entity instanceof EntityItem) {
                if (Configs.Lists.shouldHandleExplodedEntity(entity)) {
                    iter.remove();
                }
            }
        }
    }

    private void replaceExplosion(ExplosionEvent.Start event, boolean isCreeper) {
        World world = event.world;
        Explosion explosion = event.explosion;

        if (Configs.General.verbose_logging) {
            EnvironmentalCreepers.logInfo("Replacing the explosion for type '{}' (class: {})",
                    isCreeper ? "Creeper" : "Other", explosion.getClass().getName());
        }

        boolean breakBlocks = explosion.isSmoking;
        boolean isFlaming = explosion.isFlaming;
        float explosionSize;

        if (isCreeper && Configs.Toggles.modify_creeper_explosion_strength) {
            if (((EntityCreeper) explosion.getExplosivePlacedBy()).getPowered()) {
                explosionSize = (float) Configs.General.creeper_explosion_strength_charged;
            } else {
                explosionSize = (float) Configs.General.creeper_explosion_strength_normal;
            }

            explosion.explosionSize = explosionSize;
        } else {
            explosionSize = explosion.explosionSize;
        }

        explosion.doExplosionA();

        if (world instanceof WorldServer) {
            Vec3 pos = getExplosionPosition(explosion);

            if (breakBlocks == false ||
                    (isCreeper && Configs.Toggles.enable_creeper_altitude_condition &&
                            (pos.yCoord < Configs.General.creeper_altitude_damage_min_y ||
                                    pos.yCoord > Configs.General.creeper_altitude_damage_max_y))) {
                explosion.affectedBlockPositions.clear();
            }

            this.doExplosionB(world, explosion, false, isCreeper, breakBlocks, isFlaming, explosionSize);

            for (EntityPlayer player : world.playerEntities) {
                if (player.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord) < 4096.0D) {
                    ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(
                            new S27PacketExplosion(pos.xCoord, pos.yCoord, pos.zCoord, explosionSize,
                                    explosion.affectedBlockPositions,
                                    explosion.func_77277_b().get(player) // Knockback map.
                            ));
                }
            }
        } else {
            this.doExplosionB(world, explosion, true, isCreeper, breakBlocks, isFlaming, explosionSize);
        }

        event.setCanceled(true);
    }

    private void doExplosionB(World world, Explosion explosion, boolean spawnParticles, boolean isCreeper,
            boolean breakBlocks, boolean isFlaming, float explosionSize) {
        Vec3 pos = getExplosionPosition(explosion);
        Random rand = world.rand;

        world.playSoundEffect(pos.xCoord, pos.yCoord, pos.zCoord, "random.explode", 4.0F,
                (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);

        if (explosionSize >= 2.0F && breakBlocks) {
            world.spawnParticle("hugeexplosion", pos.xCoord, pos.yCoord, pos.zCoord, 1.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle("largeexplode", pos.xCoord, pos.yCoord, pos.zCoord, 1.0D, 0.0D, 0.0D);
        }

        float dropChance = (float) (isCreeper ? Configs.General.creeper_explosion_block_drop_chance
                : Configs.General.other_explosion_block_drop_chance);
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.doExplosionB() - Type: '{}', drop chance: {}",
                isCreeper ? "Creeper" : "Other", dropChance);

        if (breakBlocks) {
            for (ChunkPosition chunkPos : explosion.affectedBlockPositions) {
                Block block = world.getBlock(chunkPos.chunkPosX, chunkPos.chunkPosY, chunkPos.chunkPosZ);
                int meta = world.getBlockMetadata(chunkPos.chunkPosX, chunkPos.chunkPosY, chunkPos.chunkPosZ);

                if (spawnParticles) {
                    double d0 = chunkPos.chunkPosX + rand.nextFloat();
                    double d1 = chunkPos.chunkPosY + rand.nextFloat();
                    double d2 = chunkPos.chunkPosZ + rand.nextFloat();
                    double d3 = d0 - pos.xCoord;
                    double d4 = d1 - pos.yCoord;
                    double d5 = d2 - pos.zCoord;
                    double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / explosionSize + 0.1D);
                    d7 = d7 * (double) (rand.nextFloat() * rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.spawnParticle("explode", (d0 + pos.xCoord) / 2.0D, (d1 + pos.yCoord) / 2.0D,
                            (d2 + pos.zCoord) / 2.0D, d3, d4, d5);
                    world.spawnParticle("normal", d0, d1, d2, d3, d4, d5);
                }

                if (block.getMaterial() != Material.air) {
                    if (block.canDropFromExplosion(explosion)) {
                        block.dropBlockAsItemWithChance(world, chunkPos.chunkPosX, chunkPos.chunkPosY,
                                chunkPos.chunkPosZ, meta, dropChance, 0);
                    }

                    block.onBlockExploded(world, chunkPos.chunkPosX, chunkPos.chunkPosY, chunkPos.chunkPosZ, explosion);
                }
            }
        }

        if (isFlaming) {
            for (ChunkPosition blockpos : explosion.affectedBlockPositions) {
                Block block = world.getBlock(blockpos.chunkPosX, blockpos.chunkPosY, blockpos.chunkPosZ);
                Block block1 = world.getBlock(blockpos.chunkPosX, blockpos.chunkPosY - 1, blockpos.chunkPosZ);

                if (block.getMaterial() == Material.air && block1.func_149730_j() && rand.nextInt(3) == 0) {
                    world.setBlock(blockpos.chunkPosX, blockpos.chunkPosY, blockpos.chunkPosZ, Blocks.fire);
                }
            }
        }
    }

    private void causeCreeperChainReaction(World world, Vec3 explosionPos) {
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.causeCreeperChainReaction() - Explosion Position: '{}'",
                explosionPos);

        double r = Configs.General.creeper_chain_reaction_max_distance;
        double rSq = r * r;
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                explosionPos.xCoord - r, explosionPos.yCoord - r, explosionPos.zCoord - r,
                explosionPos.xCoord + r, explosionPos.yCoord + r, explosionPos.zCoord + r);
        List<EntityCreeper> list = world.getEntitiesWithinAABB(EntityCreeper.class, bb);

        for (EntityCreeper creeper : list) {
            if (!creeper.isEntityAlive())
                continue;

            if (creeper.getCreeperState() <= 0 && world.rand.nextFloat() < Configs.General.creeper_chain_reaction_chance
                    &&
                    creeper.getDistanceSq(explosionPos.xCoord, explosionPos.yCoord, explosionPos.zCoord) <= rSq) {
                EnvironmentalCreepers.logInfo(
                        "ExplosionEventHandler.causeCreeperChainReaction() - Igniting Creeper: '{}'",
                        creeper.toString());
                creeper.setCreeperState(1);
            }
        }
    }

    private static Vec3 getExplosionPosition(Explosion explosion) {
        return Vec3.createVectorHelper(explosion.explosionX, explosion.explosionY, explosion.explosionZ);
    }
}
