function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	var weapon = parent.getInventory().getMainWeapon();
	
	return !weapon.isMeleeWeapon();
}

function onActivate(game, slot) {
	var targeter = game.createLineTargeter(slot);
	
	targeter.setOrigin(slot.getParent().getPosition());
	targeter.setForceLineLength(10);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var parent = targeter.getParent();
	var ability = targeter.getSlot().getAbility();
	
	parent.getTimer().performAttack();
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		game.singleAttack(parent, target);
	}
}