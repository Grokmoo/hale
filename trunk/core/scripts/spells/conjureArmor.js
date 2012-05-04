function onActivate(game, slot) {
	game.addMenuLevel("Conjure Armor");
	
	var types = [ "Leather", "Mail", "Plate", "Heavy Plate" ];
		
	var ids = [ [ "boots_leather_base", "gloves_leather_base", "helmet_leather_base", "armor_leatherhard_base" ],
				[ "boots_mail_base", "gloves_mail_base", "helmet_mail_base", "armor_mail_base" ],
				[ "boots_plate_base", "gloves_plate_base", "helmet_plate_base", "armor_plate_base" ],
				[ "boots_plate_base", "gloves_plate_base", "helmet_plate_base", "armor_plateheavy_base" ] ];
		
	for (var index = 0; index < types.length; index++) {
		var cb = game.createButtonCallback(slot, "conjureArmor");
		cb.addArgument(ids[index]);
		game.addMenuButton(types[index], cb);
	}
	
	game.showMenu();
}

function conjureArmor(game, slot, itemIDs) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	// make sure all targets can equip all of the items
	for (var itemIndex = 0; itemIndex < itemIDs.length; itemIndex++) {
		var baseItem = game.entities().getItem(itemIDs[itemIndex]);
	
		for (var i = 0; i < creatures.size(); i++) {
			if ( !creatures.get(i).getInventory().hasPrereqsToEquip(baseItem) ) {
				creatures.remove(i);
				i--;
			}
		}
	}
	
	if (creatures.size() == 0) {
		game.addMessage("red", "No available targets can use that armor.");
	} else {
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.addCallbackArgument( itemIDs );
		targeter.activate();
	}
}

function onTargetSelect(game, targeter, itemIDs) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var duration = game.dice().d5(2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	// create the items, set its properties, and equip it
	var qualityIndex = parseInt(casterLevel / 3) + 1;
	if (qualityIndex >= game.ruleset().getNumItemQualities())
		qualityIndex = game.ruleset().getNumItemQualities() - 1;
	
	for (var index = 0; index < itemIDs.length; index++) {
		createItem(game, itemIDs[index], qualityIndex, target, duration, targeter);
	}
}

function createItem(game, itemID, qualityIndex, target, duration, targeter) {
	// create the item to be conjured
	var item = game.entities().getItem(itemID);
	var conjuredID = "__" + item.getID() + "Conjured";
	item.setName("Conjured " + item.getName());
	item.setID(conjuredID);
	item.setCursed(true);
	item.setQuality(game.ruleset().getItemQuality(qualityIndex));
	item.recomputeBonuses();
	game.campaign().addCreatedItem(itemID, item);
	
	// create an effect to keep track of the item
	var effect = targeter.getSlot().createEffect("effects/conjureItem");
	effect.setDuration(duration);
	effect.setTitle("Conjured Armor Piece");
	effect.put("itemID", conjuredID);
	
	// keep track of the old item to re-equip it at the end of the effect, if possible
	// also figure out the sub icon slot of the item
	if (item.isArmor()) {
		var subIconSlot = "Torso";
		var oldItem = target.getInventory().getEquippedArmor();
	} else if (item.isGloves()) {
		var subIconSlot = "Gloves";
		var oldItem = target.getInventory().getEquippedGloves();
	} else if (item.isBoots()) {
		var subIconSlot = "Boots";
		var oldItem = target.getInventory().getEquippedBoots();
	} else {
		var subIconSlot = "Head";
		var oldItem = target.getInventory().getEquippedHelmet();
	}
	
	// if there was an item in this slot, save it to re-equip after the spell ends
	if (oldItem != null) {
		effect.put("oldItemID", oldItem.getID());
		effect.put("oldItemQuality", oldItem.getQuality().getName());
	}
	
	target.applyEffect(effect);
	
	// equip the new item
	target.getInventory().addItemAndEquip(item);
	
	var anim = game.getBaseAnimation("subIconFlash");
	anim.addFrame(target.getSubIcon(subIconSlot));
	anim.setColor(target.getSubIconColor(subIconSlot));
	
	var pos = target.getSubIconScreenPosition(subIconSlot);
	anim.setPosition(pos.x, pos.y);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
