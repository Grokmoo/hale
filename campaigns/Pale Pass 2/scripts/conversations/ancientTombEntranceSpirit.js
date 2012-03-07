function startConversation(game, parent, target, conversation) {
    conversation.addText("What brings you into my domain, mortal?");
    conversation.addResponse("I was just leaving.  Farewell.", "onExit");
}

function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
