function onTargetEnter(game, target, aura) {
	createEffect(aura, target);
}

function createEffect(aura, target) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	
	var chaBonus = (parent.stats().getCha() - 10) * 2;
	var lvlBonus = parent.getRoles().getLevel("Paladin");
	
	var amount = 10 + chaBonus + lvlBonus;

	var targetEffect = slot.createEffect();
	targetEffect.setTitle(slot.getAbility().getName());
	aura.addChildEffect(targetEffect);
	
	targetEffect.getBonuses().addBonus('MentalResistance', 'Morale', amount);
	targetEffect.getBonuses().addBonus('PhysicalResistance', 'Morale', amount);
	targetEffect.getBonuses().addBonus('ReflexResistance', 'Morale', amount);
	
	targetEffect.setDuration(1);
	
	target.applyEffect(targetEffect);
}

function onTargetExit(game, target, aura) {
    var targetEffect = aura.getChildEffectWithTarget(target);
   
	target.removeEffect(targetEffect);
	aura.removeChildEffect(targetEffect);
}

function onRoundElapsed(game, aura) {
    var targets = game.currentArea().getAffectedCreatures(aura);
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var targetEffect = aura.getChildEffectWithTarget(target);
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
		
		createEffect(aura, target);
	}
}
