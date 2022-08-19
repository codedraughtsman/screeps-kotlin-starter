package starter.multiAI.Actions

import screeps.api.*
import starter.behaviour
import starter.multiAI.MultiAI
import starter.utils.getControllerClaimFlags
import starter.utils.getTargetPos

object ClaimRoom {
	fun ClaimRoom(creep: Creep) : MultiAI.ReturnType {
		if (getTargetPos(creep) == null){
			var target = getControllerClaimFlags()
					.sortedBy { it.pos.getRangeTo(creep.pos) }
					.getOrNull(0)
			if (target == null){
				//nothing to attack
				return MultiAI.ReturnType.CONTINUE
			}
			creep.memory.behaviour.targetPos = target.pos

		}
		var target: RoomPosition? = getTargetPos(creep)
		if (target == null) {
			console.log( "claimer creep unable to get a target to move towards")
			return MultiAI.ReturnType.CONTINUE
		}


		creep.memory.behaviour.targetPos = target

		//move towards closest creep
		Move.towardsTarget(creep)




		return MultiAI.ReturnType.CONTINUE
	}
}