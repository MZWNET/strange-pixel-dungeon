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

import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.CeremonialCandle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WndDebugTest {

	@Test
	public void itemFilterAllowsOrdinaryItems() {
		assertTrue(WndDebug.isDebugSpawnAllowed(Food.class));
	}

	@Test
	public void itemFilterBlocksProgressionAndNonItemCatalogEntries() {
		assertFalse(WndDebug.isDebugSpawnAllowed(IronKey.class));
		assertFalse(WndDebug.isDebugSpawnAllowed(CeremonialCandle.class));
		assertFalse(WndDebug.isDebugSpawnAllowed(Amulet.class));
		assertFalse(WndDebug.isDebugSpawnAllowed(TengusMask.class));
		assertFalse(WndDebug.isDebugSpawnAllowed(KingsCrown.class));
		assertFalse(WndDebug.isDebugSpawnAllowed(Weapon.Enchantment.class));
	}

	@Test
	public void invulnerabilityTurnsParseOnlyAcceptedRange() {
		assertEquals(Integer.valueOf(1), WndDebug.parseInvulnerabilityTurns("1"));
		assertEquals(Integer.valueOf(99999), WndDebug.parseInvulnerabilityTurns("99999"));
		assertEquals(Integer.valueOf(10), WndDebug.parseInvulnerabilityTurns(" 10 "));
		assertNull(WndDebug.parseInvulnerabilityTurns("0"));
		assertNull(WndDebug.parseInvulnerabilityTurns("100000"));
		assertNull(WndDebug.parseInvulnerabilityTurns("ten"));
		assertNull(WndDebug.parseInvulnerabilityTurns(""));
	}

	@Test
	public void debugCreatedItemsAreNeverCursed() {
		Item item = WndDebug.createDebugItem(CursedDebugItem.class);

		assertFalse(item.cursed);
		assertTrue(item.cursedKnown);
	}

	public static class CursedDebugItem extends Item {
		{
			cursed = true;
			cursedKnown = false;
		}
	}
}
