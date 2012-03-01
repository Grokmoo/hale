function isTargetValid(game, target) {
	var armor = target.getInventory().getEquippedArmor();
	
	if (armor == null || !armor.isArmor()) {
		return false;
	}
	
	return true;
}

function onActivate(game, slot) {
	if (slot.getParent().getAbilities().has("ResistWeapons")) {
		showResistWeaponsMenu(game, slot);
	} else {
		createTargeter(game, slot, null);
	}
}

function showResistWeaponsMenu(game, slot) {
	game.addMenuLevel(slot.getAbility().getName());

	var types = ["Slashing", "Piercing", "Blunt"];
	
	for (var index = 0; index < types.length; index++ ) {
		var type = types[index];
	
		var cb = game.createButtonCallback(slot, "createTargeter");
		cb.addArgument(type);
		
		game.addMenuButton(type, cb);
	}
	
	game.showMenu();
}

function createTargeter(game, slot, type) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	for (var i = 0; i < creatures.size(); i++) {
		if ( !isTargetValid(game, creatures.get(i)) ) {
			creatures.remove(i);
			i--;
		}
	}
	
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
	
	if (!isTargetValid(game, target)) return;
	
	var duration = parseInt(3 + casterLevel / 4);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	effect.getBonuses().addDamageReduction("Physical", 5 + parseInt(casterLevel / 6));
	effect.getBonuses().addBonus('ArmorPenalty', 10);
	
	if (parent.getAbilities().has("ResistWeapons")) {
		effect.getBonuses().addDamageImmunity(type, 25 + casterLevel);
	}
	
	// we already checked that the armor exists
	var armor = target.getInventory().getEquippedArmor();
	armor.applyEffect(effect);
		
	if (target.drawWithSubIcons() && !target.drawOnlyHandSubIcons()) {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getSubIcon("Torso"));
		anim.setColor(target.getSubIconColor("Torso"));
			
		var pos = target.getSubIconScreenPosition("Torso");
		anim.setPosition(pos.x, pos.y);
	} else {
		var anim = game.getBaseAnimation("iconFlash");
		anim.addFrame(target.getIcon());
		anim.setColor(target.getIconColor());
		var pos = target.getScreenPosition();
		anim.setPosition(pos.x, pos.y);
	}
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
