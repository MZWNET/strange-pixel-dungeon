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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;

public class TestItems {

	public static class TestWeapon extends KindOfWeapon {
		@Override
		public int min(int lvl) {
			return 1;
		}

		@Override
		public int max(int lvl) {
			return 1;
		}
	}

	public static class TestArmor extends Armor {
		public TestArmor() {
			super(1);
		}
	}

	public static class TestRing extends Ring {
		@Override
		public void activate(Char ch) {
			// Test-only ring: combined bonus logic does not need runtime buffs.
		}
	}

	public static class OtherTestRing extends Ring {
		@Override
		public void activate(Char ch) {
			// Test-only ring: combined bonus logic does not need runtime buffs.
		}
	}

	public static class TestArtifact extends Artifact {
		@Override
		public void activate(Char ch) {
			// Test-only artifact: avoids attaching a null passive buff.
		}
	}

	public static class OtherTestArtifact extends Artifact {
		@Override
		public void activate(Char ch) {
			// Test-only artifact: avoids attaching a null passive buff.
		}
	}
}
