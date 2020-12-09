package starter.multiAI.Actions

import screeps.api.Creep
import screeps.api.values
import starter.behaviour
import starter.multiAI.MultiAI
import starter.utils.getTargetPos

object ExtractorHauler {
	fun hauler(creep: Creep): MultiAI.ReturnType {
		var target = getTargetPos(creep)
		if (target == null) {
			if (creep.carry.values.sum() > 0) {
				MoveSetTarget.baseStoreIfCarrying(creep)
			} else {
				MoveSetTarget.miningPointWithMostResouceToPickup(creep)
			}
			target = getTargetPos(creep)!!;
		}

		if (creep.pos.isNearTo(target)) {
			creep.memory.behaviour.targetPos = null;
		} else {
			Move.towardsTarget(creep)
		}
		return MultiAI.ReturnType.STOP
	}
}