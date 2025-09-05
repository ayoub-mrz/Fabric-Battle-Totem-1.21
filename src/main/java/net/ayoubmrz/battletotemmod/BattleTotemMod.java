package net.ayoubmrz.battletotemmod;

import net.ayoubmrz.battletotemmod.block.ModBlocks;
import net.ayoubmrz.battletotemmod.block.entity.ModBlockEntities;
import net.ayoubmrz.battletotemmod.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

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
	}
}