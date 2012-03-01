function canActivate(game, parent) {
	if (!parent.getTimer().canAttack()) return false;
	
	var weapon = parent.getInventory().getMainWeapon();
	
	return weapon.isMeleeWeapon();
}

function onActivate(game, slot) {
   var creatures = game.ai.getAttackableCreatures(slot.getParent());

   var targeter = game.createListTargeter(slot);
   targeter.addAllowedCreatures(creatures);
   targeter.activate();
}

function onTargetSelect(game, targeter) {
   targeter.getSlot().activate();
   
   var ability = targeter.getSlot().getAbility();

   // perform the attack in a new thread as the standardAttack will
   // block
   var cb = ability.createDelayedCallback("performAttack");
   cb.addArgument(targeter);
   cb.start();
}

function performAttack(game, targeter) {
	var parent = targeter.getParent();
	var target = targeter.getSelectedCreature();
	
	// apply a temporary effect with the bonuses
	var effect = parent.createEffect();
	effect.setTitle(targeter.getSlot().getAbility().getName());
	effect.getBonuses().addBonus('Attack', 'Stackable', 20);
	effect.getBonuses().addBonus('Damage', 'Stackable', 80);
	parent.applyEffect(effect);

	game.standardAttack(parent, target);

	parent.removeEffect(effect);
}