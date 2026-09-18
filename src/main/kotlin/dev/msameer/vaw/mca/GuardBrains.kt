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

/** Adds a guard's errands to its brain, beside MCA's own guard tasks: fetching gear (§12.2) and bringing in loot (§16.1). */
object GuardBrains {
    fun install(villager: VillagerEntityMCA, brain: Brain<*>) {
        if (!villager.isGuard) return
        @Suppress("UNCHECKED_CAST")
        (brain as Brain<VillagerEntityMCA>).addActivity(
            Activity.CORE,
            ImmutableList.of(Pair.of(GEAR, FetchGearTask()), Pair.of(LOOT, DeliverLootTask())),
            emptySet(),
            emptySet(),
        )
    }

    /** Gear first: after MCA's own equipping (priority 1), before its attack tasks settle a target. */
    private const val GEAR = 1

    /** Loot after gear, so an unarmed guard arms itself before running errands. */
    private const val LOOT = 2
}
