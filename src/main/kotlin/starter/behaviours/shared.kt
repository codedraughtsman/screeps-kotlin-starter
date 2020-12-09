package starter.behaviours

import screeps.api.*
import starter.*
import starter.multiAI.Role



//todo rename
fun Creep.behaviourHarvestFromSavedSource(): BehavourReturn {
	if (!isHarvesting()) {
		//do not need energy right now.
		return BehavourReturn.CONTINUE_RUNNING
	}

	if (memory.behaviour.sourcePos == null) {
		//need to get a new source to harvest from.
		//todo find the closest one that does not have max number of creeps harvesting from it.
		val source = pos.findClosestByPath(FIND_SOURCES_ACTIVE)
		if (source == null) {
			//getClosestSourceOfEnergy has failed
			console.log("behaviourHarvestFromSavedSource: error failed to find a source")
			return BehavourReturn.CONTINUE_RUNNING
		}
		memory.behaviour.sourcePos = source.pos
	}

	val targetPos = loadPosFromMemory(memory.behaviour.sourcePos!!)
	if (pickupEnergyFromPosition(targetPos) == ERR_NOT_IN_RANGE) {
		//go and harvest this pos
		moveTo(targetPos)
		return BehavourReturn.STOP_RUNNING
	}
	//todo update isharvesting()?

	updateIsCollectingEnergy()
	//if we are still harvesting then don't move
	if (isHarvesting()){ return BehavourReturn.STOP_RUNNING }
	return BehavourReturn.CONTINUE_RUNNING
}


fun Creep.behaviourPickupFromBaseStorage(forced: Boolean = false): BehavourReturn {
	if (!(forced || isHarvesting())) {
//		console.log("behaviourPickupFromBaseStorage is not harvesting")
		return BehavourReturn.CONTINUE_RUNNING
	}

	if (room.memory.bunker.mainStorePos == null) {
//		console.log("behaviourPickupFromBaseStorage: error, main store position is null")
		return BehavourReturn.CONTINUE_RUNNING
	}
	val targePos = Bunker(room).storagePos()!!

	//todo only the basehauler can pickup energy below 300

	if (pickupEnergyFromPosition(targePos) == ERR_NOT_IN_RANGE) {
		if (!pos.isNearTo(targePos)) {
			moveTo(targePos)
			return BehavourReturn.STOP_RUNNING
		}
	}
	if (pos.isEqualTo(targePos)) {
		//move off square.
		//todo make this random
		val randomPos = room.getPositionAt(pos.x - 1, pos.y + 1)
		moveTo(randomPos!!)
	}

	return BehavourReturn.STOP_RUNNING
}


fun Creep.behaviourPickup(): BehavourReturn {
	if (!isHarvesting()) {
		return BehavourReturn.CONTINUE_RUNNING
	}

	if (memory.behaviour.targetPos == null) {
		memory.behaviour.targetPos = getClosestSourceOfEnergy()
		if (memory.behaviour.targetPos == null) {
//			console.log("behaviourPickup: error, could not find a source of energy")
			return BehavourReturn.CONTINUE_RUNNING
		}
	}
	val targePos = loadPosFromMemory(memory.behaviour.targetPos!!)
	if (memory.role != Role.EXTRACTOR && pickupEnergyFromPosition(targePos) == ERR_NOT_IN_RANGE) {
		//go and harvest this pos
		moveTo(targePos)
		return BehavourReturn.STOP_RUNNING
	} else {
		//success
		return BehavourReturn.STOP_RUNNING
		//todo use the move action as well
	}

	return BehavourReturn.CONTINUE_RUNNING
}

fun Creep.getClosestStructureToBuild(includeRoads: Boolean = true): ConstructionSite? {

	//todo sort by distance
	val sites = room.find(FIND_CONSTRUCTION_SITES)
			.filter { (includeRoads || (it.structureType != STRUCTURE_ROAD)) }

	return sites.getOrNull(0)
}

//fun isExtractorSite(pos:RoomPosition) {
//	Game.rooms.get(pos.roomName)
//	//todo checking
//	pos.room.find(FIND_FLAGS).any { !it.pos.isEqualTo( pos) }
//}

fun Creep.behaviourBuild(): BehavourReturn {
	//var target = getClosestStructureToBuild(includeRoads =false)

	var target = room.find(FIND_CONSTRUCTION_SITES)
			.filter { (it.structureType != STRUCTURE_ROAD) }
			.filter { it.structureType != STRUCTURE_CONTAINER }
//			.filter { constructionSite -> !room.find(FIND_FLAGS)
//					.any {it.name.startsWith("extactor") && it.pos.isEqualTo( constructionSite.pos) }  }
			.getOrNull(0)

	if (target == null) {
		//nothing to build
		return BehavourReturn.CONTINUE_RUNNING
	}
	memory.behaviour.targetPos = target.pos
	if (build(target!!) == ERR_NOT_IN_RANGE) {
		moveTo(target.pos)
		return BehavourReturn.STOP_RUNNING
	} else {
		return BehavourReturn.STOP_RUNNING
	}

	return BehavourReturn.CONTINUE_RUNNING
}
fun buildPriority(structure: ConstructionSite): Int {
	if (structure == STRUCTURE_ROAD) {
		return 1;
	}
	return 0

}
fun Creep.behaviourBuildWhileMoving(): Boolean {
//	console.log("calling behaviourBuildWhileMoving")
//	console.log("behaviourBuildWhileMoving: for $name at $pos")
	if (isHarvesting()) {
//		console.log("behaviourBuildWhileMoving: in harvesting mode")
		//do not build anything while picking up energy.
		return false
	}

	if (carry.energy < ((3 * carryCapacity) / 4)) {
		//only build with the first 1/4 of the energy that it is carrying
//		console.log("behaviourBuildWhileMoving: not carrying enough ")
		return false
	}
//	console.log("apple")
	var sortedTargets = room.find(FIND_CONSTRUCTION_SITES)
			.filter { isInBuildDistance(it.pos, pos) }
			.sortedBy { buildPriority(it) }.reversed()
			.sortedBy {it.progressTotal- it.progress}

//	console.log("targets are: ${sortedTargets}")

	var target = sortedTargets
			.getOrNull(0)
//	var target = getClosestStructureToBuild()
	if (target == null) {
//		console.log("behaviourBuildWhileMoving: target is null")
		//nothing to build
		return false
	}
	val code = build(target!!)
//	console.log("behaviourBuildWhileMoving: build $target with a code of $code")

	if (code == OK) {
//		console.log("build it $target \n")
		return true
	} else if (code == ERR_NOT_IN_RANGE) {
//		console.log("behaviourBuildWhileMoving: tried to build a target that was out of range")
	}

	return false
}



fun isInBuildDistance(pos1: RoomPosition, pos2: RoomPosition): Boolean {
	return pos1.inRangeTo(pos2, 3)
}

fun Creep.behaviourUpgradeWhileMoving(): Boolean {
	if (isHarvesting()) {
		//do not build anything while picking up energy.
		return false
	}

	if (carry.energy < ((3 * carryCapacity) / 4)) {
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

	if (carry.energy < (carryCapacity) / 2) {
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
//	console.log("chosen target is $target at ${target.pos}")
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
		val newPos: RoomPosition = directions[0]
		moveTo(newPos)

	}
	return false
}

fun Creep.moveOffBaseStoragePos(): BehavourReturn {
	if (room.memory.bunker.mainStorePos == null) {
		return BehavourReturn.CONTINUE_RUNNING
	}
	val p = Bunker(room).storagePos()
	if (p == null) {
		return BehavourReturn.CONTINUE_RUNNING
	}
//	console.log("move off base storage bunker ${p} creep ${pos}")
	if (pos.isEqualTo(p)) {
		//move in a random direction
		val directions = getAdjcentSquares(p)
		directions.shuffle()
//		val index = RandInt()
		val newPos: RoomPosition = directions[0]
		moveTo(newPos)

	}
	return BehavourReturn.CONTINUE_RUNNING
}

fun Creep.behaviourDeposit(): BehavourReturn {
	//find the closest place to deposit energy in
	var targets = room.find(FIND_MY_STRUCTURES)
			.filter {
				(it.structureType == STRUCTURE_EXTENSION
						|| it.structureType == STRUCTURE_SPAWN)
			}
			.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }

	if (targets.isEmpty()) {
		targets = room.find(FIND_MY_STRUCTURES)
				.filter { (it.structureType == STRUCTURE_TOWER) }
				.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }
	}

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
			return BehavourReturn.STOP_RUNNING
		} else if (output == OK) {
			//todo drop energy for builder
			return BehavourReturn.STOP_RUNNING
		}
	} else {
		//console.log("dropBehavour could not find anything")
	}
	return BehavourReturn.CONTINUE_RUNNING
}

fun Creep.behaviourRefillBuilders(): BehavourReturn {
	//find the closest place to deposit energy in
	val bunker = Bunker(room)
	console.log("behaviourRefillBuilders: bunker stored energy is ${bunker.storedEnergy()}")
	if (bunker.storedEnergy() < room.energyCapacityAvailable *3) {
		return BehavourReturn.CONTINUE_RUNNING
	}

	val targets = room.find(FIND_MY_CREEPS)
			.filter { (it.memory.role == Role.BUILDER) }
			.filter { (it.carry.energy * 4 < it.carryCapacity * 3) } // 3/4 of capactiy


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
			return BehavourReturn.STOP_RUNNING
		}
	} else {
		//console.log("dropBehavour could not find anything")
	}
	return BehavourReturn.CONTINUE_RUNNING
}