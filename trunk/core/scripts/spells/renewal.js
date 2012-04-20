function isTargetValid(game, target, slot) {
	var parent = slot.getParent();
	
	var types = ["Str", "Dex", "Con", "Int", "Wis", "Cha" ];
	
	if (parent.getAbilities().has("RestoreMorale")) {
		types = types.concat(["Attack", "AttackCost", "ArmorClass", "ArmorPenalty",
			"Skill", "MentalResistance", "PhysicalResistance",
			"ReflexResistance", "SpellFailure"]);
	}
	
	if (parent.getAbilities().has("RemoveParalysis")) {
		types = types.concat(["Immobilized", "Helpless"]);
	}

	return target.getEffects().hasPenaltiesOfTypes(types);
}

function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i), slot) ) {
			creatures.remove(i);
			i--;
		}
	}
	
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	// cast the spell
	targeter.getSlot().activate();
   
	// check for spell failure
	if (!spell.checkSpellFailure(parent, target)) return;
	
	var points = parseInt(2 + casterLevel / 2);
	
	var types = [ "Str", "Dex", "Con", "Int", "Wis", "Cha" ];
	
	for (var i = 0; i < types.length; i++) {
		points = target.stats().reducePenaltiesOfTypeByAmount(types[i], points);
	}
	
	if (targeter.getSlot().getParent().getAbilities().has("RestoreMorale")) {
		var reductionAmount = parseInt(5 + casterLevel / 2);
		
		var types = [ "Attack", "AttackCost", "ArmorClass", "ArmorPenalty", "Skill",
			"MentalResistance", "PhysicalResistance", "ReflexResistance", "SpellFailure" ];
		
		for (var i = 0; i < types.length; i++) {
			target.stats().reducePenaltiesOfTypeByAmount(types[i], reductionAmount);
		}
	}
	
	if (targeter.getSlot().getParent().getAbilities().has("RemoveParalysis")) {
		target.stats().removeEffectPenaltiesOfType("Immobilized");
		target.stats().removeEffectPenaltiesOfType("Helpless");
	}
	
	var anim = game.getBaseAnimation("crossFlash");
	anim.setRed(0.8);
	anim.setGreen(0.8);
	anim.setBlue(1.0);
	
	var position = target.getScreenPosition();
	anim.setPosition(position.x, position.y - 10);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
