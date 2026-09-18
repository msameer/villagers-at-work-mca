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
package dev.msameer.vaw.mca.mixin.client

import dev.msameer.vaw.mca.client.VoiceOnlyLines
import net.conczin.mca.network.ClientHandlerImpl
import net.conczin.mca.network.s2c.VillagerMessage
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/** A voice-only line is spoken and not shown (§8.3). Forwards only. */
@Mixin(ClientHandlerImpl::class)
abstract class VillagerMessageMixin {
    @Inject(method = ["handleVillagerMessage"], at = [At("HEAD")], cancellable = true)
    private fun vawVoiceOnly(message: VillagerMessage, ci: CallbackInfo) {
        if (VoiceOnlyLines.handle(message)) ci.cancel()
    }
}
