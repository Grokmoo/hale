function startQuest(game) {
    if (game.hasQuestEntry("The Master")) return;
    
    var quest = game.getQuestEntry("The Master");
    
    var entry = quest.createSubEntry("Escape from the Underground");
    
    entry.addText("You have escaped from the subterranean land you were trapped in.  While exploring the cave system, you discovered that you are of a chosen blood, and that the Master will not stop until you are dead.");
    
    entry.addText("In order to stop the seemingly endless stream of assassins, you must find and defeat the Master.");
}

function learnOfArmy(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Master's Army")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Master's Army");
    
    entry.addText("You met a scout who told you that while you were trapped underground, the Master has assembled an army and is now openly opposing the King.");
    
    entry.addText("You should travel to Aravil to learn more and help with the fight against the Master.");
}

function narkel(game) {
    var quest = game.getQuestEntry("The Master");
    
    if (quest.hasSubEntry("The Binding Focus")) return;
    
    quest.setCurrentSubEntriesCompleted();
    
    var entry = quest.createSubEntry("The Binding Focus");
    
    entry.addText("Deep in his ancient tomb, you found and spoke with the spirit of the warrior, Narkel.");
    
    entry.addText("He informed you that the Master is an ancient demon, bound to this world through a powerful spell requiring some manner of focus.");
    
    entry.addText("If the focus is destroyed, the Master should be removed from this plane.  Unfortunately, Narkell did not know where the focus would be.");
    
    entry.addText("His only clues were that the focus must be a large and pure crystal, and that the Master would most likely keep it close to him.");
}