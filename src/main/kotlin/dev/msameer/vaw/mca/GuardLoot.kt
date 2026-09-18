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

import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * A guard loots its kills (WIKI: *Cleric*): the rotten flesh a mob drops when a guard kills it goes
 * straight into the guard's inventory, for the cleric. Every other drop, and every other death,
 * lands exactly as vanilla wrote it.
 *
 * A guard carries at most [CARRIED] of it, so a village without a cleric does not fill its guards'
 * inventories with flesh; past that it drops as usual. An archer's kill counts as the archer's.
 */
object GuardLoot {
    /** What a guard keeps from its kills. */
    val LOOT: Item = Items.ROTTEN_FLESH

    /** The most a guard carries, one stack. */
    const val CARRIED = 64

    private var dying: LivingEntity? = null
    private var looter: VillagerEntityMCA? = null

    /** A mob's loot is about to drop. */
    fun begin(entity: LivingEntity, source: DamageSource) {
        val killer = source.entity as? VillagerEntityMCA
        if (killer != null && killer.isGuard && killer.isAlive) {
            dying = entity
            looter = killer
        } else {
            end()
        }
    }

    fun end() {
        dying = null
        looter = null
    }

    /**
     * Takes what the guard keeps out of [stack], a drop [entity] is about to spawn. Returns whether
     * nothing is left to spawn.
     */
    fun take(entity: Entity, stack: ItemStack): Boolean {
        val guard = looter ?: return false
        if (entity !== dying || !stack.`is`(LOOT)) return false
        val room = CARRIED - guard.inventory.countItem(LOOT)
        if (room <= 0) return false
        val left = guard.inventory.addItem(stack.split(minOf(room, stack.count)))
        stack.grow(left.count)
        return stack.isEmpty
    }
}
