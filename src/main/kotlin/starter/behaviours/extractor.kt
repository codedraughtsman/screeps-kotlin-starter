package starter.behaviours

import screeps.api.*
import starter.Role
import starter.behaviour
import starter.role

fun findNearestFreeExtractorFlag(creep: Creep, creepRole: Role): RoomPosition? {
	val flags = creep.pos.lookFor(LOOK_FLAGS)
	val flagOnSquare = (flags != null &&
			flags.any { it.name.startsWith("extractor", true) })
	if (flagOnSquare) {
		//we are standing on an extractor flag. lets grab it.
		return creep.pos
	}

//	val flagPositions = creep.room.find(FIND_FLAGS)
//			.filter { it.name.startsWith("extractor", true) }
	val flagPositions = Game.flags.values
			.filter { it.name.startsWith("extractor", true) }
	//.sortedByDescending { creep.pos.get }

	if (flagPositions.isNullOrEmpty()) {
		return null
	}

	//todo sort the flags by distance to creep

	for (flag in flagPositions) {
		if (Game.creeps.values.count {
					it.memory.role == creepRole && it.memory.behaviour.targetPos != null
							&& it.memory.behaviour.targetPos!!.x == flag.pos.x
							&& it.memory.behaviour.targetPos!!.y == flag.pos.y
							&& it.memory.behaviour.targetPos!!.roomName == flag.pos.roomName
				} == 0) {
			return flag.pos
		}
	}

	return null
}


fun Creep.behaviourHarvestExtractor(): Boolean {

	if (memory.behaviour.targetPos == null) {

		memory.behaviour.targetPos = findNearestFreeExtractorFlag(this, Role.EXTRACTOR)
		if (memory.behaviour.targetPos == null) {
			console.log("behaviourHarvestExtractor: error, could not find a free extractor flag")
			return false
		}
	}

	val targetPos = loadPosFromMemory(memory.behaviour.targetPos!!)

	if (!pos.isEqualTo(targetPos)) {
		moveTo(targetPos)
		return true
	}

	val sources = targetPos.findInRange(FIND_SOURCES_ACTIVE, 1)
	for (source in sources) {
		harvest(source)
	}


	if (carry.energy > 0) {
		drop(RESOURCE_ENERGY)
	}
	return true

}

//
//fun Creep.behaviourBuildContainer(): Boolean {
//	if (carry.energy == 0) {
//		//cannot build anything
//		return false
//	}
//	var target = getClosestStructureToBuild()
//
////	var target = room.lookAt( pos.x, pos.y).filter { (it.type == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE)  }
//	if (target == null || target.pos.x != pos.x || target.pos.y != pos.y) {
//		//nothing to build
//		return false
//	}
//	//memory.behaviour.targetPos = target.pos
//	if (build(target) == ERR_NOT_IN_RANGE) {
//		return true
//	} else {
//		return true
//	}
//
//	return false
//}