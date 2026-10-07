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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class SlotPane extends Component {

	public interface SlotFactory {
		InventorySlot create(Item item);
	}

	private boolean horizontal;
	private int slotWidth;
	private int slotHeight;
	private int gap;
	private SlotFactory slotFactory;

	private Component content;
	private ScrollPane scrollPane;
	private ArrayList<InventorySlot> slots = new ArrayList<>();

	public SlotPane(boolean horizontal, int slotWidth, int slotHeight, int gap, SlotFactory slotFactory) {
		super();
		this.horizontal = horizontal;
		this.slotWidth = slotWidth;
		this.slotHeight = slotHeight;
		this.gap = gap;
		this.slotFactory = slotFactory;

		content = new Component();
		scrollPane = horizontal ? new HorizontalScrollPane(content) : new ScrollPane(content);
		//otherwise the zoom keys never reach the map while the inventory pane is on screen
		KeyEvent.removeKeyListener(scrollPane.keyListener);
		add(scrollPane);
	}

	//new slots are only positioned by the next layout
	public void items(ArrayList<Item> items){
		while (slots.size() > items.size()){
			InventorySlot slot = slots.remove(slots.size() - 1);
			slot.destroy();
			content.remove(slot);
		}
		for (int i = 0; i < items.size(); i++){
			if (i < slots.size()){
				slots.get(i).item(items.get(i));
			} else {
				InventorySlot slot = slotFactory.create(items.get(i));
				slot.hotArea.blockLevel = PointerArea.NEVER_BLOCK;
				content.add(slot);
				slots.add(slot);
			}
		}
	}

	public ArrayList<InventorySlot> slots(){
		return slots;
	}

	public void scrollToTop(){
		scrollPane.scrollTo(0, 0);
	}

	public void alpha(float value){
		for (InventorySlot slot : slots){
			slot.alpha(value);
		}
	}

	@Override
	protected void layout() {
		//the inventory pane lays itself out once before the game scene gives it a camera
		if (camera() == null){
			return;
		}

		int columns = horizontal ? slots.size() : (int)((width + gap) / (slotWidth + gap));
		float contentWidth = 0;
		float contentHeight = 0;
		for (int i = 0; i < slots.size(); i++){
			InventorySlot slot = slots.get(i);
			slot.setRect((i % columns) * (slotWidth + gap), (i / columns) * (slotHeight + gap), slotWidth, slotHeight);
			contentWidth = Math.max(contentWidth, slot.right());
			contentHeight = Math.max(contentHeight, slot.bottom());
		}
		content.setSize(contentWidth, contentHeight);

		scrollPane.setRect(x, y, width, height);
		//clamps the old scroll position to the new content size
		scrollPane.scrollTo(content.camera.scroll.x, content.camera.scroll.y);
	}

	public static ArrayList<Item> equipment(Belongings belongings){
		ArrayList<Item> items = new ArrayList<>();

		items.add(belongings.weapon() == null ? new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER) : belongings.weapon());
		items.add(belongings.armor() == null ? new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER) : belongings.armor());

		ArrayList<KindofMisc> miscs = belongings.equippedMiscs();
		if (miscs.isEmpty()){
			items.add(new WndBag.Placeholder(ItemSpriteSheet.SOMETHING));
		} else {
			items.addAll(miscs);
		}

		if (belongings.secondWep() != null){
			items.add(belongings.secondWep());
		}

		return items;
	}

	public static ArrayList<Item> contents(Bag bag, int columns, int rows){
		ArrayList<Item> items = new ArrayList<>();

		//the container itself if it's not the root backpack
		if (!(bag instanceof Belongings.Backpack)){
			items.add(bag);
		}

		//other containers are shown as tabs instead
		for (Item item : bag.items){
			if (!(item instanceof Bag)){
				items.add(item);
			}
		}

		while (items.size() < columns * rows || items.size() % columns != 0){
			items.add(null);
		}

		return items;
	}
}
