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

import dev.msameer.vaw.api.VawApi
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

/**
 * The extension entrypoint. Empty for now: it proves the extension builds against the published
 * api alone. The guard provider and the MCA dependency arrive together, later.
 */
object VawMca : ModInitializer {
    private val LOGGER = LoggerFactory.getLogger("vaw/mca")

    override fun onInitialize() {
        LOGGER.info("Villagers at Work MCA extension loaded; guard provider registered: {}", VawApi.guardProvider != null)
    }
}
