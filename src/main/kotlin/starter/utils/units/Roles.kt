package starter.utils.units

import screeps.api.Creep
import starter.multiAI.Role
import starter.role

fun getRole(creep: Creep): Enum<Role> {
	return creep.memory.role
}

fun isUpgrader(creep: Creep): Boolean {
	return getRole(creep) == Role.UPGRADER
}
