function canUse(game, item, target) {
    return target.getTimer().canActivateAbility("Freeze");
}

function onUse(game, item, target) {
    var quality = item.getQuality().getModifier() + 100;
    var caster = game.createScrollCaster("Freeze")
    
    caster.setItemToUse(item);
    caster.setParent(target);
    caster.setCasterLevel(quality / 25);
    caster.activate();
}
