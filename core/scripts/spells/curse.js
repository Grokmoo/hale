function onActivate(game, slot) {
	if (slot.getParent().getAbilities().has("MassCurse")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Hostile");
		targeter.addAllowedPoint(slot.getParent().getPosition());
		targeter.activate();
	} else {
		var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Hostile");
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var duration = game.dice().randInt(4, 8);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (parent.getAbilities().has("MassCurse")) {
		if (!spell.checkSpellFailure(parent)) return;
	
		var targets = targeter.getAffectedCreatures();
		
		for (var i = 0; i < targets.size(); i++) {
			var target = targets.get(i);
			
			applyCurse(game, targeter, target, duration);
		}
		
		var targetsCursed = targets.size();
		
		if (parent.getAbilities().has("Drain")) {
			targeter.setRelationshipCriterion("Friendly");
			for (var i = 0; i < targets.size() && i < targetsCursed; i++) {
				var target = targets.get(i);
				
				bolsterAlly(game, targeter, target, duration);
			}
		}
		
	} else {
		var target = targeter.getSelectedCreature();
	
		if (!spell.checkSpellFailure(parent, target)) return;
	
		// perform the touch attack in a new thread as it will block
		var cb = spell.createDelayedCallback("performTouch");
		cb.addArgument(targeter);
		cb.addArgument(duration);
		cb.start();
	}
}

function performTouch(game, targeter, duration) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	if (!game.meleeTouchAttack(parent, target)) return;
	
	applyCurse(game, targeter, target, duration);
	
	if (parent.getAbilities().has("Drain"))
		bolsterAlly(game, targeter, parent, duration);
}

function bolsterAlly(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();
	
	if (parent.getAbilities().has("Enfeeble")) {
		var attrPenalty = 2 + parseInt(casterLevel / 6);
	
		var effect = targeter.getSlot().createEffect();
		effect.setDuration(duration);
		effect.setTitle(spell.getName() + " Drain");
		effect.getBonuses().addBonus('Con', attrPenalty);
		effect.getBonuses().addBonus('Dex', attrPenalty);
		
		var g1 = game.getBaseParticleGenerator("sparkle");
		g1.setDuration(1.0);
		g1.setRotationSpeedDistribution(game.getUniformDistribution(100.0, 200.0));
		g1.setPosition(target.getPosition());
		g1.setRedDistribution(game.getFixedDistribution(0.0));
		g1.setBlueDistribution(game.getFixedDistribution(0.0));
		g1.setGreenDistribution(game.getFixedDistribution(1.0));
		effect.addAnimation(g1);
		
		target.applyEffect(effect);
	}
}

function applyCurse(game, targeter, target, duration) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	var acPenalty = -10 - casterLevel;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addPenalty('ArmorClass', 'Stackable', acPenalty);
	
	if (parent.getAbilities().has("Enfeeble")) {
		var attrPenalty = -3 - parseInt(casterLevel / 6);
	
		effect.getBonuses().addPenalty('Con', attrPenalty);
		effect.getBonuses().addPenalty('Dex', attrPenalty);
	}
	
	var anim = game.getBaseAnimation("rune");
	anim.addFrames("animations/rune2-", 1, 4);
	anim.setDurationInfinite();
	var position = target.getScreenPosition();
	anim.setPosition(position.x, position.y + 15.0);
	effect.addAnimation(anim);
	   
	target.applyEffect(effect);
}