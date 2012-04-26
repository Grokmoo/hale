function startQuest(game) {
    if (game.hasQuestEntry("The Master")) return;
    
    var quest = game.getQuestEntry("The Master");
    
    var entry = quest.createSubEntry("Escape from the Underground");
    
    entry.addText("You have escaped from the subterranean land you were trapped in.  While exploring the cave system, you discovered that you are of a chosen blood, and that the Master will not stop until you are dead.");
    
    entry.addText("In order to stop the seemingly endless stream of assassins, you must find and defeat the Master.");
}