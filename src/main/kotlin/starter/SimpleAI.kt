package starter


import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower
import screeps.utils.isEmpty
import screeps.utils.unsafe.delete
import screeps.utils.unsafe.jsObject
import kotlin.math.min
import starter.behaviours.runBehaviour
import starter.behaviours.updateTower
import starter.Bunker
import starter.behaviours.DO_BUILD_ENERGY
import starter.multiAI.MultiAI
import starter.multiAI.Role


fun gameLoop() {
	val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return

	//delete memories of creeps that have passed away
	houseKeeping(Game.creeps)

	//make sure we have at least some creeps
	spawnCreeps(Game.creeps.values, mainSpawn)

	MultiAI.update()
//	for (creep in Game.creeps.values.filter { it.memory.role == Role.HAULER_BASE }) {
//		creep.runBehaviour()
//	}
//	for (creep in Game.creeps.values.filter { it.memory.role != Role.HAULER_BASE }) {
//		creep.runBehaviour()
//	}

	for ((_, room) in Game.rooms) {
		room.update()
	}

	for (structure in Game.structures.values.filter { it-> it.structureType == STRUCTURE_TOWER }) {
		val tower: StructureTower = structure.unsafeCast<StructureTower>()
		updateTower(tower)
	}
}

private fun bestWorker(spawn: StructureSpawn): Array<BodyPartConstant> {
	var body = arrayOf<BodyPartConstant>(WORK, CARRY, MOVE)
	var bodyCost = body.sumBy { BODYPART_COST[it]!! }

	var multiples = spawn.room.energyCapacityAvailable / bodyCost

//	console.log("bodyCost $bodyCost, multiples $multiples")


	var outArray: MutableList<BodyPartConstant> = arrayListOf()


	for (i in 1..multiples) {
		outArray.add(WORK)
	}
	for (i in 1..multiples) {
		outArray.add(CARRY)
	}
	for (i in 1..multiples) {
		outArray.add(MOVE)
	}

	while (outArray.sumBy { BODYPART_COST[it]!! } + BODYPART_COST[MOVE]!! <= spawn.room.energyCapacityAvailable) {
		outArray.add(MOVE)
	}
	return outArray.toTypedArray()

}



private fun bestExtractor(spawn: StructureSpawn, roadOnly :Boolean =true): Array<BodyPartConstant> {
	var body = arrayOf<BodyPartConstant>(WORK)
	var bodyCost = body.sumBy { BODYPART_COST[it]!! }


	var multiples = (spawn.room.energyCapacityAvailable - BODYPART_COST[MOVE]!! - BODYPART_COST[CARRY]!!) / bodyCost

	multiples = min(6,multiples)

	var outArray: MutableList<BodyPartConstant> = arrayListOf()
	outArray.add(MOVE)
	outArray.add(CARRY)

	for (i in 1..multiples) {
		outArray.add(WORK)
	}
	while (outArray.sumBy { BODYPART_COST[it]!! } + BODYPART_COST[MOVE]!! <= spawn.room.energyCapacityAvailable
			&& outArray.filter { it == MOVE }.size < outArray.size/2) {
		outArray.add(MOVE)
	}
	return outArray.toTypedArray()
}
private fun bestHauler(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
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
private fun bestHaulerBase(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
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

private fun bestBuilder(spawn: StructureSpawn, road :Boolean =true): Array<BodyPartConstant> {
	val mustHave = arrayOf<BodyPartConstant>(CARRY)
	var body = arrayOf<BodyPartConstant>(WORK, MOVE)
	if (road) {
		body = arrayOf<BodyPartConstant>(WORK, WORK, MOVE)
	}
	var bodyCost = body.sumBy { BODYPART_COST[it]!! }

	val mustHaveCost = mustHave.sumBy{ BODYPART_COST[it]!! }

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

private fun spawnCreeps(
		creeps: Array<Creep>,
		spawn: StructureSpawn
) {

//	if (creeps.count { it.memory.role == Role.HARVESTER } < 3) {
//		mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
//	}
//	return

	//controller must exist if the room has a spawn.
	if (spawn.room.controller!!.level <2) {
		if (creeps.count { it.memory.role == Role.HARVESTER } < 3) {
			mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
		}
		return
	}
	if (creeps.count() == 0 || creeps.count {it.memory.role == Role.HAULER_EXTRACTOR } ==0
			|| creeps.count {it.memory.role == Role.EXTRACTOR } ==0
			|| creeps.count {it.memory.role == Role.HAULER_BASE } ==0 ) {
		//restart bot
		if (creeps.count { it.memory.role == Role.RESCUE_BOT } == 0) {
			mySpawnCreeps(spawn, Role.RESCUE_BOT, arrayOf(WORK, MOVE, CARRY))
		return
		}
		if (creeps.count { it.memory.role == Role.HARVESTER } < 3) {
			mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
			return
		}
	}

	val nonOldCreeps = creeps.filter { it.ticksToLive > 300  }

	if (nonOldCreeps.count { it.memory.role == Role.HAULER_BASE } < 1){
		mySpawnCreeps(spawn, Role.HAULER_BASE,bestHaulerBase(spawn))
		return
	}
	if ( nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }  > creeps.count { it.memory.role == Role.HAULER_EXTRACTOR }) {
		mySpawnCreeps(spawn, Role.HAULER_EXTRACTOR,bestHauler(spawn,spawn.room.controller!!.level >= 3))
		return
	}

	val numberOfExtractorFlags = Game.flags.values.filter { it.name.startsWith("extractor", true) }.count()
	if ( nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }  < numberOfExtractorFlags) {
		mySpawnCreeps(spawn, Role.EXTRACTOR, bestExtractor(spawn))
		return
	}

	//note: we dont care about overlap with the builder
	if ( Bunker(spawn.room).storedEnergy() > DO_BUILD_ENERGY) {
		//todo only if there are construction sites build a builder.
		if (creeps.count { it.memory.role == Role.BUILDER } < 2 ) {
			mySpawnCreeps(spawn, Role.BUILDER, bestBuilder(spawn))
			return
		}
		//depositor spawn
	}


}

fun mySpawnCreeps(spawn: StructureSpawn, role: Role, body: Array<BodyPartConstant>) {
	val newName = "${role.name}_${Game.time}"
	val code = spawn.spawnCreep(body, newName, options {
		memory = jsObject<CreepMemory> { this.role = role }
	})

	when (code) {
		OK -> console.log("spawning $newName with body $body")
		ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { } // do nothing
		else -> console.log("unhandled error code $code")
	}
}

/*private fun spawnCreeps(
		creeps: Array<Creep>,
		spawn: StructureSpawn
) {
	var body: Array<BodyPartConstant> = arrayOf<BodyPartConstant>()
	var role: Role = Role.UNASSIGNED
	val numberOfExtractorFlags = spawn.room.find(FIND_FLAGS).filter { it.name.startsWith("extractor", true) }.count()
	if (creeps.count { it.memory.role == Role.HARVESTER } == 0) {
		body = arrayOf<BodyPartConstant>(WORK, CARRY, MOVE)
		role = Role.HARVESTER
	} else if ( creeps.count { it.memory.role == Role.EXTRACTOR }  > creeps.count { it.memory.role == Role.HAULER_EXTRACTOR }) {
		//need to make an extractor
		body = bestHauler(spawn)
		role = Role.HAULER_EXTRACTOR

	} else if (numberOfExtractorFlags > creeps.count { it.memory.role == Role.EXTRACTOR }) {
		//need to make an extractor
		body = bestExtractor(spawn)
		role = Role.EXTRACTOR

	}  else {
		body = bestWorker(spawn)
		role = when {
			creeps.count { it.memory.role == Role.HARVESTER } < spawn.room.memory.maxWorkers-> Role.HARVESTER

			//creeps.none { it.memory.role == Role.UPGRADER } -> Role.UPGRADER
/*
			spawn.room.find(FIND_MY_CONSTRUCTION_SITES).isNotEmpty() &&
					creeps.count { it.memory.role == Role.BUILDER } < 1 -> Role.BUILDER


			creeps.count { it.memory.role == Role.BUILDER } == 0 -> Role.BUILDER
*/
			else -> return
		}
	}


	if (spawn.room.energyAvailable < body.sumBy { BODYPART_COST[it]!! } && creeps.count { it.memory.role == Role.HARVESTER } > 2) {
		return
	}

	if (role == Role.UNASSIGNED || body.isEmpty()) {
		return
	}

	val newName = "${role.name}_${Game.time}"
	val code = spawn.spawnCreep(body, newName, options {
		memory = jsObject<CreepMemory> { this.role = role }
	})

	when (code) {
		OK -> console.log("spawning $newName with body $body")
		ERR_BUSY, ERR_NOT_ENOUGH_ENERGY -> run { } // do nothing
		else -> console.log("unhandled error code $code")
	}
}*/

private fun houseKeeping(creeps: Record<String, Creep>) {
	if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

	for ((creepName, _) in Memory.creeps) {
		if (creeps[creepName] == null) {
			console.log("deleting obsolete memory entry for creep $creepName")
			delete(Memory.creeps[creepName])
		}
	}
}
