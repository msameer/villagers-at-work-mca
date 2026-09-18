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

import dev.msameer.vaw.api.ArmoryView
import dev.msameer.vaw.api.VawApi
import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.entity.ai.MemoryModuleTypeMCA
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel

/**
 * The guard self-check (Technical Reference §12.2): a guard finds what its kit lacks by reading its
 * own inventory, walks to the nearest armory of its suppliers that holds a piece of it, and takes it.
 * MCA's own equipping then puts it on. If no armory holds what is missing the guard does not walk:
 * its maker is already crafting.
 */
class FetchGearTask : GuardErrand<ArmoryView>() {
    override fun choose(level: ServerLevel, guard: VillagerEntityMCA): ArmoryView? {
        val access = VawApi.armoryAccess ?: return null
        if (guard.inventory.items.none { it.isEmpty }) return null
        val missing = GuardKit.missing(guard, guard.inventory.items)
        if (missing.isEmpty()) return null
        val type = BuiltInRegistries.VILLAGER_PROFESSION.getKey(guard.villagerData.profession().value()).toString()
        return access.armoriesFor(level, type, guard.blockPosition(), RANGE).firstOrNull { armory ->
            armory.contents().any { stack -> missing.any { it.fits(stack) } }
        }
    }

    override fun where(target: ArmoryView): BlockPos = target.pos

    /**
     * Takes one piece for each slot still missing, as far as the armory and the guard's free slots
     * allow. Taking makes MCA equip again, from the inventory, on its next check.
     */
    override fun arrive(guard: VillagerEntityMCA, target: ArmoryView) {
        val inventory = guard.inventory
        var took = false
        for (need in GuardKit.missing(guard, inventory.items)) {
            if (inventory.items.none { it.isEmpty }) break
            val piece = target.takeOne(need.fits)
            if (piece.isEmpty) continue
            inventory.addItem(piece)
            took = true
        }
        if (took) guard.brain.eraseMemory(MemoryModuleTypeMCA.WEARS_ARMOR)
    }
}
