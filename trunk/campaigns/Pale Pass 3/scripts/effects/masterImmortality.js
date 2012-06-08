function onDamaged(game, damage, effect) {
	// only immortal while the focus crystal exists
	if (game.currentArea().getEntityWithID("focusCrystal") == null) return;

	var target = effect.getTarget();
	
	var damageApplied = damage.getTotalAppliedDamage();
	
	if (target.getCurrentHP() <= damageApplied) {
		target.healDamage(damageApplied, false);
		
		game.addMessage("red", "The Master is protected by the focus crystal!");
	}
}
