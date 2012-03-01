function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	var weapon = parent.getInventory().getMainWeapon();
	
	return weapon.isMeleeWeapon();
}

function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(1);
	targeter.setRelationshipCriterion("Hostile");
	targeter.addAllowedPoint(slot.getParent().getPosition());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var parent = targeter.getParent();
	
	parent.getTimer().performAttack();
	
	// apply a temporary effect with the attack penalty
	var effect = parent.createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.getBonuses().addPenalty('Attack', 'Stackable', -10);
	parent.applyEffect(effect);
	
	var creatures = targeter.getAffectedCreatures();
	for (var i = 0; i < creatures.size(); i++) {
		var target = creatures.get(i);
		
		game.singleAttack(parent, target);
	}
	
	parent.removeEffect(effect);
}
