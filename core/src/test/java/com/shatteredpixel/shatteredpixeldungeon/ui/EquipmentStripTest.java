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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EquipmentStripTest {

	@Test
	public void itemSourceContainsAllEquippedMiscsInOrder() {
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

		ArrayList<Item> items = EquipmentStrip.itemsFor(hero.belongings, false);

		assertEquals(6, items.size());
		assertSame(weapon, items.get(0));
		assertSame(armor, items.get(1));
		assertSame(first, items.get(2));
		assertSame(second, items.get(3));
		assertSame(third, items.get(4));
		assertSame(secondWeapon, items.get(5));
	}

	@Test
	public void itemSourceCanIncludeEmptyCoreEquipmentPlaceholders() {
		Hero hero = new Hero();

		ArrayList<Item> items = EquipmentStrip.itemsFor(hero.belongings, true);

		assertEquals(3, items.size());
		assertTrue(items.get(0) instanceof WndBag.Placeholder);
		assertTrue(items.get(1) instanceof WndBag.Placeholder);
		assertTrue(items.get(2) instanceof WndBag.Placeholder);
	}

	@Test
	public void horizontalScrollPaneCanBeSizedBeforeCameraIsAssigned() {
		HorizontalScrollPane pane = new TestHorizontalScrollPane(new com.watabou.noosa.ui.Component());

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
	public void equipmentStripCanResyncScrollCameraAfterParentCameraMoves() {
		Component parent = new Component();
		parent.camera = new Camera(10, 20, 100, 100, 1);

		TestEquipmentStrip strip = new TestEquipmentStrip();
		parent.add(strip);
		strip.setRect(5, 6, 40, 10);

		assertEquals(15, strip.contentCamera().x);
		assertEquals(26, strip.contentCamera().y);

		parent.camera.x = 30;
		parent.camera.y = 40;

		assertEquals(15, strip.contentCamera().x);
		assertEquals(26, strip.contentCamera().y);

		strip.syncScrollArea();

		assertEquals(35, strip.contentCamera().x);
		assertEquals(46, strip.contentCamera().y);
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

	@Test
	public void equipmentStripSlotsDoNotBlockScrollGestures() throws Exception {
		InventorySlot slot = blankInventorySlot();
		slot.hotArea = new PointerArea(0, 0, 0, 0);

		EquipmentStrip.prepareSlotForScrolling(slot);

		assertEquals(PointerArea.NEVER_BLOCK, slot.hotArea.blockLevel);
	}

	private static class TestHorizontalScrollPane extends HorizontalScrollPane {

		public TestHorizontalScrollPane(com.watabou.noosa.ui.Component content) {
			super(content);
		}

		@Override
		protected void createChildren() {
			// Avoid creating render-backed controls in the headless unit test.
		}

		@Override
		protected void layout() {
			if (camera() == null){
				return;
			}
			Point p = camera().cameraToScreen(x, y);
			content.camera.x = p.x;
			content.camera.y = p.y;
			content.camera.resize((int)width, (int)height);
		}

		@Override
		public void scrollTo(float x, float y) {
			content.camera.scroll.set(x, y);
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

	private static class TestEquipmentStrip extends EquipmentStrip {

		private Component testContent;

		public TestEquipmentStrip() {
			super(null, false, item -> null, 10, 10, 1);
		}

		@Override
		protected void createChildren() {
			testContent = new Component();
			content = testContent;
			scrollPane = new TestHorizontalScrollPane(content);
			add(scrollPane);
		}

		public Camera contentCamera() {
			return testContent.camera;
		}
	}

	private static ColorBlock blankColorBlock() throws Exception {
		return (ColorBlock) unsafe().allocateInstance(ColorBlock.class);
	}

	private static InventorySlot blankInventorySlot() throws Exception {
		return (InventorySlot) unsafe().allocateInstance(InventorySlot.class);
	}

	private static sun.misc.Unsafe unsafe() throws Exception {
		Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		return (sun.misc.Unsafe) field.get(null);
	}
}
