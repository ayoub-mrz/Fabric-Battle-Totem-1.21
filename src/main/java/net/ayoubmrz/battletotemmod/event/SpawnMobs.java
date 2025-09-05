package net.ayoubmrz.battletotemmod.event;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpawnMobs {

    public static void spawnMobs(World world, BlockPos pos, List<UUID> mobsList) {
        int[][] offsets = {{2, 2}, {-2, -2}, {-2, 2}};

        for (int i = 0; i < 3; i++) {
            ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);

            zombie.setPosition(
                    pos.getX() + offsets[i][0],
                    pos.getY() + 1,
                    pos.getZ() + offsets[i][1]
            );

            world.spawnEntity(zombie);
            mobsList.add(zombie.getUuid());

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
}