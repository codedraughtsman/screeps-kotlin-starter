package starter

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn


enum class Role {
	UNASSIGNED,
	HARVESTER,
	BUILDER,
	UPGRADER
}

fun Creep.updateIsCollectingEnergy() {
	if (memory.isCollectingEnergy && carry.energy == carryCapacity) {
		memory.isCollectingEnergy = false
		say("🔄 harvest")
	} else if (memory.isCollectingEnergy == false && carry.energy == 0) {
		memory.isCollectingEnergy = true
		say("🚧 transferring")
	}
}

fun Creep.isHarvesting(): Boolean {
	return memory.isCollectingEnergy
}

fun Creep.upgrade(controller: StructureController) {
	updateIsCollectingEnergy()
	if (isHarvesting()) {
		val sources = room.find(FIND_SOURCES)
		if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
			moveTo(sources[0].pos)
		}
	} else {
		if (upgradeController(controller) == ERR_NOT_IN_RANGE) {
			moveTo(controller.pos)
		}
	}
}

fun Creep.pause() {
	if (memory.pause < 10) {
		//blink slowly
		if (memory.pause % 3 != 0) say("\uD83D\uDEAC")
		memory.pause++
	} else {
		memory.pause = 0
		memory.role = Role.HARVESTER
	}
}

fun Creep.build(assignedRoom: Room = this.room) {
	updateIsCollectingEnergy()

	if (isHarvesting()) {
		val sources = room.find(FIND_SOURCES)
		if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
			moveTo(sources[0].pos)
		}
	} else {
		val targets = assignedRoom.find(FIND_MY_CONSTRUCTION_SITES)
		if (targets.isNotEmpty()) {
			if (build(targets[0]) == ERR_NOT_IN_RANGE) {
				moveTo(targets[0].pos)
			}
		} else {
			val roadPos = room.getBestRoadLocation()
			if (roadPos != null) {
				room.createConstructionSite(roadPos, STRUCTURE_ROAD)
			} else {
				val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return
				upgrade(mainSpawn.room.controller!!)
			}
		}
	}
}

fun Creep.harvest(fromRoom: Room = this.room, toRoom: Room = this.room) {
	updateIsCollectingEnergy()
	if (isHarvesting()) {
		val sources = fromRoom.find(FIND_SOURCES)
		if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
			moveTo(sources[0].pos)
		}
	} else {
		val targets = toRoom.find(FIND_MY_STRUCTURES)
				.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
				.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }

		if (targets.isNotEmpty()) {
			if (transfer(targets[0], RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
				moveTo(targets[0].pos)
			}
		} else {
			build()
		}
	}
}