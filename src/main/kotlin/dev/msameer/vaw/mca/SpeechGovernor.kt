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

import java.util.UUID

/**
 * Keeps a village of blocked villagers bearable (Technical Reference §8.3). The core already reports
 * a blocked state only when it changes and only during working hours; on top of that:
 *
 * - **one villager** stays quiet for [cooldownTicks] after speaking, the backstop for a state that flickers;
 * - **one cause** is said once among villagers within [range] for as long, so three butchers missing a tool
 *   are one line;
 * - **a village** says at most [cap] lines in any [windowTicks], whatever the causes.
 *
 * Kept in memory only: after a restart everyone may speak once more, which costs nothing.
 */
class SpeechGovernor(
    private val cooldownTicks: Long,
    private val cap: Int,
    private val windowTicks: Long,
    private val range: Double,
) {
    private class Line(val cause: String, val x: Double, val y: Double, val z: Double, val at: Long)

    private val lastSpoke = HashMap<UUID, Long>()
    private val recent = ArrayDeque<Line>()

    /** Whether [speaker], at x, y, z, may say [cause] now; if so the line is counted. */
    fun allow(speaker: UUID, cause: String, x: Double, y: Double, z: Double, now: Long): Boolean {
        val keep = maxOf(cooldownTicks, windowTicks)
        while (recent.isNotEmpty() && now - recent.first().at >= keep) recent.removeFirst()
        lastSpoke[speaker]?.let { if (now - it < cooldownTicks) return false }
        val near = recent.filter { (it.x - x) * (it.x - x) + (it.y - y) * (it.y - y) + (it.z - z) * (it.z - z) <= range * range }
        if (near.any { it.cause == cause && now - it.at < cooldownTicks }) return false
        if (near.count { now - it.at < windowTicks } >= cap) return false
        lastSpoke[speaker] = now
        recent.addLast(Line(cause, x, y, z, now))
        return true
    }

    fun forget(speaker: UUID) {
        lastSpoke.remove(speaker)
    }
}
