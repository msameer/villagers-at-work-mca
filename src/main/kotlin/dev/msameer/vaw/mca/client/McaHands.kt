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

import dev.msameer.vaw.api.VawAttachments
import net.conczin.mca.client.render.VillagerRenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.model.HumanoidModel
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.npc.villager.Villager
import net.minecraft.world.item.ItemDisplayContext

/**
 * The working tool in an MCA villager's hand (Technical Reference §2.3: "MCA: tool in hand"). MCA draws
 * its villagers as humanoids holding what is in their hands, so the tool from the synced attachment
 * goes into the render state of the **main arm**, as a held item would: a left-handed villager holds
 * it in its left hand (§16.1). Nothing is written to the villager's equipment (§2.2).
 *
 * **What the villager really holds wins.** A guard's sword, a mourner's flower or anything MCA put in
 * the hand is left as it is; the tool shows only in an empty main hand.
 */
object McaHands {
    fun extract(entity: Mob, state: VillagerRenderState) {
        if (entity !is Villager) return
        val tool = entity.getAttached(VawAttachments.WORKING_TOOL)
        if (tool == null || tool.isEmpty || !state.mainHandItemStack.isEmpty) return
        val right = state.mainArm == HumanoidArm.RIGHT
        val context = if (right) ItemDisplayContext.THIRD_PERSON_RIGHT_HAND else ItemDisplayContext.THIRD_PERSON_LEFT_HAND
        Minecraft.getInstance().itemModelResolver.updateForLiving(state.mainHandItemState, tool, context, entity)
        if (right) {
            state.rightHandItemStack = tool
            state.rightArmPose = HumanoidModel.ArmPose.ITEM
        } else {
            state.leftHandItemStack = tool
            state.leftArmPose = HumanoidModel.ArmPose.ITEM
        }
    }
}
