package starter.multiAI

import screeps.api.Creep
import screeps.api.FIND_FLAGS
import screeps.api.RESOURCE_ENERGY
import screeps.api.RoomPosition

object Misc {
	fun isMiningPoint(pos: RoomPosition) : Boolean {
		val flags = pos.findInRange(FIND_FLAGS,0)
				.filter { it.name.contains("extractor") }
		return !flags.isNullOrEmpty()
	}

	fun dropEnergyOnMiningPoint(creep: Creep) : MultiAI.ReturnType {

		if (isMiningPoint(creep.pos)) {
			creep.drop(RESOURCE_ENERGY)
		}
		return MultiAI.ReturnType.CONTINUE
	}
}