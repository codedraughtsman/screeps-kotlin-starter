package starter.behaviours

import screeps.api.*
import starter.*

fun Creep.behavourDepositEnergyInBaseStorage(): Boolean {
	if (isHarvesting()){
		return false
	}

	val flag = Game.flags.values.filter { it.name.startsWith("base") }
			//.sortedBy {  movedistance}
			.getOrNull(0)

	if (flag == null){
		console.log("behavourDepositEnergyInBaseStorage: error no valid base flags found for entire game")
		return false
	}
	val baseRoom = flag.room!!



	if (baseRoom.memory.bunker.mainStorePos== null) {
		console.log("behavourDepositEnergyInBaseStorage, error, no main storage pos set")
		return false
	}
	//todo what if the storage is full? should it be deposited some where else?
	//val targetPos = loadPosFromMemory(room.memory.bunker.mainStorePos!!)


	val targetPos = Bunker(baseRoom).storagePos()
	if (targetPos == null) {
		console.log("invalid pos in behavourDepositEnergyInBaseStorage")
		return false
	}
	console.log("trying to depost ")
	if (depositEnergyAt(targetPos) == ERR_NOT_IN_RANGE) {
		moveTo(targetPos)
		return true
	}
	return true

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

fun Creep.behaviourHaulerPickup(): Boolean {
	if (!isHarvesting()){
		return false
	}
	if (memory.behaviour.sourcePos == null) {
		memory.behaviour.sourcePos = findNearestExtractorThatNeedAHauler(this)
		if (memory.behaviour.sourcePos == null) {
			console.log("behaviourHaulerPickup: error, could not find a free extractor flag")
			return false
		}
	}

	val targetPos = loadPosFromMemory(memory.behaviour.sourcePos!!)
//	if (pos != targetPos) {
//		//todo never actually stand on the flag because it will block the extractor
//		moveTo(targetPos)
//		return true
//	}

	if (pickupEnergyFromPosition(targetPos) != OK) {
		moveTo(targetPos)
		//console.log("behaviourHaulerPickup: error, failed to harvest energy from $targetPos ")
	}

	return false
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
