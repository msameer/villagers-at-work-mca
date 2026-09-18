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

import net.conczin.mca.entity.EquipmentSet
import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.registry.ProfessionsMCA
import net.conczin.mca.server.world.data.villageComponents.VillageGuardsManager
import net.conczin.mca.util.InventoryUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.ProjectileWeaponItem

/**
 * What a guard wears, as MCA decides it: the tier kit of its village, or the level-0 kit with no
 * village, exactly as MCA's own `EquipmentTask` asks for it. MCA decides **which slots**; what fills
 * them is any carried piece of the same kind, so an iron sword serves a diamond-sword kit and a
 * leather chestplate an iron one. The armories decide the material (§12.2).
 */
object GuardKit {
    /** One slot of the kit and what counts as filling it. */
    class Need(val slot: EquipmentSlot, val fits: (ItemStack) -> Boolean)

    /** The kit MCA equips [guard] with. */
    fun of(guard: VillagerEntityMCA): EquipmentSet =
        guard.residency.homeVillage
            .map { it.villageGuardsManager.getGuardEquipment(guard.profession) }
            .orElseGet {
                if (guard.profession == ProfessionsMCA.ARCHER) VillageGuardsManager.getArcherEquipmentForLevel(0)
                else VillageGuardsManager.getGuardEquipmentForLevel(0)
            }

    /** Every slot the kit fills. Hands are named by the kit's main and off hand, whichever arm is dominant. */
    fun needs(kit: EquipmentSet): List<Need> = listOf(
        EquipmentSlot.MAINHAND to kit.mainHand,
        EquipmentSlot.OFFHAND to kit.getOffHand,
        EquipmentSlot.HEAD to kit.head,
        EquipmentSlot.CHEST to kit.chest,
        EquipmentSlot.LEGS to kit.legs,
        EquipmentSlot.FEET to kit.feet,
    ).filter { (_, item) -> item != Items.AIR }.map { (slot, item) -> Need(slot, fits(item, slot)) }

    /**
     * The kit's needs [carried] does not meet. Each carried piece meets one need only, so a guard
     * with one sword still lacks the second a two-sword kit asks for.
     */
    fun missing(guard: VillagerEntityMCA, carried: List<ItemStack>): List<Need> {
        val free = carried.filter { !it.isEmpty }.toMutableList()
        return needs(of(guard)).filter { need ->
            val match = free.firstOrNull(need.fits) ?: return@filter true
            free.remove(match)
            false
        }
    }

    /**
     * What counts as the same kind as [requested] in [slot], by the rule MCA itself equips by: armour
     * by the slot it is worn in, a bow by being a ranged weapon, a sword by being a weapon, a shield by
     * blocking. Anything else must be that very item.
     */
    fun fits(requested: Item, slot: EquipmentSlot): (ItemStack) -> Boolean {
        val kit = ItemStack(requested)
        return when {
            slot.type == EquipmentSlot.Type.HUMANOID_ARMOR -> { it -> it.get(DataComponents.EQUIPPABLE)?.slot() == slot }
            requested is ProjectileWeaponItem -> { it -> it.item is ProjectileWeaponItem }
            InventoryUtils.isWeapon(kit) -> InventoryUtils::isWeapon
            kit.has(DataComponents.BLOCKS_ATTACKS) -> { it -> it.has(DataComponents.BLOCKS_ATTACKS) }
            else -> { it -> it.`is`(requested) }
        }
    }

    /** How MCA ranks pieces of one kind: armour by its armour value in [slot], anything else by durability. */
    fun rank(slot: EquipmentSlot): (ItemStack) -> Double =
        if (slot.type == EquipmentSlot.Type.HUMANOID_ARMOR) {
            { it.get(DataComponents.ATTRIBUTE_MODIFIERS)?.compute(Attributes.ARMOR, 0.0, slot) ?: 0.0 }
        } else {
            { it.maxDamage.toDouble() }
        }
}
