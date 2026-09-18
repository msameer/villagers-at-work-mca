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

import dev.msameer.vaw.api.BlockCause
import dev.msameer.vaw.api.Blocked
import dev.msameer.vaw.api.SignalListener
import net.conczin.mca.Config
import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.villager.Villager
import net.minecraft.world.item.ItemStack

/**
 * Blocked MCA villagers say why, in chat and in MCA's own voice, instead of the core's error icon
 * (Technical Reference §2.3, §8.3). The particles still show; speech is additive.
 *
 * **The line is MCA's own kind of line**: a translation key with numbered variants in this extension's
 * language files, which MCA resolves on each player's client, in their language, and picks one of at
 * random. MCA speaks only the resolved line and never fills in a placeholder, so the spoken part names
 * the cause only, and the item follows as text for chat. A client without this extension reads the
 * English fallback and hears nothing.
 */
object McaSpeech : SignalListener {
    /** Marks a line to be heard but not shown, when chat is off and voice on; read by the client half. */
    const val VOICE_ONLY: String = "vaw_mca:voice_only"

    private var governor = governor()

    /** How a line reaches the players who hear it. Replaced in game tests to record what is said. */
    var deliver: (VillagerEntityMCA, List<ServerPlayer>, MutableComponent) -> Unit = ::viaMca

    fun reset() {
        governor = governor()
    }

    private fun governor() = SpeechGovernor(
        cooldownTicks = McaConfig.cooldownMinutes * 60L * 20L,
        cap = McaConfig.villageCap,
        windowTicks = McaConfig.villageWindowSeconds * 20L,
        range = McaConfig.dedupeRange.toDouble(),
    )

    override fun onBlockedChanged(villager: Villager, blocked: Blocked?) {
        if (villager !is VillagerEntityMCA || blocked == null) return
        if (!McaConfig.chat && !McaConfig.voice) return
        val level = villager.level() as? ServerLevel ?: return
        val range = McaConfig.hearingRange.toDouble()
        val hearers = level.players().filter { !it.isSpectator && it.distanceToSqr(villager) <= range * range }
        // Nobody near to hear it: say nothing, and spend no cooldown on it.
        if (hearers.isEmpty()) return
        val cause = "${blocked.cause}:${blocked.item?.let { net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(it) }}"
        if (!governor.allow(villager.uuid, cause, villager.x, villager.y, villager.z, level.gameTime)) return
        deliver(villager, hearers, line(blocked))
    }

    fun forget(villager: Villager) = governor.forget(villager.uuid)

    /** The spoken cause, then the item as text; the key has numbered variants, the fallback is English. */
    fun line(blocked: Blocked): MutableComponent {
        val key = "vaw_mca.blocked.${blocked.cause.name.lowercase()}"
        val line = Component.translatableWithFallback(key, FALLBACK.getValue(blocked.cause))
        blocked.item?.let { line.append(": ").append(ItemStack(it).hoverName) }
        return line
    }

    /**
     * Through MCA, which shows the line in chat and voices it. Chat without voice goes as a plain line in
     * MCA's format, which MCA never voices; voice without chat is marked for the client half to skip.
     */
    private fun viaMca(villager: VillagerEntityMCA, hearers: List<ServerPlayer>, line: MutableComponent) {
        for (player in hearers) {
            when {
                McaConfig.voice && McaConfig.chat -> villager.sendChatMessage(line.copy(), player)
                McaConfig.voice -> villager.sendChatMessage(line.copy().withStyle { it.withInsertion(VOICE_ONLY) }, player)
                else -> player.sendSystemMessage(
                    Component.literal(Config.getInstance().villagerChatPrefix).append(villager.displayName).append(": ").append(line.copy()),
                )
            }
        }
    }

    private val FALLBACK = mapOf(
        BlockCause.SHELF to "I can't tell which shelf is mine.",
        BlockCause.ARMOR_STAND to "I can't tell which armor stand is mine.",
        BlockCause.ARMORY to "I've nowhere to keep what I make. I need a chest.",
        BlockCause.WATER to "There's no water here to fish in.",
        BlockCause.STORAGE_FULL to "The chest is full.",
        BlockCause.MISSING_ITEM to "I can't work without this",
        BlockCause.SEVERAL_ITEMS to "I'm missing more than one thing I need to work.",
    )
}
