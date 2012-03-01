function runTurn(game, parent) {
	var turnNum = parent.get("turnNum");
	if (turnNum == null) turnNum = 0;

	if (turnNum != 0) {
		tryRoar(game, parent);
	}
	
    var preferredDistance = 1;
    var weapon = parent.getInventory().getEquippedMainHand();
        
    if (weapon != null) {
        if (weapon.isMeleeWeapon()) {
            if (weapon.threatens()) preferredDistance = weapon.getThreatenMax();
            else preferredDistance = 1;
        } else {
            preferredDistance = weapon.getMaximumRange() / 10;
        }
    }

    for (;;) {
        var closestHostile = game.ai.findNearestCreatureToAttack(parent, "Hostile");
        if (closestHostile == null) break;
        
        var curDistance = game.distance(parent, closestHostile) / 5;
        
        var distance = preferredDistance;
        
        if (preferredDistance >= curDistance) distance = curDistance / 2;
        
        if (!parent.getTimer().canAttack()) {
            break;
        } else if (!game.creatureCanAttackTarget(parent, closestHostile)) {
            if (!game.ai.moveTowards(parent, closestHostile.getPosition(), distance)) {
                break;
            }
        } else if (parent.getInventory().hasAmmoEquippedForWeapon()) {
            game.standardAttack(parent, closestHostile);
        } else {
            break;
        }
    }
	
	parent.put("turnNum", (turnNum + 1));
}

function tryRoar(game, parent) {
	// attempt to use the Grimbok's "roar" ability

	var slots = parent.getAbilities().getSlotsWithReadiedAbility("GrimbokRoar");
	for (var i = 0; i < slots.size(); i++) {
		var slot = slots.get(i);
		
		if (slot.canActivate()) {
			// the ability can be used, so use it
			var allSlots = parent.getAbilities().createAISet();
			
			var targeter = allSlots.activateAndGetTargeter(slot);
			
			var selectable = targeter.getAllowedPoints();
			targeter.setMousePosition(selectable.get(0));
            targeter.performLeftClickAction();
			
			game.sleepStandardDelay(4);
			return;
		}
	}
}

function takeAttackOfOpportunity(game, parent, target) {
    return true;
}
