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

import dev.msameer.vaw.mca.GuardEquipment
import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask
import net.minecraft.server.level.ServerLevel
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/** After MCA equips a villager, its guards give back what was made from nothing. Forwards only. */
@Mixin(EquipmentTask::class)
abstract class EquipmentTaskMixin {
    @Inject(
        method = ["start(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;J)V"],
        at = [At("RETURN")],
    )
    private fun vawEquipOnlyWhatIsCarried(level: ServerLevel, villager: VillagerEntityMCA, time: Long, ci: CallbackInfo) {
        GuardEquipment.afterMcaEquipped(villager)
    }
}
