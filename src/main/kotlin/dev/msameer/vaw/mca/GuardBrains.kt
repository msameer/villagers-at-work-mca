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
package dev.msameer.vaw.mca

import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.world.entity.ai.Brain
import net.minecraft.world.entity.schedule.Activity

/** Adds the guard self-check to a guard's brain, beside MCA's own guard tasks (§12.2). */
object GuardBrains {
    fun install(villager: VillagerEntityMCA, brain: Brain<*>) {
        if (!villager.isGuard) return
        @Suppress("UNCHECKED_CAST")
        (brain as Brain<VillagerEntityMCA>).addActivity(Activity.CORE, ImmutableList.of(Pair.of(PRIORITY, FetchGearTask())), emptySet(), emptySet())
    }

    /** After MCA's own equipping (priority 1), before its attack tasks settle a target. */
    private const val PRIORITY = 1
}
