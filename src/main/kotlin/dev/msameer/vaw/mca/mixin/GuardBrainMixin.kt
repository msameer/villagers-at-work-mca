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

import dev.msameer.vaw.mca.GuardBrains
import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.npc.villager.Villager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/**
 * MCA builds a villager's brain in two places, both ending in `VillagerTasksMCA.initializeTasks`,
 * which is where a guard gets its guard tasks. The self-check joins them there. Forwards only.
 */
@Mixin(VillagerEntityMCA::class)
abstract class GuardBrainMixin {
    @Inject(method = ["makeBrain"], at = [At("RETURN")])
    private fun vawAddGuardTasks(packed: Brain.Packed, cir: CallbackInfoReturnable<Brain<Villager>>) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        GuardBrains.install((this as Any) as VillagerEntityMCA, cir.returnValue)
    }

    @Inject(method = ["refreshBrain"], at = [At("RETURN")])
    private fun vawAddGuardTasksAgain(level: ServerLevel, ci: CallbackInfo) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val villager = (this as Any) as VillagerEntityMCA
        GuardBrains.install(villager, villager.mcaBrain)
    }
}
