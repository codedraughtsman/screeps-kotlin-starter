package starter.behaviours

import screeps.api.*
import starter.Role
import starter.bunker
import starter.role

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
	return room.controller!!.level >=2
}

/*
picks up any of the energy at the position
 */
fun Creep.pickupEnergy(targetPos: RoomPosition): ScreepsReturnCode {
	//todo just try to pick up at location. no need for checking
	val newPos = room.getPositionAt(targetPos.x, targetPos.y)
	if (newPos == null) {
		console.log("invalid pos in pickupEnergy")
	}


	val sources = newPos?.findInRange(FIND_SOURCES_ACTIVE, 1)
	if (!sources.isNullOrEmpty()) {
		return harvest(sources[0])
	}
	val structures = newPos?.findInRange(FIND_MY_STRUCTURES, 1)
	if (!structures.isNullOrEmpty()) {
		val containers = structures?.filter { it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE }
		if (containers.isNotEmpty()) {
			return withdraw(containers[0], RESOURCE_ENERGY)
		}
	}

	return ERR_NOT_IN_RANGE
}

fun Creep.behaviourDropOffEnergy(targetPos: RoomPosition): ScreepsReturnCode {
	val newPos = room.getPositionAt(targetPos.x, targetPos.y)
	if (newPos == null) {
		console.log("invalid pos in pickupEnergy")
	}
	val targets = room.find(FIND_MY_STRUCTURES).filter { it.pos == targetPos }
	if (!targets.isNullOrEmpty()) {
		return transfer(targets[0], RESOURCE_ENERGY)
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


fun Creep.getClosestStructureToBuild(): ConstructionSite? {
	return pos.findClosestByPath(FIND_MY_CONSTRUCTION_SITES)
}
