function onActivate(game, slot) {
	if (slot.getParent().getAbilities().has("MassCure")) {
		var targeter = game.createCircleTargeter(slot);
		targeter.setRadius(4);
		targeter.setRelationshipCriterion("Friendly");
		targeter.addAllowedPoint(slot.getParent().getPosition());
		targeter.activate();
	} else {
		var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter) {
	// cast the spell
	targeter.getSlot().activate();
	
	if (targeter.getSlot().getParent().getAbilities().has("MassCure")) {
	    massCure(game, targeter);
    } else {
	    cure(game, targeter);
    }
}

function cure(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();

	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
   
	// compute the amount of healing
	var healing = 5 + game.dice().rand(casterLevel, casterLevel * 2);
	spell.applyHealing(parent, target, healing);
   
	showHealAnimation(game, target);
}

function massCure(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();

	if (!spell.checkSpellFailure(parent)) return;
	
	var targets = targeter.getAffectedCreatures();
	for (var i = 0; i < targets.size(); i++) {
		showHealAnimation(game, targets.get(i));
   
		var healing = game.dice().rand(casterLevel, casterLevel * 3);
		spell.applyHealing(parent, targets.get(i), healing);
	}
}

function showHealAnimation(game, target) {
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.0);
	anim.setGreen(0.0);
	anim.setBlue(1.0);
	
	var position = target.getScreenPosition();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}