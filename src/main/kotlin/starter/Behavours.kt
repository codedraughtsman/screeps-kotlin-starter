package starter

import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject
import starter.behaviours.behaviourDropOffEnergy
import starter.behaviours.getClosestStructureToBuild
import kotlin.UInt.Companion.MAX_VALUE











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













fun Creep.behaviourDeposit(): Boolean {
	//find the closest place to deposit energy in
	val targets = room.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
			.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }


	if (targets.isNotEmpty()) {
		var bestPos = targets[0].pos
		var bestCost: Int? = null
		for (target in targets) {
			//val target = targets[0]
			val path = PathFinder.search(pos, target.pos)
			val cost: Int = path.cost
			//console.log(" looking at path from $pos to ${target.pos} cost $cost")
			if (bestCost == null || cost < bestCost) {
				bestCost = cost
				bestPos = target.pos
			}
		}
		//console.log("best path for depost is from: $pos to $bestPos cost $bestCost")

		memory.behaviour.targetPos = bestPos
		if (behaviourDropOffEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//memory.behaviour.gotoPos =memory.behaviour.targetPos
			moveTo(bestPos)
			return true
		}
	} else {
		//console.log("dropBehavour could not find anything")
	}
	return false
}







fun Creep.behaviourBuildContainer(): Boolean {
	if (carry.energy == 0) {
		return false
	}
	var target = getClosestStructureToBuild()

//	var target = room.lookAt( pos.x, pos.y).filter { (it.type == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE)  }
	if (target == null || target.pos.x != pos.x || target.pos.y != pos.y) {
		//nothing to build
		return false
	}
	//memory.behaviour.targetPos = target.pos
	if (build(target) == ERR_NOT_IN_RANGE) {
		return true
	} else {
		return true
	}

	return false
}