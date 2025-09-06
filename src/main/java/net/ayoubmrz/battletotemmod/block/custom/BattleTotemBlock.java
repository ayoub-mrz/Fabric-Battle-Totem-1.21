package net.ayoubmrz.battletotemmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.ayoubmrz.battletotemmod.block.ModBlocks;
import net.ayoubmrz.battletotemmod.block.entity.BattleTotemBlockEntity;
import net.ayoubmrz.battletotemmod.block.entity.ModBlockEntities;
import net.ayoubmrz.battletotemmod.event.SpawnMobs;
import net.ayoubmrz.battletotemmod.sound.ModSounds;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BattleTotemBlock extends HorizontalFacingBlock implements BlockEntityProvider {

    public static final BooleanProperty USED = BooleanProperty.of("used");
    public static final IntProperty STAGE = IntProperty.of("stage", 0, 3); // 0 = normal, 1-3 = damage stages

    public static final MapCodec<BattleTotemBlock> CODEC = createCodec(BattleTotemBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public BattleTotemBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(USED, false)
                .with(STAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, USED, STAGE);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!state.get(USED) && isTotem(world, pos)) {

            world.playSound(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    SoundCategory.BLOCKS,
                    1f,
                    1.6f);
            world.playSound(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                    SoundCategory.BLOCKS,
                    1f,
                    1.6f);

            // Spawn mobs and track them
            List<UUID> mobsList = new ArrayList<>();
            SpawnMobs.spawnMobs(world, pos, mobsList);

            // Store mob UUIDs in the block entity
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BattleTotemBlockEntity totemEntity) {
                totemEntity.addSpawnedMobs(mobsList);
                totemEntity.setInitialMobCount(mobsList.size());
            }

            // Also check if the other half has a block entity and update it
            BlockPos otherPos = state.isOf(ModBlocks.BATTLE_TOTEM_TOP) ? pos.down() : pos.up();
            BlockEntity otherBlockEntity = world.getBlockEntity(otherPos);
            if (otherBlockEntity instanceof BattleTotemBlockEntity otherTotemEntity) {
                otherTotemEntity.addSpawnedMobs(mobsList);
                otherTotemEntity.setInitialMobCount(mobsList.size());
            }

            if (state.isOf(ModBlocks.BATTLE_TOTEM_TOP)) {
                world.setBlockState(pos, state.with(USED, true));
                world.setBlockState(pos.down(), world.getBlockState(pos.down()).with(USED, true));
            } else if (state.isOf(ModBlocks.BATTLE_TOTEM_BOTTOM)) {
                world.setBlockState(pos, state.with(USED, true));
                world.setBlockState(pos.up(), world.getBlockState(pos.up()).with(USED, true));
            }

            return ActionResult.SUCCESS;
        } else {
            giveMobsGlowEffect(world, pos);
        }
        return ActionResult.PASS;
    }

    private boolean isTotem(World world, BlockPos pos) {

        if (world.getBlockState(pos).isOf(ModBlocks.BATTLE_TOTEM_TOP)) {
            if (world.getBlockState(pos.down()).isOf(ModBlocks.BATTLE_TOTEM_BOTTOM)) {
                return true;
            }
        } else if (world.getBlockState(pos).isOf(ModBlocks.BATTLE_TOTEM_BOTTOM)) {
            if (world.getBlockState(pos.up()).isOf(ModBlocks.BATTLE_TOTEM_TOP)) {
                return true;
            }
        }

        return false;
    }

    private void giveMobsGlowEffect(World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BattleTotemBlockEntity totemEntity) {
            List<UUID> mobsList = totemEntity.getSpawnedMobs();

            if (world instanceof ServerWorld serverWorld) {
                for (UUID mobUUID : mobsList) {
                    Entity entity = serverWorld.getEntity(mobUUID);
                    if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 200, 0));
                    }
                }
            }
        }
    }

    public void updateStage(World world, BlockPos pos, int remainingMobs, int initialMobs) {
        if (initialMobs <= 0) return;

        BlockState state = world.getBlockState(pos);
        if (!state.isOf(this)) return;

        int newStage;
        float ratio = (float) remainingMobs / initialMobs;

        if (remainingMobs == 3) {
            newStage = 0;
        } else if (remainingMobs == 2) {
            newStage = 1;
        } else if (remainingMobs == 1) {
            newStage = 2;
        } else {
            newStage = 3;
        }

        if (state.get(STAGE) != newStage) {
            world.setBlockState(pos, state.with(STAGE, newStage), Block.NOTIFY_ALL);

            world.playSound(null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE,
                    SoundCategory.BLOCKS,
                    3f,
                    0.6f + ((float) newStage / 10));

            // Update the other half
            BlockPos otherPos = state.isOf(ModBlocks.BATTLE_TOTEM_TOP) ? pos.down() : pos.up();
            BlockState otherState = world.getBlockState(otherPos);
            if ((otherState.isOf(ModBlocks.BATTLE_TOTEM_TOP) || otherState.isOf(ModBlocks.BATTLE_TOTEM_BOTTOM))
                    && otherState.get(STAGE) != newStage) {
                world.setBlockState(otherPos, otherState.with(STAGE, newStage), Block.NOTIFY_ALL);
            }
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), ModBlocks.BATTLE_TOTEM_TOP.getDefaultState());
        world.setBlockState(pos, ModBlocks.BATTLE_TOTEM_BOTTOM.getDefaultState());
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {

        if (state.isOf(ModBlocks.BATTLE_TOTEM_TOP)) {
            world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
        } else if (state.isOf(ModBlocks.BATTLE_TOTEM_BOTTOM)) {
            world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState aboveState = world.getBlockState(pos.up());

        if (!aboveState.isAir() && !aboveState.isOf(Blocks.WATER) && !aboveState.isOf(Blocks.LAVA)) {
            return false;
        }

        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BattleTotemBlockEntity(ModBlockEntities.BATTLE_TOTEM, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.BATTLE_TOTEM, BattleTotemBlockEntity::tick);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> validateTicker(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }
}