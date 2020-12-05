package starter.multiAI

import screeps.api.Creep
import starter.multiAI.Actions.*
import starter.multiAI.Actions.ExtractorHauler

enum class Role {
	UNASSIGNED,
	HARVESTER,
	BUILDER,
	UPGRADER,
	EXTRACTOR,
	HAULER_EXTRACTOR,
	HAULER_BASE,
	RESCUE_BOT
}

var roleMap : Map<Role,MultiAI_Action> = mapOf(
//		Role.EXTRACTOR to MultiAI_Action(
//				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
//						Move::towardsTarget,
//						MoveSetTarget::freeMiningPoints,
//						MoveSetTarget::oldestExtractor
//						),
//				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
//						InRange::pickupEnergyNotOnMiningPos,
//						Misc::dropEnergyOnMiningPoint,
//						InRange::mine
//				)),
//
		Role.HAULER_EXTRACTOR to MultiAI_Action(
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
//						Move::towardsTarget,
////						MoveSetTarget::baseStoreIfCarrying,
////						MoveSetTarget::miningPointWithMostResouceToPickup
				ExtractorHauler::hauler
				),
		arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
				InRange::pickupEnergyOnMiningPos,
				InRange::pickupResourceFree_NotOnBaseStore,
				InRange::depositInBaseStorage, //also need to clear target pos when doing this
				InRange::build,
				InRange::repair
		))
)

