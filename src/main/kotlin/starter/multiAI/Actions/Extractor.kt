package starter.multiAI.Actions

import screeps.api.Creep
import screeps.api.Game
import screeps.api.RoomPosition
import screeps.api.values
import starter.behaviour
import starter.multiAI.MultiAI
import starter.multiAI.Role
import starter.role
import starter.utils.getMiningPoints
import starter.utils.getTargetPos
import starter.utils.noCreepHasPosAsTarget
import starter.utils.targetPosIsEqualTo
import kotlin.math.max

object Extractor {
	fun extractor(creep: Creep) :MultiAI.ReturnType {
		if (getTargetPos(creep) == null) {
			var freeMiningPoint = getMiningPoints()
					.filter { noCreepHasPosAsTarget(it.pos)}
					.sortedBy { creep.pos.getRangeTo(it.pos) }
					.firstOrNull()
			if (freeMiningPoint != null) {
				creep.memory.behaviour.targetPos = freeMiningPoint.pos
			}

//			if (freeMiningPoint == null) {
//				//now lets get the one with the least time to live
//				var extractorCreeps = Game.creeps.values
//						.filter { it.memory.role == Role.EXTRACTOR }
//				var oldestValue = 10000000
//				var oldestPos: RoomPosition? =null
//				for (mp in getMiningPoints()){
//					var mostTimeToLive=0
//					for (creep in extractorCreeps.filter { targetPosIsEqualTo(creep, mp.pos) }) {
//						mostTimeToLive = max(mostTimeToLive, creep.ticksToLive)
//					}
//					if (mostTimeToLive < oldestValue) {
//						oldestPos = mp.pos
//						oldestValue = mostTimeToLive
//					}
//				}
//				creep.memory.behaviour.targetPos = oldestPos
//
//				return MultiAI.ReturnType.CONTINUE
//			}

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