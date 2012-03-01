package net.sf.hale.editor;

import java.util.ArrayList;
import java.util.List;

import net.sf.hale.Game;
import net.sf.hale.Updateable;
import net.sf.hale.editor.widgets.ColorSelectorPopup;
import net.sf.hale.editor.widgets.IconSelectorPopup;
import net.sf.hale.entity.Creature;
import net.sf.hale.resource.SpriteManager;
import net.sf.hale.rules.Race;
import net.sf.hale.rules.Ruleset;
import net.sf.hale.rules.SubIcon;
import net.sf.hale.editor.widgets.SpriteViewer;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;

public class CosmeticEditor extends Widget implements Updateable {
	private Creature parent;
	
	private final List<String> excludePostfixes;
	
	private final ToggleButton genderMale;
	private final ToggleButton genderFemale;
	
	private final ToggleButton drawOnlyHandSubIcons;
	private final ToggleButton drawWithSubIcons;
	
	private final Label iconLabel;
	private final SpriteViewer iconViewer;
	private final Button setIcon;
	private final Button setIconColor;
	
	private final Label hairIconLabel;
	private final SpriteViewer hairIconViewer;
	private final Button setHairIcon;
	private final Button setHairIconColor;
	
	private final Label beardIconLabel;
	private final SpriteViewer beardIconViewer;
	private final Button setBeardIcon;
	private final Button setBeardIconColor;
	
	private final Button setSkinColor;
	private final Button setClothingColor;
	
	private final ColorSelectorPopup colorSelectorPopup;
	
	private final Label portraitLabel;
	private final SpriteViewer portraitViewer;
	private final Button setPortrait;
	
	private final Label appearanceLabel;
	
	public CosmeticEditor() {
		this.setTheme("");
		
		genderMale = new ToggleButton("Male");
		genderMale.setTheme("/togglebutton");
		genderMale.setPosition(5, 5);
		genderMale.setSize(40, 20);
		genderMale.addCallback(new Runnable() {
			@Override public void run() {
				genderMale.setActive(true);
				genderFemale.setActive(false);
				parent.setGender(Ruleset.Gender.Male);
				addParentSubIcons();
			}
		});
		this.add(genderMale);
		
		genderFemale = new ToggleButton("Female");
		genderFemale.setTheme("/togglebutton");
		genderFemale.setPosition(60, 5);
		genderFemale.setSize(40, 20);
		genderFemale.addCallback(new Runnable() {
			@Override public void run() {
				genderMale.setActive(false);
				genderFemale.setActive(true);
				parent.setGender(Ruleset.Gender.Female);
				addParentSubIcons();
			}
		});
		this.add(genderFemale);
		
		drawWithSubIcons = new ToggleButton("Draw With Sub Icons");
		drawWithSubIcons.setTooltipContent("Draw this creature using base racial plus equipment sub icons");
		drawWithSubIcons.setTheme("/radiobutton");
		drawWithSubIcons.setPosition(5, 35);
		drawWithSubIcons.setSize(130, 20);
		drawWithSubIcons.addCallback(new Runnable() {
			@Override public void run() {
				boolean active = drawWithSubIcons.isActive();
				
				parent.setDrawWithSubIcons(active);
				
				if (!active) {
					drawOnlyHandSubIcons.setActive(false);
				} else {
					addParentSubIcons();
				}
			}
		});
		this.add(drawWithSubIcons);
		
		drawOnlyHandSubIcons = new ToggleButton("Only Hand Sub Icons");
		drawOnlyHandSubIcons.setTooltipContent("Draw only weapon and shield sub icons");
		drawOnlyHandSubIcons.setTheme("/radiobutton");
		drawOnlyHandSubIcons.setPosition(5, 55);
		drawOnlyHandSubIcons.setSize(130, 20);
		drawOnlyHandSubIcons.addCallback(new Runnable() {
			@Override public void run() {
				boolean active = drawOnlyHandSubIcons.isActive();
				
				parent.setDrawOnlyHandSubIcons(active);
				addParentSubIcons();
			}
		});
		this.add(drawOnlyHandSubIcons);
		
		iconLabel = new Label("Icon");
		iconLabel.setTheme("/labelblack");
		iconLabel.setPosition(5, 80);
		this.add(iconLabel);
		
		iconViewer = new SpriteViewer(75, 75, 1);
		iconViewer.setPosition(5, 90);
		iconViewer.setSelected(true);
		iconViewer.setIconOffset(false);
		this.add(iconViewer);
		
		setIcon = new Button("Set");
		setIcon.setTheme("/button");
		setIcon.setPosition(85, 90);
		setIcon.setSize(40, 20);
		setIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(CosmeticEditor.this, "images/creatures", 75, false, 1);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (parent == null) return;
						
						parent.setIcon(icon);
						iconViewer.setSprite(SpriteManager.getSprite(icon));
						iconViewer.setSpriteColor(parent.getIconColor());
					}
				});
				popup.openPopupCentered();
			}
		});
		this.add(setIcon);
		
		colorSelectorPopup = new ColorSelectorPopup(this);
		
		setIconColor = new Button("Color");
		setIconColor.setTheme("/button");
		setIconColor.setPosition(85, 115);
		setIconColor.setSize(40, 20);
		setIconColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				colorSelectorPopup.setEntityIconColorOnAccept(parent);
				colorSelectorPopup.setSpriteViewerSpriteColorOnAccept(iconViewer);
				colorSelectorPopup.openPopupCentered();
			}
		});
		this.add(setIconColor);
		
		Button clearIcon = new Button("Clear");
		clearIcon.setTheme("/button");
		clearIcon.setPosition(85, 140);
		clearIcon.setSize(40, 20);
		clearIcon.addCallback(new Runnable() {
			@Override public void run() {
				parent.setIcon(null);
				iconViewer.setSprite(null);
			}
		});
		add(clearIcon);
		
		hairIconLabel = new Label("Hair Icon");
		hairIconLabel.setTheme("/labelblack");
		hairIconLabel.setPosition(5, 180);
		this.add(hairIconLabel);
		
		hairIconViewer = new SpriteViewer(75, 75, 1);
		hairIconViewer.setPosition(5, 190);
		hairIconViewer.setSelected(true);
		hairIconViewer.setIconOffset(false);
		this.add(hairIconViewer);
		
		setHairIcon = new Button("Set");
		setHairIcon.setTheme("/button");
		setHairIcon.setPosition(85, 200);
		setHairIcon.setSize(40, 20);
		setHairIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(CosmeticEditor.this, "images/subIcons", 45, true, 1, excludePostfixes, "hair");
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (parent == null) return;

						parent.setHairSubIcon(icon, parent.getHairColor());
						
						hairIconViewer.setSprite(SpriteManager.getSprite(icon));
						hairIconViewer.setSpriteColor(parent.getHairColor());
					}
				});
				popup.openPopupCentered();
			}
		});
		this.add(setHairIcon);
		
		setHairIconColor = new Button("Color");
		setHairIconColor.setTheme("/button");
		setHairIconColor.setPosition(85, 230);
		setHairIconColor.setSize(40, 20);
		setHairIconColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				
				colorSelectorPopup.setHairIconColorOnAccept(parent);
				colorSelectorPopup.setSpriteViewerSpriteColorOnAccept(hairIconViewer);
				colorSelectorPopup.openPopupCentered();
			}
		});
		this.add(setHairIconColor);
		
		beardIconLabel = new Label("Beard Icon");
		beardIconLabel.setTheme("/labelblack");
		beardIconLabel.setPosition(155, 180);
		this.add(beardIconLabel);
		
		beardIconViewer = new SpriteViewer(75, 75, 1);
		beardIconViewer.setPosition(155, 190);
		beardIconViewer.setSelected(true);
		beardIconViewer.setIconOffset(false);
		this.add(beardIconViewer);
		
		setBeardIcon = new Button("Set");
		setBeardIcon.setTheme("/button");
		setBeardIcon.setPosition(235, 200);
		setBeardIcon.setSize(40, 20);
		setBeardIcon.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(CosmeticEditor.this, "images/subIcons", 45, true, 1, excludePostfixes, "beard");
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (parent == null) return;

						parent.setBeardSubIcon(icon, parent.getSubIconColor(SubIcon.Type.Beard));
						
						beardIconViewer.setSprite(SpriteManager.getSprite(icon));
						beardIconViewer.setSpriteColor(parent.getSubIconColor(SubIcon.Type.Beard));
					}
				});
				popup.openPopupCentered();
			}
		});
		this.add(setBeardIcon);
		
		setBeardIconColor = new Button("Color");
		setBeardIconColor.setTheme("/button");
		setBeardIconColor.setPosition(235, 230);
		setBeardIconColor.setSize(40, 20);
		setBeardIconColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				
				colorSelectorPopup.addCallback(new ColorSelectorPopup.Callback() {
					@Override public void colorSelected(Color color) {
						if (parent == null) return;
						
						parent.setBeardSubIcon(parent.getSubIcon(SubIcon.Type.Beard), color);
						
						beardIconViewer.setSpriteColor(parent.getSubIconColor(SubIcon.Type.Beard));
					}
				});

				colorSelectorPopup.openPopupCentered();
			}
		});
		this.add(setBeardIconColor);
		
		setSkinColor = new Button("Set Skin Color");
		setSkinColor.setTheme("/button");
		setSkinColor.setSize(110, 20);
		setSkinColor.setPosition(320, 200);
		setSkinColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				
				colorSelectorPopup.addCallback(new ColorSelectorPopup.Callback() {
					@Override public void colorSelected(Color color) {
						if (parent == null) return;
						
						parent.getSubIcons().setSkinColor(color);
						
						addParentSubIcons();
					}
				});
				colorSelectorPopup.openPopupCentered();
			}
		});
		add(setSkinColor);
		
		setClothingColor = new Button("Set Clothing Color");
		setClothingColor.setTheme("/button");
		setClothingColor.setSize(110, 20);
		setClothingColor.setPosition(320, 230);
		setClothingColor.addCallback(new Runnable() {
			@Override public void run() {
				colorSelectorPopup.clearCallbacks();
				
				colorSelectorPopup.addCallback(new ColorSelectorPopup.Callback() {
					@Override public void colorSelected(Color color) {
						if (parent == null) return;
						
						parent.getSubIcons().setClothingColor(color);
						
						addParentSubIcons();
					}
				});
				colorSelectorPopup.openPopupCentered();
			}
		});
		add(setClothingColor);
		
		excludePostfixes = new ArrayList<String>();
		excludePostfixes.add(Ruleset.Gender.Female.toString() + ".png");
		excludePostfixes.add(Ruleset.Gender.Male.toString() + ".png");
		excludePostfixes.add(SubIcon.Type.OffHandWeapon.toString() + ".png");
		for (Race race : Game.ruleset.getAllRaces()) {
			if (race.isPlayerSelectable()) excludePostfixes.add(race.getName() + ".png");
		}
		
		portraitLabel = new Label("Portrait");
		portraitLabel.setTheme("/labelblack");
		portraitLabel.setPosition(155, 20);
		this.add(portraitLabel);
		
		portraitViewer = new SpriteViewer(100, 100, 2);
		portraitViewer.setPosition(155, 35);
		portraitViewer.setSelected(true);
		portraitViewer.setIconOffset(false);
		this.add(portraitViewer);
		
		setPortrait = new Button("Set");
		setPortrait.setTheme("/button");
		setPortrait.setPosition(210, 5);
		setPortrait.setSize(30, 20);
		setPortrait.addCallback(new Runnable() {
			@Override public void run() {
				IconSelectorPopup popup = new IconSelectorPopup(CosmeticEditor.this, "portraits", 50, false, 4);
				popup.setCallback(new IconSelectorPopup.Callback() {
					@Override public void iconSelected(String icon) {
						if (parent == null) return;
						
						parent.setPortrait(icon);
						portraitViewer.setSprite(SpriteManager.getPortrait(icon));
					}
				});
				popup.openPopupCentered();
			}
		});
		this.add(setPortrait);
		
		appearanceLabel = new Label("Appearance");
		appearanceLabel.setTheme("/labelblack");
		appearanceLabel.setPosition(320, 20);
		this.add(appearanceLabel);
	}
	
	@Override public int getMinHeight() {
		return 270;
	}
	
	public void setCreature(Creature parent) {
		this.parent = parent;
		
		update();
	}
	
	@Override public void update() {
		if (parent == null) return;
		
		if (parent.getIcon() != null) {
			iconViewer.setSprite(SpriteManager.getSprite(parent.getIcon()));
			iconViewer.setSpriteColor(parent.getIconColor());
		} else {
			iconViewer.setSprite(null);
		}
		
		if (parent.getPortrait() != null) {
			portraitViewer.setSprite(SpriteManager.getPortrait(parent.getPortrait()));
		} else {
			portraitViewer.setSprite(null);
		}
		
		if (parent.drawWithSubIcons() && parent.getHairIcon() != null) {
			hairIconViewer.setSprite(SpriteManager.getSprite(parent.getHairIcon()));
			hairIconViewer.setSpriteColor(parent.getHairColor());
		} else {
			hairIconViewer.setSprite(null);
		}
		
		if (parent.drawWithSubIcons() && parent.getSubIcon(SubIcon.Type.Beard) != null) {
			beardIconViewer.setSprite(SpriteManager.getSprite(parent.getSubIcon(SubIcon.Type.Beard)));
			beardIconViewer.setSpriteColor(parent.getSubIconColor(SubIcon.Type.Beard));
		} else {
			beardIconViewer.setSprite(null);
		}
		
		genderMale.setActive(parent.getGender() == Ruleset.Gender.Male);
		genderFemale.setActive(!genderMale.isActive());
		
		drawWithSubIcons.setActive(parent.drawWithSubIcons());
		drawOnlyHandSubIcons.setActive(parent.drawOnlyHandSubIcons());
	}
	
	@Override public void paintWidget(GUI gui) {
		if ( parent != null && (parent.getIcon() != null || parent.drawWithSubIcons()) ) {
			parent.draw(getInnerX() + 320, getInnerY() + 40);
		}
	}
	
	private void addParentSubIcons() {
		parent.reloadAllSubIcons();
	}
}
