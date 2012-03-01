function onAreaLoadFirstTime(game, area) {

    game.showCutscene("intro");
    
    var value = game.getPartyCurrency().getValue();
    if (value < 500) {
        game.getPartyCurrency().setValue(500);
    }
    
    for (var i = 0; i < game.getParty().size(); i++) {
        var partyMember = game.getParty().get(i);
        var quickbar = partyMember.getQuickbar();
        
        quickbar.clear();
        
        partyMember.getAbilities().fillEmptySlots();
        quickbar.addAbilitiesToEmptySlots();
        
        var role = partyMember.getRoles().getBaseRole().getID();
        var inv = partyMember.getInventory();
        
        var item = inv.addItem("potionHealing", "Mediocre");
        quickbar.addToFirstEmptySlot(item);
        
        inv.addItemAndEquip("armor_clothes", "Mediocre");
        
        if (role.equals("Adept")) {
            inv.addItemAndEquip("dagger", "Mediocre");
            
            item = inv.addItem("potionCha", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        } else if (role.equals("Druid")) {
            inv.addItemAndEquip("boots_leather_base", "Mediocre");
            inv.addItemAndEquip("quarterstaff", "Mediocre");
            
            item = inv.addItem("potionWis", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        } else if (role.equals("Mage")) {
            inv.addItemAndEquip("quarterstaff", "Mediocre");
            
            item = inv.addItem("potionInt", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        } else if (role.equals("Priest")) {
            inv.addItemAndEquip("boots_leather_base", "Mediocre");
            inv.addItemAndEquip("mace", "Mediocre");
            inv.addItemAndEquip("shield_light_base", "Mediocre");
            
            item = inv.addItem("potionWis", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        } else if (role.equals("Rogue")) {
            inv.addItemAndEquip("boots_leather_base", "Mediocre");
            inv.addItemAndEquip("dagger", "Mediocre");
            
            item = inv.addItem("spikeTrap", 2, "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
            item = inv.addItem("potionDex", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        } else if (role.equals("Warrior")) {
            inv.addItemAndEquip("boots_leather_base", "Mediocre");
            inv.addItemAndEquip("gloves_leather_base", "Mediocre");
            
            inv.addItemAndEquip("shortSword", "Mediocre");
            
            item = inv.addItem("potionStrength", "Mediocre");
            quickbar.addToFirstEmptySlot(item);
            
        }
        
    }
}
