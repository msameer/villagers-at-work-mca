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
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.ai.behavior.Behavior
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.memory.MemoryStatus
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.phys.Vec3
import java.util.UUID

/**
 * Something a guard walks off to do between its duties: fetch gear (§12.2), bring in loot (§16.1).
 * It checks on a cooldown, never in a fight, and one errand at a time, so two never fight over where
 * the guard is walking.
 *
 * **Nothing is remembered between checks** (I1): each check reads the guard and the world again. If
 * the target is gone on arrival the guard goes back to its duties and asks again later (I3).
 */
abstract class GuardErrand<T : Any> : Behavior<VillagerEntityMCA>(mapOf(MemoryModuleType.ATTACK_TARGET to MemoryStatus.VALUE_ABSENT), MAX_TICKS) {
    private var nextCheck = 0L
    private var target: T? = null
    private var done = false

    /** What to walk to now, or `null` when there is nothing to do. */
    protected abstract fun choose(level: ServerLevel, guard: VillagerEntityMCA): T?

    protected abstract fun where(target: T): BlockPos

    /** Arrived within reach of [target]. */
    protected abstract fun arrive(guard: VillagerEntityMCA, target: T)

    override fun checkExtraStartConditions(level: ServerLevel, guard: VillagerEntityMCA): Boolean {
        if (level.gameTime < nextCheck || guard.uuid in busy) return false
        nextCheck = level.gameTime + CHECK_INTERVAL
        if (!guard.isGuard || guard.isSleeping) return false
        target = choose(level, guard)
        return target != null
    }

    override fun start(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        done = false
        busy += guard.uuid
        walk(guard)
    }

    override fun canStillUse(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long): Boolean =
        !done && target != null && !guard.brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)

    override fun tick(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        val current = target ?: return
        if (guard.position().distanceTo(Vec3.atCenterOf(where(current))) <= REACH) {
            arrive(guard, current)
            done = true
        } else {
            walk(guard)
        }
    }

    override fun stop(level: ServerLevel, guard: VillagerEntityMCA, gameTime: Long) {
        target = null
        busy -= guard.uuid
    }

    /** Re-asserted every tick: a patrol step would otherwise replace it. */
    private fun walk(guard: VillagerEntityMCA) {
        val pos = target?.let(::where) ?: return
        val current = guard.brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null)
        if (current == null || current.target.currentBlockPosition() != pos) {
            guard.brain.setMemory(MemoryModuleType.WALK_TARGET, WalkTarget(pos, SPEED, 1))
        }
    }

    companion object {
        /** Ticks between checks. A guard's errands are never urgent, so this can be slow. */
        const val CHECK_INTERVAL = 200L

        /** How far a guard looks for an armory or a shelf, in blocks. */
        const val RANGE = 48

        /** Close enough to reach in: a player's reach, at the block's centre. */
        private const val REACH = 2.5

        private const val SPEED = 0.6f

        /** A walk that has not arrived by then gives up, and the next check tries again. */
        private const val MAX_TICKS = 1200

        /** Guards on an errand now. Server thread only. */
        private val busy = HashSet<UUID>()
    }
}
