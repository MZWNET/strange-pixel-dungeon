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
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class EquipmentStrip extends Component {

	public interface SlotFactory {
		InventorySlot create(Item item);
	}

	private Belongings belongings;
	private boolean includePlaceholders;
	private SlotFactory slotFactory;
	private int slotWidth;
	private int slotHeight;
	private int gap;

	protected Component content;
	protected HorizontalScrollPane scrollPane;
	private ArrayList<InventorySlot> slots = new ArrayList<>();

	public EquipmentStrip(Belongings belongings, boolean includePlaceholders, SlotFactory slotFactory,
						  int slotWidth, int slotHeight, int gap) {
		super();
		this.belongings = belongings;
		this.includePlaceholders = includePlaceholders;
		this.slotFactory = slotFactory;
		this.slotWidth = slotWidth;
		this.slotHeight = slotHeight;
		this.gap = gap;
		refresh();
	}

	@Override
	protected void createChildren() {
		content = new Component();
		scrollPane = new HorizontalScrollPane(content);
		add(scrollPane);
	}

	public void belongings(Belongings belongings){
		this.belongings = belongings;
	}

	public void refresh(){
		if (content == null || slotFactory == null){
			return;
		}
		content.clear();
		slots.clear();

		ArrayList<Item> items = belongings == null ? new ArrayList<>() : itemsFor(belongings, includePlaceholders);
		float contentWidth = 0;
		for (Item item : items){
			InventorySlot slot = slotFactory.create(item);
			prepareSlotForScrolling(slot);
			content.add(slot);
			slots.add(slot);
			contentWidth += slotWidth + gap;
		}
		if (contentWidth > 0){
			contentWidth -= gap;
		}
		content.setSize(Math.max(width, contentWidth), slotHeight);
		layout();
	}

	public void syncScrollArea(){
		layout();
	}

	@Override
	protected void layout() {
		if (scrollPane == null || camera() == null){
			return;
		}
		scrollPane.setRect(x, y, width, height);

		float left = 0;
		for (InventorySlot slot : slots){
			slot.setRect(left, 0, slotWidth, slotHeight);
			left = slot.right() + gap;
		}
		content.setSize(Math.max(width, Math.max(0, left - gap)), slotHeight);
		scrollPane.scrollTo(scrollPane.content().camera.scroll.x, 0);
	}

	public void alpha(float value){
		for (InventorySlot slot : slots){
			slot.alpha(value);
		}
	}

	static void prepareSlotForScrolling(InventorySlot slot){
		if (slot != null && slot.hotArea != null){
			slot.hotArea.blockLevel = PointerArea.NEVER_BLOCK;
		}
	}

	public ArrayList<InventorySlot> slots(){
		return slots;
	}

	public static ArrayList<Item> itemsFor(Belongings belongings, boolean includePlaceholders){
		ArrayList<Item> items = new ArrayList<>();

		if (includePlaceholders || belongings.weapon() != null){
			items.add(belongings.weapon() == null ? new WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER) : belongings.weapon());
		}
		if (includePlaceholders || belongings.armor() != null){
			items.add(belongings.armor() == null ? new WndBag.Placeholder(ItemSpriteSheet.ARMOR_HOLDER) : belongings.armor());
		}

		ArrayList<KindofMisc> miscs = belongings.equippedMiscs();
		if (miscs.isEmpty()){
			if (includePlaceholders){
				items.add(new WndBag.Placeholder(ItemSpriteSheet.SOMETHING));
			}
		} else {
			items.addAll(miscs);
		}

		if (belongings.secondWep() != null){
			items.add(belongings.secondWep());
		}

		return items;
	}
}
