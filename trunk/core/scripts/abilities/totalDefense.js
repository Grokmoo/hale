function canActivate(game, parent) {
	var offHand = parent.getInventory().getEquippedOffHand();
	
	return (offHand != null && offHand.isShield());
}

function onActivate(game, slot) {
	var parent = slot.getParent();
	
	slot.setActiveRoundsLeft(1);
	
	var effect = slot.createEffect();
	effect.setTitle(slot.getAbility().getName());
	effect.getBonuses().addBonus('ArmorClass', 'Stackable', 40);
	parent.applyEffect(effect);
	
	slot.activate();
}
