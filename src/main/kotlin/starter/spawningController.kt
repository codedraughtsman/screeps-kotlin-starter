package starter

import screeps.api.*
import screeps.api.structures.StructureSpawn
import starter.SpawingController.addParts
import starter.multiAI.Role

object SpawingController {
	fun getNonOldCreeps(): List<Creep> {
		return  Game.creeps.values.filter { it.ticksToLive > 300  }
	}
	fun spawnHaulerExtractor(spawn: StructureSpawn): Boolean {
		val nonOldCreeps = getNonOldCreeps()

		val energyProduced= 0
		val hauldistance=0


		if ( nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }-2  > nonOldCreeps.count { it.memory.role == Role.HAULER_EXTRACTOR }) {
			mySpawnCreeps(spawn, Role.HAULER_EXTRACTOR, bestHauler(spawn,spawn.room.controller!!.level >= 3))
			return true
		}
		return false
	}
	fun bestDepositor(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
		val mustHave = arrayOf<BodyPartConstant>(CARRY, CARRY ,MOVE)
		var body = arrayOf<BodyPartConstant>(WORK )
		var bodyCost = body.sumBy { BODYPART_COST[it]!! }

		val mustHaveCost =  mustHave.sumBy{ BODYPART_COST[it]!! }

		var energyAvailable = spawn.room.energyCapacityAvailable
		var outArray: MutableList<BodyPartConstant> = arrayListOf()

		for (bodyPart in mustHave) {
			energyAvailable -= BODYPART_COST[bodyPart]!!
			if (energyAvailable < 0){
				break
			}
			outArray.add(bodyPart)
		}

		outArray.addAll(addParts(body,energyAvailable))

		return outArray.toTypedArray()

	}

	fun addParts(parts: Array<BodyPartConstant> , energy: Int): MutableList<BodyPartConstant> {
		var outArray: MutableList<BodyPartConstant> = arrayListOf()
		var energyLeft = energy
		while (true)
			for (bodyPart in parts) {
				energyLeft -= BODYPART_COST[bodyPart]!!
				if (energyLeft < 0) {
					return outArray
				}
				outArray.add(bodyPart)
			}
		}
	fun bestHauler(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
		var body = arrayOf<BodyPartConstant>(CARRY, MOVE)
		if (road) {
			body = arrayOf<BodyPartConstant>(CARRY, CARRY, MOVE)
		}
		var bodyCost = body.sumBy { BODYPART_COST[it]!! }


		var multiples = (spawn.room.energyCapacityAvailable - BODYPART_COST[WORK]!!) / bodyCost

		var outArray: MutableList<BodyPartConstant> = arrayListOf()
		outArray.add(WORK)

		for (i in 1..multiples) {
			for (part in body) {
				outArray.add(part)
			}
		}

		return outArray.toTypedArray()

	}
	fun bestBuilder(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
		val mustHave = arrayOf<BodyPartConstant>(CARRY)
		var body = arrayOf<BodyPartConstant>(WORK, MOVE)
		if (road) {
			body = arrayOf<BodyPartConstant>(WORK, WORK, MOVE)
		}
		var bodyCost = body.sumBy { BODYPART_COST[it]!! }

		val mustHaveCost = mustHave.sumBy{ BODYPART_COST[it]!! } *(spawn.room.energyCapacityAvailable / bodyCost)/8

		val energyInRoom = spawn.room.energyAvailable

		var multiples = (spawn.room.energyCapacityAvailable - mustHaveCost) / bodyCost

		var outArray: MutableList<BodyPartConstant> = arrayListOf()

		for (part in mustHave) {
			outArray.add(part)
		}

		for (i in 1..multiples) {
			for (obj in body){
				outArray.add(obj)
			}
		}
		val fillers = arrayOf<BodyPartConstant>(WORK, CARRY)
		for (fillPart in fillers) {
			var fillCost = BODYPART_COST[fillPart]!!

			while (outArray.sumBy { BODYPART_COST[it]!! } + fillCost <= spawn.room.energyCapacityAvailable) {
				outArray.add(fillPart)
			}
		}

		return outArray.toTypedArray()

	}

	fun bestHaulerBase(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
		var body = arrayOf<BodyPartConstant>(CARRY, MOVE)
		if (road) {
			body = arrayOf<BodyPartConstant>(CARRY, CARRY, MOVE)
		}
		var bodyCost = body.sumBy { BODYPART_COST[it]!! }


		var multiples = (spawn.room.energyCapacityAvailable - BODYPART_COST[WORK]!!) / bodyCost

		var outArray: MutableList<BodyPartConstant> = arrayListOf()
		outArray.add(WORK)

		for (i in 1..multiples) {
			for (part in body) {
				outArray.add(part)
			}
		}

		return outArray.toTypedArray()

	}
}