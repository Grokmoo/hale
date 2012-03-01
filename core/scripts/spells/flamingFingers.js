function onActivate(game, slot) {
	var targeter = game.createConeTargeter(slot);
	
	targeter.setOrigin(slot.getParent().getPosition());
	targeter.setConeAngle(90);
	targeter.setConeRadius(4);
	
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent)) return;
	
	var g1 = game.getBaseParticleGenerator("spray");
	g1.setPosition(parent.getPosition());
	g1.setGreenSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.005, 0.0, 0.05));
	g1.setBlueSpeedDistribution(game.getUniformDistributionWithBase(game.getSpeedDistributionBase(), -0.02, 0.0, 0.05));
			
	//var angle = parent.getPosition().angleTo(targeter.getEndPoint());
	var angle = targeter.getCenterAngle();
	
	g1.setVelocityDistribution(game.getUniformArcDistribution(400.0, 450.0, angle - 0.85, angle + 0.85));
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		var damage = parseInt(game.dice().d4() + casterLevel / 3);
		
		var delay = targets.get(i).getPosition().screenDistance(parent.getPosition()) / 400.0;
		
		var callback = spell.createDelayedCallback("applyDamage");
		callback.setDelay(delay);
		callback.addArguments([parent, targets.get(i), damage, targeter]);
		
		callback.start();
	}
	
	game.runParticleGeneratorNoWait(g1);
	game.lockInterface(g1.getTimeLeft());
}

function applyDamage(game, parent, target, damage, targeter) {
	var spell = targeter.getSlot().getAbility();

	spell.applyDamage(parent, target, damage, "Fire");
   
	var damageOverTime = parseInt(parent.getCasterLevel() / 3);
   
	var effect = targeter.getSlot().createEffect("effects/damageOverTime");
	effect.setTitle("Flaming Fingers");
	effect.put("damagePerRound", damageOverTime);
	effect.put("type", "Fire");
	
	effect.setDuration(3);
	
	var pos = target.getScreenPosition();
	
	var g1 = game.getBaseParticleGenerator("flame");
	g1.setPosition(pos.x - game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	effect.addAnimation(g1);
	
	var g2 = game.getBaseParticleGenerator("flame");
	g2.setPosition(pos.x + game.dice().rand(2, 8), pos.y + game.dice().rand(0, 10));
	effect.addAnimation(g2);
	
	var g3 = game.getBaseParticleGenerator("flame");
	g3.setPosition(pos.x + game.dice().rand(-2, 2), pos.y + game.dice().rand(10, 23));
	effect.addAnimation(g3);
	
	target.applyEffect(effect);
}