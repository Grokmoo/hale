function cutsceneFinished(game) {
    var popup = game.showCampaignConclusionPopup();
    popup.addText("<div style=\"font-family: vera;\">");
	popup.addText("Congratulations on completing the Pale Pass campaign.");
	popup.addText("</div>");
	
	popup.addText("<div style=\"font-family: vera; margin-top: 1em;\">");
	popup.addText("Thanks for playing!");
	popup.addText("</div>");
	
	popup.setTextAreaHeight(120);
	
	popup.show();
}

function startCutscene(game) {
    game.showCutscene("theMaster");
}

function onCreatureDeath(game, parent) {
	game.lockInterface(3.0);
    game.runExternalScriptWait("ai/theMaster", "startCutscene", 3.0);
}

function runTurn(game, parent) {
	game.runExternalScript("ai/aiStandard", "runTurn", parent);
}
