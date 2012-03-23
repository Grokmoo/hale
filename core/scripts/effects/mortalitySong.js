function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	if (parent.getFaction().isHostile(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Mortality Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		var lvls = parent.getRoles().getLevel("Bard");
	
		var conPenalty = parseInt((lvls + chaBonus) / 2);
		var acPenalty = 10 + 2 * lvls + 2 * chaBonus;
	
		targetEffect.getBonuses().addPenalty('Con', 'Luck', -conPenalty);
		targetEffect.getBonuses().addPenalty('ArmorClass', 'Stackable', -acPenalty);
		
		if (parent.getAbilities().has("SongOfEnemies"))
			targetEffect.getBonuses().addPenalty('Attack', 'Luck', -10 - chaBonus);
		
		target.applyEffect(targetEffect);
	} else if (parent.getAbilities().has("SongOfAllies") && parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Mortality Song");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		
		targetEffect.getBonuses().addBonus('Con', 'Luck', parseInt(chaBonus / 2) );
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isHostile(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	} else if (parent.getAbilities().has("SongOfAllies") && parent.getFaction().isFriendly(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}
