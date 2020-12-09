package starter.utils

import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureStorage
import starter.Bunker
import starter.behaviour
import starter.behaviours.depositEnergyAt
import starter.behaviours.loadPosFromMemory
import starter.bunker
import starter.multiAI.Role
import starter.role


fun getMiningPoints(): List<Flag> {
	return Game.flags.values.filter { it.name.contains("extractor") }
}




fun energyOnPos (pos: RoomPosition) : Int {
	return 0
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

//TODO rename this to a better name.
fun energyToBePickedUpAtPoint(pos: RoomPosition, role: Role) : Int {
	var energyAtPoint = energyOnPos(pos)

	val creeps = Game.creeps.values
			.filter { it.memory.role == role }
			.filter { targetPosIsEqualTo(it, pos) }

	for (creep in creeps) {
		energyAtPoint -= creep.carryCapacity
	}

	return energyAtPoint
}

fun isMiningPoint(pos: RoomPosition) : Boolean {
	val flags = pos.findInRange(FIND_FLAGS,0)
			.filter { isFlagMiningPoint(it) }
	return !flags.isNullOrEmpty()
}

fun isFlagMiningPoint(flag :Flag) : Boolean{
	return flag.name.contains("extractor")
}

fun totalResourceOnPos(pos: RoomPosition, resourceType: ResourceConstant) : Int {

	var totalEnergy : Int = 0

	val freeResouce = pos.findInRange(FIND_DROPPED_RESOURCES,0).
			filter { it.resourceType == resourceType }

	totalEnergy += freeResouce.sumBy { it.amount }

	//next tombstones
	val containerResouce : Int =  pos.findInRange(FIND_STRUCTURES,0).
			filter { it.structureType == STRUCTURE_CONTAINER }
			.sumBy { val s = it as StructureContainer; s.store.energy }

	totalEnergy += containerResouce

	val storageResouce =  pos.findInRange(FIND_STRUCTURES,0).
			filter { it.structureType == STRUCTURE_STORAGE }
			.sumBy { val s = it as StructureStorage; s.store.energy }

	totalEnergy += storageResouce


	return totalEnergy
}

fun pickUpResourceOnPos_Free(creep: Creep, pos: RoomPosition, resourceType: ResourceConstant) : Boolean {
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

fun pickUpResourceOnPos_Stored(creep: Creep, pos: RoomPosition, resourceType: ResourceConstant ) : Boolean {

	var stores = pos.findInRange(FIND_STRUCTURES,1)
			.filter { it.structureType == STRUCTURE_STORAGE}
			.filter { var s = it as StructureStorage; s.store.energy > 0 }
			.sortedByDescending { var s = it as StructureStorage; s.store.energy }

	for (store in stores) {
		if (creep.withdraw(store, resourceType) == OK) {
			return true
		}
	}

	var containers = pos.findInRange(FIND_STRUCTURES,1)
			.filter { it.structureType == STRUCTURE_CONTAINER}
			.filter { var s = it as StructureContainer; s.store.energy > 0 }
			.sortedByDescending { var s = it as StructureContainer; s.store.energy }

	for (container in containers) {
		if (creep.withdraw(container, resourceType) == OK) {
			return true
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