package starter

//import com.beust.klaxon.Klaxon
import screeps.api.*


private fun Room.findStoreFlag(): List<Flag> {
	return find(FIND_FLAGS).filter { it.name.contains("base", true) }
}

private fun Room.storeFlagHasMoved(): Boolean {
	if (memory.bunker.mainStorePos == null){
		//there is no flag placed so how can it move?
		return false
	}
	val storeFlag = findStoreFlag()
	if (! storeFlag.isNullOrEmpty()){
		return false
	}
	if (  storeFlag[0].pos.x == memory.bunker.mainStorePos!!.x && storeFlag[0].pos.y == memory.bunker.mainStorePos!!.y ){
		//the positions are the same
		return false
	}
	return true
}

private fun Room.commandFlag(command:String, clearFlag:Boolean=true): Boolean {
	val commandFlags = find(FIND_FLAGS).filter {
		it.name.contains("command", true)
				&& it.name.contains(command, true)
	}
	if (commandFlags.isNullOrEmpty()) {
		return false
	}
	val flag = commandFlags[0]
	flag.remove()
	return true
}

private fun Room.clearAllLayoutFlags(){
	//clear all flags that have been generated
	val flags = find(FIND_FLAGS).filter {
		it.name.contains("placeholder", true) ||
				it.name.contains("extractor", true)
	}
	for (flag in flags) {
		flag.remove()
	}
	console.log("removed all layout flags")
}

fun Room.initLayout() {
	if (controller == null) {
		return
	}

	val commandFlags = find(FIND_FLAGS).filter {
		it.name.contains("command", true)
				&& it.name.contains("initroom", true)
	}
	val forceReset = commandFlag("initroom") || storeFlagHasMoved()
	if (forceReset) {
		console.log("force reseting room")
	}
	if (!memory.isRoomInited || forceReset == true) {
		//reset data
		memory.bunker.mainStorePos = null
		memory.layoutRoadsToSourcesHaveBeenGenerated = false
		memory.layoutExtractorPoints = false
		memory.layoutExtensions = false

		clearAllLayoutFlags()

		console.log("reseted latch variables")
		memory.isRoomInited = true
		return
	}

	layoutInitMainBaseFlag()
	if (memory.bunker.mainStorePos == null) {
		//todo find the best place for it
		console.log("need to place the mainStorePos")
		return
	}
	if (!memory.layoutRoadsToSourcesHaveBeenGenerated ) {
		generateRoadsToSources()
		return
	}
	//initRoom()
	layoutBase()
	if (!memory.layoutExtensions ){
		layoutExtensions()
		return
	}

	//todo add latch variables and return
	layoutExtractorPoints()


}

private fun Room.LayoutExtensionsAlongRoads() {
	val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
	if (mainStorePos == null) {
		console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
		return
	}
	var flagCounter = 0
	val flags = mainStorePos.findInRange(FIND_FLAGS,50)
	//todo sort by distance from base
	for (flag in flags) {
		val adjcent = getAdjcentSquares(flag.pos)
		for (square in adjcent) {
			if (square.lookFor(LOOK_FLAGS).isNullOrEmpty() && square.lookFor(LOOK_STRUCTURES).isNullOrEmpty()) {
				//place an extension flag
				createFlag(square.x, square.y, "extension placeholder " + flagCounter.toString(), COLOR_GREEN)
				flagCounter ++
				if (flagCounter > 80) {
					break
				}
			}
		}
		if (flagCounter > 80) {
			break
		}
	}
}

private fun Room.squareIsEmpty(square: RoomPosition): Boolean {
	return (square.lookFor(LOOK_FLAGS).isNullOrEmpty()
			&& square.lookFor(LOOK_STRUCTURES).isNullOrEmpty()
			&& getTerrain().get(square.x,square.y) != TERRAIN_MASK_WALL
			&& square.lookFor(LOOK_CONSTRUCTION_SITES).isNullOrEmpty()
			&& square.lookFor(LOOK_SOURCES).isNullOrEmpty()

			)
}

private fun Room.LayoutExtensionsLattice() {
	val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
	if (mainStorePos == null) {
		console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
		return
	}
	var frontier : MutableList<Pair<Int,RoomPosition>> = mutableListOf(Pair(0, mainStorePos))
	var isExpandedMap = Array(50, {BooleanArray(50) {false} })
	var roadCounter = 0
	var extensionCounter =0
	var loopCounter =0
	while (frontier.isNotEmpty() && loopCounter <300) {
		loopCounter++

		val (currentScore, currentPos) = frontier.removeAt(frontier.size-1)
		if (isExpandedMap[currentPos.x][currentPos.y] ){
			console.log("already expanded $currentPos, skipping")
			continue
		}
		isExpandedMap[currentPos.x][currentPos.y] = true
		console.log("expanding $currentPos")

		if (squareIsEmpty(currentPos)) {
			console.log("square is empty. placing flag at $currentPos")
			if ((currentPos.y + currentPos.x )%2==0) {
				createFlag(currentPos.x, currentPos.y, "extension placeholder " + extensionCounter.toString(), COLOR_GREY)
				extensionCounter++
				if (extensionCounter >80){
					console.log("have placed all 80 extensions. exiting ")
					break
				}
			} else {
				createFlag(currentPos.x, currentPos.y, "road placeholder forExtensions " + roadCounter.toString(), COLOR_BROWN)
				roadCounter++
			}
		}else {
			console.log("square is occupied, skipping")
		}
		val adjacent = getAdjcentSquares(currentPos)
		var insertIndex =0
		if (frontier.size > 0) {
			for (insertIndex in 0 until  frontier.size) {
				if (frontier[insertIndex].first < currentScore) {
					break
				}
			}
		}
		for (square in adjacent) {
			if (isExpandedMap[square.x][square.y]== false
					&& getTerrain().get(square.x,square.y) != TERRAIN_MASK_WALL) {
				frontier.add(insertIndex, Pair<Int, RoomPosition>(currentScore + 1, square))
			}
		}
	}
}

private fun Room.layoutExtensions() {
	if (memory.bunker.mainStorePos == null) {
		console.log("generateRoadsToSources: error mainStore pos is null. should be the base pos")
		return
	}

	if (memory.layoutExtensions ){
		return
	}

	val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
	if (mainStorePos == null) {
		console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
		return
	}
	LayoutExtensionsLattice()
	memory.layoutExtensions =true


}
private fun Room.layoutExtractorPoints() {
	if (memory.layoutExtractorPoints == true) {
		return
	}
	//todo clear flags
	var flagCounter = 0
	for (source in find(FIND_SOURCES)) {
		val roads = find(FIND_FLAGS).filter {
			it.name.contains("placeholder", true)
					&& it.name.contains("road", true)
					&& it.pos.isNearTo(source.pos)
		}
		if (roads.isNullOrEmpty()) {
			console.log("layoutExtractorPoints: error could not find a flag next to the source ${source.pos}")
			//todo place randomly?
			break
		}
		createFlag(roads[0].pos, "extractor " + flagCounter.toString())
		flagCounter++
	}
	memory.layoutExtractorPoints = true
}

fun Room.update() {
	initLayout()

	updateHeatmap() // heatmap
	updateLayoutOnControlLevelChange()
}

fun Room.convertFlagToConstruction(idString: String, structureExtension: BuildableStructureConstant ){
	for (flag in find(FIND_FLAGS).filter { it.name.contains("placeholder", true) && it.name.contains(idString, true) }) {
		createConstructionSite(flag.pos, structureExtension)
		flag.remove()
	}
}

/*
every time the CL changes, we want to build new structures.
 */
fun Room.updateLayoutOnControlLevelChange() {
	if (controller == null) {
		return
	}

	if (memory.layoutCL == controller!!.level ){
		//skip this. the level has not changed
		return
	}
	//todo only convert one.
	convertFlagToConstruction("extension", STRUCTURE_EXTENSION)
	convertFlagToConstruction("tower", STRUCTURE_TOWER)


	if (controller!!.level >= 3) {
		convertFlagToConstruction("road", STRUCTURE_ROAD)
	}
	memory.layoutCL = controller!!.level
}

private fun Room.initRoom() {
	console.log("initRoom is being run")
	val sourceMaps: MutableList<InfluenceMap> = mutableListOf()
	for (source in find(FIND_SOURCES)) {
		var x = InfluenceMap()
		x.floodFill(listOf(source.pos))
		sourceMaps.add(x)

	}
	val dat: MutableList<Array<IntArray>> = mutableListOf()
	for (map in sourceMaps) {
		dat.add(map.map)
	}
	val dataToWrite: Array<Array<IntArray>> = dat.toTypedArray()
	memory.influenceMaps.distanceToSources = dataToWrite

	var x = InfluenceMap()
	x.floodFill(listOf(controller!!.pos))
	//x.drawOnScreen(name)
	memory.influenceMaps.distanceToController = x.map
	console.log("initRoom is ended")
}

fun Room.layoutInitMainBaseFlag() {

	if (memory.bunker.mainStorePos != null && memory.bunker.mainStoreId != "") {
		//all set up
		return
	}

	val flags = findStoreFlag()
	if (!flags.isNullOrEmpty()) {
		memory.bunker.mainStoreId = flags[0].name
		memory.bunker.mainStorePos = flags[0].pos
		console.log("findMainBaseFlag: set base flag to ${memory.bunker.mainStorePos}")
	}
}
/*
places the structures of the core of the base
 */
private fun Room.layoutBase() {
	//todo add latch variable
	//console.log("laying out base")
	var bunker = js("{name:'textExport';shard:'shard0';rcl:8;buildings:{tower:{pos:[{x:24,y:18},{x:26,y:18},{x:27,y:19},{x:23,y:21},{x:24,y:22},{x:26,y:22}]};terminal:{pos:[{x:23,y:19}]};spawn:{pos:[{x:24,y:19},{x:26,y:19},{x:26,y:21}]};road:{pos:[{x:25,y:19},{x:24,y:20},{x:26,y:20},{x:25,y:21}]};storage:{pos:[{x:25,y:20}]};powerSpawn:{pos:[{x:24,y:21}]};nuker:{pos:[{x:27,y:21}]}}};")


//	val parser: Parser = Parser.default()
//	val stringBuilder: StringBuilder = StringBuilder("{\"name\":\"Cedric Beust\", \"age\":23}")
//	val json: JsonObject = parser.parse(stringBuilder) as JsonObject

//	val sourcePos = getPositionAt(bunker.buildings.storage.pos[0].x, bunker.buildings.storage.pos[0].y)
}

fun Room.generateRoadsToSources() {
	if (memory.layoutRoadsToSourcesHaveBeenGenerated) {
		//layout is already generated
		return
	}

	console.log("running generate roads")
	if (memory.bunker.mainStorePos == null) {
		console.log("generateRoadsToSources: error mainStore pos is null. should be the base pos")
		return
	}
	val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
	if (mainStorePos == null) {
		console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
		return
	}

	var roadCounter = 0
	for (source in find(FIND_SOURCES)) {
		//find path to source
		//place road flags along path
		//todo ingore creeps
		val path = findPath(mainStorePos!!, source.pos)
		if (path == null) {
			console.log("generateRoadsToSources: error could not find a path between $mainStorePos and ${source.pos}. ")
			break
		}
		for (step in path) {
			val pos = getPositionAt(step.x, step.y)
			if (pos == source.pos) {
				//don't place one on the source pos
				continue
			}
			roadCounter++
			createFlag(step.x, step.y, "road placeholder baseToSource " + roadCounter.toString())
		}
	}

	console.log("finished generate roads. added $roadCounter roads")
	memory.layoutRoadsToSourcesHaveBeenGenerated = true
}