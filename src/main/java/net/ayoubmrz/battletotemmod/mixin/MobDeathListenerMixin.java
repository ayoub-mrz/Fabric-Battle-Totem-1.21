package net.ayoubmrz.battletotemmod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class MobDeathListenerMixin {

	@Inject(method = "onDeath", at = @At("TAIL"))
	private void onDeath(DamageSource damageSource, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		System.out.println("UUID : " + entity.getUuid());

//		for (UUID mobs : mobsList) {
//			if (entity.getUuid().equals(mobs)) {
//				System.out.println("yes");
//			}
//		}
	}

}