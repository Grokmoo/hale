function onActivate(game, slot) {
	if (slot.getParent().getAbilities().has("Meteor")) {
		var targeter = game.createCircleTargeter(slot);
        targeter.setMaxRange(12);
        targeter.setRadius(5);
        targeter.activate();
	} else if (slot.getParent().getAbilities().has("Fireball")) {
	    var targeter = game.createCircleTargeter(slot);
        targeter.setMaxRange(12);
        targeter.setRadius(4);
        targeter.activate();
	} else {
	    var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 40);

	    var targeter = game.createListTargeter(slot);
	    targeter.addAllowedCreatures(creatures);
	    targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
    // cast the spell
    targeter.getSlot().activate();
	
	if (targeter.getSlot().getParent().getAbilities().has("Meteor")) {
		performMeteor(game, targeter);
    } else if (targeter.getSlot().getParent().getAbilities().has("Fireball")) {
	    performFireball(game, targeter);
    } else {
	    performFirebolt(game, targeter);
    }
}

function performMeteor(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	// create the central explosion which harms the friendlies
	var explosionCenter = targeter.getMouseGridPosition();
	
	var g1 = game.getBaseParticleGenerator("explosion");
	g1.setPosition(explosionCenter);
	g1.setVelocityDistribution(game.getEquallySpacedAngleDistribution(150.0, 200.0, 5.0, 2000.0, 10.0));
	g1.setGreenSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.005, 0.0, 0.05));
	g1.setBlueSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.01, 0.0, 0.05));
	g1.setAlphaSpeedDistribution(game.getFixedDistribution(-0.33));
	g1.setDurationDistribution(game.getFixedDistribution(3.0));
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
	
	var explosionTime = g1.getTimeLeft();
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		// only harm friendlies in this wave
		if (parent.getFaction().isHostile(target)) continue;
		
		var damage = parseInt( (game.dice().d10(2) + casterLevel) / 3 );
		
		var delay = target.getPosition().screenDistance(explosionCenter) / 200.0;
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(delay);
		callback.addArguments([parent, target, damage, spell]);
		callback.start();
	}
	
	// now create the meteors that harm the hostiles
	var meteorTime = 0.0;
	
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		// only harm hostiles in this wave
		if (!parent.getFaction().isHostile(target)) continue;
		
		var damage = game.dice().d10(2) + casterLevel;
		
		var callback = spell.createDelayedCallback("animateMeteor");
		callback.setDelay(meteorTime);
		callback.addArguments([parent, target, damage, spell]);
		callback.start();
		meteorTime += 0.5;
	}
	
	game.lockInterface(Math.max(explosionTime, meteorTime));
}

function animateMeteor(game, parent, target, damage, spell) {
	var g1 = game.getBaseAnimation("explosion");
	g1.setPosition(target.getScreenPosition());
	game.runAnimationNoWait(g1);
	
	applyDamage(game, parent, target, damage, spell);
}

function performFireball(game, targeter) {
    var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var explosionCenter = targeter.getMouseGridPosition();
	
	var g1 = game.getBaseParticleGenerator("bolt");
	g1.setVelocityDurationBasedOnSpeed(parent.getPosition(), explosionCenter, 400.0);
	g1.setGreenSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
	g1.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));
	
	var g2 = game.getBaseParticleGenerator("explosion");
	g2.setVelocityDistribution(game.getEquallySpacedAngleDistribution(150.0, 200.0, 5.0, 2000.0, 10.0));
	g2.setGreenSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.005, 0.0, 0.05));
	g2.setBlueSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.01, 0.0, 0.05));
	g1.addSubGeneratorAtEnd(g2);

	var g3 = game.getBaseAnimation("explosion");
	g1.addSubGeneratorAtEnd(g3);
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var target = targets.get(i);
		
		var delayG1 = target.getPosition().screenDistance(parent.getPosition()) / g1.getSpeed();
		var delayG2 = target.getPosition().screenDistance(explosionCenter) / 200.0;
		
		var damage = game.dice().d6(2) + casterLevel;
		
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(delayG1 + delayG2);
		callback.addArguments([parent, target, damage, spell]);
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g2.getTimeLeft());
}

function performFirebolt(game, targeter) {
    var spell = targeter.getSlot().getAbility();
    var parent = targeter.getParent();
    var target = targeter.getSelectedCreature();
    var casterLevel = parent.getCasterLevel();
	
	// check for spell failure
    if (!spell.checkSpellFailure(parent, target)) return;

    // compute the amount of damage to apply
    var damage = game.dice().d6(2) + casterLevel;
   
    // create the particle effect
    var generator = game.getBaseParticleGenerator("bolt");
    generator.setVelocityDurationBasedOnSpeed(parent.getPosition(), target.getPosition(), 400.0);
    generator.setGreenSpeedDistribution(game.getGaussianDistribution(-1.0, 0.05));
    generator.setBlueSpeedDistribution(game.getGaussianDistribution(-6.0, 0.05));
   
    var delay = target.getPosition().screenDistance(parent.getPosition()) / generator.getSpeed();
   
    // create the callback that will apply damage at the appropriate time
    var callback = spell.createDelayedCallback("applyDamage");
    callback.setDelay(delay);
    callback.addArguments([parent, target, damage, spell]);
   
    // run the particle effect and start the callback timer
    game.runParticleGeneratorNoWait(generator);
    callback.start();
    game.lockInterface(delay);
}

function applyDamage(game, parent, target, damage, spell) {
    spell.applyDamage(parent, target, damage, "Fire");
}
