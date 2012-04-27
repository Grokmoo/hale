
function startConversation(game, parent, target, conversation) {
    if (parent.get("questStarted") != null) {
        conversation.addText("Do you have the insignia of the scout leader?");
        
        conversation.addResponse("Not yet.  Farewell.", "onExit");
    } else {
        conversation.addText("Gate pass, please.");
        conversation.addResponse("I don't have a gate pass.", "convo02a");
        conversation.addResponse("You must let me in, I have urgent news for the general!", "convo02b");
    }
}

function convo02a(game, parent, target, conversation) {
    conversation.addText("Then you cannot enter.  There is a war on, and we can't risk allowing enemy spies into the city.");
    
    conversation.addResponse("Isn't there any way I can get into the city?", "convo03");
}

function convo02b(game, parent, target, conversation) {
    conversation.addText("If you truly had urgent news, you would have a pass.  There is a war on, and we can't risk allowing enemy spies into the city.");
    
    conversation.addResponse("Isn't there any way I can get into the city?", "convo03");
}

function convo03(game, parent, target, conversation) {
    conversation.addText("Well, you look like a capable group of warriors.  Perhaps there is something you can do to prove your loyalty.");
    
    conversation.addResponse("What would you have me do?", "convo04");
    conversation.addResponse("Not interested.  Farewell.", "onExit");   
}

function convo04(game, parent, target, conversation) {
    conversation.addText("A small group of scouts for the Master's army is based to the west of here.  We don't want them reporting back our troop movements.");
    
    conversation.addResponse("<span style=\"font-family: red\">Continue</span>", "convo05");
}

function convo05(game, parent, target, conversation) {
    conversation.addText("Travel to their encampment, kill their leader, and bring me his insignia as proof of your deeds.");
    
    conversation.addText("Do this, and you will have proven your trustworthiness and usefulness.  Then, I will allow you into the city.");
    
    conversation.addResponse("We will do it.  Farewell.", "onExit");
    conversation.addResponse("We will consider your offer.  Farewell.", "onExit");
    
    game.revealWorldMapLocation("Scout Encampment");
    game.runExternalScript("quests/theScoutEncampment", "startQuest");
    parent.put("questStarted", true);
}

function onExit(game, parent, talker, conversation) {
    conversation.exit();
}
