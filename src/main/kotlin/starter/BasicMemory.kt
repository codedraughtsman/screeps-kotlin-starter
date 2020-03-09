package starter

import screeps.api.*
import screeps.utils.memory.memory

/* Add the variables that you want to store to the persistent memory for each object type.
* They can be accessed by using the .memory attribute of any of the instances of that class
* i.e. creep.memory.building = true */

/* Creep.memory */
var CreepMemory.pause: Int by memory { 0 }
var CreepMemory.role by memory(Role.UNASSIGNED)
var CreepMemory.isCollectingEnergy : Boolean by memory { false }


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

/* room.memory */
var RoomMemory.heatmap : HeatMap by memory<HeatMap> { HeatMap(Game.spawns.values.first().room) }
var RoomMemory.map: Array<IntArray> by memory { Array(50, {IntArray(50)}) }

/* spawn.memory */
var SpawnMemory.test : Int by memory { 0 }
