package net.ayoubmrz.battletotemmod.block.entity;

import net.ayoubmrz.battletotemmod.BattleTotemMod;
import net.ayoubmrz.battletotemmod.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<BattleTotemBlockEntity> BATTLE_TOTEM;

    public static void registerBlockEntities() {
        BATTLE_TOTEM = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(BattleTotemMod.MOD_ID, "battle_totem"),
                FabricBlockEntityTypeBuilder.create(
                        BattleTotemBlockEntity::new,
                        ModBlocks.BATTLE_TOTEM,
                        ModBlocks.BATTLE_TOTEM_TOP,
                        ModBlocks.BATTLE_TOTEM_BOTTOM
                ).build()
        );
    }
}