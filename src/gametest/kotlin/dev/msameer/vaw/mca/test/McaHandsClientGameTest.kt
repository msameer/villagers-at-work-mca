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

import dev.msameer.vaw.api.VawAttachments
import net.conczin.mca.Config
import net.conczin.mca.entity.ai.Traits
import net.conczin.mca.registry.EntitiesMCA
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.apache.commons.lang3.function.FailableFunction

/**
 * Visual check of §2.3's "MCA: tool in hand": two MCA villagers with an iron hoe in their working-tool
 * attachment, the one on the left right-handed and the one on the right left-handed, screenshotted
 * from the front, so a person or an agent can see each holds it in its main hand.
 *
 * Opens a game window, so it runs only with `./gradlew runClientGameTest -PvawCoreJar=...`, never in CI.
 */
class McaHandsClientGameTest : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) {
        // MCA's first-join flow (who are you, where does your journey start) is not what this test looks at.
        Config.getInstance().launchIntoDestiny = false
        context.worldBuilder().create().use { singleplayer ->
            val server = singleplayer.server
            server.runCommand("time set noon")
            server.runCommand("weather clear")
            val pos = server.computeOnServer<BlockPos, RuntimeException>(FailableFunction { minecraft -> buildScene(minecraft) })
            context.waitTicks(40)
            server.runCommand("tp @p ${pos.x + 0.5} ${pos.y + 0.3} ${pos.z + 3.2} 180 5")
            context.waitTicks(40)
            println("[vaw-mca-client-gametest] screenshot -> " + context.takeScreenshot("mca-hands-front"))
            server.runCommand("tp @p ${pos.x + 2.6} ${pos.y + 0.3} ${pos.z + 2.6} 135 5")
            context.waitTicks(30)
            println("[vaw-mca-client-gametest] screenshot -> " + context.takeScreenshot("mca-hands-three-quarter"))
        }
    }

    /** Two frozen MCA villagers facing south, a block and a half apart, working with an iron hoe. */
    private fun buildScene(minecraft: MinecraftServer): BlockPos {
        val level = minecraft.overworld()
        val player = minecraft.playerList.players.first()
        val pos = player.blockPosition().offset(0, 0, 4)
        for ((dx, leftHanded) in listOf(0.75 to false, -0.75 to true)) {
            val villager = EntitiesMCA.MALE_VILLAGER.create(level, EntitySpawnReason.COMMAND) ?: error("could not create an MCA villager")
            villager.snapTo(pos.x + 0.5 + dx, pos.y.toDouble(), pos.z + 0.5, 0f, 0f)
            // MCA's own spawn set-up: genes, clothes, and a grown-up age.
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(villager.blockPosition()), EntitySpawnReason.COMMAND, null)
            villager.age = 0
            villager.yHeadRot = 0f
            villager.yBodyRot = 0f
            villager.isNoAi = true
            level.addFreshEntity(villager)
            if (leftHanded) villager.traits.addTrait(Traits.LEFT_HANDED) else villager.traits.removeTrait(Traits.LEFT_HANDED)
            villager.setAttached(VawAttachments.WORKING_TOOL, ItemStack(Items.IRON_HOE))
        }
        return pos
    }
}
