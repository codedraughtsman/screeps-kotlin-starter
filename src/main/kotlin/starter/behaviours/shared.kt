package starter.behaviours

import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import starter.*

//todo rename
fun Creep.behaviourPickup(): Boolean {
	if (!isHarvesting()) {
		return false
	}
	memory.behaviour.targetPos = getClosestSourceOfEnergy()

//	console.log("behavourPickup: from $pos to closest source of energy ${memory.behaviour.targetPos}")
	//try to harvest location
	if (memory.behaviour.targetPos != null) {
		//memory.behaviour.targetPos = room.getPositionAt( memory.behaviour.targetPos!!.x-1, memory.behaviour.targetPos!!.y)

		if (memory.role != Role.EXTRACTOR && pickupEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//go and harvest this pos
			moveTo(memory.behaviour.targetPos!!)
			return true
		} else {
			//success
			return true
			//todo use the move action as well
		}
	}
	return false
}

fun Creep.behaviourBuild(): Boolean {
	var target = getClosestStructureToBuild()
	if (target == null) {
		//nothing to build
		return false
	}
	memory.behaviour.targetPos = target.pos
	if (build(target!!) == ERR_NOT_IN_RANGE) {
		moveTo(target.pos)
		console.log("target $target is not in range, set gotoPos to it")
		return true
	} else {
		return true
	}

	return false
}

fun Creep.behaviourBuildWhileMoving(): Boolean {
	var target = getClosestStructureToBuild()
	if (target == null) {
		//nothing to build
		return false
	}
	if (build(target!!) == ERR_NOT_IN_RANGE) {

	}

	return false
}