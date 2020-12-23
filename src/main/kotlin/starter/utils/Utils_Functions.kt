package starter.utils

import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureStorage
import starter.Bunker
import starter.behaviour
import starter.behaviours.loadPosFromMemory
import starter.multiAI.Role
import starter.role


fun getMiningPoints(): List<Flag> {
	return Game.flags.values.filter { it.name.contains("extractor") }
}




fun energyOnPos (pos: RoomPosition) : Int {
	try {
		var energy = 0

		var dropped = pos.findInRange(FIND_DROPPED_RESOURCES, 0)
		for (drop in dropped) {
			if (drop.resourceType == RESOURCE_ENERGY) {
				energy += drop.amount
			}
		}

		var containers = pos.findInRange(FIND_STRUCTURES, 0)
				.filter { it.structureType == STRUCTURE_CONTAINER }
		for (container in containers) {
			val cast = container as StructureContainer
			energy += cast.store.energy
		}
		return energy
	}catch (t: Throwable) {
		return 0
	}
}



fun noCreepHasPosAsTarget(pos: RoomPosition) :Boolean {
	for (creep in Game.creeps.values){
		val creepPos = getTargetPos(creep)
		if (creepPos == null) {
			continue
		}
		if (pos.isEqualTo(creepPos)) {
			return false
		}
	}
	return true
}

fun noCreepHasTargetID_AsTarget(targetID: String) :Boolean {
	for (creep in Game.creeps.values){
		val creepTargetID = creep.memory.behaviour.targetID
		if (creepTargetID == null) {
			continue
		}
		if (creepTargetID == targetID) {
			return false
		}
	}
	return true
}

fun getTargetPos(creep: Creep): RoomPosition? {
	if (creep.memory.behaviour.targetPos == null) {
		return null
	}
	return loadPosFromMemory(creep.memory.behaviour.targetPos!!)
}

fun targetPosIsEqualTo(creep: Creep, pos: RoomPosition) :Boolean {
	val targetPos = getTargetPos(creep)
	if (targetPos == null) {
		return false
	}
	return (pos.isEqualTo(targetPos!!))
}

fun resourceToBePickedUpAtPoint(pos: RoomPosition, role: Role) : Int {
	var resouceAtPoint = totalResourceOnPos(pos)

	val creeps = Game.creeps.values
			.filter { it.memory.role == role }
			.filter { targetPosIsEqualTo(it, pos) }

	for (creep in creeps) {
		resouceAtPoint -= creep.carryCapacity
	}

	return resouceAtPoint
}


fun isMiningPoint(pos: RoomPosition) : Boolean {
	val flags = pos.findInRange(FIND_FLAGS,0)
			.filter { isFlagMiningPoint(it) }
	return !flags.isNullOrEmpty()
}
fun resourceInStorage(store: StoreDefinition, resourceType: ResourceConstant?): Int {
	if (resourceType==null) {
		return store.values.sum ()
	}
	val total = store.get(resourceType)
	if (total == null) {
		return 0
	}
	return total
}

fun isFlagMiningPoint(flag :Flag) : Boolean{
	return flag.name.contains("extractor")
}

fun totalResourceOnPos(pos: RoomPosition, resourceType: ResourceConstant? =null) : Int {

	var totalEnergy : Int = 0

	var freeResouce: List<Resource> = pos.findInRange(FIND_DROPPED_RESOURCES,0).toList()
	if (resourceType != null) {
		freeResouce = freeResouce.filter { it.resourceType == resourceType }
	}

	totalEnergy += freeResouce.sumBy { it.amount }

	//next tombstones
	val containerResource : Int =  pos.findInRange(FIND_STRUCTURES,0).
			filter { it.structureType == STRUCTURE_CONTAINER }
			.sumBy { val s = it as StructureContainer; resourceInStorage(s.store,resourceType) }

	totalEnergy += containerResource

	val storageResouce =  pos.findInRange(FIND_STRUCTURES,0).
			filter { it.structureType == STRUCTURE_STORAGE }
			.sumBy { val s = it as StructureStorage; resourceInStorage(s.store, resourceType) }

	totalEnergy += storageResouce


	return totalEnergy
}

fun pickUpResourceOnPos_Free(creep: Creep, pos: RoomPosition, resourceType: ResourceConstant? = null) : Boolean {
	var resources = pos.findInRange(FIND_DROPPED_RESOURCES,1)
			.filter { it.resourceType == resourceType }
			.sortedByDescending { it.amount }

	for (resource in resources) {
		if (creep.pickup(resource) == OK) {
			return true
		}
	}

	return false
}

fun pickUpResourceOnPos_Stored(creep: Creep, pos: RoomPosition, resourceType: ResourceConstant?=null ) : Boolean {

	var stores = pos.findInRange(FIND_STRUCTURES,1)
			.filter { it.structureType == STRUCTURE_STORAGE}
			.filter { var s = it as StructureStorage; resourceInStorage(s.store, resourceType) > 0 }
			.sortedByDescending { var s = it as StructureStorage; resourceInStorage(s.store, resourceType) > 0 }
//	console.log("stores found are ${stores} at ${creep.pos}")
	for (store in stores) {
		if (resourceType != null) {
			if (creep.withdraw(store, resourceType) == OK) {
				return true
			}
		} else {
			val cast: StructureStorage = stores as StructureStorage
			console.log("store has ${cast.store.values}")
			for (withdrawalType in cast.store.keys) {
				if (creep.withdraw(store, withdrawalType) == OK) {
					return true
				}

			}
		}
	}


	var containers = pos.findInRange(FIND_STRUCTURES,1)
			.filter { it.structureType == STRUCTURE_CONTAINER}
			.filter { var s = it as StructureContainer; resourceInStorage(s.store, resourceType)  > 0 }
			.sortedByDescending { var s = it as StructureContainer; resourceInStorage(s.store, resourceType)  }
	console.log("containers found are ${stores} at ${creep.pos}")
	for (target in containers) {
		if (resourceType != null) {
			if (creep.withdraw(target, resourceType) == OK) {
				return true
			}
		} else {
			val cast: StructureContainer = target as StructureContainer
			console.log("store has ${cast.store.values}")
			for (withdrawalType in cast.store.keys) {
				if (creep.withdraw(target, withdrawalType) == OK) {
					return true
				}

			}
		}
	}

	return false
}

fun findClosestBunker (pos : RoomPosition): Bunker? {
	val baseFlags = Game.flags.values.filter { it.name.startsWith("base") }
	//.sortedBy {  movedistance}
	//TODO sort them by travel distance
	if (baseFlags.isNullOrEmpty()) {
		return null
	}
	var baseRoom : Room = baseFlags[0].room!!
	return Bunker(baseRoom)
}

fun depositEnergy(creep: Creep, depositPos: RoomPosition) : Boolean {

//	creep.depositEnergyAt(depositPos)

	//if not next to target pos
	if (!creep.pos.isNearTo(depositPos)) {
		return false
	}

	val carriedResources = creep.carry.keys

	if (creep.pos.isEqualTo(depositPos)) {
		//just drop it
		creep.say("dropping")
		for (resouceType in carriedResources) {
			if (creep.drop(resouceType) == OK) {
				return true
			}
		}
	}

	//will try to depost it
	val structure: Structure? = depositPos.findInRange(FIND_STRUCTURES, 0)
			.filter{ canContainResource(it)}
			.getOrNull(0)

	if (structure == null) {
		return false
	}

	for (resouceType in carriedResources) {
//		creep.say("transfering now")
//		console.log("trying to transfer ${creep.pos} ${structure.pos} ${structure}, ${resouceType}")
		val ret = creep.transfer(structure, resouceType)
//		console.log("ret code is ${ret}")
		if ( ret == OK) {
//			console.log("transferred ${structure}, ${resouceType}")
			return true
		}
	}

	return false
}

fun canContainResource(structure: Structure) : Boolean {
	return (structure.structureType == STRUCTURE_LAB ||
			structure.structureType == STRUCTURE_POWER_SPAWN ||
			structure.structureType == STRUCTURE_SPAWN ||
			structure.structureType == STRUCTURE_STORAGE ||
			structure.structureType == STRUCTURE_EXTENSION ||
			structure.structureType == STRUCTURE_CONTAINER )


}