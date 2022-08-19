package starter


import screeps.api.*
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower
import screeps.utils.contains
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
import starter.utils.getControllerClaimFlags
import starter.utils.noCreepHasPosAsTarget

fun clearOldFlags(){
	if (Game.spawns.values.count() != 1 ||
			Game.spawns.values[0].room.controller!!.level != 0){
		// This is not the beginning of the game. don't clear the flags.
		return
	}

	for (flag in Game.flags.values) {
		if ( !Game.rooms.values.contains(flag.room)){
			// we can clear the flag
			flag.remove()
		}
	}
	//Todo delete the old rooms from memeory.

}

fun gameLoop() {
//	val mainSpawn: StructureSpawn = Game.spawns.values.firstOrNull() ?: return
	clearOldFlags()
	//delete memories of creeps that have passed away
	console.log("updating housekeeping")
	houseKeeping(Game.creeps)
	val numberOfExtractorFlags = Game.flags.values.filter { it.name.startsWith("extractor", true) }.count()
	val nonOldCreeps = Game.creeps.values.filter { it.ticksToLive > 300  }.toMutableList()
	console.log("extractor flags are ${Game.flags.values.filter { it.name.startsWith("extractor", true) }}")
	console.log("number of extractor flags are ${numberOfExtractorFlags} and ${nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }} extractors")

	console.log("updating spawing creeps")
	for (spawn in Game.spawns.values) {
		spawnCreeps(Game.creeps.values, spawn)
		break
	}
	console.log("updating ai")

	MultiAI.update()
	console.log("updating room")
	for ((_, room) in Game.rooms) {
		room.update()
	}

	console.log("updating structures")
	for (structure in Game.structures.values.filter { it-> it.structureType == STRUCTURE_TOWER }) {
		val tower: StructureTower = structure.unsafeCast<StructureTower>()
		updateTower(tower)
	}
	console.log("end of gameloop")
}



//
//
//fun rolesBeingSpawned(){
//	var roles = mutableListOf<Role>()
//	for (spawn in Game.spawns.values){
//		if (spawn.spawning != null) {
//			roles.add(spawn.spawning.)
//		}
//	}
//}

private fun checkToSpawnRescueCreep(creeps: Array<Creep>, spawn: StructureSpawn): Boolean {
	// here we are going to spawn a rescue bot
	if (creeps.count() == 0
//			|| creeps.count {it.memory.role == Role.HAULER_EXTRACTOR } ==0
//			|| creeps.count {it.memory.role == Role.EXTRACTOR } ==0
			|| creeps.count {it.memory.role == Role.HAULER_BASE } ==0) {
		if (creeps.count {it.memory.role == Role.HARVESTER} > 0
				&& spawn.room.controller!!.level <3){
			//we have still got harvesters and we are just transitioning into using haulers.
			//don't worry.
			return false
		}
		//restart bot
		if (creeps.count { it.memory.role == Role.RESCUE_BOT } == 0) {
			mySpawnCreeps(spawn, Role.RESCUE_BOT, arrayOf(WORK, MOVE, CARRY))
			return true
		}
		if (creeps.count { it.memory.role == Role.HARVESTER } < 3) {
			mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
			return true
		}
	}
	return false
}
private fun spawnCreeps(
		creeps: Array<Creep>,
		spawn: StructureSpawn
) {
	if (spawn.spawning != null){
		//currently spawning something.
		return
	}

//	if (creeps.count { it.memory.role == Role.HARVESTER } < 3) {
//		mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
//	}
//	return

	//controller must exist if the room has a spawn.
	if (spawn.room.controller!!.level <2) {
		console.log("room level is below 2. only building harvesters")
		//we are at such a low level where we only want to spawn harvesters.
		if (creeps.count { it.memory.role == Role.HARVESTER } < 8) {
			mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
		}
		return
	}

	// here we are going to spawn a rescue bot
	// hack to see if not spawing a base hauler creep todo fix this.
	if (checkToSpawnRescueCreep(creeps,spawn)){
		return
	}

	val nonOldCreeps = creeps.filter { it.ticksToLive > 300  }.toMutableList()
//	console.log("non old creeps are {nonOldCreeps}")
//	//todo also add the creeps that are being spawned.
//	nonOldCreeps.addAll(Game.spawns.)

	if (spawn.room.controller!!.level >2 && nonOldCreeps.count { it.memory.role == Role.HAULER_BASE } < 1){
		//todo a better check is to see if we have any stored energy in the base.
		mySpawnCreeps(spawn, Role.HAULER_BASE, SpawingController.bestHaulerBase(spawn))
		return
	}


//	if (creeps.count { it.memory.role == Role.HARVESTER } < 3 ) {
//		mySpawnCreeps(spawn, Role.HARVESTER, arrayOf<BodyPartConstant>(WORK, CARRY, MOVE))
//		return
//	}
	//spawn an attacker if being attacked
//	if (nonOldCreeps.count{it.memory.role == Role.ATTACKER} ==0) {
//		mySpawnCreeps(spawn, Role.ATTACKER, SpawingController.bestAttacker(spawn))
//		return
//	}

//	console.log("about to see if we want to spawn a extractor/hauler")
	if (SpawingController.spawnHaulerExtractor(spawn)) {
		return
	}

	val numberOfExtractorFlags = Game.flags.values.filter { it.name.startsWith("extractor", true) }.count()
	console.log("number of extractor flags are ${numberOfExtractorFlags} and ${nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }} extractors")
	if ( nonOldCreeps.count { it.memory.role == Role.EXTRACTOR }  < numberOfExtractorFlags) {
		if (mySpawnCreeps(spawn, Role.EXTRACTOR, SpawingController.bestExtractor(spawn))) {
			return
		}
	}

//	console.log("getControllerClaimFlags().count() ${getControllerClaimFlags().count()}")
	if (getControllerClaimFlags().count() > 0 &&
			nonOldCreeps.count{it.memory.role == Role.CLAIM_ROOM} <=2 ) {
		mySpawnCreeps(spawn, Role.CLAIM_ROOM, SpawingController.bestClaimer(spawn))
		return
	}

	//note: we dont care about overlap with the builder
	if ( Bunker(spawn.room).storedEnergy() > DO_BUILD_ENERGY) {


		//only if there are construction sites build a builder.
		if (spawn.room.find(FIND_CONSTRUCTION_SITES).count() > 0) {
			if (creeps.count { it.memory.role == Role.BUILDER } < 1) {
				mySpawnCreeps(spawn, Role.BUILDER, SpawingController.bestBuilder(spawn))
				return
			}
		}
		//depositor spawn
		val depoitorFlagCount = spawn.room.find(FIND_FLAGS)
				.filter{it.name.contains("depositor")}.count()
//
		if (creeps.count { it.memory.role == Role.UPGRADER } < depoitorFlagCount) {
			mySpawnCreeps(spawn, Role.UPGRADER, SpawingController.bestDepositor(spawn))
			return
		}
//
		if (creeps.count { it.memory.role == Role.UPGRADER } > creeps.count { it.memory.role == Role.HAULER_CREEP }) {
			mySpawnCreeps(spawn, Role.HAULER_CREEP, SpawingController.bestHauler(spawn))
			return
		}
	}


}

fun mySpawnCreeps(spawn: StructureSpawn, role: Role, body: Array<BodyPartConstant>): Boolean {
	val newName = "${role.name}_${Game.time}"
	val code = spawn.spawnCreep(body, newName, options {
		memory = jsObject<CreepMemory> { this.role = role }
	})
	if (code == OK) {
		console.log("spawning $newName with body $body")
		return true
	} else {
//		console.log("unhandled error when spawning creep code $code, body $body, name $newName")
//		console.log("number of parts ${body.size}")
		return false
	}

}

private fun houseKeeping(creeps: Record<String, Creep>) {
	if (Game.creeps.isEmpty()) return  // this is needed because Memory.creeps is undefined

	for ((creepName, _) in Memory.creeps) {
		if (creeps[creepName] == null) {
//			console.log("deleting obsolete memory entry for creep $creepName")
			delete(Memory.creeps[creepName])
		}
	}
}
