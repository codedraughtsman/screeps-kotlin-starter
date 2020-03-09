package starter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import screeps.api.*
import screeps.api.Room
import screeps.api.RoomPosition
import screeps.api.STRUCTURE_EXTENSION
import screeps.api.STRUCTURE_ROAD

class HeatMap (val room: Room, val xSize: Int = 50, val ySize: Int = 50 ){
	var map: Array<IntArray> = Array(xSize, {IntArray(ySize)})
	var sortedPositions: MutableList<Pair<Int, RoomPosition>> = arrayListOf()
	var isDirty: Boolean = true

	fun addValueToLocation(pos : RoomPosition, value: Int){
		map[pos.x][pos.y] += value
	}
	fun updateRoom() {
		for (creep in room.find(FIND_MY_CREEPS)){
			if (creep.fatigue > 0) {
				map[creep.pos.x][creep.pos.y] +=1
			}
		}
		isDirty = true
	}


	private fun updateSortedPositions() {
		sortedPositions.clear()
		for (x in 0..xSize) {
			for (y in 0..ySize){
				sortedPositions.add(Pair<Int, RoomPosition>(map[x][y], RoomPosition(x, y,room.name)))
			}
		}
		sortedPositions.sortBy { it.first }
		isDirty = false
	}

	fun getBestRoadLocation(minimumCostValue: Int = 20): RoomPosition? {
		if (isDirty) {
			updateSortedPositions()
		}
		for ( (value,pos) in sortedPositions){
			val structureAtLocation = room.lookForAt(LOOK_STRUCTURES, pos.x, pos.y) == null
			if (value > minimumCostValue && structureAtLocation ) {
				//build a road here
				return pos
			}
		}
		return null
	}
}