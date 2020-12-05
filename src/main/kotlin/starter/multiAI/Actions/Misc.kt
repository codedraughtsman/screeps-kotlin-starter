package starter.multiAI.Actions

import screeps.api.Creep
import screeps.api.FIND_FLAGS
import screeps.api.RESOURCE_ENERGY
import screeps.api.RoomPosition
import starter.multiAI.MultiAI
import starter.utils.isMiningPoint

object Misc {


	fun dropEnergyOnMiningPoint(creep: Creep) : MultiAI.ReturnType {

		if (isMiningPoint(creep.pos)) {
			creep.drop(RESOURCE_ENERGY)
		}
		return MultiAI.ReturnType.CONTINUE
	}
}