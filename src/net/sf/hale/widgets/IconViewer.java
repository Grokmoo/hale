package net.sf.hale.widgets;

import org.lwjgl.opengl.GL11;

import net.sf.hale.Sprite;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ThemeInfo;

/**
 * A Widget for showing a generic icon from any source.
 * 
 * @author Jared Stephen
 *
 */

public class IconViewer extends Button {
	/**
	 * The type of offset to use when drawing the sprite for this iconviewer
	 * @author Jared Stephen
	 *
	 */
	
	public enum DrawOffset {
		/** Draw with the icon offset (the default behavior) */
		Icon,
		
		/** Draw with no offset */
		None
	}
	
	private short minWidth, minHeight;
	
	private boolean enableEventHandling;
	private Color color;
	private Sprite sprite;
	
	private DrawOffset drawOffset;
	
	/**
	 * Creates a new IconViewer displaying blank
	 */
	
	public IconViewer() {
		this(null);
	}
	
	/**
	 * Creates a new IconViewer displaying the specified sprite
	 * 
	 * @param sprite the Sprite to be displayed
	 */
	
	public IconViewer(Sprite sprite) {
		this.sprite = sprite;
		this.color = Color.WHITE;
		
		enableEventHandling = true;
		drawOffset = DrawOffset.Icon;
	}
	
	/**
	 * Creates a new IconViewer displaying the specified sprite
	 * and showing the specified tooltip
	 * 
	 * @param sprite the Sprite to be displayed
	 * @param tooltip the tooltip that will appear when the user
	 * hovers the mouse over this Widget
	 */
	
	public IconViewer(Sprite sprite, String tooltip) {
		this(sprite);
		this.setTooltipContent(tooltip);
	}
	
	/**
	 * Sets whether this widget will handle events such as Button clicks,
	 * mouse hover, etc.
	 * @param enabled whether this Widget will handle events
	 */
	
	public void setEventHandlingEnabled(boolean enabled) {
		this.enableEventHandling = enabled;
	}
	
	/**
	 * Sets the type of offset to use when drawing this IconViewer
	 * @param offset the type of offset to use
	 */
	
	public void setDrawOffset(DrawOffset offset) {
		this.drawOffset = offset;
	}
	
	/**
	 * Returns the Sprite that this IconViewer is currently displaying.
	 * If no Sprite is being displayed, returns null.
	 * 
	 * @return the Sprite currently being displayed
	 */
	
	public Sprite getSprite() {
		return this.sprite;
	}
	
	/**
	 * Sets the Sprite that will be displayed by this Widget to sprite
	 * @param sprite the Sprite to be displayed
	 */
	
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}
	
	/**
	 * Returns the Color that this IconViewer is currently using to
	 * display the Sprite.
	 * @return the Color that this IconViewer is using for the Sprite.
	 */
	
	public Color getColor() {
		return this.color;
	}
	
	/**
	 * Sets the Color that the background Sprite for this IconViewer will
	 * be multiplied by prior to being drawn.  By default, this Color is
	 * pure white.
	 * 
	 * @param color the color to draw the background Sprite
	 */
	
	public void setColor(Color color) {
		if (color == null) this.color = Color.WHITE;
		else this.color = color;
	}
	
	@Override protected void paintWidget(GUI gui) {
		super.paintWidget(gui);
		
		// draw the icon if the ability is set
		if (sprite != null) {
			GL11.glColor4ub(color.getR(), color.getG(), color.getB(), color.getA());
			
			switch (drawOffset) {
			case Icon:
				sprite.drawWithIconOffset(getInnerX(), getInnerY());
				break;
			case None:
				sprite.draw(getInnerX(), getInnerY());
			}
			
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
		}
	}
	
	@Override protected boolean handleEvent(Event evt) {
		if (!this.enableEventHandling) return false;
		
		switch (evt.getType()) {
		case MOUSE_WHEEL:
			// do not eat mouse wheel events so that scrolling
			// from containing scroll panes works
			return false;
		default:
			return super.handleEvent(evt);
		}
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		minWidth = (short)themeInfo.getParameter("minWidth", 0);
		minHeight = (short)themeInfo.getParameter("minHeight", 0);
	}
	
	@Override public int getPreferredWidth() {
		int width = getBorderHorizontal();
		if (sprite != null) width += sprite.getWidth();

		return Math.max(width, minWidth);
	}
	
	@Override public int getPreferredHeight() {
		int height = getBorderVertical();
		if (sprite != null) height += sprite.getHeight();
		
		return Math.max(height, minHeight);
	}
}
