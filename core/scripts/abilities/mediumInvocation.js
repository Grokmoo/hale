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
	if (parent.get("roleMediumWordIce") == true) words.push("Ice");
	if (parent.get("roleMediumWordAcid") == true) words.push("Acid");
	if (parent.get("roleMediumWordLightning") == true) words.push("Lightning");
	
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
	
	// create the callback that will apply the effect
	var callback = ability.createDelayedCallback("applyWords");
	callback.setDelay(0.2);
	callback.addArguments([parent, target, targeter]);
	callback.start();
}

function applyWords(game, parent, target, targeter) {
	var words = getActiveWords(parent);
	var spell = targeter.getSlot().getAbility();
	var casterLevel = parent.stats.getCasterLevel();
	
	// create the animation
	var anim = game.getBaseAnimation("blast");
	var position = target.getLocation().getCenteredScreenPoint();
	anim.setPosition(position.x, position.y);
	
	for (var i = 0; i < words.length; i++) {
		var word = words[i];
		
		switch (word) {
		case "Fire" :
			var damage = game.dice().rand(4, 8) + casterLevel;
			spell.applyDamage(parent, target, damage, "Fire");
			anim.setSecondaryRed(1.0);
			break;
		case "Ice" : 
			var damage = game.dice().rand(3, 6) + casterLevel;
			spell.applyDamage(parent, target, damage, "Cold");
			anim.setSecondaryBlue(1.0);
			break;
		case "Acid" : 
			var damage = game.dice().rand(2, 6) + casterLevel;
			spell.applyDamage(parent, target, damage, "Acid");
			anim.setSecondaryGreen(1.0);
			break;
		case "Lightning" : 
			var damage = game.dice().rand(4, 8) + casterLevel;
			spell.applyDamage(parent, target, damage, "Electrical");
			break;
		}
	}
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
