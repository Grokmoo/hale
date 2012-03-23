function onRoundElapsed(game, aura) {
	var parent = aura.getSlot().getParent();
	
	var chaBonus = (parent.stats().getCha() - 10);
	var lvls = parent.getRoles().getLevel("Bard");
	
	var bonus = parseInt( (chaBonus + lvls) / 2 );

    var targets = game.currentArea().getAffectedCreatures(aura);
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		target.healDamage(bonus);
	}
}

function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Healing Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		
		if (parent.getAbilities().has("SongOfAllies"))
			targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		
		target.applyEffect(targetEffect);
	} else if (parent.getAbilities().has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
	
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Healing Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		
		targetEffect.getBonuses().addPenalty('Attack', 'Luck', -10 - chaBonus);
		
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	} else if (parent.getAbilities().has("SongOfEnemies") && parent.getFaction().isHostile(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}
