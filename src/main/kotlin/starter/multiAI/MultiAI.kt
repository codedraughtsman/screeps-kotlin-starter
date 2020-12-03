package starter.multiAI

import screeps.api.Game
import screeps.api.values
import starter.behaviours.runBehaviour
import starter.multiAI.Role
import starter.role


object MultiAI {
	enum class ReturnType {
		CONTINUE,
		STOP
	}

	fun update(){
		for (creep in Game.creeps.values) {
			var role = creep.memory.role

			if (!roleMap.containsKey(role)){
				//call old ai.
				creep.runBehaviour()
				continue
			}

			roleMap[role]!!.run(creep)
		}
	}
}