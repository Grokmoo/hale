function startConversation(game, parent, target, conversation) {
    if (parent.get("tombQuestRecieved") != null) {
        conversation.addText("Travel to the Tomb of Narkel, and report back with what you find.  Good luck.");
        
        conversation.addResponse("Farewell.", "onExit");
    } else {
        conversation.addText("Welcome to our city, stranger.  Can I help you in some way?");
        conversation.addResponse("We wish to help you with the fight against the Master.", "convo02");
        conversation.addResponse("Not at the moment.  Farewell.", "onExit");
    }
}

function convo02(game, parent, target, conversation) {
    conversation.addText("Excellent, we need all the soldiers we can get.  Please talk to the captain of the guard, in the Commons.");
    
    conversation.addResponse("We have unique information on the nature of the Master.", "convo03");
    conversation.addResponse("We will, thank you.", "onExit");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Oh really?  What information is that?");
    
    conversation.addResponse("<span style=\"font-family: red;\">Explain your adventures and the Chosen Blood.</span>", "convo04");
}

function convo04(game, parent, target, conversation) {
    conversation.addText("<span style=\"font-family: blue;\">The Lady listens intently to your story, without interruption.</span>");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("What an amazing tale!  It is a bit much to take in all at once.  However, it does fit with what my scholars have managed to research so far.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo06");
}

function convo06(game, parent, target, conversation) {
    conversation.addText("In fact, things are starting to make more sense.  We have very few records of the time, but it seems the Master was fought and defeated by ancient mages hundreds of years ago.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo07");
}

function convo07(game, parent, target, conversation) {
    conversation.addText("I have something to ask of you, then.  We need to know more about how the Master was defeated last time, and there is one who may be able to answer.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo08");
}

function convo08(game, parent, target, conversation) {
    conversation.addText("To the west of here, carved into the mountainside, is the tomb of an ancient warrior, Narkel.");
    conversation.addText("From our records, it seems he was the leader of a great army that fought the Master.");
    
    conversation.addResponse("<span style=\"font-family: red;\">Continue</span>", "convo09");
    
    game.revealWorldMapLocation("Tomb of Narkel");
}

function convo09(game, parent, target, conversation) {
    conversation.addText("It is likely that his spirit still resides in the tomb, and you are a perfect choice to travel there, and see what you can uncover.");
    
    conversation.addText("We will continue our research here as best we can.");
    
    parent.put("tombQuestRecieved", true);
    game.runExternalScript("quests/theMaster", "learnOfTomb");
    
    conversation.addResponse("I will travel to the tomb, then.  Farewell", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
