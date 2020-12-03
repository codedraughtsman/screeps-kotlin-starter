package starter.multiAI

import screeps.api.Creep
import screeps.api.FIND_SOURCES
import screeps.api.OK
import screeps.api.Source

object InRange {

	fun pickupEnergyNotOnMiningPos(creep: Creep): MultiAI.ReturnType {
		//TODO
		return MultiAI.ReturnType.CONTINUE
	}

	fun mine(creep: Creep):  MultiAI.ReturnType {
		val targets = creep.pos.findInRange(FIND_SOURCES,1)
				.filter { var source = it as Source;  source.energy > 0 }

		for (target in targets) {
			if (creep.harvest(target) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}

		return MultiAI.ReturnType.CONTINUE
	}
	fun build(creep: Creep) : MultiAI.ReturnType {
		val targets = creep.pos.findInRange(FIND_SOURCES,1)
				.filter { var source = it as Source;  source.energy > 0 }

		for (target in targets) {
			if (creep.harvest(target) == OK) {
				return MultiAI.ReturnType.STOP
			}
		}

		return MultiAI.ReturnType.CONTINUE
	}
}