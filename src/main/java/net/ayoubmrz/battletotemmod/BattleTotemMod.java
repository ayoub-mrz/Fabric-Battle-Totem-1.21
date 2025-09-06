package net.ayoubmrz.battletotemmod;

import net.ayoubmrz.battletotemmod.block.ModBlocks;
import net.ayoubmrz.battletotemmod.block.entity.ModBlockEntities;
import net.ayoubmrz.battletotemmod.sound.ModSounds;
import net.ayoubmrz.battletotemmod.world.gen.ModWorldGeneration;
import net.fabricmc.api.ModInitializer;

import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BattleTotemMod implements ModInitializer {
	public static final String MOD_ID = "battletotemmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModSounds.registerSounds();
		ModBlockEntities.registerBlockEntities();
		ModWorldGeneration.generateModWorldGen();
	}
}