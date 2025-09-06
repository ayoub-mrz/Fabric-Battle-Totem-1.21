package net.ayoubmrz.battletotemmod.event;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SpawnMobs {

    public static void spawnMobs(World world, BlockPos pos, List<UUID> mobsList) {

        String[] entityTypes = {"zombie", "skeleton", "zombie_villager", "husk", "stray", "pillager"};
        Random random = new Random();
        String chosenEntity = entityTypes[random.nextInt(entityTypes.length)];

        int[][] offsets = {{2, 2}, {-2, -2}, {-2, 2}};

        for (int i = 0; i < 3; i++) {

            // Create the appropriate mob based on chosen type
            MobEntity entity = createMobByType(chosenEntity, world);

            if (entity == null) continue;

            // Adjust max health
            entity.setHealth(85);

            int x = pos.getX() + offsets[i][0];
            int y = pos.getY();
            int z = pos.getZ() + offsets[i][1];

            int yPlus = 1;

            while (!world.getBlockState(new BlockPos(x, y + yPlus, z)).isOf(Blocks.AIR)) {
                yPlus++;
                if (yPlus > 50) break; // Prevent searching too high
            }

            entity.setPosition(x, y + yPlus, z);

            // Get enchantments from registry
            RegistryWrapper.WrapperLookup registryLookup = world.getRegistryManager();
            RegistryWrapper<Enchantment> enchantmentRegistry = registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

            // Equipment the mob based on its type
            equipMob(entity, enchantmentRegistry, chosenEntity);

            world.spawnEntity(entity);
            mobsList.add(entity.getUuid());

            // Spawn particles
            for (int j = 0; j < 20; j++) {
                if (!world.isClient) {
                    ((ServerWorld) world).spawnParticles(
                            ParticleTypes.SONIC_BOOM,
                            pos.getX() + offsets[i][0], pos.getY() + 1, pos.getZ() + offsets[i][1],
                            10, 0, 0, 0, 0.1
                    );
                }
            }
        }
    }

    private static MobEntity createMobByType(String entityType, World world) {
        switch (entityType.toLowerCase()) {
            case "zombie":
                return new ZombieEntity(EntityType.ZOMBIE, world);
            case "skeleton":
                return new SkeletonEntity(EntityType.SKELETON, world);
            case "husk":
                return new HuskEntity(EntityType.HUSK, world);
            case "stray":
                return new StrayEntity(EntityType.STRAY, world);
            case "zombie_villager":
                return new ZombieVillagerEntity(EntityType.ZOMBIE_VILLAGER, world);
            case "pillager":
                return new PillagerEntity(EntityType.PILLAGER, world);
            default:
                return new ZombieEntity(EntityType.ZOMBIE, world);
        }
    }

    private static void equipMob(MobEntity entity, RegistryWrapper<Enchantment> enchantmentRegistry, String mobType) {
        // Create enchanted armor
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);

        // Add enchantments to armor pieces
        helmet.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 4);
        helmet.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);

        chestplate.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 4);
        chestplate.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.THORNS), 3);
        chestplate.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);

        leggings.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 4);
        leggings.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);

        boots.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PROTECTION), 4);
        boots.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.FEATHER_FALLING), 4);
        boots.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);

        // Choose weapon based on mob type
        ItemStack weapon = getWeaponForMob(mobType, enchantmentRegistry);

        // Equip armor and weapon
        entity.equipStack(EquipmentSlot.HEAD, helmet);
        entity.equipStack(EquipmentSlot.CHEST, chestplate);
        entity.equipStack(EquipmentSlot.LEGS, leggings);
        entity.equipStack(EquipmentSlot.FEET, boots);
        entity.equipStack(EquipmentSlot.MAINHAND, weapon);

        // Set drop chances -> 0
        entity.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
        entity.setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f);
        entity.setEquipmentDropChance(EquipmentSlot.LEGS, 0.0f);
        entity.setEquipmentDropChance(EquipmentSlot.FEET, 0.0f);
        entity.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    private static ItemStack getWeaponForMob(String mobType, RegistryWrapper<Enchantment> enchantmentRegistry) {
        ItemStack weapon;

        switch (mobType.toLowerCase()) {
            case "skeleton":
            case "stray":
                weapon = new ItemStack(Items.BOW);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.POWER), 5);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PUNCH), 2);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.INFINITY), 1);
                break;
            case "pillager":
                weapon = new ItemStack(Items.CROSSBOW);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.PIERCING), 4);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.QUICK_CHARGE), 3);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
                break;
            case "zombie_villager":
            case "zombie":
            case "husk":
            default:
                weapon = new ItemStack(Items.NETHERITE_SWORD);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS), 5);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.KNOCKBACK), 2);
                weapon.addEnchantment(enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
                break;
        }

        return weapon;
    }
}