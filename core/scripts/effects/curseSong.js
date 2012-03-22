function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	if (parent.getFaction().isHostile(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Curse Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		var lvls = parent.getRoles().getLevel("Bard");
	
		var penalty = parseInt((lvls + chaBonus) / 2);
	
		targetEffect.getBonuses().addPenalty('Dex', 'Luck', -penalty);
		targetEffect.getBonuses().addPenalty('Str', 'Luck', -penalty);
		
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isHostile(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}