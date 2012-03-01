function onActivate(game, slot) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
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
	
	points = target.stats().reducePenaltiesOfTypeByAmount("Str", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("Dex", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("Con", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("Int", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("Wis", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("Cha", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseStr", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseDex", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseCon", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseInt", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseWis", points);
	points = target.stats().reducePenaltiesOfTypeByAmount("BaseCha", points);
	
	if (targeter.getSlot().getParent().getAbilities().has("RestoreMorale")) {
		var reductionAmount = parseInt(5 + casterLevel / 2);
		
		target.stats().reducePenaltiesOfTypeByAmount("Attack", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("AttackCost", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ArmorClass", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ArmorPenalty", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("Skill", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("MentalResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("PhysicalResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("ReflexResistance", reductionAmount);
		target.stats().reducePenaltiesOfTypeByAmount("SpellFailure", reductionAmount);
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
