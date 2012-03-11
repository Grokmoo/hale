function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var ability = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = parent;
	
	var duration = 3;
	
	// fire the ability
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	var chaBonus = (parent.stats().getCha() - 10) * 2;
	if (parent.getAbilities().has("PersonalMagnetism"))
		chaBonus = chaBonus * 2;
	
	var lvlBonus = parent.getRoles().getLevel("Paladin");
	
	var amount = chaBonus + lvlBonus;
	
	var weapon = target.getInventory().getMainWeapon();
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.setTitle(ability.getName());
	effect.getBonuses().addStandaloneDamageBonus('Divine', amount, amount);
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
