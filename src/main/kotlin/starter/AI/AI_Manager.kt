package starter.AI

import screeps.api.Creep
import screeps.api.Record
import screeps.api.values
import starter.utils.units.*



fun updateAI(creeps: Record<String, Creep>){
	console.log("calling update ai")
	for (creep in creeps.values){
		if (isUpgrader(creep)){
			updateUpgrader(creep)
		}
		else if (isHarvester(creep)){
			updateHarvester(creep)
		}
		else {
			console.log("unable to find AI for creep ${creep}")
		}
	}
}