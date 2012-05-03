function onRoundElapsed(game, effect) {
	var damage = effect.get("healingPerRound");
	
	effect.getTarget().healDamage(damage);
}
