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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.TestItems;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.watabou.utils.Bundle;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BelongingsMiscItemsTest {

	@Test
	public void canEquipMoreThanThreeMixedMiscItems() {
		Hero hero = new Hero();
		Belongings belongings = hero.belongings;

		KindofMisc ringOne = new TestItems.TestRing();
		KindofMisc ringTwo = new TestItems.OtherTestRing();
		KindofMisc ringThree = new TestItems.TestRing();
		KindofMisc artifact = new TestItems.TestArtifact();

		belongings.equipMisc(ringOne);
		belongings.equipMisc(ringTwo);
		belongings.equipMisc(ringThree);
		belongings.equipMisc(artifact);

		assertEquals(4, belongings.rawEquippedMiscs().size());
		assertSame(ringOne, belongings.rawEquippedMiscs().get(0));
		assertSame(artifact, belongings.rawEquippedMiscs().get(3));
		assertTrue(belongings.contains(ringThree));
	}

	@Test
	public void tracksRingsAndArtifactsAcrossAllMiscItems() {
		Hero hero = new Hero();
		Belongings belongings = hero.belongings;

		belongings.equipMisc(new TestItems.TestRing());
		belongings.equipMisc(new TestItems.OtherTestRing());
		belongings.equipMisc(new TestItems.TestArtifact());
		belongings.equipMisc(new TestItems.OtherTestArtifact());

		assertEquals(2, belongings.rings().size());
		assertEquals(2, belongings.artifacts().size());
		assertTrue(belongings.hasEquippedArtifact(TestItems.TestArtifact.class));
		assertFalse(belongings.hasEquippedArtifact(TestItems.class));
		assertTrue(belongings.hasEquippedMiscClass(TestItems.TestArtifact.class));
	}

	@Test
	public void restoresLegacyMiscSlotsIntoUnifiedList() {
		Bundle bundle = new Bundle();
		TestItems.TestRing legacyRing = new TestItems.TestRing();
		TestItems.TestArtifact legacyArtifact = new TestItems.TestArtifact();
		TestItems.OtherTestArtifact legacyMisc = new TestItems.OtherTestArtifact();

		bundle.put("ring", legacyRing);
		bundle.put("artifact", legacyArtifact);
		bundle.put("misc", legacyMisc);

		Hero hero = new Hero();
		hero.belongings.restoreFromBundle(bundle);

		assertEquals(3, hero.belongings.rawEquippedMiscs().size());
		assertTrue(hero.belongings.rawEquippedMiscs().get(0) instanceof TestItems.TestArtifact);
		assertTrue(hero.belongings.rawEquippedMiscs().get(1) instanceof TestItems.OtherTestArtifact);
		assertTrue(hero.belongings.rawEquippedMiscs().get(2) instanceof TestItems.TestRing);
	}

	@Test
	public void storesAndRestoresMoreThanThreeMiscItems() {
		Hero hero = new Hero();
		hero.belongings.equipMisc(new TestItems.TestRing());
		hero.belongings.equipMisc(new TestItems.OtherTestRing());
		hero.belongings.equipMisc(new TestItems.TestRing());
		hero.belongings.equipMisc(new TestItems.TestArtifact());

		Bundle bundle = new Bundle();
		hero.belongings.storeInBundle(bundle);

		Hero restored = new Hero();
		restored.belongings.restoreFromBundle(bundle);

		assertEquals(4, restored.belongings.rawEquippedMiscs().size());
		assertTrue(restored.belongings.rawEquippedMiscs().get(0) instanceof TestItems.TestRing);
		assertTrue(restored.belongings.rawEquippedMiscs().get(3) instanceof TestItems.TestArtifact);
	}

	@Test
	public void equippedItemsIncludeAllMiscItemsInStableOrder() {
		Hero hero = new Hero();
		TestItems.TestWeapon weapon = new TestItems.TestWeapon();
		TestItems.TestArmor armor = new TestItems.TestArmor();
		TestItems.TestRing ring = new TestItems.TestRing();
		TestItems.TestArtifact artifact = new TestItems.TestArtifact();
		TestItems.TestWeapon secondWeapon = new TestItems.TestWeapon();

		hero.belongings.weapon = weapon;
		hero.belongings.armor = armor;
		hero.belongings.equipMisc(ring);
		hero.belongings.equipMisc(artifact);
		hero.belongings.secondWep = secondWeapon;

		ArrayList<?> equippedItems = hero.belongings.equippedItems();

		assertEquals(5, equippedItems.size());
		assertSame(weapon, equippedItems.get(0));
		assertSame(armor, equippedItems.get(1));
		assertSame(ring, equippedItems.get(2));
		assertSame(artifact, equippedItems.get(3));
		assertSame(secondWeapon, equippedItems.get(4));
	}

	@Test
	public void artifactClassChecksCoverEveryEquippedArtifact() {
		Hero hero = new Hero();

		hero.belongings.equipMisc(new TestItems.TestArtifact());
		hero.belongings.equipMisc(new TestItems.OtherTestArtifact());

		assertTrue(hero.belongings.hasEquippedArtifact(TestItems.TestArtifact.class));
		assertTrue(hero.belongings.hasEquippedArtifact(TestItems.OtherTestArtifact.class));
	}

	@Test
	public void duplicateArtifactChecksIgnoreLostInventoryFiltering() {
		Hero hero = new Hero();

		hero.belongings.equipMisc(new TestItems.TestArtifact());
		hero.belongings.lostInventory(true);

		assertEquals(0, hero.belongings.equippedMiscs().size());
		assertTrue(hero.belongings.hasEquippedArtifact(TestItems.TestArtifact.class));
	}
}
