package starter

import screeps.api.*
import screeps.api.structures.StructureSpawn
import starter.SpawingController.addParts
import starter.multiAI.Role
import kotlin.math.min

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
	fun bestExtractor(spawn: StructureSpawn, roadOnly :Boolean =true): Array<BodyPartConstant> {
		var body = arrayOf<BodyPartConstant>(WORK)
//		var bodyCost = body.sumBy { BODYPART_COST[it]!! }


//		var multiples = (spawn.room.energyCapacityAvailable - BODYPART_COST[MOVE]!! - BODYPART_COST[CARRY]!!) / bodyCost
//
//		multiples = min(6,multiples)

		var outArray: MutableList<BodyPartConstant> = arrayListOf()
		outArray.add(MOVE)
		outArray.add(CARRY)

		val maxWork = 6
		var energyLeft = spawn.room.energyCapacityAvailable -outArray.sumBy { BODYPART_COST[it]!! }

		outArray.addAll(addMultiples(energyLeft,body,min(50-outArray.size, maxWork)))

		outArray.addAll(addMultiples(energyLeft, arrayOf<BodyPartConstant>(MOVE),
				min(50-outArray.size, maxWork)))
//
//		while (outArray.sumBy { BODYPART_COST[it]!! } + BODYPART_COST[MOVE]!! <= spawn.room.energyCapacityAvailable
//				&& outArray.filter { it == MOVE }.size < outArray.size/2) {
//			outArray.add(MOVE)
//		}
		return outArray.toTypedArray()
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
			body = arrayOf<BodyPartConstant>(CARRY, CARRY, MOVE,CARRY, CARRY, MOVE,CARRY, CARRY, MOVE, WORK, MOVE)
		}
//		var bodyCost = body.sumBy { BODYPART_COST[it]!! }


//		var multiples = (spawn.room.energyCapacityAvailable - BODYPART_COST[WORK]!!) / bodyCost

		var outArray: MutableList<BodyPartConstant> = arrayListOf()
		outArray.add(WORK)

//		for (i in 1..multiples) {
//			for (part in body) {
//				outArray.add(part)
//			}
//		}

		var energyLeft = spawn.room.energyCapacityAvailable -outArray.sumBy { BODYPART_COST[it]!! }
		outArray.addAll(addMultiples(energyLeft,body,50-outArray.size))

		return outArray.toTypedArray()

	}
	fun bestBuilder(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
		val mustHave = arrayOf<BodyPartConstant>(CARRY)
		var body = arrayOf<BodyPartConstant>(WORK, MOVE)
		if (road) {
			body = arrayOf<BodyPartConstant>(WORK, WORK, MOVE,WORK, WORK, MOVE,WORK, WORK, MOVE,WORK, WORK,
					MOVE, CARRY)
		}

		val fillers = arrayOf<BodyPartConstant>(WORK, CARRY)

//		var bodyCost = body.sumBy { BODYPART_COST[it]!! }

//		val mustHaveCost = mustHave.sumBy{ BODYPART_COST[it]!! } *(spawn.room.energyCapacityAvailable / bodyCost)/8

//		val energyInRoom = spawn.room.energyAvailable
//
//		var multiples = (spawn.room.energyCapacityAvailable - mustHaveCost) / bodyCost

		var outArray: MutableList<BodyPartConstant> = arrayListOf()

		var energyLeft = spawn.room.energyCapacityAvailable -outArray.sumBy { BODYPART_COST[it]!! }
		outArray.addAll(addMultiples(energyLeft,mustHave,1))

		energyLeft = spawn.room.energyCapacityAvailable -outArray.sumBy { BODYPART_COST[it]!! }
		outArray.addAll(addMultiples(energyLeft,body,50 - outArray.size))

		energyLeft = spawn.room.energyCapacityAvailable -outArray.sumBy { BODYPART_COST[it]!! }
		outArray.addAll(addMultiples(energyLeft,fillers,50 - outArray.size))

//		for (part in mustHave) {
//			outArray.add(part)
//		}

//		for (i in 1..multiples) {
//			for (obj in body){
//				outArray.add(obj)
//			}
//		}
//
//		for (fillPart in fillers) {
//			var fillCost = BODYPART_COST[fillPart]!!
//
//			while (outArray.sumBy { BODYPART_COST[it]!! } + fillCost <= spawn.room.energyCapacityAvailable) {
//				outArray.add(fillPart)
//			}
//		}

		return outArray.toTypedArray()

	}


	fun addMultiples(energy:Int, body: Array<BodyPartConstant>, maxNumberOfBodyParts:Int =50): Array<BodyPartConstant> {

		var energyLeft = energy
		var outArray: MutableList<BodyPartConstant> = arrayListOf()

		var i=0
		while (energyLeft> 0 && outArray.size < maxNumberOfBodyParts ){
			val bodyPart = body.get(i%body.size)
			i ++
			val cost = BODYPART_COST[bodyPart]
			if (cost == null) {
				console.log("tried to add an invalid body part ${bodyPart} in function addMultiples")
			}
			else if (energyLeft < cost) {
			}
			else if (outArray.size >= maxNumberOfBodyParts ) {
			} else {
				outArray.add(bodyPart)
				energyLeft -= cost

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
				if (outArray.size >=50) {
					break;
				}
			}
			if (outArray.size >=50) {
				break;
			}
		}

		return outArray.toTypedArray()

	}
}