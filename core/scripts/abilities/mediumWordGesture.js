function onActivate(game, slot) {
   var abilityID = slot.getAbilityID();
   var parent = slot.getParent();
   var duration = 2;
   
   slot.setActiveRoundsLeft(duration);
   slot.activate();
   parent.timer.performAction(5000);
	   
   var effect = slot.createEffect("abilities/mediumWordGesture");
   effect.setDuration(duration);
   
   if (abilityID.startsWith("Word")) {
	  var wordString = abilityID.substring(4);
	  effect.setTitle("Medium Word: " + wordString);
      effect.put("parentMediumValue", "roleMediumWord" + wordString);
      parent.put("roleMediumWord" + wordString, true);
	  
   } else if (abilityID.startsWith("Gesture")) {
	  var gestureString = abilityID.substring(7);
	  effect.setTitle("Medium Gesture: " + gestureString);
      effect.put("parentMediumValue", "roleMediumGesture" + gestureString);
      parent.put("roleMediumGesture" + gestureString, true);
   }
   
   
   parent.applyEffect(effect);
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
	} else if (abilityID.startsWith("Gesture")) {
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
	if (parent.get("roleMediumWordIce") == true) count++;
	if (parent.get("roleMediumWordAcid") == true) count++;
	if (parent.get("roleMediumWordLightning") == true) count++;
	
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
