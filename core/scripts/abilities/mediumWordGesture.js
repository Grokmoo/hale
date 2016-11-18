function onActivate(game, slot) {
   var abilityID = slot.getAbilityID();
   var parent = slot.getParent();
   var duration = 2;
   
   if (abilityID == "WordFire") {
	   slot.setActiveRoundsLeft(duration);
	   slot.activate();
	   parent.timer.performAction(5000);
	   
	   var effect = slot.createEffect("abilities/mediumWordGesture");
	   effect.setDuration(duration);
	   effect.setTitle("Medium Word: Fire");
	   effect.put("parentMediumValue", "roleMediumWordFire");
	   parent.put("roleMediumWordFire", true);
	
	   parent.applyEffect(effect);
   }
}

function canActivate(game, parent, slot) {
	var abilityID = slot.getAbilityID();
    var parent = slot.getParent();
	
	var numWords = getNumActiveWords(parent);
	var numGestures = getNumActiveGestures(parent);
	
	if (abilityID.startsWith("Word")) {
		// a word
		if (numWords + 1 > numGestures) {
			return parent.timer.canPerformAction(5000);
		}
	} else {
		// a gesture
		if (numGestures + 1 > numWords) {
			return parent.timer.canPerformAction(5000);
		}
	}
	
	return true;
}

function getNumActiveWords(parent) {
	var count = 0;
	
	if (parent.get("roleMediumWordFire") == true) count++;
	
	return count;
}

function getNumActiveGestures(parent) {
	var count = 0;
	
	return count;
}

// called on the removal of the word / gesture effect
function onRemove(game, effect) {
	parent.put(effect.get("parentMediumValue"), false);
}
