package net.ayoubmrz.battletotemmod.block.entity;

import net.ayoubmrz.battletotemmod.block.custom.BattleTotemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
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

    public BattleTotemBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

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

    public static void tick(World world, BlockPos pos, BlockState state, BattleTotemBlockEntity blockEntity) {

        if (blockEntity.destroy) {
            blockEntity.destroyDelay++;
        }

        if (blockEntity.destroyDelay == 60) {
            blockEntity.destroyTotem(world);
        }

        if (world.isClient || !blockEntity.isActive) {
            return;
        }

        blockEntity.tickCounter++;

        if (blockEntity.tickCounter % 20 == 0) {
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

        // Play breaking effects
        world.syncWorldEvent(2001, topPos, Block.getRawIdFromState(currentState));
        world.syncWorldEvent(2001, bottomPos, Block.getRawIdFromState(currentState));

        isActive = false;
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