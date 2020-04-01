package starter

import screeps.api.*
import screeps.utils.memory.memory
import screeps.utils.unsafe.jsObject


 external interface Person {
	var name: String?
	var age: Int // non nullable type here is dangerous because we could forget initialization
}

 external interface Kangaroo {
	var belly: Person?
	var name: String
	var friends: Array<Person>?
}

external interface Heatmap {
	var ticksSinceLastClear: Int
	var map: Array<IntArray>
	var buildRoadLimit: Int

}

external interface BehaviourMemory {
	var targetPos : RoomPosition?
	var gotoPos : RoomPosition?
	var sourcePos : RoomPosition?

}
external interface InfluenceMapsMemory {
	var distanceToSources : Array<Array<IntArray>>
	var distanceToController : Array<IntArray>
}

external interface BunkerMemory {
	var mainStoreId : String
	var mainStorePos : RoomPosition?
}

/* Add the variables that you want to store to the persistent memory for each object type.
* They can be accessed by using the .memory attribute of any of the instances of that class
* i.e. creep.memory.building = true */

/* Creep.memory */
var CreepMemory.pause: Int by memory { 0 }
var CreepMemory.role by memory(Role.UNASSIGNED)
var CreepMemory.isCollectingEnergy : Boolean by memory { false }
var CreepMemory.behaviour : BehaviourMemory by memory { jsObject<starter.BehaviourMemory> {  targetPos =null; sourcePos =null; gotoPos =null} }


/* Rest of the persistent memory structures.
* These set an unused test variable to 0. This is done to illustrate the how to add variables to
* the memory. Change or remove it at your convenience.*/

/* Power creep is a late game hero unit that is spawned from a Power Spawn
   see https://docs.screeps.com/power.html for more details.
   This set sets up the memory for the PowerCreep.memory class.
 */
var PowerCreepMemory.test : Int by memory { 0 }

/* flag.memory */
var FlagMemory.test : Int by memory { 0 }

//todo pull out all of the layout latches into a class
/* room.memory */
var RoomMemory.heat : Heatmap by memory { jsObject<Heatmap> {map = Array(50, {IntArray(50) {0} }) ; buildRoadLimit = 10} }
var RoomMemory.heatmap: Array<IntArray> by memory { jsObject<Array<IntArray>> {  }}
var RoomMemory.heatmapSortedPositions: MutableList<Pair<Int, RoomPosition>> by memory {arrayListOf<Pair<Int, RoomPosition>>()}
var RoomMemory.maxWorkers : Int by memory { 3 }
var RoomMemory.minControlLevelBeforeBuildingRoads: Int by memory { 3 }
var RoomMemory.mainStore : String by memory { "" }
var RoomMemory.bunker : BunkerMemory by memory { jsObject<BunkerMemory>{mainStoreId= ""; mainStorePos = null} }
var RoomMemory.layoutRoadsToSourcesHaveBeenGenerated : Boolean by memory { false }
var RoomMemory.layoutCL : Int by memory { 0 }
var RoomMemory.isRoomInited : Boolean by memory {false}
var RoomMemory.influenceMaps : InfluenceMapsMemory by memory { jsObject<InfluenceMapsMemory> {} }
var RoomMemory.layoutExtractorPoints: Boolean by memory { false }
var RoomMemory.layoutExtensions: Boolean by memory { false }

//var screeps.api.RoomMemory.influenceMaps : InfluenceMapsMemory by memory { jsObject<InfluenceMapsMemory> {distanceToSources = Array<Array<IntArray>>() }}





var RoomMemory.kangaroo: Kangaroo by memory {
	jsObject<Kangaroo> {
		name = "Kang"
	}
}


/* spawn.memory */
var SpawnMemory.test : Int by memory { 0 }
