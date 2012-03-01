function onActivate(game, slot) {
	game.addMenuLevel("Conjure Armor");
	
	if (slot.getParent().getAbilities().has("ConjureArmorSet")) {
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
	} else {
		var ids = [ "boots_leather_base", "boots_mail_base", "boots_plate_base",
			"gloves_leather_base", "gloves_mail_base", "gloves_plate_base",
			"helmet_leather_base", "helmet_mail_base", "helmet_plate_base" ];
	
		var names = [ "Leather Boots", "Mail Boots", "Plate Boots",
			"Leather Gloves", "Mail Gloves", "Plate Gloves",
			"Leather Helmet", "Mail Helmet", "Plate Helmet" ];
	
		for (var index = 0; index < ids.length; index++) {
			var cb = game.createButtonCallback(slot, "conjureArmor");
			cb.addArgument( [ ids[index] ] );
		
			game.addMenuButton(names[index], cb);
		}
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
		game.addMessage("red", "No available targets can use that item.");
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
	
	// create the item, set its properties, and equip it
	var qualityIndex = parseInt(casterLevel / 3) + 1;
	if (qualityIndex >= game.ruleset().getNumItemQualities())
		qualityIndex = game.ruleset().getNumItemQualities() - 1;
	
	for (var index = 0; index < itemIDs.length; index++) {
		createItem(game, itemIDs[index], qualityIndex, target, duration, targeter);
	}
}

function createItem(game, itemID, qualityIndex, target, duration, targeter) {
	var item = game.entities().getItem(itemID);
	var conjuredID = "__" + item.getID() + "Conjured";
	item.setName("Conjured " + item.getName());
	item.setID(conjuredID);
	item.setCursed(true);
	item.setQuality(game.ruleset().getItemQuality(qualityIndex));
	item.createEnchantment("entity.addBonus(\"ArmorClass\", \"NaturalArmor\", 5);");
	item.recomputeBonuses();
	game.campaign().addCreatedItem(itemID, item);
	
	target.getInventory().addItemAndEquip(item);
	
	// create an effect to keep track of the item
	var effect = targeter.getSlot().createEffect("effects/conjureItem");
	effect.setDuration(duration);
	effect.setTitle("Conjured Armor Piece");
	effect.put("itemID", conjuredID);
	
	target.applyEffect(effect);
	
	var subIconSlot = "Head";
	if (item.isArmor()) subIconSlot = "Torso";
	else if (item.isGloves()) subIconSlot = "Gloves";
	else if (item.isBoots()) subIconSlot = "Boots";
	
	var anim = game.getBaseAnimation("subIconFlash");
	anim.addFrame(target.getSubIcon(subIconSlot));
	anim.setColor(target.getSubIconColor(subIconSlot));
	
	var pos = target.getSubIconScreenPosition(subIconSlot);
	anim.setPosition(pos.x, pos.y);
	
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
