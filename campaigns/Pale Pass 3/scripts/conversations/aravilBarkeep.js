function startConversation(game, parent, target, conversation) {
    conversation.addText("Hello, friend.  What do you need?");
    conversation.addResponse("Nothing today, thanks.", "onExit");
}


function onExit(game, parent, talker, conversation) {
    conversation.exit()
}
