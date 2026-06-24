/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collection;

public class WndDebug extends WndTabbed {

	private static final int WIDTH_P     = 126;
	private static final int HEIGHT_P    = 180;

	private static final int WIDTH_L     = 216;
	private static final int HEIGHT_L    = 130;

	private static final int BUTTON_HEIGHT = 18;
	private static final int INVULN_MIN_TURNS = 1;
	private static final int INVULN_MAX_TURNS = 99999;

	private static WndDebug instance;

	private ItemsTab itemsTab;
	private InvulnerabilityTab invulnerabilityTab;

	public WndDebug(){

		if (instance != null){
			instance.hide();
		}

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		int height = PixelScene.landscape() ? HEIGHT_L : HEIGHT_P;

		resize(width, height);

		itemsTab = new ItemsTab();
		add(itemsTab);
		itemsTab.setRect(0, 0, width, height);
		itemsTab.updateList();

		invulnerabilityTab = new InvulnerabilityTab();
		add(invulnerabilityTab);
		invulnerabilityTab.setRect(0, 0, width, height);
		invulnerabilityTab.updateStatus();

		Tab[] tabs = {
				new LabeledTab(Messages.get(WndDebug.class, "items_tab")){
					@Override
					protected void select(boolean value) {
						super.select(value);
						itemsTab.active = itemsTab.visible = value;
					}

					@Override
					protected String hoverText() {
						return Messages.get(WndDebug.class, "items_tab");
					}
				},
				new LabeledTab(Messages.get(WndDebug.class, "invulnerability_tab")){
					@Override
					protected void select(boolean value) {
						super.select(value);
						invulnerabilityTab.active = invulnerabilityTab.visible = value;
						if (value) {
							invulnerabilityTab.updateStatus();
						}
					}

					@Override
					protected String hoverText() {
						return Messages.get(WndDebug.class, "invulnerability_tab");
					}
				}
		};

		for (Tab tab : tabs){
			add(tab);
		}

		layoutTabs();
		select(0);

		instance = this;
	}

	@Override
	public void destroy() {
		super.destroy();
		if (instance == this){
			instance = null;
		}
	}

	static boolean isDebugSpawnAllowed(Class<?> itemClass){
		if (!Item.class.isAssignableFrom(itemClass)){
			return false;
		}

		Package itemPackage = itemClass.getPackage();
		String packageName = itemPackage == null ? "" : itemPackage.getName();
		if (packageName.contains(".items.keys") || packageName.contains(".items.quest")){
			return false;
		}

		return itemClass != Amulet.class
				&& itemClass != TengusMask.class
				&& itemClass != KingsCrown.class;
	}

	static Integer parseInvulnerabilityTurns(String text){
		try {
			int turns = Integer.parseInt(text.trim());
			if (turns >= INVULN_MIN_TURNS && turns <= INVULN_MAX_TURNS){
				return turns;
			}
		} catch (NumberFormatException ignored) {
			// Invalid debug input is handled by the caller with a game-log warning.
		}
		return null;
	}

	static Item createDebugItem(Class<?> itemClass){
		Item item = (Item)Reflection.newInstance(itemClass);
		if (item != null){
			item.cursed = false;
			item.cursedKnown = true;
		}
		return item;
	}

	private static void spawnItem(Class<?> itemClass){
		Item item = createDebugItem(itemClass);
		if (item == null || Dungeon.hero == null || Dungeon.level == null){
			return;
		}

		String itemName = Messages.titleCase(item.trueName());
		if (item.collect(Dungeon.hero.belongings.backpack)){
			GameScene.pickUp(item, Dungeon.hero.pos);
			GLog.p(Messages.get(WndDebug.class, "spawned", itemName));
		} else {
			Heap heap = Dungeon.level.drop(item, Dungeon.hero.pos);
			if (heap.sprite != null){
				heap.sprite.drop();
			}
			GLog.w(Messages.get(WndDebug.class, "dropped", itemName));
		}
	}

	private static ArrayList<Class<?>> allowedItems(Collection<Class<?>> classes){
		ArrayList<Class<?>> result = new ArrayList<>();
		for (Class<?> itemClass : classes){
			if (isDebugSpawnAllowed(itemClass)){
				result.add(itemClass);
			}
		}
		return result;
	}

	private static class ItemsTab extends Component {

		private ScrollingGridPane grid;

		@Override
		protected void createChildren() {
			grid = new ScrollingGridPane();
			add(grid);
		}

		@Override
		protected void layout() {
			super.layout();
			grid.setRect(x, y, width, height);
		}

		private void updateList(){
			grid.clear();
			grid.addHeader("_" + Messages.get(WndDebug.class, "items_title") + "_", 9, true);

			for (Catalog catalog : Catalog.equipmentCatalogs){
				addCatalog(catalog);
			}
			for (Catalog catalog : Catalog.consumableCatalogs){
				addCatalog(catalog);
			}

			grid.setRect(x, y, width, height);
		}

		private void addCatalog(Catalog catalog){
			ArrayList<Class<?>> items = allowedItems(catalog.items());
			if (items.isEmpty()){
				return;
			}

			grid.addHeader("_" + Messages.titleCase(catalog.title()) + "_:");
			for (Class<?> itemClass : items){
				Item item = createDebugItem(itemClass);
				if (item == null){
					continue;
				}

				ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(new ItemSprite(item)){
					@Override
					public boolean onClick(float x, float y) {
						if (inside(x, y)){
							spawnItem(itemClass);
							return true;
						} else {
							return false;
						}
					}
				};
				grid.addItem(gridItem);
			}
		}
	}

	private static class InvulnerabilityTab extends Component {

		private RenderedTextBlock title;
		private RenderedTextBlock body;
		private RenderedTextBlock status;
		private RedButton setButton;
		private RedButton clearButton;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(WndDebug.class, "invulnerability_title"), 9);
			title.hardlight(Window.TITLE_COLOR);
			add(title);

			body = PixelScene.renderTextBlock(Messages.get(WndDebug.class, "invulnerability_desc"), 6);
			add(body);

			status = PixelScene.renderTextBlock(7);
			add(status);

			setButton = new RedButton(Messages.get(WndDebug.class, "set_invulnerability")){
				@Override
				protected void onClick() {
					GameScene.show(new WndTextInput(
							Messages.get(WndDebug.class, "duration_title"),
							Messages.get(WndDebug.class, "duration_desc"),
							"",
							5,
							false,
							Messages.get(WndDebug.class, "apply"),
							Messages.get(WndDebug.class, "cancel")){
						@Override
						public void onSelect(boolean positive, String text) {
							if (!positive){
								return;
							}

							Integer turns = parseInvulnerabilityTurns(text);
							if (turns == null){
								GLog.w(Messages.get(WndDebug.class, "invalid_duration", INVULN_MIN_TURNS, INVULN_MAX_TURNS));
								return;
							}

							Buff.prolong(Dungeon.hero, Invulnerability.class, turns);
							GLog.p(Messages.get(WndDebug.class, "invulnerability_set", turns));
							updateStatus();
						}
					});
				}
			};
			add(setButton);

			clearButton = new RedButton(Messages.get(WndDebug.class, "clear_invulnerability")){
				@Override
				protected void onClick() {
					if (Dungeon.hero != null){
						Buff.detach(Dungeon.hero, Invulnerability.class);
					}
					GLog.w(Messages.get(WndDebug.class, "invulnerability_cleared"));
					updateStatus();
				}
			};
			add(clearButton);
		}

		@Override
		protected void layout() {
			super.layout();

			float top = y + 2;

			title.maxWidth((int)width);
			title.setPos(x + (width - title.width()) / 2f, top);
			PixelScene.align(title);
			top = title.bottom() + 4;

			body.maxWidth((int)width);
			body.setPos(x, top);
			top = body.bottom() + 6;

			status.maxWidth((int)width);
			status.setPos(x, top);
			top = status.bottom() + 6;

			setButton.setRect(x, top, width, BUTTON_HEIGHT);
			top = setButton.bottom() + 2;

			clearButton.setRect(x, top, width, BUTTON_HEIGHT);
		}

		private void updateStatus(){
			if (status == null){
				return;
			}

			Invulnerability buff = Dungeon.hero == null ? null : Dungeon.hero.buff(Invulnerability.class);
			if (buff == null){
				status.text(Messages.get(WndDebug.class, "invulnerability_inactive"));
			} else {
				status.text(Messages.get(WndDebug.class, "invulnerability_active", (int)buff.visualcooldown()));
			}
			layout();
		}
	}
}
