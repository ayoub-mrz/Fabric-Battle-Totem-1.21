package net.ayoubmrz.battletotemmod.sound;

import net.ayoubmrz.battletotemmod.BattleTotemMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent TOTEM_CHARGE = registerSoundEvent("totem_charge");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(BattleTotemMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        BattleTotemMod.LOGGER.info("Registering Mod Sounds for " + BattleTotemMod.MOD_ID);
    }
}