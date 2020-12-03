package starter.multiAI

import screeps.api.Creep

class MultiAI_Action(newMoves: Array<(creep: Creep) -> MultiAI.ReturnType>,
					 newActions: Array<(creep: Creep) -> MultiAI.ReturnType>) {
	var  moves :Array<(creep: Creep) -> MultiAI.ReturnType> = newMoves
	var  actions :Array<(creep: Creep) -> MultiAI.ReturnType> = newActions

	fun run(creep: Creep) {
		for (action in actions) {
			if (action(creep) == MultiAI.ReturnType.STOP) {
				break
			}
		}
		for (action in moves) {
			if (action(creep) == MultiAI.ReturnType.STOP) {
				break
			}
		}

	}
}