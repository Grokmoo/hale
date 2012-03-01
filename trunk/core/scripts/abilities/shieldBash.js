function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	return parent.getInventory().hasEquippedShield();
}

function onActivate(game, slot) {
   var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Hostile");

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
	targeter.getSlot().activate();

	var ability = targeter.getSlot().getAbility();

	// perform the attack in a new thread as the melee touch attack will block
	var cb = ability.createDelayedCallback("performAttack");
	cb.addArgument(targeter);
	cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();

	var improved = parent.getAbilities().has("ImprovedShieldBash");
	
	parent.getTimer().performAttack();
	
	// apply a temporary effect with the bonuses
	var effect = parent.createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	
	if (improved) {
		effect.getBonuses().addBonus('Attack', 'Stackable', 10);
	}
	
	parent.applyEffect(effect);
	
	if (game.meleeTouchAttack(parent, target)) {
		// touch attack succeeded
		
		var checkDC = 50 + 2 * (parent.stats().getStr() - 10);
		
		if (improved) {
			checkDC += parent.stats().getLevelAttackBonus();
		} else {
			checkDC += parent.stats().getLevelAttackBonus() / 2;
		}
		
		if (!target.physicalResistanceCheck(checkDC)) {
			// target failed check
			
			var effect = targeter.getSlot().createEffect();
			
			if (improved) {
				effect.setDuration(3);
			} else {
				effect.setDuration(2);
			}
			
			effect.setTitle(targeter.getSlot().getAbility().getName());
			effect.getBonuses().add("Immobilized");
			effect.getBonuses().add("Helpless");
			
			var g1 = game.getBaseParticleGenerator("sparkle");
			g1.setDurationInfinite();
			g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
			g1.setPosition(target.getPosition());
			g1.setBlueDistribution(game.getFixedDistribution(0.0));
			g1.setGreenDistribution(game.getFixedDistribution(0.0));
			g1.setBlueSpeedDistribution(game.getUniformDistribution(0.5, 1.0));
			effect.addAnimation(g1);
			
			target.applyEffect(effect);
			
			game.addMessage("red", parent.getName() + " stuns " + target.getName() + ".");
		} else {
			game.addMessage("red", parent.getName() + " fails to stun " + target.getName() + ".");
		}
	} else {
		game.addMessage("red", parent.getName() + " misses shield bash attempt.");
	}
	
	parent.removeEffect(effect);
}