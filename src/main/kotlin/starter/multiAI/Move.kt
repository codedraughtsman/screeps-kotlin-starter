package starter.multiAI

import screeps.api.Creep
import screeps.api.OK
import screeps.api.RoomPosition
import starter.behaviour
import starter.behaviours.loadPosFromMemory

object Move {
	fun towardsTarget(creep: Creep): MultiAI.ReturnType {

		if (creep.memory.behaviour.targetPos == null) {
			//no target to move towards
			return MultiAI.ReturnType.CONTINUE
		}

		val targetPos: RoomPosition = loadPosFromMemory(creep.memory.behaviour.targetPos!!)

		if (creep.pos.isEqualTo(targetPos)) {
			//no point in moving
			return MultiAI.ReturnType.CONTINUE
		}

		if (creep.moveTo(targetPos) == OK) {
			//moved towards target
			return MultiAI.ReturnType.STOP
		}

		// could not move for some reason.
		return MultiAI.ReturnType.CONTINUE
	}
}