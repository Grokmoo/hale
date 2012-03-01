function onActivate(game, slot) {
	if (slot.getParent().getAbilities().has("MassCrush")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Hostile");
		targeter.addAllowedPoint(slot.getParent().getPosition());
		targeter.activate();
	} else {
		var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 15);

		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	// cast the spell
	targeter.getSlot().activate();

	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	if (parent.getAbilities().has("MassCrush")) {
		// check for spell failure
		if (!spell.checkSpellFailure(parent)) return;
	
		var targets = targeter.getAffectedCreatures();
		for (var i = 0; i < targets.size(); i++) {
			var target = targets.get(i);
			
			performCrush(game, parent, target, spell);
		}
	} else {
		var target = targeter.getSelectedCreature();
		
		// check for spell failure
		if (!spell.checkSpellFailure(parent, target)) return;
		
		performCrush(game, parent, target, spell);
	}
}

function performCrush(game, parent, target, spell) {
	var casterLevel = parent.getCasterLevel();
	// compute the amount of damage to apply
	var damage = game.dice().rand(7, 14) + casterLevel;
   
	var g1 = game.getBaseParticleGenerator("inwardBurst");
	g1.setPosition(target.getPosition());
	g1.setRedDistribution(game.getUniformDistribution(0.438 - 0.05, 0.438 + 0.05));
	g1.setGreenDistribution(game.getUniformDistribution(0.379 - 0.05, 0.379 + 0.05));
	g1.setBlueDistribution(game.getFixedDistribution(0.0));
	g1.setVelocityDistribution(game.getVelocityTowardsPointDistribution(target.getScreenPosition(), g1.getTimeLeft()));
   
	// create the callback that will apply damage at the appropriate time
	var callback = spell.createDelayedCallback("applyDamage");
	callback.setDelay(g1.getTimeLeft());
	callback.addArguments([parent, target, damage, spell]);
   
	// run the particle effect and start the callback timer
	game.runParticleGeneratorNoWait(g1);
	callback.start();
	game.lockInterface(g1.getTimeLeft());
}

function applyDamage(game, parent, target, damage, spell) {
	spell.applyDamage(parent, target, damage, "Blunt");
}
