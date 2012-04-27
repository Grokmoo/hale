function startQuest(game) {
    if (game.hasQuestEntry("The Scout Encampment")) return;
    
    var quest = game.getQuestEntry("The Scout Encampment");
    
    var entry = quest.createSubEntry("Travel to the Encampment");
    
    entry.addText("In order to enter the city of Aravil, the guard commander wants you to prove your loyalty.  She has offered to let you into the city if you travel to a nearby encampment, kill the Master's scout commander there, and return with his insignia.");
}
