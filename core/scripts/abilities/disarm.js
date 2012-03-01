function isTargetValid(game, target) {
	var weapon = target.getInventory().getEquippedMainHand();
	
	if (weapon == null || weapon.getBaseWeapon().getName().equals("Unarmed"))
		return false;
	
	return true;
}

function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	var weapon = parent.getInventory().getMainWeapon();
	
	return weapon.isMeleeWeapon();
}

function onActivate(game, slot) {
	var creatures = game.ai.getAttackableCreatures(slot.getParent());

	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i)) ) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);

	targeter.setMenuTitle(slot.getAbility().getName());

	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var ability = targeter.getSlot().getAbility();

	// perform the attack in a new thread as the melee touch attack will block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = game.currentArea().getCreatureAtGridPoint(targeter.getSelected());

	if (!isTargetValid(game, target))
		return;
	
	var parentWeapon = parent.getInventory().getMainWeapon();
	var targetWeapon = target.getInventory().getEquippedMainHand();
	
	var bonus = 0;
	if (!targetWeapon.isMeleeWeapon()) {
		bonus = 30;
	} else {
		bonus = 10 * parentWeapon.getWeaponSizeDifference(targetWeapon);
	}
	
	var effect = parent.createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.getBonuses().addBonus('Attack', 'Stackable', bonus);
	parent.applyEffect(effect);
	
	parent.getTimer().performAttack();
	
	if (game.meleeTouchAttack(parent, target)) {
		// touch attack succeeded
		
		var checkDC = 50 + 2 * (parent.stats().getStr() - 10) +
			parent.stats().getLevelAttackBonus() / 2;
		
		if (!target.reflexCheck(checkDC)) {
			// target failed reflex check
			
			target.getInventory().removeEquippedItem(targetWeapon);
			game.addItemToArea(targetWeapon, target.getPosition());
			
			game.addMessage("red", parent.getName() + " disarms " + target.getName() + ".");
		} else {
			game.addMessage("red", parent.getName() + " fails to disarm " + target.getName() + ".");
		}
	} else {
		game.addMessage("red", parent.getName() + " misses disarm attempt.");
	}
	
	parent.removeEffect(effect);
}