/*
 * Villagers at Work: MCA Reborn extension
 * Copyright (C) 2026 Mohammed Sameer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package dev.msameer.vaw.mca.mixin

import dev.msameer.vaw.mca.GuardLoot
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Marks a mob's loot drop, so a guard's kill can be told apart from every other death. Forwards only. */
@Mixin(LivingEntity::class)
abstract class GuardKillMixin {
    @Inject(method = ["dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V"], at = [At("HEAD")])
    private fun vawBeginGuardLoot(level: ServerLevel, source: DamageSource, playerKilled: Boolean, ci: CallbackInfo) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        GuardLoot.begin((this as Any) as LivingEntity, source)
    }

    @Inject(method = ["dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V"], at = [At("RETURN")])
    private fun vawEndGuardLoot(level: ServerLevel, source: DamageSource, playerKilled: Boolean, ci: CallbackInfo) {
        GuardLoot.end()
    }
}

/** A guard's share of that drop goes to its inventory instead of the ground. Forwards only. */
@Mixin(Entity::class)
abstract class GuardLootDropMixin {
    @Inject(
        method = ["spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;"],
        at = [At("HEAD")],
        cancellable = true,
    )
    private fun vawGuardTakesLoot(level: ServerLevel, stack: ItemStack, offset: Float, ci: CallbackInfoReturnable<ItemEntity?>) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        if (GuardLoot.take((this as Any) as Entity, stack)) ci.returnValue = null
    }
}
