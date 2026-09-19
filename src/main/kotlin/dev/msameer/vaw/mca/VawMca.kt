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

import dev.msameer.vaw.api.GuardProvider
import dev.msameer.vaw.api.ReserveProvider
import dev.msameer.vaw.api.VawApi
import net.conczin.mca.entity.VillagerEntityMCA
import net.conczin.mca.registry.EntitiesMCA
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.npc.villager.Villager
import net.minecraft.world.item.ItemStack
import org.slf4j.LoggerFactory

/**
 * The extension entrypoint. Registering the guard provider is what tells the core that guards
 * exist, which switches on armories and the three guard-only makers (Technical Reference §12.3).
 */
object VawMca : ModInitializer {
    private val LOGGER = LoggerFactory.getLogger("vaw/mca")

    /** MCA's guards: swordsmen and archers (§12.2). */
    private object McaGuards : GuardProvider {
        override val id: String = "mca"
    }

    /** Has the core draw its error icon on MCA villagers too (§2.2). */
    fun showErrorIcons() {
        VawApi.showErrorIconsFor(EntitiesMCA.MALE_VILLAGER)
        VawApi.showErrorIconsFor(EntitiesMCA.FEMALE_VILLAGER)
    }

    /**
     * §16.1: MCA villagers breed by MCA's own rules, not by sharing food, so they keep no food back.
     * Seed is still kept, as a vanilla villager keeps it. Vanilla villagers get the core's default.
     */
    private object McaReserve : ReserveProvider {
        override fun reserves(villager: Villager, stack: ItemStack): Boolean? =
            if (villager is VillagerEntityMCA) stack.`is`(ItemTags.VILLAGER_PLANTABLE_SEEDS) else null
    }

    override fun onInitialize() {
        McaConfig.load(FabricLoader.getInstance().configDir)
        McaSpeech.reset()
        VawApi.registerGuardProvider(McaGuards)
        VawApi.registerReserveProvider(McaReserve)
        VawApi.registerSignalListener(McaSpeech)
        VawApi.registerFamilyProvider(McaFamily)
        // §2.2: MCA villagers speak instead of showing the icon, unless the player asks for both.
        if (McaConfig.showErrorIcon) showErrorIcons()
        ServerEntityEvents.ENTITY_UNLOAD.register { entity, _ -> if (entity is Villager) McaSpeech.forget(entity) }
        LOGGER.info("Villagers at Work MCA extension loaded; guard provider '{}' registered", McaGuards.id)
    }
}
