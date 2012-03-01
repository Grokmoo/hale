function onUse(game, item, target) {
    var creatures = game.ai.getTouchableCreatures(target, "Friendly");

    game.hideOpenWindows();
    
    var targeter = game.createListTargeter(target, item.getScript());
    targeter.addAllowedCreatures(creatures);
    targeter.setMenuTitle(item.getName());
    targeter.addCallbackArgument(item);
    
    targeter.activate();
}

function onTargetSelect(game, targeter, item) {
    var parent = targeter.getParent();
    var target = targeter.getSelectedCreature();
    
    var quality = item.getQuality().getModifier() + 100;
    var hp = parseInt((game.dice().rand(1, 5) * quality) / 100);
    
    target.healDamage(hp);
    parent.getInventory().removeItem(item);
    
    game.ai.provokeAttacksOfOpportunity(parent);
}
