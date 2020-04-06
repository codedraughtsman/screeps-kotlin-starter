package starter

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn


enum class Role {
	UNASSIGNED,
	HARVESTER,
	BUILDER,
	UPGRADER,
	EXTRACTOR,
	HAULER_EXTRACTOR,
	HAULER_BASE,
	RESCUE_BOT
}

fun Creep.updateIsCollectingEnergy() {
	if (memory.isCollectingEnergy && carry.energy >= (carryCapacity -6)) {
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

fun Creep.behavourUpgrade(controller: StructureController): Boolean {
	updateIsCollectingEnergy()
	if (isHarvesting()) {
		return getEnergy()

	} else {
		if (upgradeController(controller) == ERR_NOT_IN_RANGE) {
			moveTo(controller.pos)
			return true
		}
	}
	return true
}



fun Creep.build(assignedRoom: Room = this.room): Boolean {
	updateIsCollectingEnergy()

	if (isHarvesting()) {
		return getEnergy()

	} else {
		val targets = assignedRoom.find(FIND_MY_CONSTRUCTION_SITES)
		if (targets.isNotEmpty()) {
			if (build(targets[0]) == ERR_NOT_IN_RANGE) {
				moveTo(targets[0].pos)
				return true
			}
		} else {
			val roadPos = room.getBestRoadLocation()
			if (roadPos != null) {
				room.createConstructionSite(roadPos, STRUCTURE_ROAD)
			} else {
				val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return false
				return behavourUpgrade(mainSpawn.room.controller!!)
			}
		}
	}
	return true
}

fun Creep.getEnergy(fromRoom: Room = this.room, toRoom: Room = this.room): Boolean {
	val collectors = fromRoom.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
	//.filter { it.unsafeCast<EnergyContainer>().energy >0 }
	//console.log("containers $collectors")
	if (room.find(FIND_MY_CREEPS).count { it.memory.role == Role.EXTRACTOR } > 0 && collectors.isNotEmpty()) {
		for (c in collectors) {
			//console.log("c is $c")
			//console.log("energy is ${c.unsafeCast<EnergyContainer>().energy}")
			if (true) {
				console.log("withdraw(c, RESOURCE_ENERGY) code ${withdraw(c, RESOURCE_ENERGY)}")
				if (withdraw(c, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
					moveTo(c.pos)
					return true
				}
			}
			break
		}
	} else {
		val sources = fromRoom.find(FIND_SOURCES)
		if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
			moveTo(sources[0].pos)
			return true
		}
	}
	return false
}

fun Creep.harvest(fromRoom: Room = this.room, toRoom: Room = this.room): Boolean {
	updateIsCollectingEnergy()
	if (isHarvesting()) {
		getEnergy()
		return true
	}

	val targets = toRoom.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
			.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }

	if (targets.isNotEmpty()) {
		if (transfer(targets[0], RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
			moveTo(targets[0].pos)
			return false
		}
	} else {
		return build()
		/*
		//TODO first repair the structures that need it.
		val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return
		upgrade(mainSpawn.room.controller!!)

		 */
	}
	return false
}
