function onActivate(game, slot) {
	var targeter = game.createCircleTargeter(slot);
	targeter.setRadius(4);
	targeter.setRelationshipCriterion("Friendly");
	targeter.addAllowedPoint(slot.getParent().getPosition());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();
	
	var duration = parseInt(game.dice().rand(5, 10));
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	if (parent.getAbilities().has("DivineAura")) {
		// do the divine luck aura
		
		var aura = targeter.getSlot().createAura("effects/divineAura");
		aura.setHasDescription(false);
		aura.setAuraMaxRadius(4);
		aura.setDuration(duration);
	
		for (var i = 0; i < targeter.getAffectedPoints().size(); i++) {
			var point = targeter.getAffectedPoints().get(i);
	   
			var g1 = game.getBaseParticleGenerator("fog");
			g1.setRedDistribution(game.getFixedDistribution(1.0));
			g1.setGreenDistribution(game.getFixedDistribution(0.8));
			g1.setBlueDistribution(game.getFixedDistribution(0.2));
			g1.setAlphaDistribution(game.getUniformDistribution(0.1, 0.2));
			g1.setDurationInfinite();
			g1.setPosition(point);
			aura.addAnimation(g1);
		}
	
		parent.applyEffect(aura);
		
	} else {
		// do the normal divine luck spell with optional "resistance"
	
		var attackBonus = 5 + casterLevel;
		var damageBonus = 10 + 2 * casterLevel;
		var resistanceBonus = 10 + casterLevel;
	
		var creatures = targeter.getAffectedCreatures();
		for (var i = 0; i < creatures.size(); i++) {
			var effect = targeter.getSlot().createEffect();
			effect.setDuration(duration);
			effect.setTitle(spell.getName());
			effect.getBonuses().addBonus('Attack', 'Morale', attackBonus);
			effect.getBonuses().addBonus('Damage', 'Morale', damageBonus);
		
			if (parent.getAbilities().has("Resistance")) {
				effect.getBonuses().addBonus('MentalResistance', 'Morale', resistanceBonus);
				effect.getBonuses().addBonus('PhysicalResistance', 'Morale', resistanceBonus);
				effect.getBonuses().addBonus('ReflexResistance', 'Morale', resistanceBonus);
			}
		
			var g1 = game.getBaseParticleGenerator("sparkle");
			g1.setDurationInfinite();
			g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
			g1.setPosition(creatures.get(i).getPosition());
			g1.setBlueDistribution(game.getFixedDistribution(0.0));
			g1.setBlueSpeedDistribution(game.getUniformDistribution(0.5, 1.0));
			effect.addAnimation(g1);
	   
			creatures.get(i).applyEffect(effect);
		}
	}
}
