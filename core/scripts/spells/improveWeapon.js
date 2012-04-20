function isTargetValid(game, target, slot) {
	var weapon = target.getInventory().getEquippedMainHand();
	
	if (weapon == null || !weapon.isMeleeWeapon())
		return false;
		
	return true;
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
	
	if (!isTargetValid(game, target)) return;
	
	var duration = game.dice().rand(5, 10);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(spell.getName());
	
	// we have already validated the weapon
	var weapon = target.getInventory().getEquippedMainHand();
	
	effect.getBonuses().addBonus("WeaponAttack", 10);
	effect.getBonuses().addBonus("WeaponCriticalChance", 10);
	
	if (targeter.getSlot().getParent().getAbilities().has("FlamingWeapon")) {
		// apply the flaming weapon effect
	
		var minDamage = parseInt(1 + casterLevel / 4);
		var maxDamage = parseInt(4 + casterLevel / 4);
		effect.getBonuses().addStandaloneDamageBonus("Fire", minDamage, maxDamage);
		
		var generator = game.getBaseParticleGenerator("flame");
		
		if (target.drawWithSubIcons()) {
			var pos = target.getSubIconScreenPosition("MainHandWeapon");
			generator.setPosition(pos.x - 5.0, pos.y - 5.0);
		} else {
			var pos = target.getPosition().toScreen();
			generator.setPosition(pos.x - 15.0, pos.y - 15.0);
		}
		
		effect.addAnimation(generator);
	}
	
	weapon.applyEffect(effect);
	
	if (target.drawWithSubIcons()) {
		var anim = game.getBaseAnimation("subIconFlash");
		anim.addFrame(target.getSubIcon("MainHandWeapon"));
		anim.setColor(target.getSubIconColor("MainHandWeapon"));
		
		var pos = target.getSubIconScreenPosition("MainHandWeapon");
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
