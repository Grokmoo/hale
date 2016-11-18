function onActivate(game, slot) {
	var parent = slot.getParent();
	
	var gestures = getActiveGestures(parent);
	
	var creatures = game.ai.getTouchableCreatures(parent, "Hostile");
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function canActivate(game, parent, slot) {
	var parent = slot.getParent();
	
	if (!parent.timer.canActivateAbility(slot.getAbilityID())) return false;
	
	var words = getActiveWords(parent);
	var gestures = getActiveGestures(parent);
	
	if (words.length == 0) return false;
	
	return true;
}

function getActiveWords(parent) {
	var words = [];
	
	if (parent.get("roleMediumWordFire") == true) words.push("Fire");
	
	return words;
}

function getActiveGestures(parent) {
	var gestures = [];
	
	return gestures;
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
    // cast the spell
    targeter.getSlot().activate();
	
	var target = targeter.getSelectedCreature();

	// check for spell failure
	if (!ability.checkSpellFailure(parent, target)) return;
	
	// perform the touch attack in a new thread as it will block
	var cb = ability.createDelayedCallback("performTouch");
	cb.addArgument(targeter);
	cb.start();
}

function performTouch(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.stats.getCasterLevel();
	
	if (game.meleeTouchAttack(parent, target)) {
		// create the callback that will apply damage partway through the animation
		var callback = spell.createDelayedCallback("applyWords");
		callback.setDelay(0.2);
		callback.addArguments([parent, target, targeter]);
		
		// create the animation
		var anim = game.getBaseAnimation("blast");
		var position = target.getLocation().getCenteredScreenPoint();
		anim.setPosition(position.x, position.y);
		
		game.runAnimationNoWait(anim);
		game.lockInterface(anim.getSecondsRemaining());
		callback.start();
	}
}

function applyWords(game, parent, target, targeter) {
	var words = getActiveWords(parent);
	var spell = targeter.getSlot().getAbility();
	var casterLevel = parent.stats.getCasterLevel();
	
	for (var i = 0; i < words.length; i++) {
		var word = words[i];
		
		if (word == "Fire") {
			var damage = game.dice().rand(4, 8) + casterLevel;
			spell.applyDamage(parent, target, damage, "Fire");
		}
	}
}