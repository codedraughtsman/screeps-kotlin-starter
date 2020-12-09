package starter.multiAI.Actions

import screeps.api.Creep
import screeps.api.Game
import screeps.api.values
import starter.behaviour
import starter.multiAI.MultiAI
import starter.multiAI.Role
import starter.role
import starter.utils.getTargetPos
import starter.utils.noCreepHasTargetID_AsTarget

object HaulerCreep {
	fun haulerCreep(creep: Creep) : MultiAI.ReturnType {
		var targetID = creep.memory.behaviour.targetID
		if (targetID == null){
			//get a new creep to fill up.
			val newTarget = Game.creeps.values
					.filter { it.memory.role == Role.UPGRADER }
					.filter{ noCreepHasTargetID_AsTarget(it.id)}
					.getOrNull(0)
			if (newTarget == null) {
				return MultiAI.ReturnType.CONTINUE
			}
			creep.memory.behaviour.targetID = newTarget.id
		}

		targetID = creep.memory.behaviour.targetID
		if (targetID == null) {
			return MultiAI.ReturnType.CONTINUE
		}
		val targetCreep = Game.getObjectById<Creep>(targetID)
		if (targetCreep == null) {
			//target creep nolonger exists.
			//reset target
			creep.memory.behaviour.targetID = null
			return MultiAI.ReturnType.CONTINUE
		}

		if( creep.carry.energy > 0) {
			if (!creep.pos.isNearTo(targetCreep.pos)){
				creep.memory.behaviour.targetPos = targetCreep.pos
				return Move.towardsTarget(creep)
			}
		} else {
			//need more energy.

			MoveSetTarget.baseStore(creep)

			Move.towardsTarget(creep)
		}

		return MultiAI.ReturnType.CONTINUE
	}
}