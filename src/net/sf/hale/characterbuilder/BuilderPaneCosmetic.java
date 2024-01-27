/*
 package net.sf.hale.characterbuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.sf.hale.Game;
import net.sf.hale.entity.Creature;
import net.sf.hale.entity.PC;
import net.sf.hale.icon.IconFactory;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.widgets.BasePortraitViewer;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 * The BuilderPane for editing cosmetic aspects of the Buildable character:
 * name, gender, portrait, hair, and hair color
 * @author Jared Stephen
 */

public class BuilderPaneCosmetic extends AbstractBuilderPane implements PortraitSelector.Callback {
	private static final Color DEFAULT_CLOTHING_COLOR = Color.GREEN;
	
	private Widget appearanceHolder;
	private CharacterViewer characterViewer;
	private PortraitViewer portraitViewer;
	
	private int gap, smallGap;
	private String noNameMessage, noGenderMessage, noPortraitMessage;
	
	private PC workingCopy;
	private Race workingRace;
	private boolean[] beardsValid, hairValid;
	
	private Label nameLabel;
	private EditField nameField;
	private Button randomName;
	
	private Label genderLabel;
	private List<GenderSelector> genderSelectors;
	
	private int hairStyleMax;
	private Color currentHairColor;
	private int currentHairStyle;
	private final NumberFormat numberFormat;
	
	private Label hairLabel;
	private Label hairStyleLabel;
	private Button prevHairStyle, nextHairStyle;
	private ColorPicker hairColorPicker;
	
	private int beardStyleMax;
	private Color currentBeardColor;
	private int currentBeardStyle;
	
	private Label beardLabel;
	private Label beardStyleLabel;
	private Button prevBeardStyle, nextBeardStyle;
	private ColorPicker beardColorPicker;
	
	private Label skinLabel;
	private ColorPicker skinColorPicker;
	private Color currentSkinColor;
	
	private Label clothingLabel;
	private ColorPicker clothingColorPicker;
	private Color currentClothingColor;
	
	private Label portraitLabel;
	private Button choosePortrait;
	
	/**
	 * Create a new BuilderPaneCosmetic editing the specified character
	 * @param builder the parent CharacterBuilder
	 * @param character the Buildable character being edited
	 */
	
	public BuilderPaneCosmetic(CharacterBuilder builder, Buildable character) {
		super(builder, "Cosmetic", character);
		
		numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumIntegerDigits(3);
		numberFormat.setMaximumIntegerDigits(3);

		this.hairStyleMax = determineMaxFeaturestyles();
        hairValid = new boolean[hairStyleMax + 1];

        this.beardStyleMax = determineMaxFeaturestyles();
        beardsValid = new boolean[beardStyleMax + 1];

		beardsValid = new boolean[beardStyleMax + 1];


		addAppearanceWidgets();
        addNameWidgets();
        addGenderWidgets();
        addHairstyleWidgets();
        addBeardWidgets();
        addSkinWidgets();
        addClothingWidgets();
        addPortraitWidgets();
        setupAfterAddingWidgets();

	}

	@Override public void next() {
		getCharacterBuilder().finish();
	}
	
	@Override protected void applyTheme(ThemeInfo themeInfo) {
		super.applyTheme(themeInfo);
		
		smallGap = themeInfo.getParameter("smallgap", 0);
		gap = themeInfo.getParameter("gap", 0);
		
		noNameMessage = themeInfo.getParameter("nonamemessage", (String)null);
		noGenderMessage = themeInfo.getParameter("nogendermessage", (String)null);
		noPortraitMessage = themeInfo.getParameter("noportraitmessage", (String)null);
	}
	
	@Override protected void layout() {
		super.layout();
		
		Label[] labels = { nameLabel, genderLabel, hairLabel, beardLabel, skinLabel, portraitLabel, clothingLabel };

		for (Label label : labels) {
		    label.setSize(label.getPreferredWidth(), label.getPreferredHeight());
		}

		int labelColumnWidth = 0;

		for (Label label : labels) {
		    labelColumnWidth = Math.max(labelColumnWidth, label.getWidth());
		}

		labelColumnWidth += gap;

		
		// layout content column
		int contentColumnX = getInnerX() + labelColumnWidth + gap;
		
		// layout name field
		nameField.setSize(nameField.getPreferredWidth(), nameField.getPreferredHeight());
		nameField.setPosition(contentColumnX, getInnerY());
		int nameCenterY = nameField.getY() + nameField.getHeight() / 2;
		
		randomName.setSize(randomName.getPreferredWidth(), randomName.getPreferredHeight());
		randomName.setPosition(nameField.getRight() + smallGap, nameCenterY - randomName.getHeight() / 2);
		
		int curY = nameField.getBottom() + gap;
		
		// layout gender selectors
		int curX = contentColumnX;
		for (GenderSelector g : genderSelectors) {
			g.setSize(g.getPreferredWidth(), g.getPreferredHeight());
			g.setPosition(curX, curY);
			
			curX = g.getRight() + smallGap;
		}
		int genderCenterY = genderSelectors.get(0).getY() + genderSelectors.get(0).getHeight() / 2;
		
		int hairCenterY = genderSelectors.get(0).getBottom() + gap + hairColorPicker.getHeight() / 2;
		layoutSelectorWidgets(contentColumnX,hairColorPicker, hairStyleLabel, prevHairStyle, nextHairStyle, hairCenterY);

		int beardCenterY = hairColorPicker.getBottom() + gap + beardColorPicker.getHeight() / 2;
		layoutSelectorWidgets(contentColumnX,beardColorPicker, beardStyleLabel, prevBeardStyle, nextBeardStyle, beardCenterY);
		
		// layout skin selector
		skinColorPicker.setSize(skinColorPicker.getPreferredWidth(), skinColorPicker.getPreferredHeight());
		int skinCenterY = beardColorPicker.getBottom() + gap + skinColorPicker.getHeight() / 2;
		skinColorPicker.setPosition(contentColumnX, skinCenterY - skinColorPicker.getHeight() / 2);
		
		// layout clothing selector
		clothingColorPicker.setSize(clothingColorPicker.getPreferredWidth(), clothingColorPicker.getPreferredHeight());
		int clothingCenterY = skinColorPicker.getBottom() + gap + clothingColorPicker.getHeight() / 2;
		clothingColorPicker.setPosition(contentColumnX, clothingCenterY - clothingColorPicker.getHeight() / 2);
		
		// layout portrait selector
		choosePortrait.setSize(choosePortrait.getPreferredWidth(), choosePortrait.getPreferredHeight());
		int portraitCenterY = clothingColorPicker.getBottom() + gap + choosePortrait.getHeight() / 2;
		choosePortrait.setPosition(contentColumnX, portraitCenterY - choosePortrait.getHeight() / 2);
		
		// layout labels column
		nameLabel.setPosition(contentColumnX - gap - nameLabel.getWidth(),
				nameCenterY - nameLabel.getHeight() / 2);
		genderLabel.setPosition(contentColumnX - gap - genderLabel.getWidth(),
				genderCenterY - genderLabel.getHeight() / 2);
		hairLabel.setPosition(contentColumnX - gap - hairLabel.getWidth(),
				hairCenterY - hairLabel.getHeight() / 2);
		beardLabel.setPosition(contentColumnX - gap - beardLabel.getWidth(),
				beardCenterY - beardLabel.getHeight() / 2);
		skinLabel.setPosition(contentColumnX - gap - skinLabel.getWidth(),
				skinCenterY - skinLabel.getHeight() / 2);
		clothingLabel.setPosition(contentColumnX - gap - clothingLabel.getWidth(),
				clothingCenterY - clothingLabel.getHeight() / 2);
		portraitLabel.setPosition(contentColumnX - gap - portraitLabel.getWidth(),
				portraitCenterY - portraitLabel.getHeight() / 2);
		
		// layout appearance holder
		appearanceHolder.setSize(appearanceHolder.getPreferredWidth(), appearanceHolder.getPreferredHeight());
		
		// determine farther x we have layed out so far
		int maxX = Math.max(genderSelectors.get(genderSelectors.size() -1).getRight(), nameField.getRight());
		maxX = Math.max(beardColorPicker.getRight(), maxX);
		maxX = Math.max(hairColorPicker.getRight(), maxX);
		maxX = Math.max(skinColorPicker.getRight(), maxX);
		maxX = Math.max(clothingColorPicker.getRight(), maxX);
		maxX = Math.max(choosePortrait.getRight(), maxX);
		
		int centerX = (getInnerRight() + maxX) / 2;
		
		appearanceHolder.setPosition(centerX - appearanceHolder.getWidth() / 2, getInnerY() + gap);
	}
	
	private void updateWorkingCopy() {
		workingCopy = getCharacter().getWorkingCopy();
		characterViewer.character = workingCopy;
		
		portraitViewer.setCreature(workingCopy);
		
		// set next button state
		Button next = getNextButton();
		
		if (nameField.getTextLength() == 0) {
			next.setTooltipContent(noNameMessage);
			next.setEnabled(false);
		} else if (getCharacter().getSelectedGender() == null) {
			next.setTooltipContent(noGenderMessage);
			next.setEnabled(false);
		} else if (getCharacter().getSelectedPortrait() == null) {
			next.setTooltipContent(noPortraitMessage);
			next.setEnabled(false);
		} else {
			next.setTooltipContent(null);
			next.setEnabled(true);
		}
		
		// set editing buttons state
		boolean editEnabled = getCharacter().getSelectedGender() != null;
		boolean hasBeard = getCharacter().getSelectedGender() != Ruleset.Gender.Female;
		
		if (beardColorPicker != null) beardColorPicker.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
		if (hairColorPicker != null) hairColorPicker.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
		
		nextHairStyle.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
		prevHairStyle.setEnabled(editEnabled && getCharacter().getSelectedRace().hasHair());
		nextBeardStyle.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
		prevBeardStyle.setEnabled(editEnabled && hasBeard && getCharacter().getSelectedRace().hasBeard());
		choosePortrait.setEnabled(editEnabled);
		
		if (skinColorPicker != null) skinColorPicker.setEnabled(editEnabled);
		clothingColorPicker.setEnabled(editEnabled);
		
		portraitViewer.setVisible(getCharacter().getSelectedPortrait() != null);
		
		invalidateLayout();
	}
	
	private void createHairBeardAndSkinPickers() {
		removeIfExists(hairColorPicker);
	    removeIfExists(skinColorPicker);
	    removeIfExists(beardColorPicker);
		
	    List<String> hairBeardColorsList = workingRace.getHairAndBeardColors();
	    Color[] hairBeardColors = convertStringListToColorArray(hairBeardColorsList);

	    List<String> skinColorsList = workingRace.getSkinColors();
	    Color[] skinColors = convertStringListToColorArray(skinColorsList);

		
	    setupColorPicker(hairColorPicker, hairBeardColors, "haircolorpicker", this::setHairColor,
	            () -> getCharacter().getSelectedHairColor());

	    setupColorPicker(beardColorPicker, hairBeardColors, "beardcolorpicker", this::setBeardColor,
	            () -> getCharacter().getSelectedBeardColor());

	    setupColorPicker(skinColorPicker, skinColors, "skincolorpicker", this::setSkinColor,
	            () -> getCharacter().getSelectedSkinColor());

		
	    setValidIndices(hairValid, workingRace.getSelectableHairIndices());
	    setValidIndices(beardsValid, workingRace.getSelectableBeardIndices());
	}
	
	private void removeIfExists(ColorPicker picker) {
	    if (picker != null) {
	        this.removeChild(picker);
	    }
	}
	
	@Override protected void updateCharacter() {
		// reset the fields as needed
		if (getCharacter().getSelectedRace() != workingRace) {
			workingRace = getCharacter().getSelectedRace();
			createHairBeardAndSkinPickers();
		}
		
		if (getCharacter().getSelectedName() == null)
			nameField.setText("");
		
		if (getCharacter().getSelectedGender() == null) {
			for (GenderSelector g : genderSelectors) {
				g.setSelected(false);
			}
		}
		
		if (getCharacter().getSelectedHairIcon() == null) {
			currentHairStyle = workingRace.getDefaultHairIndex();
			currentHairColor = Color.parserColor(workingRace.getDefaultHairColor());
		}
		
		if (getCharacter().getSelectedBeardIcon() == null) {
			currentBeardStyle = workingRace.getDefaultBeardIndex();
			currentBeardColor = Color.parserColor(workingRace.getDefaultBeardColor());
		}
		
		if (getCharacter().getSelectedSkinColor() == null) {
			currentSkinColor = Color.parserColor(workingRace.getDefaultSkinColor());
		}
		
		if (getCharacter().getSelectedClothingColor() == null) {
			currentClothingColor = DEFAULT_CLOTHING_COLOR;
		}
		
		updateWorkingCopy();
	}
	
	@Override public void portraitSelected(String portrait) {
		getCharacter().setSelectedPortrait(portrait);
		
		updateWorkingCopy();
	}
	
	private void setGender(Ruleset.Gender gender) {
		getCharacter().setSelectedGender(gender);

		getCharacter().setSelectedSkinColor(currentSkinColor);
		getCharacter().setSelectedClothingColor(currentClothingColor);
		
		getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));
		getCharacter().setSelectedHairColor(currentHairColor);
		
		if (gender == Ruleset.Gender.Female) {
			getCharacter().setSelectedBeardIcon(null);
			getCharacter().setSelectedBeardColor(null);
			currentBeardStyle = getCharacter().getSelectedRace().getDefaultBeardIndex();
		} else {
			getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));
			getCharacter().setSelectedBeardColor(currentBeardColor);
		}
		
		updateWorkingCopy();
	}

	
	private void setSkinColor(Color color) {
		currentSkinColor = color;
		
		getCharacter().setSelectedSkinColor(color);
		
		updateWorkingCopy();
	}
	
	private void setHairColor(Color color) {
		currentHairColor = color;
		
		getCharacter().setSelectedHairColor(color);
		
		updateWorkingCopy();
	}
	
	private void setBeardColor(Color color) {
		currentBeardColor = color;
		
		getCharacter().setSelectedBeardColor(color);
		
		updateWorkingCopy();
	}
	
	private void previousBeardStyle() {
		int startStyle = currentBeardStyle;
		do {
			currentBeardStyle--;
			if (currentBeardStyle == 0) currentBeardStyle = beardStyleMax;
		} while (!beardsValid[currentBeardStyle] && currentBeardStyle != startStyle);
		
		getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));
		
		updateWorkingCopy();
	}
	
	private void nextBeardStyle() {
		int startStyle = currentBeardStyle;
		do {
			currentBeardStyle++;
			if (currentBeardStyle > beardStyleMax) currentBeardStyle = 1;
		} while (!beardsValid[currentBeardStyle] && currentBeardStyle != startStyle);
		
		getCharacter().setSelectedBeardIcon("subIcons/beard" + numberFormat.format(currentBeardStyle));
		
		updateWorkingCopy();
	}
	
	private void previousHairStyle() {
		int startStyle = currentHairStyle;
		do {
			currentHairStyle--;
			if (currentHairStyle == 0) currentHairStyle = hairStyleMax;
		} while (!hairValid[currentHairStyle] && currentHairStyle != startStyle);
		
		getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));
		
		updateWorkingCopy();
	}
	
	private void nextHairStyle() {
		int startStyle = currentHairStyle;
		do {
			currentHairStyle++;
			if (currentHairStyle > hairStyleMax) currentHairStyle = 1;
		} while (!hairValid[currentHairStyle] && currentHairStyle != startStyle);
		
		getCharacter().setSelectedHairIcon("subIcons/hair" + numberFormat.format(currentHairStyle));
		
		updateWorkingCopy();
	}
	
	
	private int determineMaxFeaturestyles()
    {
        int i = 1;
        String currentValue = null;

        do {
		    currentValue = "subIcons/feature" + numberFormat.format(i);
		    i++;
	    } while(SpriteManager.hasSprite(currentValue));

        return  i - 2;
    }  

	private void addAppearanceWidgets()
	{
	    appearanceHolder = new AppearanceArea();
		add(appearanceHolder);

		characterViewer = new CharacterViewer();
		characterViewer.setTheme("characterviewer");
		appearanceHolder.add(characterViewer);

		portraitViewer = new PortraitViewer(workingCopy);
		appearanceHolder.add(portraitViewer);
	}

	
	private void addNameWidgets()
	{
	    nameLabel = new Label("Name");
			nameLabel.setTheme("namelabel");
			add(nameLabel);
	}

	private void addGenderWidgets()
	{
	    genderLabel = new Label("Gender");
			genderLabel.setTheme("genderlabel");
			add(genderLabel);
	}

	private void addHairstyleWidgets()
	{
	    hairLabel = new Label("Hair");
			hairLabel.setTheme("hairlabel");
			add(hairLabel);
			
			hairStyleLabel = new Label("Style");
			hairStyleLabel.setTheme("hairstylelabel");
			add(hairStyleLabel);
			
			prevHairStyle = new Button("<");
			prevHairStyle.addCallback(new Runnable() {
				@Override public void run() {
					previousHairStyle();
				}
			});
			prevHairStyle.setTheme("prevhairstyle");
			add(prevHairStyle);
			
			nextHairStyle = new Button(">");
			nextHairStyle.addCallback(new Runnable() {
				@Override public void run() {
					nextHairStyle();
				}
			});
			nextHairStyle.setTheme("nexthairstyle");
			add(nextHairStyle);
	}

	private void addBeardWidgets()
	{
	    beardLabel = new Label("Beard");
			beardLabel.setTheme("beardlabel");
			add(beardLabel);
			
			beardStyleLabel = new Label("Style");
			beardStyleLabel.setTheme("beardstylelabel");
			add(beardStyleLabel);
			
			prevBeardStyle = new Button("<");
			prevBeardStyle.addCallback(new Runnable() {
				@Override public void run() {
					previousBeardStyle();
				}
			});
			prevBeardStyle.setTheme("prevbeardstyle");
			add(prevBeardStyle);
			
			nextBeardStyle = new Button(">");
			nextBeardStyle.addCallback(new Runnable() {
				@Override public void run() {
					nextBeardStyle();
				}
			});
			nextBeardStyle.setTheme("nextbeardstyle");
			add(nextBeardStyle);
	}

	private void addSkinWidgets()
	{
	    skinLabel = new Label("Skin");
			skinLabel.setTheme("skinlabel");
			add(skinLabel);
	}

	private void addClothingWidgets()
	{
	    clothingLabel = new Label("Clothing");
			clothingLabel.setTheme("clothinglabel");
			add(clothingLabel);
	}

	private void addPortraitWidgets()
	{
	    portraitLabel = new Label("Portrait");
			portraitLabel.setTheme("portraitlabel");
			add(portraitLabel);
	}
	
	private void setupAfterAddingWidgets() {
	    getNextButton().setText("Finish & Save");
	    updateWorkingCopy();
	}
	
	private Color[] convertStringListToColorArray(List<String> colorList) {
	    Color[] colors = new Color[colorList.size()];
	    for (int i = 0; i < colorList.size(); i++) {
	        colors[i] = Color.parserColor(colorList.get(i));
	    }
	    return colors;
	}

	private void setupColorPicker(ColorPicker colorPicker, Color[] colorArray, String theme,
            Consumer<Color> setColorFunction, Supplier<Color> getCurrentColorFunction) {
			colorPicker = new ColorPicker(new ColorCallback() {
				@Override
				public void colorSet(Color color) {
					setColorFunction.accept(color);
				}

				@Override
				public Color getCurrentColor() {
					return getCurrentColorFunction.get();
				}
			}, colorArray);

			colorPicker.setTheme(theme);
			add(colorPicker);
		}

	private void setValidIndices(boolean[] validArray, List<Integer> selectableIndices) {
	    Arrays.fill(validArray, false);
	    for (int index : selectableIndices) {
	        validArray[index] = true;
	    }
	}
	
	private void layoutSelectorWidgets(int contentColumnX,ColorPicker colorPicker, Label styleLabel, Button prevStyle, Button nextStyle,int centerY) {
			colorPicker.setSize(colorPicker.getPreferredWidth(), colorPicker.getPreferredHeight());

			styleLabel.setSize(styleLabel.getPreferredWidth(), styleLabel.getPreferredHeight());
			prevStyle.setSize(prevStyle.getPreferredWidth(), styleLabel.getPreferredHeight());
			nextStyle.setSize(nextStyle.getPreferredWidth(), styleLabel.getPreferredHeight());

			prevStyle.setPosition(contentColumnX, centerY - prevStyle.getHeight() / 2);
			styleLabel.setPosition(prevStyle.getRight() + smallGap, centerY - styleLabel.getHeight() / 2);
			nextStyle.setPosition(styleLabel.getRight() + smallGap, centerY - nextStyle.getHeight() / 2);
			colorPicker.setPosition(nextStyle.getRight() + smallGap, centerY - colorPicker.getHeight() / 2);
	}
	
	private class AppearanceArea extends Widget {
		@Override public int getPreferredWidth() {
			return getBorderHorizontal() + portraitViewer.getPreferredWidth() +
					characterViewer.getPreferredWidth();
		}
		
		@Override public int getPreferredHeight() {
			return Math.max(portraitViewer.getPreferredHeight(), characterViewer.getPreferredHeight()) +
					getBorderVertical();
		}
		
		@Override protected void layout() {
			portraitViewer.setSize(portraitViewer.getPreferredWidth(), portraitViewer.getPreferredHeight());
			portraitViewer.setPosition(getInnerX(), getInnerY());
			
			characterViewer.setSize(characterViewer.getPreferredWidth(), characterViewer.getPreferredHeight());
			characterViewer.setPosition(portraitViewer.getRight(),
					getInnerY() + getInnerHeight() / 2 - characterViewer.getHeight() / 2);
		}
	}
	
	private class PortraitViewer extends BasePortraitViewer {
		private PortraitViewer(Creature creature) {
			super(creature);
		}
		
		@Override public int getPreferredWidth() {
			return 100 + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return 100 + getBorderVertical();
		}
	}
	
	private class GenderSelector extends BuildablePropertySelector {
		private Ruleset.Gender gender;
		
		private GenderSelector(Ruleset.Gender gender, String icon) {
			super(gender.toString(), IconFactory.createIcon(icon), false);
			this.gender = gender;
			this.setSelectable(true);
		}
		
		@Override protected void onMouseClick() {
			setGender(gender);
			
			for (GenderSelector g : genderSelectors) {
				g.setSelected(false);
			}
			
			this.setSelected(true);
		}
	}
	
	private class ColorPicker extends Widget {
		private int numColumns;
		private final ColorCallback callback;
		
		private List<ColorButton> colorButtons;
		private Button otherColorButton;
		
		private ColorPicker(ColorCallback callback, Color[] buttonColors) {
			this.callback = callback;
			
			colorButtons = new ArrayList<ColorButton>();
			for (Color color : buttonColors) {
				colorButtons.add( new ColorButton(color, callback) );
			}
			
			otherColorButton = new Button("Other...");
			otherColorButton.addCallback(new Runnable() {
				@Override public void run() {
					ColorSelectorPopup popup = new ColorSelectorPopup(BuilderPaneCosmetic.this);
					popup.addCallback(new ColorSelectorPopup.Callback() {
						@Override public void colorSelected(Color color) {
							ColorPicker.this.callback.colorSet(color);
						}
					});
					popup.setColor(ColorPicker.this.callback.getCurrentColor());
					popup.openPopupCentered();
				}
			});
			otherColorButton.setTheme("othercolorbutton");
			add(otherColorButton);
			
			for (ColorButton button : colorButtons) {
				add(button);
			}
		}
		
		@Override protected void layout() {
			int curX = getInnerX();
			int curY = getInnerY();
			int count = 0;
			
			for (ColorButton button : colorButtons) {
				button.setSize(button.getPreferredWidth(), button.getPreferredHeight());
				button.setPosition(curX, curY);
				
				curX = button.getRight();
				
				count++;
				
				if (count == numColumns) {
					count = 0;
					curX = getInnerX();
					curY = button.getBottom();
				}
			}
			
			otherColorButton.setSize(getInnerRight() - curX, colorButtons.get(0).getPreferredHeight());
			otherColorButton.setPosition(curX, curY);
		}
		
		@Override protected void applyTheme(ThemeInfo themeInfo) {
			super.applyTheme(themeInfo);
			
			this.numColumns = themeInfo.getParameter("numcolumns", 0);
		}
		
		@Override public int getPreferredWidth() {
			return colorButtons.get(0).getPreferredWidth() * numColumns + getBorderHorizontal();
		}
		
		@Override public int getPreferredHeight() {
			return colorButtons.get(0).getPreferredHeight() * ((colorButtons.size() / numColumns) + 1);
		}
	}
	
	private interface ColorCallback {
		public void colorSet(Color color);
		
		public Color getCurrentColor();
	}
	
	private class ColorButton extends Button implements Runnable {
		private Color color;
		private ColorCallback callback;
		
		private ColorButton(Color color, ColorCallback callback) {
			setTintAnimator(new TintAnimator(new TintAnimator.GUITimeSource(this), color));
			addCallback(this);
			
			this.color = color;
			this.callback = callback;
		}
		
		@Override public void run() {
			callback.colorSet(color);
		}
	}
	
	private class CharacterViewer extends Widget {
		private PC character;
		
		@Override protected void paintWidget(GUI gui) {
			if (character != null) {
				character.uiDraw(getInnerX(), getInnerY());
			}
		}
		
		@Override public int getPreferredInnerWidth() {
			return Game.TILE_SIZE;
		}
		
		@Override public int getPreferredInnerHeight() {
			return Game.TILE_SIZE;
		}
	}
}