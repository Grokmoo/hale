function onRoundElapsed(game, effect) {
	var damage = effect.get("healingPerRound");
	
	// don't heal if target is at full health
	if (effect.getTarget().stats().getMaxHP() == effect.getTarget().getCurrentHP())
		return;
	
	effect.getTarget().healDamage(damage);
}
