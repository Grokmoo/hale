function canUse(game, trap, parent) {
    return trap.canPlace(parent);
}

function onUse(game, trap, parent) {
    trap.tryPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {
    var effect = target.createEffect();
    effect.setTitle("Freeze Trap");
    effect.setDuration(3);
    
    if ( !target.physicalResistanceCheck(trap.modifyValueByQuality(100)) ) {
        effect.getBonuses().addPenalty("ActionPoint", trap.modifyValueByQuality(20) );
    } else {
        effect.getBonuses().addPenalty("ActionPoint", trap.modifyValueByQuality(20) / 2);   
    }
    
    target.applyEffect(effect);
}