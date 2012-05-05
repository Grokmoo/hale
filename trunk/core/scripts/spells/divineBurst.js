function onActivate(game, slot) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setRadius(4);
   targeter.setRelationshipCriterion("Hostile");
   targeter.addAllowedPoint(slot.getParent().getPosition());
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();
	
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var g1 = game.getBaseParticleGenerator("ring");
	g1.setStopParticlesAtOpaque(false);
	g1.setPosition(parent.getPosition());
	g1.setVelocityDistribution( game.getEquallySpacedAngleDistribution(288.0, 288.0,
		0.0, g1.getNumParticles(), 0.0) );
	
	var creatures = targeter.getAffectedCreatures();
	for (var i = 0; i < creatures.size(); i++) {
		var target = creatures.get(i);
	
		var delay = target.getPosition().screenDistance(parent.getPosition()) / 288.0;
	
		var callback = spell.createDelayedCallback("applyEffect");
		callback.setDelay(delay);
		callback.addArguments([parent, target, targeter.getSlot()]);
		
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyEffect(game, parent, target, slot) {
	var spell = slot.getAbility();

	if (target.getRace().hasRacialType("Undead")) {
		var damage = game.dice().randInt(1, 10) + parent.getCasterLevel();
		spell.applyDamage(parent, target, damage, "Divine");
	}
	
	if ( target.mentalResistanceCheck(spell.getCheckDifficulty(parent)) )
		return;

	if ( target.stats().has("ImmobilizationImmunity")) {
		game.addMessage("blue", target.getName() + " is immune.");
		return;
	}
		
	var effect = slot.createEffect();
	effect.setDuration(1);
	effect.setTitle(spell.getName());
	effect.getBonuses().add("Immobilized");
	effect.getBonuses().add("Helpless");
	
	var g1 = game.getBaseParticleGenerator("sparkle");
	g1.setDurationInfinite();
	g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
	g1.setPosition(target.getPosition());
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setGreenDistribution(game.getFixedDistribution(0.0));
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}