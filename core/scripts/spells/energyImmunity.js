function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var lvls = parent.getRoles().getLevel("War Wizard");
	
	var duration = parseInt(2 + lvls / 3);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageImmunity("Fire", 100);
	effect.getBonuses().addDamageImmunity("Cold", 100);
	effect.getBonuses().addDamageImmunity("Acid", 100);
	effect.getBonuses().addDamageImmunity("Electrical", 100);
	effect.getBonuses().addBonus("SpellResistance", 100);
	
	effect.getBonuses().addPenalty("SpellFailure", -100);
	
	var g1 = game.getBaseParticleGenerator("continuousRing");
	g1.setPosition(target.getPosition());
	
	g1.setRedDistribution(game.getUniformDistribution(0.5, 1.0));
	g1.setGreenDistribution(game.getUniformDistribution(0.5, 1.0));
	g1.setBlueDistribution(game.getUniformDistribution(0.5, 1.0));
	g1.setRedSpeedDistribution(game.getUniformDistribution(0.0, -1.0));
	g1.setGreenSpeedDistribution(game.getUniformDistribution(0.0, -1.0));
	g1.setBlueSpeedDistribution(game.getUniformDistribution(0.0, -1.0));
	
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}
