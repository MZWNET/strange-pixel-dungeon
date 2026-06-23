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

import com.watabou.input.ScrollEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class HorizontalScrollPane extends ScrollPane {

	public HorizontalScrollPane(Component content) {
		super(content);
	}

	@Override
	protected void createChildren() {
		controller = new HorizontalPointerController();
		add(controller);

		thumb = new ColorBlock(1, 1, THUMB_COLOR);
		thumb.am = THUMB_ALPHA;
		thumb.visible = false;
		add(thumb);
	}

	@Override
	protected void layout() {
		if (camera() == null){
			return;
		}
		super.layout();
		if (thumb != null){
			thumb.visible = false;
		}
	}

	public class HorizontalPointerController extends PointerController {

		@Override
		protected void onScroll(ScrollEvent event) {
			scrollTo(content.camera.scroll.x + horizontalAmount(event) * content.camera.zoom * 10, content.camera.scroll.y);
		}
	}

	static float horizontalAmount(ScrollEvent event){
		return event.amountX != 0 ? event.amountX : event.amountY;
	}
}
