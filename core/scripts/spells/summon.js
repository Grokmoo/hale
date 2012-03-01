function onActivate(game, slot) {
	game.addMenuLevel(slot.getAbility().getName());
	
	var creatureIDs = [ "rat", "wolfsmall", "tiger", "wolfmedium", "bear", "sabretooth", "wolflarge",
		"spidergiant", "yeti", "wolfgiant" ];
	
	var casterLevel = slot.getParent().getCasterLevel();
	
	var max = casterLevel - 3;
	if (max > creatureIDs.length) max = creatureIDs.length;
	
	for (var index = 0; index < max; index++) {
		addButton(game, creatureIDs[index], slot);
	}
	
	if (slot.getParent().getAbilities().has("SummonElemental")) {
		addButton(game, "elementalAir", slot);
		addButton(game, "elementalEarth", slot);
		addButton(game, "elementalFire", slot);
		addButton(game, "elementalWater", slot);
	}
	
	game.showMenu();
}

function addButton(game, creatureID, slot) {
	var cb = game.createButtonCallback(slot, "castSpell");
	cb.addArgument(creatureID);
		
	var name = game.entities().getCreature(creatureID).getName();
		
	game.addMenuButton(name, cb);
}

function castSpell(game, slot, id) {
   var targeter = game.createCircleTargeter(slot);
   targeter.setAllowOccupiedTileSelection(false);
   targeter.setRadius(0);
   targeter.setMaxRange(4);
   
   targeter.addCallbackArgument(id);
   targeter.activate();
}

function onTargetSelect(game, targeter, id) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var casterLevel = parent.getCasterLevel();
	
	var position = targeter.getAffectedPoints().get(0);
	
	var duration = parseInt(3 + casterLevel / 2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var creature = game.summonCreature(id, position, parent, duration);
	
	if (parent.getAbilities().has("ImprovedSummon")) {
		var bonusLevels = parseInt( 2 * (casterLevel - 10) / 3 );
		
		if (bonusLevels > 0) {
			var roleSet = creature.getRoles();
			roleSet.addLevels(roleSet.getBaseRole(), bonusLevels);
		}
	}
	
	// set this if we want direct player control of the creature, otherwise the
	// creature will follow in party follow mode and use its AI in combat mode
	// creature.setPlayerSelectable(true);
	
	creature.resetAll();
}
