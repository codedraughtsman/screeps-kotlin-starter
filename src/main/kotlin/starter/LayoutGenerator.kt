package starter

//import com.beust.klaxon.Klaxon
import screeps.api.*


private fun Room.findStoreFlag(): List<Flag> {
	return find(FIND_FLAGS).filter { it.name.contains("base", true) }
}

private fun Room.storeFlagHasMoved(): Boolean {
	if (memory.bunker.mainStorePos == null) {
		//there is no flag placed so how can it move?
		return false
	}
	val storeFlag = findStoreFlag()
	if (!storeFlag.isNullOrEmpty()) {
		return false
	}
	if (storeFlag[0].pos.x == memory.bunker.mainStorePos!!.x && storeFlag[0].pos.y == memory.bunker.mainStorePos!!.y) {
		//the positions are the same
		return false
	}
	return true
}

private fun Room.commandFlag(command: String, clearFlag: Boolean = true): Boolean {
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

private fun Room.clearAllLayoutFlags() {
	//clear all flags that have been generated
	val flags = find(FIND_FLAGS).filter {
		it.name.contains("placeholder", true) ||
				it.name.contains("extractor", true)
	}
	for (flag in flags) {
		flag.remove()
	}
//	console.log("removed all layout flags")
}

	fun Room.initLayout() {
		console.log("init layout is being called")
		if (controller == null) {
			return
		}

		val forceReset = commandFlag("initroom")
		if (forceReset) {
			console.log("force reseting room")
			memory.isRoomInited = false
		}
		if (!memory.isRoomInited ) {

			//reset data
			memory.bunker.mainStorePos = null
			memory.layoutRoadsToSourcesHaveBeenGenerated = false
			memory.layoutExtractorPoints = false
			memory.layoutExtensions = false
			memory.layoutCoreHasBeenGenerated = false

			clearAllLayoutFlags()

//		console.log("reseted latch variables")
			memory.isRoomInited = true
			return
		}
		console.log("layoutInitMainBaseFlag is being called")
		layoutInitMainBaseFlag()

		if (memory.bunker.mainStorePos == null) {
			//todo find the best place for it
//		console.log("need to place the mainStorePos")
			return
		}

		//initRoom()
		if (!memory.layoutCoreHasBeenGenerated) {
			layoutBaseCore()
			return
		}

		if (!memory.layoutExtensions) {
			layoutExtensions()
			return
		}

		if (!memory.layoutRoadsToSourcesHaveBeenGenerated) {
			generateRoadsToSources()
			return
		}
		//todo add latch variables and return
		layoutExtractorPoints()


	}


	private fun Room.squareIsEmpty(square: RoomPosition): Boolean {
		return (square.lookFor(LOOK_FLAGS).isNullOrEmpty()
				&& square.lookFor(LOOK_STRUCTURES).isNullOrEmpty()
				&& getTerrain().get(square.x, square.y) != TERRAIN_MASK_WALL
				&& square.lookFor(LOOK_CONSTRUCTION_SITES).isNullOrEmpty()
				&& square.lookFor(LOOK_SOURCES).isNullOrEmpty()

				)
	}

	private fun Room.LayoutExtensionsLattice() {
		val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
		if (mainStorePos == null) {
//		console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
			return
		}
		var frontier: MutableList<Pair<Int, RoomPosition>> = mutableListOf(Pair(0, mainStorePos))
		var isExpandedMap = Array(50, { BooleanArray(50) { false } })
		var roadCounter = 0
		var extensionCounter = 0
		var loopCounter = 0
		while (frontier.isNotEmpty() && loopCounter < 300) {
			loopCounter++

			val (currentScore, currentPos) = frontier.removeAt(frontier.size - 1)
			if (isExpandedMap[currentPos.x][currentPos.y]) {
				console.log("already expanded $currentPos, skipping")
				continue
			}
			isExpandedMap[currentPos.x][currentPos.y] = true
			console.log("expanding $currentPos")

			if (squareIsEmpty(currentPos)) {
				console.log("square is empty. placing flag at $currentPos")
				if ((currentPos.y + currentPos.x) % 2 == 0) {
					createFlag(currentPos.x, currentPos.y, "extension placeholder " + extensionCounter.toString(), COLOR_BROWN)
					extensionCounter++
					if (extensionCounter > 80) {
						console.log("have placed all 80 extensions. exiting ")
						break
					}
				} else {
					createFlag(currentPos.x, currentPos.y, "road placeholder forOuterRing " + roadCounter.toString(), COLOR_GREY)
					roadCounter++
				}
			} else {
				console.log("square is occupied, skipping")
			}
			val adjacent = getAdjcentSquares(currentPos)
			var insertIndex = 0
			if (frontier.size > 0) {
				for (insertIndex in 0 until frontier.size) {
					if (frontier[insertIndex].first < currentScore) {
						break
					}
				}
			}
			for (square in adjacent) {
				if (isExpandedMap[square.x][square.y] == false
						&& getTerrain().get(square.x, square.y) != TERRAIN_MASK_WALL) {
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

		if (memory.layoutExtensions) {
			return
		}

		val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
		if (mainStorePos == null) {
			console.log("generateRoadsToSources: error creating a pos from mainStore pos failed")
			return
		}
		LayoutExtensionsLattice()
		memory.layoutExtensions = true


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
			createFlag(roads[0].pos, "extractor " + flagCounter.toString(), COLOR_ORANGE)
			flagCounter++
		}
		memory.layoutExtractorPoints = true
	}

	fun Room.update() {
		initLayout()

		updateHeatmap() // heatmap
		updateLayoutOnControlLevelChange()
	}

	fun Room.convertFlagToConstruction(idString: String, structureExtension: BuildableStructureConstant, onlyOne: Boolean = false) {
		for (flag in find(FIND_FLAGS).filter { it.name.contains("placeholder", true) && it.name.contains(idString, true) }) {
			createConstructionSite(flag.pos, structureExtension)
			flag.remove()
			if (onlyOne) {
				return
			}
		}
	}

	/*
every time the CL changes, we want to build new structures.
 */
	fun Room.updateLayoutOnControlLevelChange() {
		if (controller == null) {
			return
		}

//	if (memory.layoutCL == controller!!.level) {
//		//skip this. the level has not changed
//		return
//	}
		val objs = arrayListOf<Pair<String, BuildableStructureConstant>>(Pair("extension", STRUCTURE_EXTENSION),
				Pair("tower", STRUCTURE_TOWER))
		for ((stringId, structureId) in objs) {
			val existingStructures = find(FIND_MY_STRUCTURES).filter { it.structureType == structureId }
			val existingConstructionsSites = find(FIND_CONSTRUCTION_SITES).filter { it.structureType == structureId }
			val maxAmount = CONTROLLER_STRUCTURES[structureId]?.get(controller!!.level)
			if (maxAmount == null) {
//				console.log("updateLayoutOnControlLevelChange: error could not find the max amount of structures at this level")
				continue
			}
			if (!existingConstructionsSites.isNullOrEmpty()) {
				//one is being already being built
				continue
			}
			if (existingStructures != null && existingStructures.size >= maxAmount) {
				//have already built the limit of this structure
				continue
			}
			//if already one being built
			//continue
			convertFlagToConstruction(stringId, structureId, onlyOne = true)
		}


		if (controller!!.level >= 3) {
			convertFlagToConstruction("road", STRUCTURE_ROAD)
		}
		memory.layoutCL = controller!!.level
	}


	fun Room.layoutInitMainBaseFlag() {

		if (memory.bunker.mainStorePos != null && memory.bunker.mainStoreId != "") {
			//all set up
			console.log("bunker main store pos is already set, not going to layout main base flag. ${memory.bunker.mainStorePos}")
			return
		}

		val flags = findStoreFlag()
		if (!flags.isNullOrEmpty()) {
			memory.bunker.mainStoreId = flags[0].name
			memory.bunker.mainStorePos = flags[0].pos
			console.log("findMainBaseFlag: set base flag to ${memory.bunker.mainStorePos}")
		} else {
			console.log("main store flag is already present")
		}
	}

	/*
places the structures of the core of the base
 */
	private fun Room.layoutBaseCore() {
		if (memory.layoutCoreHasBeenGenerated == true) {
			return
		}
		if (memory.bunker.mainStorePos == null) {
			console.log("layoutBaseCore: error mainStore pos is null. should be the base pos")
			return
		}
		val mainStorePos = getPositionAt(memory.bunker.mainStorePos!!.x, memory.bunker.mainStorePos!!.y)
		if (mainStorePos == null) {
			console.log("layoutBaseCore: error creating a pos from mainStore pos failed")
			return
		}

		var flagCounter = 0

		for (x in -2..2) {
			for (y in -2..2) {
				val square = getPositionAt(mainStorePos.x + x, mainStorePos.y + y)
				if (square != null && squareIsEmpty(square)) {
					createFlag(square.x, square.y, "core placeholder  " + flagCounter.toString(), COLOR_PURPLE)
					flagCounter++
				}
			}
		}

		//todo place storage.
		//todo place towers
		//can do the rest with a json object later


		//console.log("laying out baseCore")
		var bunker = js("{name:'textExport';shard:'shard0';rcl:8;buildings:{tower:{pos:[{x:24,y:18},{x:26,y:18},{x:27,y:19},{x:23,y:21},{x:24,y:22},{x:26,y:22}]};terminal:{pos:[{x:23,y:19}]};spawn:{pos:[{x:24,y:19},{x:26,y:19},{x:26,y:21}]};road:{pos:[{x:25,y:19},{x:24,y:20},{x:26,y:20},{x:25,y:21}]};storage:{pos:[{x:25,y:20}]};powerSpawn:{pos:[{x:24,y:21}]};nuker:{pos:[{x:27,y:21}]}}};")
		var s = """{"name":"basic","shard":"shard0","rcl":"8","buildings":{"tower":{"pos":[{"x":24,"y":18},{"x":26,"y":18},{"x":27,"y":19},{"x":23,"y":21},{"x":24,"y":22},{"x":26,"y":22}]},"road":{"pos":[{"x":25,"y":18},{"x":25,"y":19},{"x":23,"y":20},{"x":24,"y":20},{"x":26,"y":20},{"x":27,"y":20},{"x":25,"y":21},{"x":25,"y":22}]},"terminal":{"pos":[{"x":23,"y":19}]},"spawn":{"pos":[{"x":24,"y":19},{"x":26,"y":19},{"x":26,"y":21}]},"storage":{"pos":[{"x":25,"y":20}]},"powerSpawn":{"pos":[{"x":24,"y":21}]},"nuker":{"pos":[{"x":27,"y":21}]}}}"""

//	val parser: Parser = Parser.default()
//	val stringBuilder: StringBuilder = StringBuilder("{\"name\":\"Cedric Beust\", \"age\":23}")
//	val json: JsonObject = parser.parse(stringBuilder) as JsonObject

//	val sourcePos = getPositionAt(bunker.buildings.storage.pos[0].x, bunker.buildings.storage.pos[0].y)
		memory.layoutCoreHasBeenGenerated = true
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
				if (pos == null) {
					//invalid position
					continue
				}
				if (pos == source.pos) {
					//don't place one on the source pos
					continue
				}
				if (squareIsEmpty(pos)) {
					roadCounter++
					createFlag(pos.x, pos.y, "road placeholder baseToSource " + roadCounter.toString())
				}
			}
		}

		console.log("finished generate roads. added $roadCounter roads")
		memory.layoutRoadsToSourcesHaveBeenGenerated = true
	}
