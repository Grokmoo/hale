function startConvo(game, player) {
    game.scrollToCreature("theMaster");
    
    game.sleep(2000);
    
    game.currentArea().getEntityWithID("theMaster").startConversation(player);
}

function onCreatureDeath(game, parent) {
	game.lockInterface(3.0);
    game.runExternalScriptWait("ai/focusCrystal", "startConvo", 1.0, game.getParty().get(0));
}

function runTurn(game, parent) {
	var target = game.currentArea().getEntityWithID("theMaster");
	
	var damage = target.stats().getMaxHP() - target.getCurrentHP();
	
	if (damage > 0)
		target.healDamage(damage);
}