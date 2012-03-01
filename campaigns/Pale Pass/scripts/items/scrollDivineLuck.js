function canUse(game, item, target) {
	return target.getTimer().canActivateAbility("DivineLuck");
}

function onUse(game, item, target) {
	var quality = item.getQuality().getModifier() + 100;
	
	var caster = game.createScrollCaster("DivineLuck");
	caster.setItemToUse(item);
	caster.setParent(target);
	caster.setCasterLevel(quality / 25);
	caster.activate();
}