package de.melanx.ultimatools.util;

import com.google.common.collect.ImmutableList;
import de.melanx.ultimatools.lib.Function5;
import de.melanx.ultimatools.lib.ListHandlers;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ToolEffects {

    private ToolEffects() {

    }

    public static boolean placeWater(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        BlockPos target = pos.offset(face);
        if (!world.getBlockState(target).isReplaceable(Fluids.WATER)
                && !world.getBlockState(target).hasProperty(BlockStateProperties.WATERLOGGED))
            return false;
        if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;

        if (!world.getBlockState(target).hasProperty(BlockStateProperties.WATERLOGGED)) {
            world.destroyBlock(target, true);
            world.setBlockState(target, Fluids.WATER.getDefaultState().getBlockState());
        } else {
            world.setBlockState(target, world.getBlockState(target).with(BlockStateProperties.WATERLOGGED, true));
        }

        return true;
    }

    public static boolean spawnAnimal(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        BlockPos target = pos.offset(face);
        if (world.getBlockState(target).isSolid())
            return false;
        if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;

        EntityType<?> entityType;
        if (world.getBlockState(target).getFluidState().getFluid() == Fluids.WATER
                || world.getBlockState(target).getFluidState().getFluid() == Fluids.FLOWING_WATER) {
            entityType = ListHandlers.WATER_ANIMALS.get(world.rand.nextInt(ListHandlers.WATER_ANIMALS.size()));
        } else {
            entityType = ListHandlers.ANIMALS.get(world.rand.nextInt(ListHandlers.ANIMALS.size()));
        }

        MobEntity entity = (MobEntity) entityType.create(world);
        if (entity == null)
            return false;
        entity.setLocationAndAngles(target.getX() + 0.5, target.getY() + 0.1, target.getZ() + 0.5, player.getRotationYawHead() - 180, 0);
        if (world instanceof ServerWorld) {
            entity.onInitialSpawn((ServerWorld) world, world.getDifficultyForLocation(target), SpawnReason.TRIGGERED, null, null);
        }
        if (entity instanceof AnimalEntity) {
            ((AnimalEntity) entity).setGrowingAge(-24000);
        }
        world.addEntity(entity);

        return true;
    }

    public static boolean applyMagicDamage(LivingEntity target, PlayerEntity player) {
        if (target.isAlive()) {
            target.attackEntityFrom(new EntityDamageSource(DamageSource.MAGIC.getDamageType(), player).setDamageBypassesArmor().setMagicDamage(), 60);
            return true;
        }
        return false;
    }

    public static boolean useBonemeal(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;

        return BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), world, pos, player);
    }

    public static boolean upgradeOre(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;

        Block block = world.getBlockState(pos).getBlock();
        List<Block> ORES = ImmutableList.copyOf(ListHandlers.ORES);
        for (int i = 0; i < ORES.size() - 1; i++) {
            if (block == ORES.get(i)) {
                BlockState newState = ORES.get(i + 1).getDefaultState();
                SoundType sound = newState.getSoundType();
                world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                world.setBlockState(pos, newState);
            }
        }

        List<Block> ORES_NETHER = ImmutableList.copyOf(ListHandlers.NETHER_ORES);
        for (int i = 0; i < ORES_NETHER.size() - 1; i++) {
            if (block == ORES_NETHER.get(i)) {
                BlockState newState = ORES_NETHER.get(i + 1).getDefaultState();
                SoundType sound = newState.getSoundType();
                world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                world.setBlockState(pos, newState);
            }
        }

        return false;
    }

    public static Function5<World, PlayerEntity, Hand, BlockPos, Direction, Boolean> changeBlock(Block from, Block to) {
        return changeBlock(Collections.singleton(from), to.getDefaultState());
    }

    public static Function5<World, PlayerEntity, Hand, BlockPos, Direction, Boolean> changeBlock(Block from, BlockState to) {
        return changeBlock(Collections.singleton(from), to);
    }

    public static Function5<World, PlayerEntity, Hand, BlockPos, Direction, Boolean> changeBlock(Set<Block> from, Block to) {
        return changeBlock(from, to.getDefaultState());
    }

    public static Function5<World, PlayerEntity, Hand, BlockPos, Direction, Boolean> changeBlock(Set<Block> from, BlockState to) {
        return (World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) -> {
            if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
                return false;

            if (!from.contains(world.getBlockState(pos).getBlock()))
                return false;

            SoundType sound = to.getSoundType();
            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            world.setBlockState(pos, to);

            return true;
        };
    }

    public static boolean applyPotion(LivingEntity target, PlayerEntity player) {
        if (target.isAlive()) {
            switch (player.getEntityWorld().rand.nextInt(5)) {
                case 0:
                    target.attackEntityFrom(new EntityDamageSource(DamageSource.MAGIC.getDamageType(), player).setDamageBypassesArmor().setMagicDamage(), 10);
                    break;
                case 1:
                    target.addPotionEffect(new EffectInstance(Effects.POISON, 600));
                    break;
                case 2:
                    target.addPotionEffect(new EffectInstance(Effects.WITHER, 600));
                    break;
                case 3:
                    target.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 600));
                    break;
                default:
                    target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 600, 3));
                    break;
            }
            return true;
        }
        return false;
    }

    public static boolean generateOre(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        if (!player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;

        if (Tags.Blocks.COBBLESTONE.contains(world.getBlockState(pos).getBlock())
        || Tags.Blocks.STONE.contains(world.getBlockState(pos).getBlock())) {

            Block block = Tags.Blocks.ORES.getRandomElement(world.rand);
            BlockState state = block.getDefaultState();
            SoundType sound = state.getSoundType();
            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            world.setBlockState(pos, state);

            return true;
        }

        return false;
    }

    public static boolean ultimate(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        Block block = world.getBlockState(pos).getBlock();
        if (!Tags.Blocks.DIRT.contains(block) && block != Blocks.GRASS_BLOCK) {
            if (player.isSneaking()) {
                return placeWater(world, player, hand, pos, face);
            }
        } else if (!player.isSneaking()) {
            if (Tags.Blocks.DIRT.contains(block)) {
                BlockState newState = Blocks.GRASS_BLOCK.getDefaultState();
                SoundType sound = newState.getSoundType();
                world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                world.setBlockState(pos, newState);
                return true;
            } else if (Tags.Blocks.COBBLESTONE.contains(block) || Tags.Blocks.STONE.contains(block)) {
                return generateOre(world, player, hand, pos, face);
            }
        } else {
            if (Tags.Blocks.DIRT.contains(block) || block == Blocks.GRASS_BLOCK || world.getBlockState(pos.offset(face)).getBlock() == Blocks.WATER) {
                return spawnAnimal(world, player, hand, pos, face);
            } else {
                return useBonemeal(world, player, hand, pos, face);
            }
        }
        return false;
    }

    public static boolean applyRegeneration(World world, PlayerEntity player, Hand hand) {
        player.addPotionEffect(new EffectInstance(Effects.REGENERATION, 100, 1));
        return true;
    }

    public static boolean applyLevitation(World world, PlayerEntity player, Hand hand) {
        player.addPotionEffect(new EffectInstance(Effects.LEVITATION, 40, 9));
        return true;
    }

    public static boolean removeFluid(World world, PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
        if (player.canPlayerEdit(pos, face, player.getHeldItem(hand))) {
            BlockPos target = pos.offset(face);
            BlockState state = world.getBlockState(target);
            if (state.getBlock() instanceof IBucketPickupHandler) {
                Fluid fluid = ((IBucketPickupHandler) state.getBlock()).pickupFluid(world, target, state);
                if (fluid != Fluids.EMPTY) {
                    player.playSound(fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);

                    for (int x = 0; x < 5; ++x) {
                        world.addParticle(ParticleTypes.POOF, pos.getX() + world.rand.nextDouble(), pos.getY() + world.rand.nextDouble(), pos.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
                    }

                    return true;
                }
            }
        }
        return false;
    }
}
