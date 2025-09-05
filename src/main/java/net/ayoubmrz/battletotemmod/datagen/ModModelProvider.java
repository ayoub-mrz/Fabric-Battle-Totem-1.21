package net.ayoubmrz.battletotemmod.datagen;

import net.ayoubmrz.battletotemmod.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(ModBlocks.BATTLE_TOTEM);
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(ModBlocks.BATTLE_TOTEM_TOP);
        blockStateModelGenerator.registerNorthDefaultHorizontalRotation(ModBlocks.BATTLE_TOTEM_BOTTOM);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {



    }
}
