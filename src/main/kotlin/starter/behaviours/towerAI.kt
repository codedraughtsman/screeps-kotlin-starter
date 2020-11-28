package starter.behaviours

import screeps.api.*
import screeps.api.structures.Structure
import screeps.api.structures.StructureTower


fun updateTower(tower:StructureTower) {
	attackAnything(tower)
}
fun attackAnything(tower: StructureTower) {
	val room = tower.room
	val target = room.find(FIND_HOSTILE_CREEPS)
			//todo sort by distance
			.getOrNull(0)
	if (target != null) {
		tower.attack(target)
		return
	}

	val repairTarget : Structure? = room.find(FIND_STRUCTURES)
			.filter { 4*it.hits < it.hitsMax }
			.filter {(it.structureType != STRUCTURE_RAMPART
					&& it.structureType != STRUCTURE_WALL)
					|| it.hits < 2000 }
			.sortedBy { it.hits }
			.getOrNull(0)
	if (repairTarget != null) {
		tower.repair(repairTarget)
		return
	}

}