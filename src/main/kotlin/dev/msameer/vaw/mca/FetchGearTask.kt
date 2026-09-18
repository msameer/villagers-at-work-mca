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
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.Vec3

/**
 * The guard self-check (Technical Reference §12.2): a guard finds what its kit lacks by reading its
 * own inventory, walks to the nearest armory that holds a piece of it, and takes it. MCA's own
 * equipping then puts it on. A guard in a fight never goes: combat comes first.
 *
 * **Nothing is remembered between checks** (I1). If no armory holds what is missing the guard does
 * not walk — its maker is already crafting — and it asks again after [CHECK_INTERVAL]. If the piece
 * is gone when it arrives, it goes back to its duties and asks again later (I3).
 */
class FetchGearTask : Behavior<VillagerEntityMCA>(mapOf(MemoryModuleType.ATTACK_TARGET to MemoryStatus.VALUE_ABSENT), MAX_TICKS) {
    private var nextCheck = 0L
    private var armory: ArmoryView? = null
    private var done = false

    override fun checkExtraStartConditions(level: ServerLevel, guard: VillagerEntityMCA): Boolean {
        if (level.gameTime < nextCheck) return false
        nextCheck = level.gameTime + CHECK_INTERVAL
        if (!guard.isGuard || guard.isSleeping) return false
        armory = choose(level, guard)
        return armory != null
    }

    override fun start(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        done = false
        walk(guard)
    }

    override fun canStillUse(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long): Boolean =
        !done && armory != null && !guard.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)

    override fun tick(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        val target = armory ?: return
        if (guard.position().distanceTo(Vec3.atCenterOf(target.pos)) <= REACH) {
            collect(guard, target)
            done = true
        } else {
            walk(guard)
        }
    }

    override fun stop(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        armory = null
    }

    /** The nearest armory of the guard's suppliers that holds a piece its kit lacks. */
    private fun choose(level: ServerLevel, guard: VillagerEntityMCA): ArmoryView? {
        val access = VawApi.armoryAccess ?: return null
        if (guard.inventory.items.none { it.isEmpty }) return null
        val missing = GuardKit.missing(guard, guard.inventory.items)
        if (missing.isEmpty()) return null
        val type = BuiltInRegistries.VILLAGER_PROFESSION.getKey(guard.villagerData.profession().value()).toString()
        return access.armoriesFor(level, type, guard.blockPosition(), RANGE).firstOrNull { armory ->
            armory.contents().any { stack -> missing.any { it.fits(stack) } }
        }
    }

    /**
     * Takes one piece for each slot still missing, as far as the armory and the guard's free slots
     * allow. Taking makes MCA equip again, from the inventory, on its next check.
     */
    private fun collect(guard: VillagerEntityMCA, armory: ArmoryView) {
        val inventory = guard.inventory
        var took = false
        for (need in GuardKit.missing(guard, inventory.items)) {
            if (inventory.items.none { it.isEmpty }) break
            val piece = armory.takeOne(need.fits)
            if (piece.isEmpty) continue
            inventory.addItem(piece)
            took = true
        }
        if (took) guard.brain.eraseMemory(MemoryModuleTypeMCA.WEARS_ARMOR)
    }

    /** Re-asserted every tick: a patrol step would otherwise replace it. */
    private fun walk(guard: VillagerEntityMCA) {
        val target = armory ?: return
        val current = guard.brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null)
        if (current == null || current.target.currentBlockPosition() != target.pos) {
            guard.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(target.pos, SPEED, 1))
        }
    }

    companion object {
        /** Ticks between self-checks. A guard only ever lacks what it was never given, so this can be slow. */
        const val CHECK_INTERVAL = 200L

        /** How far a guard looks for an armory, in blocks. */
        const val RANGE = 48

        /** Close enough to reach into the armory: a player's reach at the block's centre. */
        private const val REACH = 2.5

        private const val SPEED = 0.6f

        /** A walk that has not arrived by then gives up, and the next check tries again. */
        private const val MAX_TICKS = 1200
    }
}
