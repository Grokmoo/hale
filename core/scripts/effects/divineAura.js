function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	var targetEffect = slot.createEffect();
	targetEffect.setDuration(slot.getActiveRoundsLeft());
	targetEffect.setTitle(slot.getAbility().getName());
	aura.addChildEffect(targetEffect);
	
	var attackBonus = 5 + casterLevel;
	var damageBonus = 10 + 2 * casterLevel;
	var resistanceBonus = 10 + casterLevel;
	
	if (parent.getFaction().isFriendly(target)) {
		targetEffect.getBonuses().addBonus('Attack', 'Morale', attackBonus);
		targetEffect.getBonuses().addBonus('Damage', 'Morale', damageBonus);
		targetEffect.getBonuses().addBonus('MentalResistance', 'Morale', resistanceBonus);
		targetEffect.getBonuses().addBonus('PhysicalResistance', 'Morale', resistanceBonus);
		targetEffect.getBonuses().addBonus('ReflexResistance', 'Morale', resistanceBonus);
		
		var anim = game.getBaseAnimation("sparkleAnim");
		var position = target.getScreenPosition();
		anim.setPosition(position.x, position.y);
		game.runAnimationNoWait(anim);
		
	} else if (parent.getFaction().isHostile(target)) {
		targetEffect.getBonuses().addPenalty('Attack', 'Morale', -attackBonus);
		targetEffect.getBonuses().addPenalty('Damage', 'Morale', -damageBonus);
		targetEffect.getBonuses().addPenalty('MentalResistance', 'Morale', -resistanceBonus);
		targetEffect.getBonuses().addPenalty('PhysicalResistance', 'Morale', -resistanceBonus);
		targetEffect.getBonuses().addPenalty('ReflexResistance', 'Morale', -resistanceBonus);
		
		var anim = game.getBaseAnimation("sparkleAnim");
		var position = target.getScreenPosition();
		anim.setGreen(0.0);
		anim.setBlue(0.0);
		anim.setPosition(position.x, position.y);
		game.runAnimationNoWait(anim);
	}
	
	target.applyEffect(targetEffect);
}

function onTargetExit(game, target, aura) {
	var targetEffect = aura.getChildEffectWithTarget(target);
   
	target.removeEffect(targetEffect);
	aura.removeChildEffect(targetEffect);
}
