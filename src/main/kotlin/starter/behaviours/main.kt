package starter.behaviours

import screeps.api.Creep
import screeps.api.FIND_DROPPED_RESOURCES
import starter.*

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
private fun Creep.globalBehavour() {
	updateIsCollectingEnergy()

	val resourceToPickup = pos.findInRange(FIND_DROPPED_RESOURCES, 1)
	if (!resourceToPickup.isNullOrEmpty()) {
		//todo grab the biggest resource available
		val errorCode = pickup(resourceToPickup[0])
		//displayErrorCode(errorCode, "pickup resource behavour")
	}
}

private fun getBehavioursForRole(role: Role): MutableList<Behavours> {
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