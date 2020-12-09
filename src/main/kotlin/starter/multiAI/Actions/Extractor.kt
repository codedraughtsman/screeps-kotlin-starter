package starter.multiAI.Actions

import screeps.api.Creep
import starter.behaviour
import starter.multiAI.MultiAI
import starter.utils.getMiningPoints
import starter.utils.getTargetPos
import starter.utils.noCreepHasPosAsTarget

object Extractor {
	fun extractor(creep: Creep) :MultiAI.ReturnType {
		if (getTargetPos(creep) == null) {
			var freeMiningPoint = getMiningPoints()
					.filter { noCreepHasPosAsTarget(it.pos)}
					.sortedBy { creep.pos.getRangeTo(it.pos) }
					.firstOrNull()
			if (freeMiningPoint == null) {
				//now lets get the one with the least time to live
				return MultiAI.ReturnType.CONTINUE
			}
			creep.memory.behaviour.targetPos = freeMiningPoint.pos
		}
		val target = getTargetPos(creep)
		if (target== null) {
			return MultiAI.ReturnType.CONTINUE
		}
		if (!creep.pos.isEqualTo(target)) {
			Move.towardsTarget(creep)
		}

		return MultiAI.ReturnType.CONTINUE
	}
}