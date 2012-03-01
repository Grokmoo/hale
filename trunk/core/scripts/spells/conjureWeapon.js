function onActivate(game, slot) {
    game.addMenuLevel("Conjure Weapon");
	
	var ids = [ "dagger", "shortSword", "longsword", "greatsword", "mace",
	    "morningstar", "lighthammer", "warhammer", "maul", "javelin",
		"shortspear", "longspear", "longbow", "shortbow", "handaxe",
		"battleaxe", "greataxe", "quarterstaff", "sling", "club", "crossbow",
		"bastardSword", "flail", "waraxe" ];
	
	var names = [ "Dagger", "Short Sword", "Longsword", "Greatsword", "Mace",
		"Morningstar", "Light Hammer", "Warhammer", "Maul", "Javelin",
		"Short Spear", "Longspear", "Longbow", "Shortbow", "Handaxe",
		"Battleaxe", "Greataxe", "Quarterstaff", "Sling", "Club", "Crossbow",
		"Bastard Sword", "Flail", "Waraxe" ];
	
	for (var index = 0; index < ids.length; index++) {
		var cb = game.createButtonCallback(slot, "castSpell");
		cb.addArgument(ids[index]);
		
		game.addMenuButton(names[index], cb);
	}
	
	game.showMenu();
}

function castSpell(game, slot, weaponID) {
	var creatures = game.ai.getTouchableCreatures(slot.getParent(), "Friendly");
	
	var baseItem = game.entities().getItem(weaponID);
	
	for (var i = 0; i < creatures.size(); i++) {
		if (!creatures.get(i).getInventory().hasPrereqsToEquip(baseItem)) {
			creatures.remove(i);
			i--;
		}
	}
	
	if (creatures.size() == 0) {
		game.addMessage("red", "No available targets can use that weapon.");
	} else {
		var targeter = game.createListTargeter(slot);
		targeter.addAllowedCreatures(creatures);
		targeter.addCallbackArgument(weaponID);
		targeter.activate();
	}
}

function onTargetSelect(game, targeter, weaponID) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var duration = game.dice().d5(2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	// create the weapon, set its properties, and equip it
	var qualityIndex = parseInt(casterLevel / 3) + 1;
	if (qualityIndex >= game.ruleset().getNumItemQualities())
		qualityIndex = game.ruleset().getNumItemQualities() - 1;
	
	var weapon = game.entities().getItem(weaponID);
	var conjuredID = "__" + weapon.getID() + "Conjured";
	weapon.setName("Conjured " + weapon.getName());
	weapon.setID(conjuredID);
	weapon.setCursed(true);
	weapon.setQuality(game.ruleset().getItemQuality(qualityIndex));
	weapon.createEnchantment("entity.addBonus(\"WeaponAttack\", 10);");
	weapon.recomputeBonuses();
	game.campaign().addCreatedItem(weaponID, weapon);
	
	target.getInventory().addItemAndEquip(weapon);
	
	// create an effect to keep track of the weapon
	var effect = targeter.getSlot().createEffect("effects/conjureItem");
	effect.setDuration(duration);
	effect.setTitle("Conjured Weapon");
	effect.put("itemID", conjuredID);
	
	target.applyEffect(effect);
	
	// animate the new item initially
	if (weaponID == "longbow" || weaponID == "shortbow") {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getSubIcon("OffHandWeapon"));
		anim.setColor(target.getSubIconColor("OffHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("OffHandWeapon");
		anim.setPosition(pos.x, pos.y);
	} else {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getSubIcon("MainHandWeapon"));
		anim.setColor(target.getSubIconColor("MainHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("MainHandWeapon");
		anim.setPosition(pos.x, pos.y);
	}
		
	game.runAnimationNoWait(anim);
	game.lockInterface(anim.getSecondsRemaining());
}
