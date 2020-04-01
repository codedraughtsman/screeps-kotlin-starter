package starter

import screeps.api.RoomPosition
import screeps.api.RoomVisual


fun getAdjcentSquares(pos: RoomPosition): MutableList<RoomPosition> {
	var output = mutableListOf<RoomPosition>()
	for (x in -1..1) {
		for (y in -1..1) {
//			try {
//				val newPos = RoomPosition(pos.x + x, pos.y + y, pos.roomName)
//				output.add(newPos)
//			} catch (e: Exception){
//
//			}

			if ((pos.x + x) >= 0 && (pos.x + x) < 50
					&& (pos.y) + y >= 0 && (pos.y + y) < 50
					&& !(y == 0 && x == 0)) {
				val newPos = RoomPosition(pos.x + x, pos.y + y, pos.roomName)
				output.add(newPos)
			}
		}
	}
	return output
}

class InfluenceMap(var x: Int = 50, var y: Int = 50) {
	lateinit var map: Array<IntArray>


	init {
		map = Array(x, { IntArray(y) { 0 } })
	}

	fun floodFill(startingPositions: List<RoomPosition>) {
		map = Array(x, { IntArray(y) { Int.MAX_VALUE } })
		var frontier: MutableList<Pair<Int, RoomPosition>> = mutableListOf<Pair<Int, RoomPosition>>()
		for (pos in startingPositions) {
			frontier.add(Pair<Int, RoomPosition>(0, pos))
		}
		console.log("floodfill have converted pos into frontier")
		var counter =0
		while (frontier.isNotEmpty() && counter < 3000) {
			//counter ++
			console.log("frontier is ${frontier}")
			val (currentScore, currentPos) = frontier.removeAt(frontier.size-1)
			if (map[currentPos.x][currentPos.y] > currentScore) {
				// this is a better route, so update it.
				map[currentPos.x][currentPos.y] = currentScore
				val adjcentSquares = getAdjcentSquares(currentPos)
				console.log("starting insert indexes")
				var insertIndex = 0
				if (frontier.size > 0) {
					for (insertIndex in 0 until  frontier.size) {
						if (frontier[insertIndex].first < currentScore) {
							break
						}
					}
				}
				for (square in adjcentSquares) {
					frontier.add(insertIndex, Pair<Int, RoomPosition>(currentScore + 1, square))
				}
			}
		}

	}
	fun drawOnScreen(roomName: String){

		for (x in 0 until map.size) {
			for (y in 0 until map[0].size) {
				val v = RoomVisual(roomName)
				v.text(map.toString(), x, y)
			}

		}

	}
	fun add(input :InfluenceMap){
		for (x in 0 until map.size) {
			for (y in 0 until map[0].size) {
				map[x][y] += input.map[x][y]
			}
		}
	}
}