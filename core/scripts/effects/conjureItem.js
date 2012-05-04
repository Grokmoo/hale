function onRemove(game, effect) {
	var target = effect.getTarget();

	target.getInventory().removeItemEvenIfEquipped(effect.get("itemID"));
	
	// if possible, re-equip the old item
	if (effect.get("oldItemID") != null) {
		reequip(game, target, effect.get("oldItemID"), effect.get("oldItemQuality"));
	}
	
	// if possible, re-equip old item 2
	if (effect.get("oldItem2ID") != null) {
		reequip(game, target, effect.get("oldItem2ID"), effect.get("oldItem2Quality"));
	}
}

function reequip(game, target, id, quality) {
	var index = target.getInventory().getUnequippedItems().findItem(id, quality);
	if (index >= 0) {
		var oldItem = target.getInventory().getUnequippedItems().getItem(index);
		target.getInventory().equipItem(oldItem, 0);
	}
}
