function onRemove(game, effect) {
	effect.getTarget().getInventory().removeItemEvenIfEquipped(effect.get("itemID"));
}
