package starter.behaviours

import screeps.api.*
import starter.Role
import starter.behaviour
import starter.role



fun Creep.behaviourHarvestExtractor(): Boolean {

	if (memory.behaviour.targetPos == null) {
		//want to recalulate
		memory.behaviour.sourcePos = null

		memory.behaviour.targetPos = findNearestFreeExtractorFlag(this, Role.EXTRACTOR)
		if (memory.behaviour.targetPos == null) {
			console.log("behaviourHarvestExtractor: error, could not find a free extractor flag")
			return false
		}
	}

	val targetPos = loadPosFromMemory(memory.behaviour.targetPos!!)

	if (memory.behaviour.sourcePos == null) {
		//find adjcent source to the
		val sources = targetPos.findInRange(FIND_SOURCES, 2)
		if (sources.isNullOrEmpty()) {
			console.log("behaviourHarvestExtractor: error, there is no source next to extractor flag")
			return false
		}
		memory.behaviour.sourcePos = sources[0].pos
	}


	if (pos.x != targetPos.x || pos.y != targetPos.y || pos.roomName != targetPos.roomName) {
		moveTo(targetPos)
		say("moving to $targetPos")
		return true
	}


	val sourcePos = loadPosFromMemory(memory.behaviour.sourcePos!!)
	val sources = sourcePos.findInRange(FIND_SOURCES_ACTIVE, 1)
	if (!sources.isNullOrEmpty()) {
		 harvest(sources[0])
	}

//	if (pickupEnergyFromPosition(sourcePos) != OK) {
//		console.log("behaviourHarvestExtractor: error, failed to harvest energy from $targetPos ")
//	}
	say("pickup energy")
	if (carry.energy > 0) {
		drop(RESOURCE_ENERGY)
	}

	return true
}



fun Creep.behaviourBuildContainer(): Boolean {
	if (carry.energy == 0) {
		//cannot build anything
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