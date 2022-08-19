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
			try {

//				console.log("running creep:${creep.memory.role}")
				var role = creep.memory.role

				if (!roleMap.containsKey(role)) {
					//call old ai.
//					console.log("calling old behaviour for creep ${creep} with role ${role}")
					creep.runBehaviour()
					continue
				}

				roleMap[role]!!.run(creep)
			} catch (t: Throwable){

			}
		}
	}
}