package starter.multiAI

import screeps.api.Creep
import starter.multiAI.Actions.MoveSetTarget
import starter.multiAI.Actions.MoveSetTarget.miningPointWithMostResouceToPickup

class MultiAI_Action(newMoves: Array<(creep: Creep) -> MultiAI.ReturnType>,
					 newActions: Array<(creep: Creep) -> MultiAI.ReturnType>) {
	var  moves :Array<(creep: Creep) -> MultiAI.ReturnType> = newMoves
	var  actions :Array<(creep: Creep) -> MultiAI.ReturnType> = newActions

	fun run(creep: Creep) {
		for (action in actions) {
			console.log("multiai creep ${creep.name} is calling action ${action}")
			if (action(creep) == MultiAI.ReturnType.STOP) {
				break
			}
		}
		for (action in moves) {
			console.log("multiai creep ${creep.name} is calling move ${action}")
			if (miningPointWithMostResouceToPickup(creep)== MultiAI.ReturnType.STOP){
//			if (action(creep) == MultiAI.ReturnType.STOP) {
				console.log("stopping moves because fn retuned stop")
				break
			}
		}

	}
}