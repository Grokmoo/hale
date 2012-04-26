/*
 * A basic AI script that attempts to attack with the weapon currently in the creature's hands.
 * It attempts to close to an appropriate range and then attack as many times as possible
 */

function runTurn(game, parent) {
    var preferredDistance = 1;
    var weapon = parent.getInventory().getEquippedMainHand();
    
	// compute preferred distance for melee or ranged weapon
    if (weapon != null) {
        if (weapon.isMeleeWeapon()) {
            if (weapon.threatens()) preferredDistance = weapon.getThreatenMax();
            else preferredDistance = 1;
        } else {
            preferredDistance = weapon.getMaximumRange() / 10;
        }
    }

	// move towards opponents and attack as many times as possible
    for (;;) {
        var closestHostile = game.ai.findNearestCreatureToAttack(parent, "Hostile");
		
		// no targets, so nothing to do
        if (closestHostile == null) break;
        
        var curDistance = game.distance(parent, closestHostile) / 5;
        
        var distance = preferredDistance;
        
		// if we are already closer than preferred distance and cannot attack, attempt to
		// move in by 2 and attack again
        if (preferredDistance >= curDistance) distance = curDistance - 2;
        
        if (!parent.getTimer().canAttack()) {
			// if we don't have enough AP to attack, nothing else to do
            break;
        } else if (!game.creatureCanAttackTarget(parent, closestHostile)) {
			// if we cannot attack, then attempt to move towards the target
			var initialDistance = distance;
			
			// try to move next to the target.  if that fails, try to move one square further away
			// from the target, and so on
			while (!game.ai.moveTowards(parent, closestHostile.getPosition(), distance)) {
				distance++;
				
				if (distance >= curDistance)
					break;
			}
			
			// if we were unable to move the initial distance, then we cannot attack
			// and should end our turn
            if (distance > initialDistance) {
                break;
            }
        } else if (parent.getInventory().hasAmmoEquippedForWeapon()) {
			// attack the target
            game.standardAttack(parent, closestHostile);
        } else {
			// we have no ammo for our weapon and cannot attack
            break;
        }
    }
}

function takeAttackOfOpportunity(game, parent, target) {
    return true;
}
