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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.TestItems;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RingCombinedBonusTest {

	@Test
	public void combinedBonusCountsEveryEquippedRingOfTheSameClass() {
		Hero hero = new Hero();
		Dungeon.hero = hero;
		TestItems.TestRing baseRing = new TestItems.TestRing();
		TestItems.TestRing upgradedRing = new TestItems.TestRing();
		TestItems.OtherTestRing otherRing = new TestItems.OtherTestRing();

		upgradedRing.level(2);
		otherRing.level(5);

		hero.belongings.equipMisc(baseRing);
		hero.belongings.equipMisc(upgradedRing);
		hero.belongings.equipMisc(otherRing);

		assertEquals(baseRing.soloBonus() + upgradedRing.soloBonus(), baseRing.combinedBonus(hero));
		assertEquals(baseRing.soloBuffedBonus() + upgradedRing.soloBuffedBonus(), baseRing.combinedBuffedBonus(hero));
	}
}
