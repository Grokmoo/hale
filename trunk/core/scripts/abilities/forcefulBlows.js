function enablePowerfulBlows(game, slot, level) {
   var effect = slot.createEffect();
   effect.setTitle(slot.getAbility().getName());
   
   effect.getBonuses().addPenalty('OneHandedMeleeWeaponAttack', -level);
   effect.getBonuses().addPenalty('TwoHandedMeleeWeaponAttack', -level);
   
   var damage = 2 * level;
   
   effect.getBonuses().addBonus('OneHandedMeleeWeaponDamage', damage);
   effect.getBonuses().addBonus('TwoHandedMeleeWeaponDamage', damage);
   
   effect.setRemoveOnDeactivate(true);

   slot.getParent().applyEffect(effect);

   slot.activate();
}

function onActivate(game, slot) {
   game.addMenuLevel("Powerful Blows");

   var improved = slot.getParent().getAbilities().has("PowerfulBlows");
   
   var max = 20;
   if (improved) max = 40;
   
   for (var i = 5; i <= max; i += 5) {
      var dam = 2 * i;
   
      var cb = game.createButtonCallback(slot, "enablePowerfulBlows");
      cb.addArgument(i);

      game.addMenuButton("Damage +" + dam + ", Attack -" + i, cb);
   }
   
   game.showMenu();
}

function onDeactivate(game, slot) {
   slot.deactivate();
}
