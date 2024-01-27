package net.sf.hale.entity;

import net.sf.hale.Game;
import net.sf.hale.rules.Currency;
import net.sf.hale.rules.Merchant;
import net.sf.hale.widgets.MultipleItemPopup;

public class InvCallBackManager {
	private Creature parent;
	public InvCallBackManager(Inventory inv) {}
	
	public Runnable getBuyCallback(Item item, int maxQuantity, Merchant merchant) {
		return new BuyCallback(item, maxQuantity, merchant);
	}
	
	public Runnable getSellCallback(Item item, int maxQuantity, Merchant merchant) {
		return new SellCallback(item, maxQuantity, merchant);
	}
	
	private class BuyCallback extends MultipleCallback {
		private Merchant merchant;
		
		private BuyCallback(Item item, int maxQuantity, Merchant merchant) {
			super(item, maxQuantity, "Buy");
			this.merchant = merchant;
		}

		@Override public String getValueText(int quantity) {
			int percent = merchant.getCurrentSellPercentage();
			return "Price: " + Currency.getPlayerBuyCost(item, quantity, percent).shortString();
		}

		@Override public void performItemAction(int quantity) {
			merchant.sellItem(item, parent, quantity);
			
			Game.mainViewer.getMenu().hide();
			Game.mainViewer.updateInterface();
		}
	}
	
	private class SellCallback extends MultipleCallback {
		private Merchant merchant;
		
		private SellCallback(Item item, int maxQuantity, Merchant merchant) {
			super(item, maxQuantity, "Sell");
			this.merchant = merchant;
		}
		
		@Override public String getValueText(int quantity) {
			int percent = merchant.getCurrentBuyPercentage();
			return "Price: " + Currency.getPlayerSellCost(item, quantity, percent).shortString();
		}
		
		@Override public void performItemAction(int quantity) {
			merchant.buyItem(item, parent, quantity);
			
			Game.mainViewer.getMenu().hide();
			Game.mainViewer.updateInterface();
		}
	}
		
		private abstract class MultipleCallback implements MultipleItemPopup.Callback {
			protected final Item item;
			private final int maxQuantity;
			private final String labelText;
			
			private MultipleCallback(Item item, int maxQuantity, String labelText) {
				this.item = item;
				this.maxQuantity = maxQuantity;
				this.labelText = labelText;
			}
			
			@Override public void run() {
				if (maxQuantity == 1) {
					performItemAction(1);
				} else {
					MultipleItemPopup popup = new MultipleItemPopup(Game.mainViewer);
					popup.openPopupCentered(this);
				}
			}
			
			@Override public String getLabelText() { return labelText; }
			
			@Override public int getMaximumQuantity() { return maxQuantity; }
			
			@Override public String getValueText(int quantity) { return ""; }
		}
}
