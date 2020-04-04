package starter

import screeps.api.LOOK_CONSTRUCTION_SITES
import screeps.api.Room
import screeps.api.RoomPosition
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

		//use the backup store pos.
		val adjcent = getAdjcentSquares(storePos)
		if (adjcent == null) {
			return null
		}
		return adjcent[0]
	}
//	fun storedEnergy() :Int {
//		return
//	}
}