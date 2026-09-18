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

import dev.msameer.vaw.api.ShelfOrder
import dev.msameer.vaw.api.VawApi
import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

/**
 * WIKI: *Cleric* — "MCA Guards: rotten flesh from looting kills". A guard carrying loot some shelf
 * has ordered walks it there, through the core's own order view: shelves holding none first, then
 * top-ups (§7.6). Loot nobody ordered stays in the guard's inventory until somebody does.
 */
class DeliverLootTask : GuardErrand<ShelfOrder>() {
    override fun choose(level: ServerLevel, guard: VillagerEntityMCA): ShelfOrder? {
        val delivery = VawApi.delivery ?: return null
        if (guard.inventory.countItem(GuardLoot.LOOT) == 0) return null
        return delivery.ordersFor(level, GuardLoot.LOOT, guard.blockPosition(), RANGE).firstOrNull()
    }

    override fun where(target: ShelfOrder): BlockPos = target.shelf

    override fun arrive(guard: VillagerEntityMCA, target: ShelfOrder) {
        val inventory = guard.inventory
        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            if (!stack.`is`(GuardLoot.LOOT)) continue
            if (target.deliver(stack) == 0) break
        }
        inventory.setChanged()
    }
}
