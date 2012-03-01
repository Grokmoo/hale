function canUse(game, trap, parent) {
    return trap.canPlace(parent);
}

function onUse(game, trap, parent) {
    trap.tryPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {
    var effect = target.createEffect("effects/damageOverTime");
    effect.setDuration(2);
    effect.setTitle("Flame Trap");
    effect.put("damagePerRound", trap.modifyValueByQuality(3) );
    effect.put("damageType", "Fire");
    target.applyEffect(effect);
    game.addMessage("red", target.getName() + " is on fire.");
}