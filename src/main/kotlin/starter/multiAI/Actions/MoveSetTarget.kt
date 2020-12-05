package starter.multiAI.Actions

import screeps.api.*
import starter.Bunker
import starter.behaviour
import starter.behaviours.loadPosFromMemory
import starter.multiAI.MultiAI
import starter.multiAI.Role
import starter.role
import starter.utils.findClosestBunker

fun getMiningPoints(): List<Flag> {
	return Game.flags.values.filter { it.name.contains("extractor") }
}




fun energyOnPos (pos: RoomPosition) : Int {
	return 0
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

object MoveSetTarget {
//	fun freeMiningPoints(creep: Creep) : MultiAI.ReturnType {
//
//		var extractorCreeps = Game.creeps.values
//				.filter { it.memory.role == Role.HAULER_EXTRACTOR }
//
//		var freeMiningPoints = getMiningPoints()
//				.filter { miningPoint ->extractorCreeps.count {
//					var creepPos = loadPosFromMemory( it.memory.behaviour.targetPos );
//					creepPos.isEqualTo(miningPoint)
//				} > 0 }
//
//
//		return MultiAI.ReturnType.CONTINUE
//	}
	fun baseStoreIfCarrying(creep: Creep) : MultiAI.ReturnType {
		console.log("starting baseStoreIfCarrying")
		if (creep.carry.energy == 0) {
			//cannot drop off energy for the creep has none.
			return MultiAI.ReturnType.CONTINUE
		}

	return baseStore(creep)
	}

	fun baseStore(creep: Creep) : MultiAI.ReturnType {
		var closestBunker :Bunker? = findClosestBunker(creep.pos)
		if (closestBunker == null) {
			//we have no bunkers yet.
			return MultiAI.ReturnType.CONTINUE
		}
		creep.memory.behaviour.targetPos = closestBunker.storagePos()
//		console.log("setting creep target pos to closest bunker ${closestBunker.storagePos()}")
		return MultiAI.ReturnType.STOP
	}

	fun miningPointWithMostResouceToPickup(creep: Creep) : MultiAI.ReturnType {
		var miningFlags = getMiningPoints()
				.sortedByDescending { energyToBePickedUpAtPoint(it.pos, Role.HAULER_EXTRACTOR) }

		val target = miningFlags.firstOrNull()
		if (target == null){
			return MultiAI.ReturnType.CONTINUE
		}

		creep.memory.behaviour.targetPos = target.pos
		return MultiAI.ReturnType.STOP
	}
}