package starter.multiAI.Actions

import screeps.api.*
import starter.Bunker
import starter.behaviour
import starter.multiAI.MultiAI
import starter.utils.getTargetPos

object Builder {
	fun builder (creep: Creep) : MultiAI.ReturnType {
		if (getTargetPos(creep) == null) {
			//just spawned. set to get energy.
			MoveSetTarget.baseStore(creep)
		}

		if (getTargetPos(creep) == null) {
			//spawn did not set this properly
		}

		var target: RoomPosition = getTargetPos(creep)!!
		var isHarvesting = target.isEqualTo(Bunker(Game.rooms.get(target.roomName)!!).storagePos()!!)

		if (isHarvesting) {

			if (creep.pos.isNearTo(target) || creep.carry.energy >0) {
				//have just fulled up.
				MoveSetTarget.constructionSite(creep)
				val t = getTargetPos(creep)!!
				creep.say("${t.x} ${t.y}")
			} else {
				Move.towardsTarget(creep)
			}
		} else {
			if (target.findInRange(FIND_CONSTRUCTION_SITES,0).count() == 0) {
				creep.memory.behaviour.targetPos = null
				return MultiAI.ReturnType.CONTINUE
			}
			if (creep.carry.energy == 0) {
				//run out of energy.
				MoveSetTarget.baseStore(creep)
			} else if (!creep.pos.inRangeTo(target,3)) {
				Move.towardsTarget(creep)
			}
		}

		return MultiAI.ReturnType.CONTINUE
	}
//	fun builder2 (creep: Creep) : MultiAI.ReturnType {
//		var target: RoomPosition? = getTargetPos(creep)
//		var isHarvesting = target.isEqualTo(Bunker(Game.rooms.get(target!!.roomName)!!).storagePos()!!)
//
//		if (isHarvesting && creep.carry.energy == 0) {
//			//need to go and pickup more energy
//			creep.memory.behaviour.targetPos = null
//		}
//
//		if ( getTargetPos(creep) == null) {
//			if (creep.carry.energy ==0) {
//				MoveSetTarget.baseStore(creep)
//				//todo change this to closest source of energy
//			}
//			else {
//				MoveSetTarget.constructionSite(creep)
//			}
//
//		}
//
//		if (target == null) {
//			return MultiAI.ReturnType.CONTINUE
//		}
//
//		target: RoomPosition? = getTargetPos(creep)
//
//		isHarvesting = target.isEqualTo(Bunker(Game.rooms.get(target!!.roomName)!!).storagePos()!!)
//
//		if (isHarvesting) {
//			if (creep.pos.inRangeTo(target,1)  ) {
//				creep.memory.behaviour.targetPos = null
//			}
//		} else {
//			if (!creep.pos.inRangeTo(target,3) ) {
//				Move.towardsTarget(creep)
//			}
//		}
////		if (creep.pos.inRangeTo(target,1) && creep.carry.energy ==0 ) {
////			creep.memory.behaviour.targetPos = null
////		} else if (creep.pos.inRangeTo(target,3)  && creep.carry.energy > 0) {
////
////		} else {
////			Move.towardsTarget(creep)
////		}
//
//		return MultiAI.ReturnType.CONTINUE
//	}
}
