function onActivate(game, slot) {
	var creatures = game.ai.getVisibleCreaturesWithinRange(slot.getParent(), "Hostile", 20);

	var targeter = game.createListTargeter(slot);
	targeter.addAllowedCreatures(creatures);
	targeter.activate();
}

function onTargetSelect(game, targeter) {
	var spell = targeter.getSlot().getAbility();
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	var casterLevel = parent.getCasterLevel();
	
	var duration = parseInt(3 + casterLevel / 2);
	
	targeter.getSlot().setActiveRoundsLeft(duration);
	targeter.getSlot().activate();
	
	if (!spell.checkSpellFailure(parent)) return;
	
	if ( target.physicalResistanceCheck(spell.getCheckDifficulty(parent)) )
		return;
	
	var effect = targeter.getSlot().createEffect();
	effect.setDuration(duration);
	effect.getBonuses().add('Blind');
	effect.setTitle(spell.getName());
	
	var g1 = game.getBaseParticleGenerator("fog");
	g1.setDurationInfinite();
	g1.setPosition(target.getPosition());
	g1.setCircleBounds(0.0, 12.0);
	effect.addAnimation(g1);
	
	target.applyEffect(effect);
}
