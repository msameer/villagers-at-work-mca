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

import dev.msameer.vaw.mca.VawMca
import net.conczin.mca.Config
import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.registry.EntitiesMCA
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import net.minecraft.core.GlobalPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.function.FailableFunction
import java.util.UUID

/**
 * Visual check of the extension's error-icon option (§2.2, §19): where MCA's humanoid rig puts the
 * core's icon, which rides the villager as a passenger. A blocked MCA farmer is screenshotted from the
 * front and the side once its icon is up.
 *
 * Opens a game window, so it runs only with `./gradlew runClientGameTest -PvawCoreJar=...`, never in CI.
 */
class McaIconClientGameTest : FabricClientGameTest {
    override fun runTest(context: ClientGameTestContext) {
        Config.getInstance().launchIntoDestiny = false
        VawMca.showErrorIcons()
        context.worldBuilder().create().use { singleplayer ->
            val server = singleplayer.server
            server.runCommand("time set noon")
            server.runCommand("weather clear")
            val farmer = server.computeOnServer<UUID, RuntimeException>(FailableFunction { minecraft -> buildScene(minecraft) })
            // The core scans on its own cadence; wait until the icon rides the farmer.
            var waited = 0
            while (waited < 600 && !server.computeOnServer<Boolean, RuntimeException>(FailableFunction { minecraft -> iconUp(minecraft, farmer) })) {
                context.waitTicks(20)
                waited += 20
            }
            println("[vaw-mca-client-gametest] icon up after $waited ticks")
            for ((name, offset) in listOf("mca-icon-front" to Vec3(0.0, 0.4, 3.0), "mca-icon-side" to Vec3(3.0, 0.4, 0.0))) {
                val at = server.computeOnServer<Vec3, RuntimeException>(FailableFunction { minecraft -> villager(minecraft, farmer)!!.position() })
                val yaw = if (offset.z > 0) 180 else 90
                server.runCommand("tp @p ${at.x + offset.x} ${at.y + offset.y} ${at.z + offset.z} $yaw 0")
                context.waitTicks(20)
                println("[vaw-mca-client-gametest] screenshot -> " + context.takeScreenshot(name))
            }
        }
    }

    private fun villager(minecraft: MinecraftServer, id: UUID): VillagerEntityMCA? = minecraft.overworld().getEntity(id) as? VillagerEntityMCA

    private fun iconUp(minecraft: MinecraftServer, id: UUID): Boolean =
        villager(minecraft, id)?.passengers?.any { "vaw_error_icon" in it.entityTags() } == true

    /** An MCA farmer at a composter whose shelf has no hoe: blocked, during working hours. */
    private fun buildScene(minecraft: MinecraftServer): UUID {
        val level = minecraft.overworld()
        val player = minecraft.playerList.players.first()
        val station = player.blockPosition().offset(3, 0, 5)
        level.setBlockAndUpdate(station, Blocks.COMPOSTER.defaultBlockState())
        level.setBlockAndUpdate(station.above(), Blocks.OAK_SHELF.defaultBlockState())
        val farmer = EntitiesMCA.MALE_VILLAGER.create(level, EntitySpawnReason.COMMAND) ?: error("could not create an MCA villager")
        farmer.snapTo(station.x - 2.5, station.y.toDouble(), station.z + 0.5, 0f, 0f)
        farmer.finalizeSpawn(level, level.getCurrentDifficultyAt(farmer.blockPosition()), EntitySpawnReason.COMMAND, null)
        farmer.age = 0
        level.addFreshEntity(farmer)
        val profession = ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.withDefaultNamespace("farmer"))
        farmer.setVillagerData(farmer.villagerData.withProfession(level.registryAccess(), profession))
        farmer.refreshBrain(level)
        farmer.brain.setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(level.dimension(), station))
        return farmer.uuid
    }
}
