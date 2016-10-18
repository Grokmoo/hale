function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();

	var duration = parseInt(2 + parent.roles.getLevel("Monk") / 4);
	
	// cast the ability
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
   
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.addPositiveIcon("items/enchant_physical_small");
	effect.setTitle(ability.getName());
	effect.getBonuses().addBonus('PhysicalResistance', 'Stackable', 25);
	effect.getBonuses().addBonus('MentalResistance', 'Stackable', 25);
	effect.getBonuses().addBonus('ReflexResistance', 'Stackable', 25);
	effect.getBonuses().add("ImmobilizationImmunity");
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getLocation());
	g1.setRedDistribution(game.getUniformDistribution(0.4, 0.6));
	g1.setGreenDistribution(game.getUniformDistribution(0.2, 0.3));
	g1.setBlueDistribution(game.getUniformDistribution(0.5, 0.8));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}