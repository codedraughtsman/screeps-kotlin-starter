package starter.behaviours

import screeps.api.Creep
import screeps.api.FIND_DROPPED_RESOURCES
import screeps.api.FIND_TOMBSTONES
import screeps.api.RESOURCE_ENERGY
import starter.*
import starter.multiAI.Role


val DO_BUILD_ENERGY =2000

enum class BehavourReturn {
	STOP_RUNNING,
	CONTINUE_RUNNING
}

enum class Behavours {
	UNDEFINED,
	GOTO,
	HARVEST,
	PICKUP,
	REFILL_STRUCTURES,
	BUILD,
	UPGRADE,
	HARVEST_EXTRACTOR,
	HAULER_PICKUP,
	DEPOSIT_ENERGY_IN_NEAREST_STORAGE,
//	BUILD_CONTAINER,
	HARVEST_FROM_SAVED_SOURCE,
	PICKUP_FROM_BASE_STORAGE,
	REFILL_BUILDERS,
	PICKUP_FROM_BASE_STORAGE_FORCED,
	MOVE_OFF_BASE_STORAGE,
	BUILD_IN_RANGE,
	PICKUP_ADJECENT_RESOURCES,
	REPAIR_IN_RANGE

}

fun Creep.runBehaviour() {
	try {
	updateIsCollectingEnergy()
	globalBehavour()
	var behavours = getBehavioursForRole(memory.role as Role)
	console.log("behavours for creep ${name} are ${behavours}")
		for (behaviour in behavours) {
			console.log("creep ${name} is running behaviour ${behaviour}")
			val isFinshed = runTheBehaviour(behaviour)
			if (isFinshed == BehavourReturn.STOP_RUNNING) {
				break
			}
		}
		if (memory.role != Role.HARVESTER) {
			moveOffSourcePos()

			//repairing will override the build action so only repair if you don't build
			if (!behaviourBuildWhileMoving()) {
				behaviourRepairWhileMoving()
			}

			behaviourUpgradeWhileMoving()
		}
	}catch (e: Error){
		//stop the error doing anything else
	}
}

private fun Creep.globalBehavour() {
	updateIsCollectingEnergy()


	if (memory.role == Role.HAULER_EXTRACTOR ||
			memory.role == Role.EXTRACTOR){
		return
	}



	val resourceToPickup = pos.findInRange(FIND_DROPPED_RESOURCES, 1)
	val bunker = Bunker(room)
	val storagePos = bunker.storagePos()

	for (resource in resourceToPickup) {
		if (storagePos != null
				&& resource.pos.isEqualTo(storagePos) ) {
			if (memory.role != Role.HAULER_BASE && bunker.storedEnergy() < DO_BUILD_ENERGY){
				//don't let the builder start building
				continue
			}
		}
		val errorCode = pickup(resource)
	}

	val tombstoneToPickup = pos.findInRange(FIND_TOMBSTONES, 1)

	for (tomb in tombstoneToPickup) {
		val errorCode = withdraw(tomb, RESOURCE_ENERGY)
	}

	behavourFillUpAdjcentExtentions()

	if (memory.role != Role.EXTRACTOR) {
		behavourRepairInRange()
	}
}

private fun getBehavioursForRole(role: Role): MutableList<Behavours> {
	var out: MutableList<Behavours> = arrayListOf()
	when (role) {
		Role.HARVESTER -> out = arrayListOf(Behavours.HARVEST_FROM_SAVED_SOURCE, Behavours.REFILL_STRUCTURES, Behavours.BUILD, Behavours.UPGRADE)
		Role.BUILDER -> out = arrayListOf(Behavours.MOVE_OFF_BASE_STORAGE, Behavours.PICKUP_FROM_BASE_STORAGE, Behavours.BUILD, Behavours.REPAIR_IN_RANGE, Behavours.UPGRADE)
//		Role.BUILDER -> out = arrayListOf(Behavours.MOVE_OFF_BASE_STORAGE, Behavours.PICKUP_FROM_BASE_STORAGE, Behavours.BUILD, Behavours.UPGRADE)

		Role.UPGRADER -> out = arrayListOf(Behavours.PICKUP, Behavours.UPGRADE)
		Role.EXTRACTOR -> out = arrayListOf( Behavours.HARVEST_EXTRACTOR, Behavours.BUILD_IN_RANGE, Behavours.REPAIR_IN_RANGE, Behavours.PICKUP_ADJECENT_RESOURCES) //TODO static build and upgrade
//		Role.HAULER_EXTRACTOR -> out = arrayListOf(Behavours.HAULER_PICKUP, Behavours.DEPOSIT_ENERGY_IN_NEAREST_STORAGE, Behavours.REFILL_STRUCTURES, Behavours.BUILD, Behavours.UPGRADE)
		Role.HAULER_EXTRACTOR -> out = arrayListOf(Behavours.HAULER_PICKUP, Behavours.DEPOSIT_ENERGY_IN_NEAREST_STORAGE)
		Role.HAULER_BASE -> out = arrayListOf(Behavours.MOVE_OFF_BASE_STORAGE, Behavours.PICKUP_FROM_BASE_STORAGE, Behavours.REFILL_STRUCTURES, Behavours.REPAIR_IN_RANGE,
				Behavours.REFILL_BUILDERS ) //Behavours.DEPOSIT_ENERGY_IN_NEAREST_STORAGE
		Role.RESCUE_BOT -> out = arrayListOf(Behavours.MOVE_OFF_BASE_STORAGE, Behavours.PICKUP_FROM_BASE_STORAGE, Behavours.REFILL_STRUCTURES,
				Behavours.REFILL_BUILDERS, Behavours.DEPOSIT_ENERGY_IN_NEAREST_STORAGE, Behavours.HAULER_PICKUP)

	}
	return out
}

private fun Creep.runTheBehaviour(behaviour: Behavours): BehavourReturn {
	var isFinished : BehavourReturn = BehavourReturn.CONTINUE_RUNNING
	when (behaviour) {
//		Behavours.GOTO -> isFinished = behaviourGoto()
		Behavours.PICKUP -> isFinished = behaviourPickup()
		Behavours.PICKUP_FROM_BASE_STORAGE -> isFinished = behaviourPickupFromBaseStorage()
		Behavours.REFILL_STRUCTURES -> isFinished = behaviourDeposit()
		Behavours.BUILD -> isFinished = behaviourBuild()
		Behavours.UPGRADE -> isFinished = behavourUpgrade(room.controller!!)
		Behavours.HARVEST_EXTRACTOR -> isFinished = behaviourHarvestExtractor()
		Behavours.HAULER_PICKUP -> isFinished = behaviourHaulerPickup()
		Behavours.DEPOSIT_ENERGY_IN_NEAREST_STORAGE -> isFinished = behavourDepositEnergyInBaseStorage()
//		Behavours.BUILD_CONTAINER -> isFinished = behaviourBuildContainer()
		Behavours.HARVEST_FROM_SAVED_SOURCE -> isFinished = behaviourHarvestFromSavedSource()
		Behavours.REFILL_BUILDERS -> isFinished = behaviourRefillBuilders()
		Behavours.PICKUP_FROM_BASE_STORAGE_FORCED -> isFinished = behaviourPickupFromBaseStorage(forced=true)
		Behavours.MOVE_OFF_BASE_STORAGE -> isFinished = moveOffBaseStoragePos()
		Behavours.BUILD_IN_RANGE -> isFinished = behavourBuildInRange()
		Behavours.PICKUP_ADJECENT_RESOURCES -> isFinished = behavourPickupAdjcentResouces()
		Behavours.REPAIR_IN_RANGE -> isFinished =behavourBuildInRange()

	}
	return isFinished
}