package starter.multiAI.Actions

import screeps.api.*
import starter.Bunker
import starter.behaviour
import starter.behaviours.buildPriority
import starter.behaviours.loadPosFromMemory
import starter.multiAI.MultiAI
import starter.multiAI.Role
import starter.role
import starter.utils.*


object MoveSetTarget {
	fun upgraderPoint(creep: Creep) :MultiAI.ReturnType {
		val depositorFlag = creep.room.find(FIND_FLAGS)
				.filter { it.name.contains("depositor") }
				.filter { noCreepHasPosAsTarget(it.pos) }
				.firstOrNull()

		if (depositorFlag == null) {
			return MultiAI.ReturnType.CONTINUE
		}
		creep.memory.behaviour.targetPos = depositorFlag.pos
		return MultiAI.ReturnType.CONTINUE
	}
	fun constructionSite(creep:Creep) : MultiAI.ReturnType {
		val sites = creep.room.find(FIND_CONSTRUCTION_SITES)
				.sortedByDescending { (it.progress *1000) / (it.progressTotal *1000) }
				.sortedByDescending { buildPriority(it) }
				.sortedBy { creep.pos.getRangeTo(it) }
				.firstOrNull()

//		console.log("constructionSite is ${sites} ${sites!!.pos}")
		if (sites != null) {

			creep.memory.behaviour.targetPos = sites.pos
		}


		return MultiAI.ReturnType.CONTINUE
	}
	fun baseStoreIfCarrying(creep: Creep) : MultiAI.ReturnType {
//		console.log("starting baseStoreIfCarrying")
		if (creep.carry.values.sum() == 0) {
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
				.filter { resourceToBePickedUpAtPoint(it.pos, Role.HAULER_EXTRACTOR) > 0 }
				.sortedByDescending { resourceToBePickedUpAtPoint(it.pos, Role.HAULER_EXTRACTOR) }
		console.log("sorted mining flags ${miningFlags}")
		for (flag in miningFlags) {
			console.log("flag ${flag} is ${resourceToBePickedUpAtPoint(flag.pos, Role.HAULER_EXTRACTOR)}")
		}
		val target = miningFlags.firstOrNull()
		if (target == null){
			return MultiAI.ReturnType.CONTINUE
		}
		console.log("miningPointWithMostResouceToPickup is ${target} at ${target.pos.x} ${target.pos.y} ${target.pos.roomName}")
		creep.memory.behaviour.targetPos = target.pos
		return MultiAI.ReturnType.STOP
	}
}