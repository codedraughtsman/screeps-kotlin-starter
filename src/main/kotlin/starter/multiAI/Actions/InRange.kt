package starter.multiAI.Actions

import screeps.api.*
import starter.Bunker
import starter.multiAI.MultiAI
import starter.roomContainsBunker
import starter.utils.isFlagMiningPoint
import starter.utils.pickUpResourceOnPos_Free
import starter.utils.*
import starter.utils.totalResourceOnPos

object InRange {
	fun depositInExtension(creep: Creep) : MultiAI.ReturnType {
		val targets = creep.pos.findInRange(FIND_MY_STRUCTURES,1)
				.filter { it.structureType == STRUCTURE_EXTENSION }

		for (target in targets) {
			if (depositEnergy(creep,target.pos)) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun repair(creep: Creep) : MultiAI.ReturnType {
		val targets =
				creep.pos.findInRange(FIND_STRUCTURES, 3)
						.filter { it.hits != it.hitsMax }
						.sortedBy { (1000 *it.hitsMax) - (1000 *it.hits)}

//				.sortByDescending { (it.progress *1000) / (it.progressTotal *1000) }
		for (target in targets) {
			if (creep.repair(target) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun build(creep: Creep) : MultiAI.ReturnType {
		val constructionSites: Array<ConstructionSite> =
				creep.pos.findInRange(FIND_CONSTRUCTION_SITES, 3)

//				.sortByDescending { (it.progress *1000) / (it.progressTotal *1000) }
		for (constructionSite in constructionSites) {
			if (creep.build(constructionSite) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}
	fun upgrade(creep: Creep) : MultiAI.ReturnType {
		val target =
				creep.room.controller
		if (target == null || target.my) {
			return MultiAI.ReturnType.CONTINUE
		}


		if (creep.upgradeController(target) == OK) {
			return MultiAI.ReturnType.STOP
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun depositInBaseStorage(creep: Creep) : MultiAI.ReturnType {
		console.log("starting depositInBaseStorage")
		if (!roomContainsBunker(creep.room)){
			return MultiAI.ReturnType.CONTINUE
		}

		val base = Bunker(creep.room)
		val storePos = base.storagePos()

		if (storePos == null) {
			return MultiAI.ReturnType.CONTINUE
		}

		if (depositEnergy(creep, storePos)) {
			return MultiAI.ReturnType.STOP
		}

		return MultiAI.ReturnType.CONTINUE
	}
	fun pickupStoredEnergy(creep: Creep) :MultiAI.ReturnType{
		val stores = creep.pos.findInRange(FIND_STRUCTURES,1)
				.filter { it.structureType == STRUCTURE_STORAGE ||
				it.structureType == STRUCTURE_CONTAINER}

		for (store in stores) {
			if (pickUpResourceOnPos_Stored(creep, store.pos, RESOURCE_ENERGY) ) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun pickupResourceFree_NotOnBaseStore(creep: Creep) :MultiAI.ReturnType {
		console.log("starting pickupResourceFree_NotOnBaseStore")
		val baseStorePos = Bunker(creep.room).storagePos()

		if (baseStorePos == null) {
			return pickupResourceFree(creep)
		}

		val resources : List<Resource> = creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1)
				.filter { !it.pos.isEqualTo(baseStorePos) }
				.sortedByDescending { it.amount }

		for (resource in resources) {
			if (pickUpResourceOnPos_Free(creep,resource.pos, resource.resourceType)) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun pickupResourceFree(creep: Creep) : MultiAI.ReturnType {
		val resources : List<Resource> = creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1)
//				.filter { it.resourceType == RESOURCE_ENERGY }
				.sortedByDescending { it.amount }
		for (resource in resources) {
			if (pickUpResourceOnPos_Free(creep,resource.pos, resource.resourceType)) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

	fun pickupEnergyNotOnMiningPos(creep: Creep): MultiAI.ReturnType {
		//TODO
		return MultiAI.ReturnType.CONTINUE
	}

	fun pickupEnergyOnMiningPos(creep: Creep) : MultiAI.ReturnType {
		val miningPointFlags = creep.pos.findInRange(FIND_FLAGS,1)
				.filter { isFlagMiningPoint(it) }
				.filter {totalResourceOnPos(it.pos, RESOURCE_ENERGY) > 0}

		for (flag in miningPointFlags) {
			if (pickUpResourceOnPos_Free(creep, flag.pos, RESOURCE_ENERGY)) {
				return MultiAI.ReturnType.STOP
			}

			if (pickUpResourceOnPos_Stored(creep, flag.pos, RESOURCE_ENERGY)) {
				return MultiAI.ReturnType.STOP
			}
		}

		return MultiAI.ReturnType.CONTINUE
	}

	fun mine(creep: Creep): MultiAI.ReturnType {
		val targets = creep.pos.findInRange(FIND_SOURCES,1)
				.filter { var source = it as Source;  source.energy > 0 }

		for (target in targets) {
			if (creep.harvest(target) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}

		//if we got to here then there are no mineable sources.
		//we may be mining resources
		val mineralTargets = creep.pos.findInRange(FIND_MINERALS,1)
//				.filter { var source = it as Source;  source.energy > 0 }
		for (target in mineralTargets) {
			if (creep.harvest(target) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}
		return MultiAI.ReturnType.CONTINUE
	}

}