package starter.behaviours

import screeps.api.*
import screeps.api.structures.Structure
import starter.Role
import starter.behaviour
import starter.bunker
import starter.role

fun loadPosFromMemory(targetPos: RoomPosition): RoomPosition {
	return RoomPosition(targetPos!!.x, targetPos!!.y, targetPos!!.roomName)
}

private fun displayErrorCode(errorCode: ScreepsReturnCode, msg: String = "") {
	if (errorCode != OK) {
		console.log("got ${errorCode} error code for ${msg}")
	}
}

fun Creep.positionHasEnergy(pos: RoomPosition): Boolean {
	if (room.controller == null || memory.role == Role.HARVESTER) {
		console.log("Creep.positionHasEnergy")
		return false
	}
	return room.controller!!.level >= 2
}

fun Creep.behavourPickUpEnergyFromTarget() : Boolean {
	return false
}

fun Creep.behavourSetTarget_GetEnergyFromCollectors() :Boolean {
	//find the closest collector that has energy.
	//order by energythatcan be picked up divided by trip distance
	return false
}


/*
picks up any of the energy from storage OR mines it if is a source.
 */
fun Creep.pickupEnergyFromPosition(targetPos: RoomPosition): ScreepsReturnCode {


	val resourceToPickup = targetPos.findInRange(FIND_DROPPED_RESOURCES, 1)

	for (resource in resourceToPickup) {
		if (pickup(resource) == OK) {
			return OK
		}
	}


	val structures = targetPos.findInRange(FIND_MY_STRUCTURES, 1)
	if (!structures.isNullOrEmpty()) {
		val containers = structures.filter { it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE }
		if (containers.isNotEmpty()) {
			return withdraw(containers[0], RESOURCE_ENERGY)
		}
	}

	val sources = targetPos.findInRange(FIND_SOURCES_ACTIVE, 1)
	if (!sources.isNullOrEmpty()) {
		return harvest(sources[0])
	}
	return ERR_NOT_IN_RANGE
}

fun Creep.depositEnergyAt(targetPos: RoomPosition): ScreepsReturnCode {
	val newPos = room.getPositionAt(targetPos.x, targetPos.y)
	if (newPos == null) {
		console.log("invalid pos in pickupEnergy")
	}

	//try to transfer to target
	val targets = room.find(FIND_MY_STRUCTURES).filter { it.pos.isEqualTo(targetPos) }

	for (structure in targets) {
//		console.log("trying to transfer to ${structure.pos}")
		if (transfer(structure, RESOURCE_ENERGY) == OK) {
			console.log("it workes")
			return OK
		}
	}


	//it might be a creep
	val creepTargets = room.find(FIND_MY_CREEPS).filter { it.pos == targetPos }
	if (!creepTargets.isNullOrEmpty()) {
		for (structure in creepTargets) {
			if (transfer(structure, RESOURCE_ENERGY) == OK) {
				return OK
			}
		}
	}

	if (pos.x == targetPos.x && pos.y == targetPos.y && pos.roomName == targetPos.roomName) {
		drop(RESOURCE_ENERGY)
	}
	return ERR_NOT_IN_RANGE
}


fun Creep.getClosestSourceOfEnergy(): RoomPosition? {
	if (room.memory.bunker.mainStorePos != null) {
		val bunkerStoragePos = room.getPositionAt(room.memory.bunker.mainStorePos!!.x, room.memory.bunker.mainStorePos!!.y)
		if (bunkerStoragePos != null && positionHasEnergy(bunkerStoragePos)) {
			return bunkerStoragePos
		}
	}
	if (room.controller!!.level > 2) {
		//var container = pos.findClosestByPath(FIND_MY_STRUCTURES, jsObject { i} )
		//TODO fix this to use containers


		var bestLocation: RoomPosition? = null
		var bestDistance: Int? = null
		//val collectors = room.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }

		// target prioraty list
		// storage, then flag with at least one hauler creep on the map, then containers that are not next to a source (extractors ones), then sources.

		var targets = room.find(FIND_MY_STRUCTURES)
				.filter { (it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE) }
		//console.log("not storage filtering ${targets}")
		targets = targets.filter { it.unsafeCast<Store>().storeCapacity > it.unsafeCast<Store>().store.energy }


		for (target in targets) {
			val distance = PathFinder.search(pos, target.pos).cost
			if (bestDistance == null || distance < bestDistance) {
				bestLocation = target.pos
				bestDistance = distance
			}
		}


		val collectors = room.find(FIND_FLAGS).filter { it.name.startsWith("extractor", ignoreCase = true) }
		if (room.find(FIND_MY_CREEPS).count { it.memory.role == Role.EXTRACTOR } > 0 && collectors.isNotEmpty()) {
			for (c in collectors) {
				val distance = PathFinder.search(pos, c.pos).cost
				if (bestDistance == null || distance < bestDistance) {
					bestLocation = c.pos
					bestDistance = distance
				}
			}
		}

		if (bestLocation != null) {
			return bestLocation
		}
	}

	var source = pos.findClosestByPath(FIND_SOURCES_ACTIVE)
	if (source != null) {
		return source.pos
	}
	return null
}


fun healthPercentage(creep: Creep): Int {
	return (100 * creep.hits) / creep.hitsMax
}

fun Creep.getStructureInRangeToRepair(): Structure? {
	val structures = pos.findInRange(FIND_STRUCTURES, 3)
	var leastHitPointsPercentage = Int.MAX_VALUE
	var outputStructure: Structure? = null

	for (structure in structures) {
		console.log("structure ${structure.structureType }at ${structure.pos} has ${structure.hitsMax} hitMax, and ${structure.hits} hits")
		val hitPointsLeftPercentage = healthPercentage(this)
		if (hitPointsLeftPercentage < leastHitPointsPercentage) {
			leastHitPointsPercentage = hitPointsLeftPercentage
			outputStructure = structure
		}
	}
	return outputStructure
}


fun isTraversable(pos: RoomPosition): Boolean {

	//todo
	return true
}