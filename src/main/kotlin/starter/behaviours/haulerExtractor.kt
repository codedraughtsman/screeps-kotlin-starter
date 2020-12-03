package starter.behaviours

import screeps.api.*
import screeps.api.structures.StructureExtension
import starter.*
import starter.multiAI.Role


fun Creep.behavourDepositEnergyInBaseStorage(): BehavourReturn {
	if (isHarvesting()){
		return BehavourReturn.CONTINUE_RUNNING
	}

	val flag = Game.flags.values.filter { it.name.startsWith("base") }
			//.sortedBy {  movedistance}
			.getOrNull(0)

	if (flag == null){
		console.log("behavourDepositEnergyInBaseStorage: error no valid base flags found for entire game")
		return BehavourReturn.CONTINUE_RUNNING
	}
	val baseRoom = flag.room!!



	if (baseRoom.memory.bunker.mainStorePos== null) {
		console.log("behavourDepositEnergyInBaseStorage, error, no main storage pos set")
		return BehavourReturn.CONTINUE_RUNNING
	}
	//todo what if the storage is full? should it be deposited some where else?
	//val targetPos = loadPosFromMemory(room.memory.bunker.mainStorePos!!)


	val targetPos = Bunker(baseRoom).storagePos()
	if (targetPos == null) {
		console.log("invalid pos in behavourDepositEnergyInBaseStorage")
		return BehavourReturn.CONTINUE_RUNNING
	}
	console.log("trying to depost ")
	if (depositEnergyAt(targetPos) == ERR_NOT_IN_RANGE) {
//		drop(RESOURCE_ENERGY)
		moveTo(targetPos)
		return BehavourReturn.STOP_RUNNING
	}
	return BehavourReturn.STOP_RUNNING

}

fun Creep.behavourRepairInRange() : BehavourReturn {
	var targets = pos.findInRange(FIND_STRUCTURES, 3)
			.filter { it.hits != it.hitsMax }
			.sortedBy { (100000.0 *it.hits)/ (100000.0 *it.hitsMax) }
//			.sortedBy { if (it.structureType == STRUCTURE_ROAD) return 1; return 0 }
	for (target in targets) {
		if (repair(target)== OK) {
			return BehavourReturn.STOP_RUNNING
		}
	}
	return BehavourReturn.CONTINUE_RUNNING
}

private fun findNearestExtractorThatNeedAHauler(creep: Creep): RoomPosition? {
	val extractorCreeps = Game.creeps.values.filter { it.memory.role == Role.EXTRACTOR }
	val haulerCreeps = Game.creeps.values.filter { it.memory.role == Role.HAULER_EXTRACTOR }
	var leastPos :RoomPosition? = null
	var leastMatch = Int.MAX_VALUE
	for (extractor in extractorCreeps){
		if (extractor.memory.behaviour.targetPos == null) {
			continue
		}
		val targetPos = loadPosFromMemory(extractor.memory.behaviour.targetPos!!)
		var foundMatch = 0
		for (hauler in haulerCreeps) {
			if (hauler.memory.behaviour.sourcePos == null){
				continue
			}
			val source = loadPosFromMemory(hauler.memory.behaviour.sourcePos!!)
			if (source.isEqualTo(targetPos)){
				foundMatch ++
			}
		}
		if (foundMatch == 0) {
			return extractor.memory.behaviour.targetPos
		}
		if (foundMatch < leastMatch) {
			leastMatch = foundMatch
			leastPos = targetPos
		}

	}
	return leastPos
}

fun Creep.behavourFillUpAdjcentExtentions() : Boolean {
	var targets = pos.findInRange(FIND_MY_STRUCTURES,1).filter { it.structureType == STRUCTURE_EXTENSION }
			.filter{var x:StructureExtension =it as StructureExtension; return x.energy < x.energyCapacity }

	//now select one
	if (! targets.isNullOrEmpty()) {
		depositEnergyAt(targets[0].pos)
	}
	return false
}

fun Creep.behaviourHaulerPickup(): BehavourReturn {
	if (!isHarvesting()){
		return BehavourReturn.CONTINUE_RUNNING
	}
	if (memory.behaviour.sourcePos == null) {
		memory.behaviour.sourcePos = findNearestExtractorThatNeedAHauler(this)
		if (memory.behaviour.sourcePos == null) {
			console.log("behaviourHaulerPickup: error, could not find a free extractor flag")
			return BehavourReturn.CONTINUE_RUNNING
		}
	}
	console.log("this is creep ${this.name}")
	val targetPos = loadPosFromMemory(memory.behaviour.sourcePos!!)
//	if (pos != targetPos) {
//		//todo never actually stand on the flag because it will block the extractor
//		moveTo(targetPos)
//		return true
//	}

	if (pos.inRangeTo( targetPos, 1)){
		console.log("trying to pickup energy from ${targetPos}")
		pickupEnergyFromPosition(targetPos)
		memory.isCollectingEnergy = false
	}else {
		moveTo(targetPos)
	}
//
//	if (pickupEnergyFromPosition(pos) != OK) {
//		moveTo(targetPos)
//		//console.log("behaviourHaulerPickup: error, failed to harvest energy from $targetPos ")
//	}

	return BehavourReturn.CONTINUE_RUNNING
}
/*
fun Creep.behaviourDepositEnergyInNearestStorage(): Boolean {
	//find the closest place to deposit energy in

	var targets = room.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_STORAGE) }

	//todo what if the storage is full? should it be deposited some where else?
	targets = targets.filter { it.unsafeCast<Store>().storeCapacity > it.unsafeCast<Store>().store.energy }


	if (targets.isNotEmpty()) {
		var bestPos = targets[0].pos
		var bestCost: Int? = null
		for (target in targets) {
			//val target = targets[0]
			val path = PathFinder.search(pos, target.pos)
			val cost: Int = path.cost
//			console.log(" looking at path from $pos to ${target.pos} cost $cost")
			if (bestCost == null || cost < bestCost) {
				bestCost = cost
				bestPos = target.pos
			}
		}
//		console.log("best path for store is from: $pos to $bestPos cost $bestCost")

		memory.behaviour.targetPos = bestPos
		if (depositEnergyAt(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//memory.behaviour.gotoPos =memory.behaviour.targetPos
			moveTo(bestPos)
			return true
		}
	} else {
		var targets = room.find(FIND_FLAGS)
				.filter { (it.name.contains("store", ignoreCase = true))}
		if (targets.isNotEmpty()) {
			val bestPos = targets[0].pos
			memory.behaviour.targetPos = bestPos
			if (depositEnergyAt(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
				//memory.behaviour.gotoPos =memory.behaviour.targetPos
				moveTo(bestPos)
				return true
			}
		}
		console.log("storBehavour could not find anything, no targets")
	}
	return false
}
*/
