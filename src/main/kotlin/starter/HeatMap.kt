package starter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import screeps.api.*
import screeps.api.Room
import screeps.api.RoomPosition
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_ROAD
import kotlin.math.max


fun Room.addValueToLocation(pos: RoomPosition, value: Int) {
	memory.heatmap[pos.x][pos.y] += value
}

fun Room.updateRoom() {
	memory.heat.ticksSinceLastClear +=1
	if (memory.heat.ticksSinceLastClear > 400) {
		console.log("clearing tick counter for heatmap in room: $name")
		memory.heat.ticksSinceLastClear =0
		for (x in 0..49) {
			for (y in 0..49) {
				memory.heat.map[x][y] = max(memory.heat.map[x][y] -10, 0)
			}
		}
	}
	for (creep in this.find(FIND_MY_CREEPS)) {
		if (creep.fatigue > 0) {
			/*
			console.log("adding fatigue to ${creep.pos.x}, ${creep.pos.y} ")
			//console.log("main object ${memory.heatmap} ")
			//val casted = memory.heatmap.unsafeCast<Array<IntArray>>()
			val casted : Array<IntArray> = memory.heatmap.unsafeCast<Array<IntArray>>()
			val myArr : Array<IntArray> = Array(50, {IntArray(50) { 0} })

			console.log("myArr main object ${myArr} ")
			console.log( "bool is ${memory.heatmapIsDirty}")
			console.log( "memory.arr is ${memory.arr}")
			console.log( "memory.person is ${memory.person}")
			console.log( "memory.person is ${memory.person.name}")
			console.log( "memory.heat is ${memory.heat}")
			console.log( "memory.heat.map is ${memory.heat.map}")
			console.log( "memory.heat.map[0] is ${memory.heat.map[0]}")
			console.log( "memory.heat.map[0][0] is ${memory.heat.map[0][0]}")
*/


			memory.heat.map[creep.pos.x][creep.pos.y] += 1
//			if (memory.heat.map[creep.pos.x][creep.pos.y] > memory.heat.buildRoadLimit){
			if (memory.heat.map[creep.pos.x][creep.pos.y] > 10){

				createConstructionSite(creep.pos, STRUCTURE_ROAD)
				console.log( "heatmap creating road at value ${creep.pos} is ${memory.heat.map[creep.pos.x][creep.pos.y]}")

			}
			console.log("heatmap at ${creep.pos} is ${memory.heat.map[creep.pos.x][creep.pos.y]}")
		}
	}
	//memory.heatmapIsDirty = true
}

/*
private fun Room.updateSortedPositions() {
	//memory.heatmapSortedPositions.clear()
	memory.heatmapSortedPositions = arrayListOf<Pair<Int, RoomPosition>>()
	for (x in 0..50) {
		for (y in 0..50) {
			memory.heatmapSortedPositions.add(Pair<Int, RoomPosition>(memory.heatmap[x][y], RoomPosition(x, y, this.name)))
		}
	}
	memory.heatmapSortedPositions.sortBy { it.first }
	//memory.heatmapIsDirty = false
}

 */

private fun Room.getSortedPositions(): MutableList<Pair<Int, RoomPosition>> {
	//memory.heatmapSortedPositions.clear()
	var sortedPositions :  MutableList<Pair<Int, RoomPosition>> = arrayListOf<Pair<Int, RoomPosition>>()
	for (x in 0..49) {
		for (y in 0..49) {
			//console.log("adding at $x, $y in room ${name}")
			sortedPositions.add(Pair<Int, RoomPosition>(memory.heat.map[x][y], RoomPosition(x, y, this.name)))
		}
	}
	sortedPositions.sortByDescending { it.first }
	return sortedPositions
	//memory.heatmapIsDirty = false
}

fun Room.getBestRoadLocation(minimumCostValue: Int = 4): RoomPosition? {
	//if (memory.heatmapIsDirty) {
	//	updateSortedPositions()
	//}
	var sortedPositions = getSortedPositions()
	console.log(" best road is ${sortedPositions[0]}")
	for ((value, pos) in sortedPositions) {
		if (value < minimumCostValue) {
			//since the positions are sorted by value there is no point looking any further.
			return null
		}
		val noStructureAtLocation = this.lookForAt(LOOK_STRUCTURES, pos.x, pos.y).isNullOrEmpty()

		if ( noStructureAtLocation) {
			//build a road here
			console.log("creating road at $pos")
			createConstructionSite(pos , STRUCTURE_ROAD)
			return pos
		}
	}
	return null
}
