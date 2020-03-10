package starter

import screeps.api.*
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn


enum class Role {
	UNASSIGNED,
	HARVESTER,
	BUILDER,
	UPGRADER,
	EXTRACTOR
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
		getEnergy()

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
		getEnergy()

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

fun Creep.getEnergy(fromRoom: Room = this.room, toRoom: Room = this.room) {
	val collectors = fromRoom.find(FIND_STRUCTURES).filter { it.structureType == STRUCTURE_CONTAINER }
	//.filter { it.unsafeCast<EnergyContainer>().energy >0 }
	console.log("containers $collectors")
	if (collectors.isNotEmpty()) {
		for (c in collectors) {
			console.log("c is $c")
			console.log("energy is ${c.unsafeCast<EnergyContainer>().energy}")
			if (true) {
				console.log("withdraw(c, RESOURCE_ENERGY) code ${withdraw(c, RESOURCE_ENERGY)}")
				if (withdraw(c, RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
					moveTo(c.pos)
				}
			}
			break
		}
	} else {
		val sources = fromRoom.find(FIND_SOURCES)
		if (harvest(sources[0]) == ERR_NOT_IN_RANGE) {
			moveTo(sources[0].pos)
		}
	}
}

fun Creep.harvest(fromRoom: Room = this.room, toRoom: Room = this.room) {
	updateIsCollectingEnergy()
	if (isHarvesting()) {
		getEnergy()
		return
	}

	val targets = toRoom.find(FIND_MY_STRUCTURES)
			.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
			.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }

	if (targets.isNotEmpty()) {
		if (transfer(targets[0], RESOURCE_ENERGY) == ERR_NOT_IN_RANGE) {
			moveTo(targets[0].pos)
		}
	} else {
		build()
		/*
		//TODO first repair the structures that need it.
		val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return
		upgrade(mainSpawn.room.controller!!)

		 */
	}

}

fun Creep.extractor() {
	console.log("calling extactor function")
	val flags = pos.lookFor(LOOK_FLAGS)
	val flagOnSquare = (flags != null && flags.filter { it.name.startsWith("extractor", true) }.isNotEmpty())
	console.log("flags on square ${flagOnSquare}")
	if (flagOnSquare == false) {
		console.log("found flag with extractor prefix")
		//move to unocupied flag
		val flagPositions = room.find(FIND_FLAGS).filter { it.name.startsWith("extractor", true) }
		//val unoccupiedFlags = flagPositions.filter { it.pos.lookFor(LOOK_CREEPS).filter { it.name.startsWith("Extractor") }.isNotEmpty()}

		for (flag in flagPositions) {
			val c = flag.pos.lookFor(LOOK_CREEPS)

			val isUnoccupied = c.isNullOrEmpty() || c.filter { it.name.startsWith("EXTRACTOR", true) }.isEmpty()
			if (isUnoccupied) {
				moveTo(flag.pos)
			}
		}
	}
	val targets = pos.findInRange(FIND_MY_CONSTRUCTION_SITES, 3)
	if (carry.energy > 0 && targets.isNotEmpty()) {
		if (build(targets[0]) == ERR_NOT_IN_RANGE) {
			console.log("extractor tried to build ${targets[0]} but was out of range")
		}
	}



	if (carry.energy > 0) {
		//deposit it
		val containersInRange = pos.findInRange(FIND_MY_STRUCTURES, 2)
				.filter { it.structureType == STRUCTURE_CONTAINER }
				.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }
		console.log("from pos $pos")
		console.log("containersInRange $containersInRange")
		console.log("all structures in range ${pos.findInRange(FIND_MY_STRUCTURES, 2)
		}")
		if (containersInRange.isNotEmpty()) {
			//depost energy in container
			console.log("trying to transfer energy to container")
			transfer(containersInRange[0], RESOURCE_ENERGY)
		}
/*
		val targets = pos.findInRange(FIND_MY_STRUCTURES, 1)
				.filter { (it.structureType == STRUCTURE_EXTENSION || it.structureType == STRUCTURE_SPAWN) }
				.filter { it.unsafeCast<EnergyContainer>().energy < it.unsafeCast<EnergyContainer>().energyCapacity }
		if (targets.isNotEmpty()) {
			transfer(targets[0], RESOURCE_ENERGY)
		}

 */
	}
	if (carry.energy == carryCapacity) {

	}
	//harvest energy
	val target = pos.findClosestByPath(FIND_SOURCES_ACTIVE)
	if (target != null) {
		console.log("harvesting with extractor")
		var error = harvest(target)
		console.log("error code is $error")
	}


}