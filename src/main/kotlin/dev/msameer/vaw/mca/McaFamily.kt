package dev.msameer.vaw.mca

import dev.msameer.vaw.api.FamilyProvider
import net.conczin.mca.entity.VillagerEntityMCA
import net.minecraft.world.entity.npc.villager.Villager
import java.util.UUID

/**
 * Keeps an MCA child's errands to its parents (WIKI: *MCA Reborn*, Technical Reference §16.1): MCA
 * remembers them in its family tree, where vanilla remembers nobody. A child whose parents are gone,
 * or are players with no station, runs no errands. A vanilla child in the same world is left to the
 * core's default of any working villager nearby.
 */
object McaFamily : FamilyProvider {
    override fun parents(child: Villager): Set<UUID>? =
        (child as? VillagerEntityMCA)?.relationships?.familyEntry?.streamParents()?.toList()?.toSet()
}
