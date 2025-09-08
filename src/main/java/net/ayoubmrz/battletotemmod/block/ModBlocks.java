package net.ayoubmrz.battletotemmod.block;

import net.ayoubmrz.battletotemmod.BattleTotemMod;
import net.ayoubmrz.battletotemmod.block.custom.BattleTotemBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {

    public static final Block BATTLE_TOTEM = registerBlock("battle_totem",
            properties -> new BattleTotemBlock(properties.nonOpaque()
                    .strength(-1.0F, 3600000.0F).dropsNothing()));

    public static final Block BATTLE_TOTEM_TOP = registerBlock("battle_totem_top",
            properties -> new BattleTotemBlock(properties.nonOpaque()
                    .strength(-1.0F, 3600000.0F).dropsNothing()));
    public static final Block BATTLE_TOTEM_BOTTOM = registerBlock("battle_totem_bottom",
            properties -> new BattleTotemBlock(properties.nonOpaque()
                    .strength(-1.0F, 3600000.0F).dropsNothing()));

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> function) {
        Block toRegister = function.apply(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(BattleTotemMod.MOD_ID, name))));
        registerBlockItem(name, toRegister);
        return Registry.register(Registries.BLOCK, Identifier.of(BattleTotemMod.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(BattleTotemMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey().maxCount(1)
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(BattleTotemMod.MOD_ID, name)))));
    }

    public static void registerModBlocks() {
        BattleTotemMod.LOGGER.info("Registering Mod Blocks For " + BattleTotemMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(BATTLE_TOTEM);
        });

    }

}
