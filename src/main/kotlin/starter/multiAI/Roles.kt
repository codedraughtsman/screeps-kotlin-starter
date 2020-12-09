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
	HAULER_CREEP,
	RESCUE_BOT
}

var roleMap : Map<Role,MultiAI_Action> = mapOf(
		Role.HAULER_CREEP to MultiAI_Action(
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						HaulerCreep::haulerCreep
				),
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						InRange::pickupResourceFree,
						InRange::pickupStoredEnergy,
						InRange::depositInExtension,
						InRange::fullUpTargetCreep
				)),

		Role.EXTRACTOR to MultiAI_Action(
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
					Extractor::extractor
						),
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						InRange::pickupEnergyNotOnMiningPos,
						Misc::dropResouceOnMiningPoint,
						InRange::mine
				)),

		Role.BUILDER to MultiAI_Action(
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						Builder::builder
				),
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						InRange::build,
//						InRange::repair,
						InRange::upgrade,
						InRange::pickupResourceFree,
						InRange::pickupStoredEnergy

				)),

		Role.UPGRADER to MultiAI_Action(
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						Upgrader::Upgrader
				),
				arrayOf<(creep: Creep) -> MultiAI.ReturnType>(
						InRange::build,
//						InRange::repair,
						InRange::upgrade,
						InRange::pickupResourceFree,
						InRange::pickupStoredEnergy

				)),

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
				InRange::depositInExtension,
				InRange::build,
				InRange::repair
		))
)

