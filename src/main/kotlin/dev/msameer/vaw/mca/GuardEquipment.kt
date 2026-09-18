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
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack

/**
 * Stops MCA equipping guards from nowhere (Technical Reference §12.2). **MCA still decides what a
 * guard wears**; only where it comes from changes.
 *
 * MCA's `EquipmentTask` fills each slot of the guard's tier kit from the guard's own inventory, and
 * when the inventory has nothing for a slot it creates the kit item on the spot — the off-hand shield
 * always. So after MCA equips a guard, anything it wears that is not a stack from its inventory was
 * created from nothing. Each such piece is swapped for the inventory's best of the same kind, or for
 * nothing, so a guard wears only what it carries, and what it carries comes from the armories.
 *
 * The same check keeps a worn piece and its inventory stack the same object after a reload, when they
 * load as two copies: the copy is swapped back for the inventory's own stack.
 */
object GuardEquipment {
    private val SLOTS = EquipmentSlot.entries.filter { it.type == EquipmentSlot.Type.HUMANOID_ARMOR || it.type == EquipmentSlot.Type.HAND }

    /** Called after MCA's `EquipmentTask` has equipped [villager]. */
    fun afterMcaEquipped(villager: VillagerEntityMCA) {
        if (!villager.isGuard) return
        val inventory = villager.inventory
        val carried = inventory.items
        for (slot in SLOTS) {
            val worn = villager.getItemBySlot(slot)
            if (worn.isEmpty || carried.any { it === worn }) continue
            val equipped = SLOTS.filter { it != slot }.map { villager.getItemBySlot(it) }
            val replacement = sameKind(carried, worn, slot).firstOrNull { candidate -> equipped.none { it === candidate } }
            villager.setItemSlot(slot, replacement ?: ItemStack.EMPTY)
        }
    }

    /** The inventory's stacks that could fill [slot] in place of [worn], best first, as MCA itself ranks them. */
    private fun sameKind(carried: List<ItemStack>, worn: ItemStack, slot: EquipmentSlot): List<ItemStack> =
        carried.filter { !it.isEmpty && GuardKit.fits(worn.item, slot)(it) }.sortedByDescending(GuardKit.rank(slot))
}
