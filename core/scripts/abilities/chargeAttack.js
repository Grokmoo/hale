function canActivate(game, parent) {
	var weapon = parent.getInventory().getMainWeapon();
	if (!weapon.isMeleeWeapon()) return false;
	
	var attackCost = parent.stats().getAttackCost();
	var moveCost = parent.stats().getMovementCost();
	
	return parent.getTimer().canPerformAction(attackCost + 2 * moveCost);
}

function onActivate(game, slot) {
	var parent = slot.getParent();

	// figure out the shortest and longest distances that can be charged based on available
	// AP and weapon range
	var weapon = parent.getInventory().getMainWeapon();
	var maxDistAway = 1;
	if (weapon.threatens()) maxDistAway = weapon.getThreatenMax();
	
	var minDistAway = 1;
	if (weapon.threatens()) minDistAway = weapon.getThreatenMin();
	
	var moveAPLeft = (parent.getTimer().getAP() - parent.stats().getAttackCost());
	var maxDist = maxDistAway + parseInt(moveAPLeft / parent.stats().getMovementCost());
	
	var targeter = game.createLineTargeter(slot);
	
	targeter.setOrigin(parent.getPosition());
	targeter.setMaxRange(maxDist);
	targeter.setMinRange(2 + minDistAway);
	targeter.setRelationshipCriterion("Hostile");
	targeter.setAllowAffectedCreaturesEmpty(false);
	targeter.setStopLineAtCreature(true);
	targeter.setStopLineAtImpassable(true);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var parent = targeter.getParent();
	var target = targeter.getAffectedCreatures().get(0);
	var ability = targeter.getSlot().getAbility();
	
	var weapon = parent.getInventory().getMainWeapon();
	var distance = game.distance(parent, target) / 5;
	
	// normally, charge to the weapon's maximum range and attack
	var distAway = 1;
	if (weapon.threatens()) distAway = weapon.getThreatenMax();
	
	// if the maximum range doesn't give at least 2 movement tiles,
	// then charge to a shorter distance instead
	if (distance - distAway < 2) {
		distAway = distance - 2;
	}
	
	var cb = ability.createDelayedCallback("performCharge");
	cb.addArgument(parent);
	cb.addArgument(target);
	cb.addArgument(distAway);
	cb.start();
}

function performCharge(game, parent, target, distanceAway) {
	if (!game.ai.moveTowards(parent, target.getPosition(), distanceAway)) {
		game.addMessage("red", "Charge attack by " + parent + " was interrupted.");
		return;
	}
	
	// apply a temporary effect with the bonuses
	var effect = parent.createEffect();
	effect.getBonuses().addBonus('Attack', 'Stackable', 40);
	effect.getBonuses().addBonus('Damage', 'Stackable', 100);
	parent.applyEffect(effect);
	
	game.standardAttack(parent, target);
	
	parent.removeEffect(effect);
}