package starter.behaviours

import screeps.api.*
import starter.*

private fun Creep.behavourDepositEnergyInBaseStorage() {
	if (room.memory.bunker.mainStorePos== null) {
		return
	}
	behaviourDeposit()
	val targetPos = room.getPositionAt(room.memory.bunker.mainStorePos!!.x, room.memory.bunker.mainStorePos!!.y)
	if (targetPos == null) {
		console.log("invalid pos in behavourDepositEnergyInBaseStorage")
		return
	}
	if (behaviourDropOffEnergy(targetPos) == ERR_NOT_IN_RANGE) {
		moveTo(targetPos)
	}

}

fun Creep.behaviourHaulerPickup(): Boolean {
	if (!isHarvesting()) {
		return false
	}
	var haulerFlags = room.find(FIND_FLAGS).filter { it.name.startsWith("extractor", true) }
	if (haulerFlags.isNullOrEmpty()) {
		return false
	}
	//find a hauler flag that on one else is using
	for (flag in haulerFlags) {
		if (Game.creeps.values.count {
					it.memory.role == Role.HAULER && it.memory.behaviour.sourcePos != null
							&& it.memory.behaviour.sourcePos!!.x == flag.pos.x
							&& it.memory.behaviour.sourcePos!!.y == flag.pos.y
				} == 0) {
			memory.behaviour.sourcePos = flag.pos
			break
		}
	}
	if (memory.behaviour.sourcePos == null) {
		console.log("haulerPickup: could not find a vacant flag")
		memory.behaviour.sourcePos = haulerFlags[0].pos
	}
	memory.behaviour.targetPos = memory.behaviour.sourcePos

	console.log("behavourPickup: from $pos to closest source of energy ${memory.behaviour.targetPos}")
	//try to harvest location
	if (memory.behaviour.targetPos != null) {
		//memory.behaviour.targetPos = room.getPositionAt( memory.behaviour.targetPos!!.x-1, memory.behaviour.targetPos!!.y)

		if (pickupEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//go and harvest this pos
			console.log("HaulerPickup out of range of pickup, moving to ${memory.behaviour.targetPos!!}")
			moveTo(memory.behaviour.targetPos!!.x, memory.behaviour.targetPos!!.y)
			return true
		} else {
			//success
			return true
			//todo use the move action as well
		}
	}

	return false
}

fun Creep.behaviourStore(): Boolean {
	//find the closest place to deposit energy in

	var targets = room.find(FIND_MY_STRUCTURES)
//			.filter { (it.structureType == STRUCTURE_CONTAINER || it.structureType == STRUCTURE_STORAGE) }
			.filter { (it.structureType == STRUCTURE_STORAGE) }

	//console.log("not storage filtering ${targets}")
	targets = targets.filter { it.unsafeCast<Store>().storeCapacity > it.unsafeCast<Store>().store.energy }
//	console.log("after storage filtering ${targets}")


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
		if (behaviourDropOffEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
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
			if (behaviourDropOffEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
				//memory.behaviour.gotoPos =memory.behaviour.targetPos
				moveTo(bestPos)
				return true
			}
		}
		console.log("storBehavour could not find anything, no targets")
	}
	return false
}