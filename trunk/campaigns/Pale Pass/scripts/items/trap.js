function canUse(game, trap, parent) {
    return trap.canPlace(parent);
}

function onUse(game, trap, parent) {
    trap.tryPlace(parent);
}

function onSpringTrap(game, trap, target) {
    
}

function onTrapReflexFailed(game, trap, target) {
    
}