function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var duration = parseInt(3 + casterLevel / 3);
	
	// cast the spell
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageImmunity("Physical", 75);
	effect.getBonuses().addDamageVulnerability("Fire", -25);
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getPosition());
	g1.setRedDistribution(game.getUniformDistribution(0.4, 0.6));
	g1.setGreenDistribution(game.getUniformDistribution(0.2, 0.3));
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}