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
    effect.setDuration(6);
    effect.setTitle("Acid Trap");
    effect.put("damagePerRound", trap.modifyValueByQuality(6) );
    effect.put("damageType", "Acid");
    target.applyEffect(effect);
    game.addMessage("red", target.getName() + " is covered in burning acid.");
}