function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(5);
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
	g1.setVelocityDistribution( game.getEquallySpacedAngleDistribution(360.0, 360.0, 0.0, g1.getNumParticles(), 0.0) );
	
	var creatures = targeter.getAffectedCreatures();
	for (var i = 0; i < creatures.size(); i++) {
		var target = creatures.get(i);
		
		if ( target.getID().equals(parent.getID()) ) continue;
		
		// apply the effect when the ring reaches this target
		var delay = target.getPosition().screenDistance(parent.getPosition()) / 360.0;
		
		var cb = spell.createDelayedCallback("applyEffect");
		cb.setDelay(delay);
		cb.addArguments([parent, target, targeter.getSlot()]);
		cb.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyEffect(game, parent, target, slot) {
	var casterLevel = parent.getCasterLevel();
	var spell = slot.getAbility();
	var duration = game.dice().d2(2);
	
	// apply the ap penalty effect
	var effect = slot.createEffect();
	effect.setDuration(duration);
	effect.getBonuses().addPenalty("ActionPoint", -15 - casterLevel);
	
	// apply the damage
	var damage = game.dice().d6(2) + casterLevel;
	spell.applyDamage(parent, target, damage, "Cold");
	
	// apply the paralysis if the target fails the check and is still alive
	if (!target.isDead() && !target.physicalResistanceCheck(spell.getCheckDifficulty(parent)) ) {
		effect.getBonuses().add("Immobilized");
		effect.getBonuses().add("Helpless");
		
		var position = target.getScreenPosition();
		
		var g1 = game.getBaseParticleGenerator("paralysis");
		var g2 = game.getBaseParticleGenerator("paralysis");
	
		g1.setPosition(position.x, position.y - 10.0);
		g2.setPosition(position.x, position.y + 10.0);
		
		g2.setLineStart(-18.0, 0.0);
		g2.setLineEnd(18.0, 0.0);
		g2.setRedDistribution(game.getFixedDistribution(0.6));

		effect.addAnimation(g1);
		effect.addAnimation(g2);
	}
	
	target.applyEffect(effect);
}