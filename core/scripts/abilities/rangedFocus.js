function onActivate(game, slot) {
	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreature(slot.getParent());
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var slot = targeter.getSlot();
	var parent = slot.getParent();

	var lvls = parent.getRoles().getLevel("Ranger");

	if (parent.getAbilities().has("GreaterFocus"))
		lvls = lvls * 2;
	
	var duration = parseInt(3 + lvls / 3);
	
	var effect = slot.createEffect();
	effect.setDuration(duration);
	effect.setTitle(slot.getAbility().getName());
	
	if (parent.getAbilities().has("DefensiveFocus"))
		effect.getBonuses().addBonus('ArmorClass', 'Stackable', 10 + lvls);
	
	effect.getBonuses().addBonus('RangedAttack', 10 + lvls);
	effect.getBonuses().addBonus('RangedDamage', 20 + 2 * lvls);
	slot.getParent().applyEffect(effect);

	slot.setActiveRoundsLeft(duration);
	slot.activate();
}
