package net.ayoubmrz.battletotemmod.block.entity;

import net.ayoubmrz.battletotemmod.block.custom.BattleTotemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BattleTotemBlockEntity extends BlockEntity {
    private Set<UUID> spawnedMobs = new HashSet<>();
    private boolean isActive = false;
    private int tickCounter = 0;
    private int initialMobCount = 0;
    private int lastMobCount = -1;
    private boolean destroy = false;
    private int destroyDelay = 0;


    public BattleTotemBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTLE_TOTEM, pos, state);
    }

    // Add spawned mobs to tracking list
    public void addSpawnedMobs(List<UUID> mobs) {
        spawnedMobs.addAll(mobs);
        isActive = true;
        markDirty();
    }

    public void setInitialMobCount(int count) {
        this.initialMobCount = count;
        this.lastMobCount = count;
        markDirty();
    }

    public List<UUID> getSpawnedMobs() {
        return new ArrayList<>(spawnedMobs);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BattleTotemBlockEntity blockEntity) {

        if (blockEntity.destroy) {
            blockEntity.destroyDelay++;
        }

        if (world.getBlockState(pos.down()).isOf(Blocks.WATER)) {
            world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
        }

        if (blockEntity.destroyDelay == 30) {

            blockEntity.destroyTotem(world);

            world.playSound(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                    SoundCategory.BLOCKS,
                    2f,
                    0.6f);

        }

        if (world.isClient || !blockEntity.isActive) {
            return;
        }

        blockEntity.tickCounter++;

        if (blockEntity.tickCounter % 5 == 0) {
            blockEntity.checkSpawnedMobs(world, pos, state);
        }
    }

    private void checkSpawnedMobs(World world, BlockPos pos, BlockState state) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Iterator<UUID> iterator = spawnedMobs.iterator();
        while (iterator.hasNext()) {
            UUID mobId = iterator.next();
            Entity entity = serverWorld.getEntity(mobId);

            // Remove from list if entity is dead or doesn't exist
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
            }
        }

        int currentMobCount = spawnedMobs.size();

        // Update texture stage if mob count changed
        if (currentMobCount != lastMobCount && state.getBlock() instanceof BattleTotemBlock totemBlock) {
            totemBlock.updateStage(world, pos, currentMobCount, initialMobCount);
            lastMobCount = currentMobCount;
        }

        // If all mobs are dead, destroy the totem
        if (spawnedMobs.isEmpty() && isActive) {
            if (state.getBlock() instanceof BattleTotemBlock totemBlock) {
                totemBlock.updateStage(world, pos, 0, initialMobCount);
            }

            this.destroy = true;
        }
    }

    private void destroyTotem(World world) {
        BlockPos bottomPos = pos;
        BlockPos topPos = pos.up();

        // Check if this is the top block, adjust positions accordingly
        BlockState currentState = world.getBlockState(pos);
        if (currentState.getBlock().getTranslationKey().contains("top")) {
            bottomPos = pos.down();
            topPos = pos;
        }

        // Destroy both blocks
        world.setBlockState(topPos, Blocks.AIR.getDefaultState());
        world.setBlockState(bottomPos, Blocks.AIR.getDefaultState());

        // Give random chest of loot
        spawnFireworks(world, bottomPos);
        giveLoot(world, bottomPos);

        // Play breaking effects
        world.syncWorldEvent(2001, topPos, Block.getRawIdFromState(currentState));
        world.syncWorldEvent(2001, bottomPos, Block.getRawIdFromState(currentState));

        isActive = false;
    }


    private void giveLoot(World world, BlockPos pos) {
        if (world.isClient) return;

        BlockPos chestPos = pos;

        world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());

        BlockEntity blockEntity = world.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chestEntity) {
            // Base items
            List<Item> possibleItems = Arrays.asList(
                    Items.DIAMOND,
                    Items.GOLD_INGOT,
                    Items.IRON_INGOT,
                    Items.EMERALD,
                    Items.GOLDEN_APPLE,
                    Items.ENCHANTED_GOLDEN_APPLE,
                    Items.ENDER_PEARL,
                    Items.BLAZE_ROD,
                    Items.EXPERIENCE_BOTTLE,
                    Items.SADDLE,
                    Items.NAME_TAG,
                    Items.BREAD,
                    Items.ARROW
            );

            // Randomly fill slots with loot
            int numItems = world.random.nextInt(16) + 8;
            Set<Integer> usedSlots = new HashSet<>();

            for (int i = 0; i < numItems; i++) {
                int slot;
                do {
                    slot = world.random.nextInt(27);
                } while (usedSlots.contains(slot));

                usedSlots.add(slot);

                // Select random item and create ItemStack with random amount each time
                Item selectedItem = possibleItems.get(world.random.nextInt(possibleItems.size()));
                ItemStack lootItem = createRandomItemStack(world, selectedItem);

                chestEntity.setStack(slot, lootItem);
            }

            chestEntity.markDirty();
        }
    }

    private ItemStack createRandomItemStack(World world, Item item) {
        int amount;

        if (item == Items.DIAMOND) {
            amount = world.random.nextInt(8) + 1;
        } else if (item == Items.GOLD_INGOT) {
            amount = world.random.nextInt(12) + 1;
        } else if (item == Items.IRON_INGOT) {
            amount = world.random.nextInt(14) + 1;
        } else if (item == Items.EMERALD) {
            amount = world.random.nextInt(9) + 1;
        } else if (item == Items.GOLDEN_APPLE) {
            amount = world.random.nextInt(5) + 1;
        } else if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            amount = world.random.nextInt(2);
        } else if (item == Items.ENDER_PEARL) {
            amount = world.random.nextInt(6) + 1;
        } else if (item == Items.BLAZE_ROD) {
            amount = world.random.nextInt(4) + 1;
        } else if (item == Items.EXPERIENCE_BOTTLE) {
            amount = world.random.nextInt(5) + 1;
        } else if (item == Items.BREAD) {
            amount = world.random.nextInt(24) + 1;
        } else if (item == Items.ARROW) {
            amount = world.random.nextInt(32) + 1;
        } else {
            // For items like SADDLE and NAME_TAG
            amount = 1;
        }

        return new ItemStack(item, amount);
    }

    private void spawnFireworks(World world, BlockPos pos) {
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            double x = pos.getX() + 0.5;
            double y = pos.getY() + 5;
            double z = pos.getZ() + 0.5;

            // Create colorful explosion particles
            for (int i = 0; i < 50; i++) {
                double velocityX = (world.random.nextDouble() - 0.5) * 2.0;
                double velocityY = (world.random.nextDouble() - 0.5) * 2.0;
                double velocityZ = (world.random.nextDouble() - 0.5) * 2.0;

                serverWorld.spawnParticles(ParticleTypes.FIREWORK,
                        x, y, z, 5,
                        velocityX, velocityY, velocityZ, 0.1);
            }

            for (int i = 0; i < 30; i++) {
                double velocityX = (world.random.nextDouble() - 0.5) * 1.5;
                double velocityY = (world.random.nextDouble() - 0.5) * 1.5;
                double velocityZ = (world.random.nextDouble() - 0.5) * 1.5;

                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        x, y, z, 5,
                        velocityX, velocityY, velocityZ, 0.05);
            }

        }
    }

    // Save data to NBT
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        NbtList mobsList = new NbtList();
        for (UUID uuid : spawnedMobs) {
            mobsList.add(NbtString.of(uuid.toString()));
        }
        nbt.put("SpawnedMobs", mobsList);
        nbt.putBoolean("IsActive", isActive);
        nbt.putInt("InitialMobCount", initialMobCount);
        nbt.putInt("LastMobCount", lastMobCount);
    }

    // Load data from NBT
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        spawnedMobs.clear();
        NbtList mobsList = nbt.getList("SpawnedMobs", 8);
        for (int i = 0; i < mobsList.size(); i++) {
            spawnedMobs.add(UUID.fromString(mobsList.getString(i)));
        }
        isActive = nbt.getBoolean("IsActive");
        initialMobCount = nbt.getInt("InitialMobCount");
        lastMobCount = nbt.getInt("LastMobCount");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}