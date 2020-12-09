package starter.multiAI.Actions

import screeps.api.*
import starter.multiAI.MultiAI
import starter.utils.isMiningPoint

object Misc {


	fun dropResouceOnMiningPoint(creep: Creep) : MultiAI.ReturnType {

		if (isMiningPoint(creep.pos)) {
			for (resource in creep.carry.keys) {
				creep.drop(resource)
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

}