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

import dev.msameer.vaw.mca.client.McaHands
import net.conczin.mca.client.render.VillagerLikeEntityMCARenderer
import net.conczin.mca.client.render.VillagerRenderState
import net.minecraft.world.entity.Mob
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/** After MCA reads a villager into its render state, the working tool joins it (§2.3). Forwards only. */
@Mixin(VillagerLikeEntityMCARenderer::class)
abstract class McaRendererMixin {
    @Inject(
        method = ["extractRenderState(Lnet/minecraft/world/entity/Mob;Lnet/conczin/mca/client/render/VillagerRenderState;F)V"],
        at = [At("TAIL")],
    )
    private fun vawWorkingToolInHand(entity: Mob, state: VillagerRenderState, partialTicks: Float, ci: CallbackInfo) {
        McaHands.extract(entity, state)
    }
}
