function onTargetEnter(game, target, aura) {
	var slot = aura.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = slot.createEffect();
		targetEffect.setTitle("Song of Luck");
		targetEffect.setRemoveOnDeactivate(true);
		aura.addChildEffect(targetEffect);
	
		var chaBonus = (parent.stats().getCha() - 10);
		var lvls = parent.getRoles().getLevel("Bard");
	
		var bonus = 10 + 2 * lvls + 2 * chaBonus;
	
		targetEffect.getBonuses().addBonus('Attack', 'Luck', bonus);
		targetEffect.getBonuses().addBonus('SpellFailure', 'Luck', bonus);
		targetEffect.getBonuses().addSkillBonus('Locks', 'Luck', bonus);
		targetEffect.getBonuses().addSkillBonus('Traps', 'Luck', bonus);
		
		target.applyEffect(targetEffect);
	}
}

function onTargetExit(game, target, aura) {
	var parent = aura.getSlot().getParent();

	if (parent.getFaction().isFriendly(target)) {
		var targetEffect = aura.getChildEffectWithTarget(target);
   
		target.removeEffect(targetEffect);
		aura.removeChildEffect(targetEffect);
	}
}