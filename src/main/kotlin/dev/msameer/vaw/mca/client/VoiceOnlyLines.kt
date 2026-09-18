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
package dev.msameer.vaw.mca.client

import dev.msameer.vaw.mca.McaSpeech
import net.conczin.mca.client.tts.SpeechManager
import net.conczin.mca.network.s2c.VillagerMessage

/**
 * The client half of "voice without chat" (§8.3): MCA shows every line it voices, so a line the server
 * marked voice-only is spoken here and not shown. Resolving it first is what lets MCA find its voice:
 * MCA voices only a line whose translation it has seen resolved.
 */
object VoiceOnlyLines {
    /** Returns whether [message] was a voice-only line and has been spoken. */
    fun handle(message: VillagerMessage): Boolean {
        if (message.message().style.insertion != McaSpeech.VOICE_ONLY) return false
        message.message().string
        SpeechManager.INSTANCE.onChatMessage(message.message(), message.uuid())
        return true
    }
}
