function onActivate(game, slot) {
	game.addMenuLevel("Absorb Elements");

	var types = ["Fire", "Cold", "Electrical", "Acid"];
	
	for (var index = 0; index < types.length; index++ ) {
		var type = types[index];
	
		var cb = game.createButtonCallback(slot, "castSpell");
		cb.addArgument(type);
		
		game.addMenuButton(type, cb);
	}
	
	game.showMenu();
}

function castSpell(game, slot, type) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.addCallbackArgument(type);
	targeter.activate();
}

function onTargetSelect(game, targeter, type) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var duration = game.dice().rand(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var dr = parseInt(5 + casterLevel / 2);
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageReduction(type, dr);
	
	if (parent.getAbilities().has("ResistSpells")) {
		effect.getBonuses().addBonus("SpellResistance", 15 + casterLevel);
	}
	
	var g1 = game.getBaseParticleGenerator("rotatingRing");
	g1.setDurationInfinite();
	g1.setPosition(target.getPosition());
	
	if (type == "Fire") {
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.6));
	} else if (type == "Cold") {
		// default white color is fine
	} else if (type == "Electrical") {
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(0.0));
	} else if (type == "Acid") {
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
	}

	effect.addAnimation(g1);	
	target.applyEffect(effect);
}
