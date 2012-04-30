function startConversation(game, parent, target, conversation) {
    conversation.addText("Welcome to our city, stranger.  Can I help you in some way?");
    conversation.addResponse("Not at the moment.  Farewell.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
