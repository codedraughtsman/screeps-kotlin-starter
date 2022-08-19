package starter.utils.units
import screeps.api.*
import starter.behaviour

import starter.behaviours.loadPosFromMemory
import starter.multiAI.Role
import starter.role
import starter.utils.getTargetPos
import starter.utils.noCreepHasPosAsTarget

fun hasNoTarget(creep: Creep) :Boolean{
	return creep.memory.behaviour.targetPos == null
}

fun Creep.moveTowardsTargetPos(): Boolean {
	if (isOnTarget(this)) {
		//already on target, don't move
		return false
	}
	//move toward target
	val targetPos = getTargetPos(this) ?: return false

	return this.moveTo(targetPos) == OK
}

/**
 * sets the position the creep will move to over the next few turns.
 * todo move this somewhere else.
 */
fun Creep.setTarget(position: RoomPosition) : Boolean{
	memory.behaviour.targetPos = position
	return true
}

fun updateHarvester(creep: Creep): Boolean {
	if (hasNoTarget(creep)){
		val target = creep.pos.findClosestByPath(FIND_SOURCES_ACTIVE)
		if (target != null) {
			creep.setTarget(target.pos)
		}
		//todo add some logging?
	}

	if (isEmpty(creep)){
		setPosToClosestSource(creep)
	} else if (!adjcentTo(creep, FIND_SOURCES_ACTIVE) || isFull(creep)){
		setPosToClosestStructure(creep, STRUCTURE_SPAWN )
				|| setPosToClosestStructure(creep, STRUCTURE_EXTENSION )
				|| setPosToClosestConstructionSite(creep)
				|| setPosToController(creep)
		//todo set pos to closest spawn or extension site.
	}

	creep.moveTowardsTargetPos()

	return depositIn(creep, STRUCTURE_SPAWN)
			|| depositIn(creep, STRUCTURE_EXTENSION)

			|| pickupFreeEnergy(creep)
			|| mine(creep)
			|| buildConstructionSite(creep)
			|| upgradeContoller(creep)
}

fun setPosToClosestConstructionSite(creep: Creep): Boolean {
	val site = creep.room.find(FIND_CONSTRUCTION_SITES)
			.sortedByDescending { (it.progress *1000) / (it.progressTotal *1000) }
			.sortedByDescending { starter.behaviours.buildPriority(it) }
			.sortedBy { creep.pos.getRangeTo(it) }
			.firstOrNull()
	if (site == null) {
		return false
	}
	return creep.setTarget(site.pos)
}

fun setPosToController(creep: Creep): Boolean {
	if (creep.room.controller == null){
		return false
	}
	creep.setTarget(creep.room.controller!!.pos)
	return true
}

fun pickupFreeEnergy(creep: Creep): Boolean {
	if (isFull(creep)){
		//can't pick up anymore
		return false
	}
	val resources = creep.pos.findInRange(FIND_DROPPED_RESOURCES, 1)
	//compiler says that this is always evaluates to false. not sure what resources returns if
	// there is nothing in range.
//	if (resources == null) {
//		return false
//	}
	for (resource in resources) {
		if (creep.pickup(resource) == OK) {
			return true
		}
	}
	return false
}
fun isEmpty(creep: Creep): Boolean {
	return creep.carry.energy == 0
}

fun getClosestAdjcentPos(creep: Creep, targetPos: RoomPosition) {

	return
}

fun setPosToClosestSource(creep: Creep): Boolean {
	val source = creep.pos.findClosestByPath(FIND_SOURCES_ACTIVE)
	if (source == null) {
		false
	}
	//todo this needs to set the closest free point, not the actuall source point
	return creep.setTarget(source!!.pos)

}

fun isFull(creep: Creep): Boolean {
	return creep.carry.energy == creep.carryCapacity
}

fun adjcentTo(creep: Creep, findFlag: FindConstant<Source> ): Boolean {
	return creep.pos.findInRange(findFlag, 1).count() > 0
}

fun setPosToClosestStructure(creep: Creep, structure: BuildableStructureConstant): Boolean {
	var targets = creep.room.find(FIND_MY_STRUCTURES)
			.filter {
				(it.structureType == STRUCTURE_EXTENSION
						|| it.structureType == STRUCTURE_SPAWN)
			}
			.filter { it.unsafeCast<EnergyContainer>().energy <
					it.unsafeCast<EnergyContainer>().energyCapacity
					&& it.isActive()}
	// note that it has to be active. if the room has downgraded then we can have inactive extensions.
	if (targets.isNullOrEmpty()) {
		return false
	}
	creep.setTarget(targets[0].pos)
	return true
}

fun updateUpgrader(creep: Creep): Boolean {

	if (hasNoTarget(creep)) {
		val pos: RoomPosition = getFreeUpgraderPos(creep.room)
		creep.setTarget(pos)
	}

	creep.moveTowardsTargetPos()

	//todo still need to pick up energy.
	return upgradeContoller(creep) ||
			buildConstructionSite(creep)

}

fun depositIn(creep: Creep, targetStructure: BuildableStructureConstant): Boolean {
	if (!isCarryingEnergy(creep)) {
		//can't build anything. no energy.
		return false
	}

	val targets = creep.pos.findInRange(FIND_STRUCTURES, 1)
			.filter { it -> it.structureType == targetStructure }

	if (targets.isNullOrEmpty()){
		return false
	}
	for (target in targets){
		if (depositEnergyAt(creep, target.pos) == OK) {
			return true
		}
	}
	return false
}

fun depositEnergyAt(creep: Creep, targetPos: RoomPosition): Any {
	return depostitEnergyInStructure(creep, targetPos) || dropEnergy(creep, targetPos)
}

fun dropEnergy(creep: Creep, targetPos: RoomPosition ): Boolean {

	if (creep.memory.behaviour.targetPos ==null ||
			loadPosFromMemory(creep.memory.behaviour.targetPos!!) != targetPos) {
		//not on square, so can't drop it.
		return false
	}
	return creep.drop(RESOURCE_ENERGY) == OK
}

fun depostitEnergyInStructure(creep: Creep, targetPos: RoomPosition): Boolean {
	val targets = creep.room.find(FIND_MY_STRUCTURES).filter { it.pos.isEqualTo(targetPos) }
	if (targets.isNullOrEmpty()) {
		return false
	}

	for (structure in  targets) {
		if (creep.transfer(structure, RESOURCE_ENERGY) == OK) {
			return true
		}
	}

	return false
}
fun isHarvester(creep: Creep): Boolean {
	return creep.memory.role == Role.HARVESTER
}


fun mine(creep: Creep): Boolean {
	console.log("mine")
	val sources = creep.pos.findInRange(FIND_SOURCES_ACTIVE, 1)
	if (sources.isNullOrEmpty()) {
		return false
	}
	for (source in sources) {
		if (creep.harvest(source) == OK) {
			return true
		}
	}
	return false
}

/**
 * upgrades the controller if able.
 */
fun upgradeContoller(creep: Creep): Boolean {
	if (!isCarryingEnergy(creep)){
		//can't build anything. no energy.
		return false
	}
	val controller = creep.room.controller
	if (controller == null || !controller.my) {
		return false
	}
	return creep.upgradeController(controller) == OK
}

fun buildConstructionSite(creep: Creep): Boolean {
	if (!isCarryingEnergy(creep)){
		//can't build anything. no energy.
		return false
	}
	var constructionSites = getConstructionSitesInRange(creep)

	if (constructionSites.isNullOrEmpty()){
		return false
	}

	for (site in constructionSites){
		if (creep.build(site) == OK) {
			return true
		}
	}
	return false
}

fun buildPriority(structure: ConstructionSite): Int {
	//always build roads last
	if (structure == STRUCTURE_ROAD) {
		return 30;
	}
	// the lower on this list the lower prioity it is
	val list = listOf<BuildableStructureConstant>(STRUCTURE_TOWER,
			STRUCTURE_SPAWN,
			STRUCTURE_EXTENSION,
			STRUCTURE_STORAGE,
			STRUCTURE_CONTAINER)
	var i=0
	for (item in list){
		i++
		if (structure == item){
			return i
		}
	}
	//if its not on the list then build it last. But still before the roads
	return i +1

}

fun getConstructionSitesInRange(creep: Creep): List<ConstructionSite> {
	return creep.pos.findInRange(FIND_CONSTRUCTION_SITES, 3).sortedBy { buildPriority(it) }
}

fun isCarryingEnergy(creep: Creep): Boolean {
	return creep.carry.energy > 0
}

/**
 * checks to see if the creep is ontop of it's target position.
 */
fun isOnTarget(creep: Creep): Boolean {
	return creep.memory.behaviour.targetPos != null &&
			loadPosFromMemory(creep.memory.behaviour.targetPos!!)  == creep.pos
}

/**
 * Gets the position for the upgrader to move to that is marked with an upgrader flag
 * and doesn't have a currently assigned upgrader.
 */
fun getFreeUpgraderPos(room: Room): RoomPosition {

	val points = room.find(FIND_FLAGS).filter { it -> isUpgraderFlag(it) &&
			noCreepHasPosAsTarget(it.pos) }
	//todo could also add that the creep that is targeting this position has to be an upgrader.
	//todo could optimise this with selecting a closer flag
	return points[0].pos
}


/**
 * checks to see if the flag is an upgrader flag.
 * should follow format of "upgrader_xx" with xx being the number.
 */
fun isUpgraderFlag(flag: Flag): Boolean {
	return flag.name.startsWith("upgrader_")
}
