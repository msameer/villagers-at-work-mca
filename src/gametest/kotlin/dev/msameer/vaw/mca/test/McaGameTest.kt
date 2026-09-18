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
package dev.msameer.vaw.mca.test

import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.registry.EntitiesMCA
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.ResourceKey
import net.minecraft.world.Container
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.npc.villager.VillagerProfession
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

/**
 * The extension in a live world: MCA Reborn, the Villagers at Work core and this extension loaded
 * together. Only vanilla and MCA types are used here; the core is reached through the world alone,
 * as a player would see it.
 */
class McaGameTest {
    private val workTime = 3000L

    private fun abs(helper: GameTestHelper, pos: BlockPos): BlockPos = helper.absolutePos(pos)

    /** A stone floor walled two blocks high, with its chunks kept ticking. */
    private fun arena(helper: GameTestHelper, xs: IntRange, zs: IntRange) {
        val from = abs(helper, BlockPos(xs.first - 1, 2, zs.first - 1))
        val to = abs(helper, BlockPos(xs.last + 1, 2, zs.last + 1))
        for (cx in SectionPos.blockToSectionCoord(minOf(from.x, to.x))..SectionPos.blockToSectionCoord(maxOf(from.x, to.x))) {
            for (cz in SectionPos.blockToSectionCoord(minOf(from.z, to.z))..SectionPos.blockToSectionCoord(maxOf(from.z, to.z))) {
                helper.level.setChunkForced(cx, cz, true)
            }
        }
        for (x in xs.first - 1..xs.last + 1) {
            for (z in zs.first - 1..zs.last + 1) {
                helper.setBlock(x, 1, z, Blocks.STONE)
                val edge = x !in xs || z !in zs
                for (y in 2..3) helper.setBlock(x, y, z, if (edge) Blocks.STONE else Blocks.AIR)
            }
        }
    }

    /** A workstation with a shelf on top, stocked, and its job site claimed. */
    private fun station(helper: GameTestHelper, pos: BlockPos, workstation: Block, profession: ResourceKey<VillagerProfession>, vararg shelf: ItemStack) {
        helper.setBlock(pos, workstation)
        helper.setBlock(pos.above(), Blocks.OAK_SHELF)
        val container = helper.level.getBlockEntity(abs(helper, pos.above())) as Container
        for ((slot, stack) in shelf.withIndex()) container.setItem(slot, stack)
        val predicate = BuiltInRegistries.VILLAGER_PROFESSION.getValue(profession)!!.heldJobSite()
        val claimed = helper.level.poiManager.take(predicate, { _, p -> p == abs(helper, pos) }, abs(helper, pos), 1)
        helper.assertTrue(claimed.isPresent, "could not claim the $profession station at $pos")
    }

    /** An MCA villager with [profession], working at [station]. */
    private fun mcaWorker(helper: GameTestHelper, profession: ResourceKey<VillagerProfession>, station: BlockPos, at: BlockPos): VillagerEntityMCA {
        val villager = helper.spawn(EntitiesMCA.MALE_VILLAGER, at.x, at.y, at.z)
        villager.setVillagerData(villager.villagerData.withProfession(helper.level.registryAccess(), profession))
        villager.refreshBrain(helper.level)
        villager.brain.setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(helper.level.dimension(), abs(helper, station)))
        return villager
    }

    private fun containerCount(helper: GameTestHelper, pos: BlockPos, item: Item): Int =
        (helper.level.getBlockEntity(abs(helper, pos)) as Container).countItem(item)

    @GameTest(maxTicks = 2400, padding = 32)
    fun anMcaWeaponsmithStocksItsArmory(helper: GameTestHelper) {
        arena(helper, 0..8, 0..6)
        // §16.1: MCA villagers run on the vanilla tick the core hooks, with MCA's own brain. An
        // armory binds only with a guard provider (§12.3), and the extension's is the real one.
        val grindstone = BlockPos(2, 2, 3)
        station(helper, grindstone, Blocks.GRINDSTONE, VillagerProfession.WEAPONSMITH, ItemStack(Items.IRON_INGOT, 8), ItemStack(Items.STICK, 8))
        val armory = grindstone.north()
        helper.setBlock(armory, Blocks.CHEST)
        val weaponsmith = mcaWorker(helper, VillagerProfession.WEAPONSMITH, grindstone, BlockPos(3, 2, 3))
        helper.setTime(workTime)

        helper.succeedWhen {
            val swords = containerCount(helper, armory, Items.IRON_SWORD)
            helper.assertTrue(swords == 1, "the MCA weaponsmith should stock one sword, got $swords at ${weaponsmith.blockPosition()}")
        }
    }

    @GameTest(maxTicks = 1200, padding = 32)
    fun anMcaVillagerKeepsSeedButNotFood(helper: GameTestHelper) {
        arena(helper, 0..14, 0..3)
        // §16.1's reserve profile: bread is in the core's default reserve, which a vanilla villager
        // keeps. An MCA villager keeps only seed, so its bread is ordinary surplus and reaches
        // storage while the seed stays in its inventory.
        val smithing = BlockPos(1, 2, 1)
        station(helper, smithing, Blocks.SMITHING_TABLE, VillagerProfession.TOOLSMITH)
        val chest = BlockPos(12, 2, 1)
        helper.setBlock(chest, BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("copper_chest")))
        val toolsmith = mcaWorker(helper, VillagerProfession.TOOLSMITH, smithing, BlockPos(2, 2, 2))
        val inventory = toolsmith.inventory
        repeat(inventory.containerSize) { inventory.setItem(it, ItemStack(Items.BREAD, 64)) }
        inventory.setItem(0, ItemStack(Items.WHEAT_SEEDS, 64))
        helper.setTime(workTime)

        helper.succeedWhen {
            helper.assertTrue(containerCount(helper, chest, Items.BREAD) > 0, "the MCA villager's bread should reach storage")
            helper.assertTrue(containerCount(helper, chest, Items.WHEAT_SEEDS) == 0, "and its seed must stay")
            helper.assertTrue(inventory.countItem(Items.WHEAT_SEEDS) == 64, "in its inventory")
        }
    }
}
