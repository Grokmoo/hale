function onRoundElapsed(game, effect) {
	var healingLeft = effect.get("healingLeft");
	
	var healThisRound = Math.round( healingLeft / (effect.get("totalRounds") - effect.get("currentRound")) );
	
	effect.put("currentRound", parseInt(effect.get("currentRound")) + 1);
	effect.put("healingLeft", healingLeft - healThisRound);
	
	var spell = effect.getSlot().getAbility();
	var parent = effect.getSlot().getParent();
	
	spell.applyHealing(parent, effect.getTarget(), healThisRound);
}
