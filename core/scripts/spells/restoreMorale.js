function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(4);
	targeter.setRelationshipCriterion("Friendly");
	targeter.addAllowedPoint(slot.getParent().getPosition());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();
	
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var reductionAmount = parseInt(5 + casterLevel / 2);
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);

		target.stats().reducePenaltiesOfTypeByAmount("Attack", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("AttackCost", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ArmorClass", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ArmorPenalty", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("Skill", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("MentalResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("PhysicalResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ReflexResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("SpellFailure", reductionAmount);
		
		var anim = game.getBaseAnimation("crossFlash");
		anim.setRed(0.8);
		anim.setGreen(0.8);
		anim.setBlue(1.0);
	
		var position = target.getScreenPosition();
		anim.setPosition(position.x, position.y - 10);
	
		game.runAnimationNoWait(anim);
		game.lockInterface(anim.getSecondsRemaining());
	}
}
