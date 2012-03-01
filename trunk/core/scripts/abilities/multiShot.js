function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	var weapon = parent.getInventory().getMainWeapon();
	
	var baseWeaponName = weapon.getBaseWeapon().getName();
	return (baseWeaponName.equals("Longbow") || baseWeaponName.equals("Shortbow") ||
		baseWeaponName.equals("Crossbow"))
}

function onActivate(game, slot) {
	var creatures = game.ai.getAttackableCreatures(slot.getParent());
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();
	
	var ability = targeter.getSlot().getAbility();
	
	// perform the attack in a new thread as the standardAttack will
	// block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	var numAttacks = parseInt(parent.stats().getLevelAttackBonus() / 25);
	
	game.standardAttack(parent, target);
	numAttacks--;
	
	while (numAttacks > 0) {
		game.singleAttack(parent, target);
		
		if (target.isDead()) break;
		numAttacks--;
	}
}