package starter.behaviours

import screeps.api.*
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


fun Creep.behaviourPickupFromBaseStorage(forced: Boolean =false): Boolean {
	if ( !( forced || isHarvesting())) {
//		console.log("behaviourPickupFromBaseStorage is not harvesting")
		return false
	}

	if (room.memory.bunker.mainStorePos == null) {
		console.log("behaviourPickupFromBaseStorage: error, main store position is null")
		return false
	}
	val targePos = loadPosFromMemory(room.memory.bunker.mainStorePos!!)

	//todo only the basehauler can pickup energy below 300

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
	var target = getClosestStructureToBuild(includeRoads =false)
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
	console.log("behaviourBuildWhileMoving: for $name at $pos")
	if (isHarvesting()) {
		console.log("behaviourBuildWhileMoving: in harvesting mode")
		//do not build anything while picking up energy.
		return false
	}

	if (carry.energy < ((3*carryCapacity)/4)) {
		//only build with the first 1/4 of the energy that it is carrying
		console.log("behaviourBuildWhileMoving: not carrying enough ")
		return false
	}
	var target = room.find(FIND_CONSTRUCTION_SITES)
			.filter { isInBuildDistance(it.pos, pos) }
//			.sortedBy {buildPriority(it)  }
			.getOrNull(0)
//	var target = getClosestStructureToBuild()
	if (target == null) {
		console.log("behaviourBuildWhileMoving: target is null")
		//nothing to build
		return false
	}
	val code = build(target!!)
	console.log("behaviourBuildWhileMoving: build $target with a code of $code")

	if (code == OK) {
		return true
	}
	else if ( code== ERR_NOT_IN_RANGE) {
		console.log("behaviourBuildWhileMoving: tried to build a target that was out of range")
	}

	return false
}

fun buildPriority(structure: ConstructionSite): Int {
	if (structure == STRUCTURE_ROAD) {
		return 1;
	}
	return 0

}

fun isInBuildDistance(pos1: RoomPosition, pos2: RoomPosition): Boolean {
	return pos1.inRangeTo(pos2, 3)
}

fun Creep.behaviourUpgradeWhileMoving(): Boolean {
	if (isHarvesting()) {
		//do not build anything while picking up energy.
		return false
	}

	if (carry.energy < ((3*carryCapacity)/4)) {
		//only build with the first 1/4 of the energy that it is carrying
		return false
	}

	var target = room.controller
	if (target == null) {
		//nothing to build
		return false
	}
	if (upgradeController(target!!) == ERR_NOT_IN_RANGE) {

	}

	return false
}


fun Creep.behaviourRepairWhileMoving(): Boolean {
	if (isHarvesting()) {
		//do not repair anything while picking up energy.
		return false
	}

	if (carry.energy < ((3*carryCapacity)/4)) {
//		console.log("behaviourRepairWhileMoving: not carrying enough energy")
		//only repair with the first 1/4 of the energy that it is carrying
		return false
	}

	var target = getStructureInRangeToRepair()
	if (target == null) {
		//nothing to build
//		console.log("behaviourRepairWhileMoving: no targets")
		return false
	}
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
		directions.shuffle()
		val newPos :RoomPosition = directions[0]
		moveTo(newPos)

	}
	return false
}
fun Creep.moveOffBaseStoragePos(): Boolean {
	if (room.memory.bunker.mainStorePos == null) {
		return false
	}
	val p = loadPosFromMemory(room.memory.bunker.mainStorePos!!)
	if (pos.isEqualTo(p)) {
		//move in a random direction
		val directions = getAdjcentSquares(p)
		directions.shuffle()
//		val index = RandInt()
		val newPos :RoomPosition = directions[0]
		moveTo(newPos)

	}
	return false
}

fun Creep.behaviourDeposit(): Boolean {
	//find the closest place to deposit energy in
	val targets = room.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_EXTENSION
					|| it.structureType == STRUCTURE_SPAWN
					|| it.structureType == STRUCTURE_TOWER) }
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
		val output = depositEnergyAt(memory.behaviour.targetPos!!)
		if (output == ERR_NOT_IN_RANGE) {
			//memory.behaviour.gotoPos =memory.behaviour.targetPos
			moveTo(bestPos)
			return true
		}
		else if (output == OK){
			//todo drop energy for builder
			return true
		}
	} else {
		//console.log("dropBehavour could not find anything")
	}
	return false
}

fun Creep.behaviourRefillBuilders(): Boolean {
	//find the closest place to deposit energy in
	val targets = room.find(FIND_MY_CREEPS)
			.filter { (it.memory.role == Role.BUILDER) }
			.filter { (it.carry.energy *4 < it.carryCapacity*3)} // 3/4 of capactiy


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
		if (depositEnergyAt(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//memory.behaviour.gotoPos =memory.behaviour.targetPos
			moveTo(bestPos)
			return true
		}
	} else {
		//console.log("dropBehavour could not find anything")
	}
	return false
}