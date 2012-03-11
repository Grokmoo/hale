function onActivate(game, slot) {
	var effect = slot.createAura("effects/leadership");
	effect.setAuraMaxRadius(4);
	effect.setTitle(slot.getAbility().getName());
	
	effect.setRemoveOnDeactivate(true);
	effect.setHasDescription(false);
	
	var g1 = game.getBaseParticleGenerator("halo");
	g1.setPosition(slot.getParent().getPosition());
	g1.setDurationInfinite();
	effect.addAnimation(g1);
	
	effect.getBonuses().addPenalty("ActionPoint", "Stackable", -10);

    slot.getParent().applyEffect(effect);

	slot.activate();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}
