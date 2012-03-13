function onOpen(game, item, opener) {
    game.runExternalScriptWait("items/theGate", "startCutscene", 1.0);
    game.lockInterface(1.0);
}

function startCutscene(game) {
    game.showCutscene("theGate");
}

function campaignConclusion(game) {
	var popup = game.showCampaignConclusionPopup();
    popup.addText("<div style=\"font-family: vera;\">");
	popup.addText("You have completed Chapter 2 of the Pale Pass campaign.");
	popup.addText("  Look for Chapter 3 and the conclusion of the Pale Pass saga soon.");
	popup.addText("</div>");
	
	popup.addText("<div style=\"font-family: vera; margin-top: 1em;\">");
	popup.addText("Thanks for playing!");
	popup.addText("</div>");
	
	//popup.setNextCampaign("Pale Pass 3", "Continue to Chapter 3");
	popup.setTextAreaHeight(120);
	
	popup.show();
}