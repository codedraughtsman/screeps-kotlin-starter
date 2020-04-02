package starter

import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.utils.unsafe.jsObject
import kotlin.UInt.Companion.MAX_VALUE

enum class Behavours {
	UNDEFINED,
	GOTO,
	HARVEST,
	PICKUP,
	DEPOSIT,
	BUILD,
	UPGRADE,
	EXTRACT,
	HAULER_PICKUP,
	STORE_ENERGY,
	BUILD_CONTAINER
}

fun Creep.runBehaviour() {
	globalBehavour()
	var behavours = getBehavioursForRole(memory.role as Role)
	for (behaviour in behavours) {
		val isFinshed = runTheBehaviour(behaviour)
		if (isFinshed) {
			break
		}
	}
	if (memory.role != Role.HARVESTER) {
		behaviourBuildWhileMoving()
	}
}

private fun displayErrorCode(errorCode: ScreepsReturnCode, msg: String = "") {
	if (errorCode != OK) {
		console.log("got ${errorCode} error code for ${msg}")
	}
}

fun Creep.globalBehavour() {
	updateIsCollectingEnergy()

	val resourceToPickup = pos.findInRange(FIND_DROPPED_RESOURCES, 1)
	if (!resourceToPickup.isNullOrEmpty()) {
		//todo grab the biggest resource available
		val errorCode = pickup(resourceToPickup[0])
		//displayErrorCode(errorCode, "pickup resource behavour")
	}
}

fun getBehavioursForRole(role: Role): MutableList<Behavours> {
	var out: MutableList<Behavours> = arrayListOf()
	when (role) {
		Role.HARVESTER -> out = arrayListOf(Behavours.PICKUP, Behavours.DEPOSIT, Behavours.BUILD, Behavours.UPGRADE)
		Role.BUILDER -> out = arrayListOf(Behavours.GOTO, Behavours.PICKUP, Behavours.BUILD, Behavours.DEPOSIT, Behavours.UPGRADE)
		Role.UPGRADER -> out = arrayListOf(Behavours.PICKUP, Behavours.UPGRADE)
		Role.EXTRACTOR -> out = arrayListOf(Behavours.BUILD_CONTAINER,Behavours.EXTRACT) //TODO static build and upgrade
		Role.HAULER -> out = arrayListOf(Behavours.HAULER_PICKUP, Behavours.STORE_ENERGY, Behavours.DEPOSIT, Behavours.BUILD, Behavours.UPGRADE)
	}
	return out
}

private fun Creep.runTheBehaviour(behaviour: Behavours): Boolean {
	var isFinished = false
	when (behaviour) {
		Behavours.GOTO -> isFinished = behaviourGoto()
		Behavours.PICKUP -> isFinished = behaviourPickup()
		Behavours.DEPOSIT -> isFinished = behaviourDeposit()
		Behavours.BUILD -> isFinished = behaviourBuild()
		Behavours.UPGRADE -> isFinished = upgrade(room.controller!!)
		Behavours.EXTRACT -> isFinished = extractor()
		Behavours.HAULER_PICKUP -> isFinished = behaviourHaulerPickup()
		Behavours.STORE_ENERGY -> isFinished = behaviourStore()
		Behavours.BUILD_CONTAINER -> isFinished = behaviourBuildContainer()

	}
	return isFinished
}

fun Creep.behaviourGoto(): Boolean {
	if (memory.behaviour.gotoPos == null) {
		return false
	}
	var target = room.getPositionAt(memory.behaviour.gotoPos!!.x, memory.behaviour.gotoPos!!.y)
	if (target == null) {
		console.log("behavourGoto: target did not covert to position")
	}

	if (pos == target || pos.isNearTo(target!!)) {
		//have reached destination
		memory.behaviour.gotoPos = null
		return false
	}

	var output = moveTo(target)
	//console.log("running goto, move return code is $output and gotoPos is ${memory.behaviour.gotoPos}")

	return true
}

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

fun Creep.getClosestSourceOfEnergy(): RoomPosition? {

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

fun Creep.behaviourPickup(): Boolean {
	if (!isHarvesting()) {
		return false
	}
	memory.behaviour.targetPos = getClosestSourceOfEnergy()

//	console.log("behavourPickup: from $pos to closest source of energy ${memory.behaviour.targetPos}")
	//try to harvest location
	if (memory.behaviour.targetPos != null) {
		//memory.behaviour.targetPos = room.getPositionAt( memory.behaviour.targetPos!!.x-1, memory.behaviour.targetPos!!.y)

		if (memory.role != Role.EXTRACTOR && pickupEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//go and harvest this pos
			moveTo(memory.behaviour.targetPos!!)
			return true
		} else {
			//success
			return true
			//todo use the move action as well
		}
	}

	return false
}

fun Creep.behaviourHaulerPickup(): Boolean {
	if (!isHarvesting()) {
		return false
	}
	var haulerFlags = room.find(FIND_FLAGS).filter { it.name.startsWith("extractor", true) }
	if (haulerFlags.isNullOrEmpty()) {
		return false
	}
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
			moveTo(memory.behaviour.targetPos!!.x, memory.behaviour.targetPos!!.y, opts = MoveToOptions)
			return true
		} else {
			//success
			return true
			//todo use the move action as well
		}
	}

	return false
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

fun Creep.behaviourDeposit(): Boolean {
	//find the closest place to deposit energy in
	val targets = room.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
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
		if (behaviourDropOffEnergy(memory.behaviour.targetPos!!) == ERR_NOT_IN_RANGE) {
			//memory.behaviour.gotoPos =memory.behaviour.targetPos
			moveTo(bestPos)
			return true
		}
	} else {
		//console.log("dropBehavour could not find anything")
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


private fun Creep.getClosestStructureToBuild(): ConstructionSite? {
	return pos.findClosestByPath(FIND_MY_CONSTRUCTION_SITES)
}

fun Creep.behaviourBuildWhileMoving(): Boolean {
	var target = getClosestStructureToBuild()
	if (target == null) {
		//nothing to build
		return false
	}
	if (build(target!!) == ERR_NOT_IN_RANGE) {

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
		console.log("target $target is not in range, set gotoPos to it")
		return true
	} else {
		return true
	}

	return false
}

fun Creep.behaviourBuildContainer(): Boolean {
	if (carry.energy == 0) {
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