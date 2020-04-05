package starter

import screeps.api.*
import starter.behaviours.isTraversable
import starter.behaviours.loadPosFromMemory

class Bunker (val room: Room) {
	init {

	}

	fun storagePos(): RoomPosition? {
		if (room.memory.bunker.mainStorePos == null) {
			return null
		}
		var storePos: RoomPosition = loadPosFromMemory(room.memory.bunker.mainStorePos!!)
		if (storePos.lookFor(LOOK_CONSTRUCTION_SITES).isNullOrEmpty()) {
			//something is being constructed here. don't do anything
			return storePos
		}
	//hack
		return room.getPositionAt(storePos.x +1, storePos.y)
		//use the backup store pos.
		val adjcent = getAdjcentSquares(storePos).filter { isTraversable(it) }
		if (adjcent == null) {
			return null
		}
		return adjcent[0]
	}



	fun storedEnergy() :Int {
		var storedEnergy = 0

		val targetPos = storagePos()
		if (targetPos == null) {
			return 0
		}

		for (drop in targetPos.findInRange(FIND_DROPPED_RESOURCES,0)){
			if (drop.resourceType == RESOURCE_ENERGY) {
				storedEnergy += drop.amount
			}
		}


		return storedEnergy
	}
}