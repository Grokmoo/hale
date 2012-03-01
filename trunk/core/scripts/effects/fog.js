function onTargetEnter(game, effect, target) {
	var parent = effect.getSlot().getParent();
	
	if (parent.getAbilities().has("DistractingFog")) {
		addDistractingFog(game, effect, target);
	}
	
	if (parent.getAbilities().has("PoisonFog")) {
		addPoisonFog(game, effect, target);
	}
}

function onRoundElapsed(game, effect) {
	var parent = effect.getSlot().getParent();

	if (parent.getAbilities().has("PoisonFog")) {
		checkPoisonFogRoundElapsed(game, effect);
	}
}

function checkPoisonFogRoundElapsed(game, effect) {
	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	var casterLevel = parent.getCasterLevel();
	
	var targets = effect.getTarget().getAffectedCreatures(effect);
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		// get the old effect and cause it to remove itself
		var targetEffect = getCurrentPoisonFogEffect(game, effect, target);
		targetEffect.setDuration(0);
		effect.removeChildEffect(targetEffect);
		
		// determine the old penalty
		var prevPenalty = targetEffect.getBonuses().getBonusOfType('Str').getValue();
		
		// create a new effect with the increased penalty
		var newEffect = effect.getSlot().createEffect();
		newEffect.setDuration(effect.getSlot().getActiveRoundsLeft());
		newEffect.setTitle("Poison Fog");
		effect.addChildEffect(newEffect);
	
		newEffect.getBonuses().addPenalty('Str', prevPenalty - 1);
		newEffect.getBonuses().addPenalty('Con', prevPenalty - 1);
	
		target.applyEffect(newEffect);
	}
}

function getCurrentPoisonFogEffect(game, effect, target) {
	var targetEffects = effect.getChildEffectsWithTarget(target);
	
	for (var i = 0; i < targetEffects.size(); i++) {
		if (targetEffects.get(i).getTitle() == "Poison Fog")
			return targetEffects.get(i);
	}
}

function addPoisonFog(game, effect, target) {
	var slot = effect.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();
	
	var targetEffect = slot.createEffect();
	targetEffect.setDuration(slot.getActiveRoundsLeft());
	targetEffect.setTitle("Poison Fog");
	effect.addChildEffect(targetEffect);
	
	targetEffect.getBonuses().addPenalty('Str', -2);
	targetEffect.getBonuses().addPenalty('Con', -2);
	
	target.applyEffect(targetEffect);
}

function addDistractingFog(game, effect, target) {
	var slot = effect.getSlot();
	var parent = slot.getParent();
	var casterLevel = parent.getCasterLevel();

	var targetEffect = slot.createEffect();
	targetEffect.setDuration(slot.getActiveRoundsLeft());
	targetEffect.setTitle("Distracting Fog");
	effect.addChildEffect(targetEffect);
	
	targetEffect.getBonuses().addPenalty('Attack', 'Morale', -10 - parseInt(casterLevel / 2));
	targetEffect.getBonuses().addPenalty('SpellFailure', 'Morale', -10 - parseInt(casterLevel / 2));
	targetEffect.getBonuses().addPenalty('MentalResistance', 'Morale', -10);
	target.applyEffect(targetEffect);
}

function onTargetExit(game, effect, target) {
	var targetEffects = effect.getChildEffectsWithTarget(target);
	
	for (var i = 0; i < targetEffects.size(); i++) {
		target.removeEffect(targetEffects.get(i));
		effect.removeChildEffect(targetEffects.get(i));
	}
}