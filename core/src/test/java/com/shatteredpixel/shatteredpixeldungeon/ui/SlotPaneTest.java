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

import com.shatteredpixel.shatteredpixeldungeon.TestItems;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.utils.PointF;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SlotPaneTest {

	@Test
	public void equipmentContainsAllEquippedMiscsInOrder() {
		Hero hero = new Hero();
		TestItems.TestWeapon weapon = new TestItems.TestWeapon();
		TestItems.TestArmor armor = new TestItems.TestArmor();
		TestItems.TestRing first = new TestItems.TestRing();
		TestItems.TestArtifact second = new TestItems.TestArtifact();
		TestItems.OtherTestRing third = new TestItems.OtherTestRing();
		TestItems.TestWeapon secondWeapon = new TestItems.TestWeapon();

		hero.belongings.weapon = weapon;
		hero.belongings.armor = armor;
		hero.belongings.equipMisc(first);
		hero.belongings.equipMisc(second);
		hero.belongings.equipMisc(third);
		hero.belongings.secondWep = secondWeapon;

		ArrayList<Item> items = SlotPane.equipment(hero.belongings);

		assertEquals(6, items.size());
		assertSame(weapon, items.get(0));
		assertSame(armor, items.get(1));
		assertSame(first, items.get(2));
		assertSame(second, items.get(3));
		assertSame(third, items.get(4));
		assertSame(secondWeapon, items.get(5));
	}

	@Test
	public void equipmentHasPlaceholdersForEmptyCoreSlots() {
		Hero hero = new Hero();

		ArrayList<Item> items = SlotPane.equipment(hero.belongings);

		assertEquals(3, items.size());
		assertTrue(items.get(0) instanceof WndBag.Placeholder);
		assertTrue(items.get(1) instanceof WndBag.Placeholder);
		assertTrue(items.get(2) instanceof WndBag.Placeholder);
	}

	@Test
	public void subBagContentsStartWithTheBagItself() {
		VelvetPouch pouch = new VelvetPouch();
		TestItems.TestWeapon item = new TestItems.TestWeapon();
		pouch.items.add(item);

		ArrayList<Item> slots = SlotPane.contents(pouch, 5, 4);

		assertSame(pouch, slots.get(0));
		assertSame(item, slots.get(1));
	}

	@Test
	public void backpackContentsSkipBagsShownAsTabs() {
		Belongings.Backpack backpack = new Belongings.Backpack();
		TestItems.TestWeapon item = new TestItems.TestWeapon();
		backpack.items.add(new VelvetPouch());
		backpack.items.add(item);

		ArrayList<Item> slots = SlotPane.contents(backpack, 5, 4);

		assertSame(item, slots.get(0));
		assertNull(slots.get(1));
	}

	@Test
	public void contentsFillTheVisibleRowsAndEndOnAFullRow() {
		Belongings.Backpack backpack = new Belongings.Backpack();

		assertEquals(20, SlotPane.contents(backpack, 5, 4).size());

		for (int i = 0; i < 21; i++){
			backpack.items.add(new TestItems.TestWeapon());
		}
		ArrayList<Item> slots = SlotPane.contents(backpack, 5, 4);

		assertEquals(25, slots.size());
		assertNotNull(slots.get(20));
		assertNull(slots.get(21));
	}

	@Test
	public void horizontalScrollPaneCanBeSizedBeforeCameraIsAssigned() {
		HorizontalScrollPane pane = new TestHorizontalScrollPane(new Component());

		pane.setRect(0, 0, 20, 20);
	}

	@Test
	public void horizontalScrollUsesNativeHorizontalAmountWhenPresent() {
		ScrollEvent event = new ScrollEvent(new PointF(0, 0), 7, 2);

		assertEquals(7, HorizontalScrollPane.horizontalAmount(event), 0);
		assertEquals(7, event.amountX, 0);
		assertEquals(2, event.amountY, 0);
	}

	@Test
	public void horizontalScrollFallsBackToVerticalWheelAmount() {
		ScrollEvent event = new ScrollEvent(new PointF(0, 0), 0, 2);

		assertEquals(2, HorizontalScrollPane.horizontalAmount(event), 0);
	}

	@Test
	public void horizontalDragStartCancelsPressedButton() throws Exception {
		int oldZoom = PixelScene.defaultZoom;
		PixelScene.defaultZoom = 1;

		try {
			Button button = new Button();
			Button.pressedButton = button;

			TestGestureHorizontalScrollPane pane = new TestGestureHorizontalScrollPane(new Component());
			pane.thumb = blankColorBlock();
			PointerEvent drag = new PointerEvent(0, 0, 1, PointerEvent.Type.DOWN);
			drag.start = new PointF(0, 0);
			drag.current = new PointF(20, 0);

			pane.controller.onDrag(drag);

			assertNull(Button.pressedButton);
		} finally {
			Button.pressedButton = null;
			PixelScene.defaultZoom = oldZoom;
		}
	}

	private static class TestHorizontalScrollPane extends HorizontalScrollPane {

		public TestHorizontalScrollPane(Component content) {
			super(content);
		}

		@Override
		protected void createChildren() {
			// Avoid creating render-backed controls in the headless unit test.
		}
	}

	private static class TestGestureHorizontalScrollPane extends HorizontalScrollPane {

		public TestGestureHorizontalScrollPane(Component content) {
			super(content);
		}

		@Override
		protected void createChildren() {
			controller = new HorizontalPointerController();
			add(controller);
		}
	}

	private static ColorBlock blankColorBlock() throws Exception {
		return (ColorBlock) unsafe().allocateInstance(ColorBlock.class);
	}

	private static sun.misc.Unsafe unsafe() throws Exception {
		Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (sun.misc.Unsafe) field.get(null);
	}
}
