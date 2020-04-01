package starter

import screeps.api.FIND_FLAGS
import screeps.api.FIND_SOURCES
import screeps.api.Game
import screeps.api.Room
import screeps.utils.contains
import screeps.utils.jsonToMap
import screeps.utils.unsafe.jsObject

fun Room.initLayout(){
	if (controller == null){
		return
	}
	if ( ! memory.isRoomInited){
		//initRoom()
		layoutBase()
		memory.isRoomInited = true
	}
}

private fun Room.initRoom(){
	console.log("initRoom is being run")
	val sourceMaps :MutableList<InfluenceMap> = mutableListOf()
	for (source in find(FIND_SOURCES)){
		var x = InfluenceMap()
		x.floodFill(listOf(source.pos))
		sourceMaps.add(x)

	}
	val dat :MutableList<Array<IntArray>> = mutableListOf()
	for (map in sourceMaps) {
		dat.add(map.map)
	}
	val dataToWrite : Array<Array<IntArray>> = dat.toTypedArray()
	memory.influenceMaps.distanceToSources = dataToWrite

	var x = InfluenceMap()
	x.floodFill(listOf(controller!!.pos))
	//x.drawOnScreen(name)
	memory.influenceMaps.distanceToController = x.map
	console.log("initRoom is ended")
}

fun Room.runLayout(){

	if (memory.mainStore.isEmpty()) {
		//you need to place the flag
		//look to see if the flag exists
		val flags = find(FIND_FLAGS).filter { it.name.contains("store",true) }
		if (! flags.isNullOrEmpty()) {
			memory.mainStore =flags[0].name

		}
		return
	}

	if (memory.layoutHasBeenGenerated) {
		//layout is already generated
		return
	}

	//generate the layout
	generateRoadsToSources()

	memory.layoutHasBeenGenerated =true
}

private fun Room.layoutBase() {
	console.log("laying out base")
	jsonToMap<>()
	var bunker= js("{name:'textExport';shard:'shard0';rcl:8;buildings:{tower:{pos:[{x:24,y:18},{x:26,y:18},{x:27,y:19},{x:23,y:21},{x:24,y:22},{x:26,y:22}]};terminal:{pos:[{x:23,y:19}]};spawn:{pos:[{x:24,y:19},{x:26,y:19},{x:26,y:21}]};road:{pos:[{x:25,y:19},{x:24,y:20},{x:26,y:20},{x:25,y:21}]};storage:{pos:[{x:25,y:20}]};powerSpawn:{pos:[{x:24,y:21}]};nuker:{pos:[{x:27,y:21}]}}};")
	val sourcePos = getPositionAt(bunker.buildings.storage.pos[0].x, bunker.buildings.storage.pos[0].y)
}
fun Room.generateRoadsToSources(){

}