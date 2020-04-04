package starter.behaviours

import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_SOURCES_ACTIVE
import screeps.api.RoomPosition
import starter.*




//todo rename
fun Creep.behaviourHarvestFromSavedSource(): Boolean {
	if (!isHarvesting()) {
		//do not need energy right now.
		return false
	}

	if (memory.behaviour.sourcePos == null) {
		//need to get a new source to harvest from.
		//todo find the closest one that does not have max number of creeps harvesting from it.
		val source = pos.findClosestByPath(FIND_SOURCES_ACTIVE)
		if (source == null) {
			//getClosestSourceOfEnergy has failed
			console.log("behaviourHarvestFromSavedSource: error failed to find a source")
			return false
		}
		memory.behaviour.sourcePos = source.pos
	}

	val targetPos = loadPosFromMemory(memory.behaviour.sourcePos!!)
	if (pickupEnergyFromPosition(targetPos) == ERR_NOT_IN_RANGE) {
		//go and harvest this pos
		moveTo(targetPos)
		return true
	}
	//todo update isharvesting()?

	updateIsCollectingEnergy()
	//if we are still harvesting then don't move
	return isHarvesting()
}


fun Creep.behaviourPickupFromBaseStorage(): Boolean {
	if (!isHarvesting()) {
		console.log("behaviourPickupFromBaseStorage is not harvesting")
		return false
	}

	if (room.memory.bunker.mainStorePos == null) {
		console.log("behaviourPickupFromBaseStorage: error, main store position is null")
		return false
	}
	val targePos = loadPosFromMemory(room.memory.bunker.mainStorePos!!)
	console.log("behaviourPickupFromBaseStorage: ${name}, target pos $targePos")
	if (pickupEnergyFromPosition(targePos) == ERR_NOT_IN_RANGE) {
		if (!pos.isNearTo(targePos)) {
			moveTo(targePos)
			return true
		}
	}
	if (pos.isEqualTo(targePos)) {
		//move off square.
		//todo make this random
		val randomPos = room.getPositionAt(pos.x -1, pos.y+1)
		moveTo(randomPos!!)
	}

	return true
}


fun Creep.behaviourPickup(): Boolean {
	if (!isHarvesting()) {
		return false
	}

	if (memory.behaviour.targetPos == null) {
		memory.behaviour.targetPos = getClosestSourceOfEnergy()
		if (memory.behaviour.targetPos == null) {
			console.log("behaviourPickup: error, could not find a source of energy")
			return false
		}
	}
	val targePos = loadPosFromMemory(memory.behaviour.targetPos!!)
	if (memory.role != Role.EXTRACTOR && pickupEnergyFromPosition(targePos) == ERR_NOT_IN_RANGE) {
		//go and harvest this pos
		moveTo(targePos)
		return true
	} else{
		//success
		return true
		//todo use the move action as well
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
		return true
	} else {
		return true
	}

	return false
}

fun Creep.behaviourBuildWhileMoving(): Boolean {
	if (isHarvesting()) {
		//do not build anything while picking up energy.
		return false
	}

	if (carry.energy < ((3*carryCapacity)/4)) {
		//only build with the first 1/4 of the energy that it is carrying
		return false
	}

	var target = getClosestStructureToBuild()
	if (target == null) {
		//nothing to build
		return false
	}
	if (build(target!!) == ERR_NOT_IN_RANGE) {

	}

	return false
}

fun Creep.behaviourRepairWhileMoving(): Boolean {
	if (isHarvesting()) {
		//do not repair anything while picking up energy.
		return false
	}

	if (carry.energy < ((3*carryCapacity)/4)) {
		//only repair with the first 1/4 of the energy that it is carrying
		return false
	}

	var target = getStructureInRangeToRepair()
	if (target == null) {
		//nothing to build
		console.log("behaviourRepairWhileMoving: no targets")
		return false
	}
	console.log("behaviourRepairWhileMoving: found a target $target")
	if (repair(target!!) == ERR_NOT_IN_RANGE) {
		console.log("behaviourRepairWhileMoving: error, target is not in range. it should be")
	}

	return false
}

fun Creep.moveOffSourcePos(): Boolean {
	if (memory.behaviour.sourcePos == null) {
		return false
	}
	val p = loadPosFromMemory(memory.behaviour.sourcePos!!)
	if (pos.isEqualTo(p)) {
		//move in a random direction
		val directions = getAdjcentSquares(p)
//		val index = RandInt()
		val newPos :RoomPosition = directions[0]
		moveTo(newPos)

	}
	return false
}