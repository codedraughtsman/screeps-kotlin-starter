package starter.multiAI.Actions

import screeps.api.Creep
import screeps.api.FIND_HOSTILE_CREEPS
import screeps.api.Game
import screeps.api.RoomPosition
import starter.behaviour
import starter.multiAI.MultiAI
import starter.utils.getTargetPos

object Attacker {
	fun Attacker(creep: Creep) : MultiAI.ReturnType {

		if (getTargetPos(creep) == null){
			// get new target
			//find closest creep
			val target = creep.pos.findClosestByPath(FIND_HOSTILE_CREEPS)
			if (target == null){
				//nothing to attack
				return MultiAI.ReturnType.CONTINUE
			}
			creep.memory.behaviour.targetPos = target.pos

		}
		var target: RoomPosition? = getTargetPos(creep)
		if (target == null) {
			console.log( "attacker creep unable to get a target to move towards")
			return MultiAI.ReturnType.CONTINUE
		}
		if (target.roomName == creep.pos.roomName){
			//we are in the room
			val targetCreep = creep.pos.findClosestByPath(FIND_HOSTILE_CREEPS)

			if (targetCreep == null) {
				//there are no creeps to attack in this room. find another target.
				creep.memory.behaviour.targetPos = null
				return MultiAI.ReturnType.CONTINUE
			}

			creep.memory.behaviour.targetPos = targetCreep.pos

			//move towards closest creep
			Move.towardsTarget(creep)
		}



		return MultiAI.ReturnType.CONTINUE
	}
}