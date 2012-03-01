function startQuest(game) {
    if (game.hasQuestEntry("Dwarven Slavers")) return;
    
    var quest = game.getQuestEntry("Dwarven Slavers")
    var entry = quest.createSubEntry("Find the Slaver Camp")
    
    entry.addText("The smith in the goblin village told you the story of how his sons have apparently been taken by slavers.");
    entry.addText("He believes that they will be found at the north end of the Mushroom Forest, in a slaver camp located there.");
}

function endQuest(game) {
    var quest = game.getQuestEntry("Dwarven Slavers");
    
    var entry = quest.createSubEntry("Quest complete");
    
    // add text
    
    quest.setCompleted();
}