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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * The extension's config, `villagers-at-work-mca.json` (Technical Reference §8.3, §17). Read once at
 * start; a missing file is written with the defaults, an unreadable one falls back to them.
 */
object McaConfig {
    private val LOGGER = LoggerFactory.getLogger("vaw/mca")
    private const val FILE_NAME = "villagers-at-work-mca.json"

    /** A blocked MCA villager says why in chat. */
    var chat = true

    /** And aloud, through MCA's own voice. */
    var voice = true

    /** Show the core's error icon on MCA villagers as well (§2.2). */
    var showErrorIcon = false

    /** How long one villager stays quiet after speaking, in minutes: the flicker backstop. */
    var cooldownMinutes = 5

    /** At most this many lines in a rolling window, among villagers within [dedupeRange]. */
    var villageCap = 3

    var villageWindowSeconds = 60

    /** How far a line is heard, in blocks. Never server-wide. */
    var hearingRange = 20

    /** Villagers this close together count as one village, for the cap and for one line per cause. */
    var dedupeRange = 64

    fun load(configDir: Path) {
        val file = configDir.resolve(FILE_NAME)
        try {
            if (!Files.exists(file)) return write(file)
            val json = JsonParser.parseString(Files.readString(file)).asJsonObject
            chat = json.get("chat")?.asBoolean ?: chat
            voice = json.get("voice")?.asBoolean ?: voice
            showErrorIcon = json.get("showErrorIcon")?.asBoolean ?: showErrorIcon
            cooldownMinutes = json.get("cooldownMinutes")?.asInt ?: cooldownMinutes
            villageCap = json.get("villageCap")?.asInt ?: villageCap
            villageWindowSeconds = json.get("villageWindowSeconds")?.asInt ?: villageWindowSeconds
            hearingRange = json.get("hearingRange")?.asInt ?: hearingRange
            dedupeRange = json.get("dedupeRange")?.asInt ?: dedupeRange
        } catch (e: Exception) {
            LOGGER.warn("Unreadable {}, using the defaults: {}", file, e.toString())
        }
    }

    private fun write(file: Path) {
        val json = JsonObject().apply {
            addProperty("chat", chat)
            addProperty("voice", voice)
            addProperty("showErrorIcon", showErrorIcon)
            addProperty("cooldownMinutes", cooldownMinutes)
            addProperty("villageCap", villageCap)
            addProperty("villageWindowSeconds", villageWindowSeconds)
            addProperty("hearingRange", hearingRange)
            addProperty("dedupeRange", dedupeRange)
        }
        runCatching {
            Files.createDirectories(file.parent)
            Files.writeString(file, GsonBuilder().setPrettyPrinting().create().toJson(json))
        }
    }
}
