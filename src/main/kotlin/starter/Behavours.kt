package starter

import screeps.api.*
import starter.behaviours.depositEnergyAt


fun Creep.behaviourGoto(): Boolean {
	if (memory.behaviour.gotoPos == null) {
		return false
	}
	var target = room.getPositionAt(memory.behaviour.gotoPos!!.x, memory.behaviour.gotoPos!!.y)
	if (target == null) {
		console.log("behavourGoto: target did not covert to position")
	}

	if (pos == target || pos.isNearTo(target!!)) {
		//have reached destination
		memory.behaviour.gotoPos = null
		return false
	}

	var output = moveTo(target)
	//console.log("running goto, move return code is $output and gotoPos is ${memory.behaviour.gotoPos}")

	return true
}



















