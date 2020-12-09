package starter.multiAI.Actions

import screeps.api.Creep
import starter.multiAI.MultiAI
import starter.utils.getTargetPos
import starter.utils.targetPosIsEqualTo

object Upgrader {
	fun Upgrader(creep: Creep) : MultiAI.ReturnType {
		if (getTargetPos(creep) == null) {
			MoveSetTarget.upgraderPoint(creep)
		}
		val target = getTargetPos(creep)

		if (target == null) {
			return MultiAI.ReturnType.CONTINUE
		}

		if (!creep.pos.isEqualTo(target)){
			Move.towardsTarget(creep)
		}

//		if (creep.carry.energy == 0) {
//			val upgradePoint = getUpgradePoints(creep.pos)
//			if (target.isEqualTo(upgradePoint)) {
//				Move.towardsTarget(creep)
//			} else {
//				MoveSetTarget.upgraderPoint(creep)
//			}
//		} else {
//			MoveSetTarget.controller(creep)
//		}
//		if (creep.pos.isNearTo(target)) {
//
//		}

		return MultiAI.ReturnType.CONTINUE
	}
}