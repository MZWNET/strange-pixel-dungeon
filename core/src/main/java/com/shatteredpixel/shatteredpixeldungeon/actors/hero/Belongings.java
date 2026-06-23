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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public ArrayList<KindofMisc> miscItems = new ArrayList<>();

	//legacy mirrors for old fixed-slot code and save migration; miscItems is the source of truth
	public Artifact artifact = null;
	public KindofMisc misc = null;
	public Ring ring = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	public Ring ring(){
		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	public ArrayList<KindofMisc> rawEquippedMiscs(){
		return miscItems;
	}

	public ArrayList<KindofMisc> equippedMiscs(){
		ArrayList<KindofMisc> result = new ArrayList<>();
		boolean lostInvent = lostInventory();
		for (KindofMisc item : miscItems){
			if (!lostInvent || item.keptThroughLostInventory()){
				result.add(item);
			}
		}
		return result;
	}

	public KindofMisc miscSlot(int index){
		ArrayList<KindofMisc> items = equippedMiscs();
		return index >= 0 && index < items.size() ? items.get(index) : null;
	}

	public ArrayList<Ring> rings(){
		ArrayList<Ring> result = new ArrayList<>();
		for (KindofMisc item : equippedMiscs()){
			if (item instanceof Ring){
				result.add((Ring)item);
			}
		}
		return result;
	}

	public ArrayList<Artifact> artifacts(){
		ArrayList<Artifact> result = new ArrayList<>();
		for (KindofMisc item : equippedMiscs()){
			if (item instanceof Artifact){
				result.add((Artifact)item);
			}
		}
		return result;
	}

	public boolean hasEquippedArtifact(Class<?> artifactClass){
		for (KindofMisc item : rawEquippedMiscs()){
			if (item instanceof Artifact && item.getClass().equals(artifactClass)){
				return true;
			}
		}
		return false;
	}

	public boolean hasEquippedMiscClass(Class<?> itemClass){
		for (KindofMisc item : rawEquippedMiscs()){
			if (item.getClass().equals(itemClass)){
				return true;
			}
		}
		return false;
	}

	public void equipMisc(KindofMisc item){
		if (!miscItems.contains(item)){
			miscItems.add(item);
			syncLegacyMiscSlots();
		}
	}

	public void unequipMisc(KindofMisc item){
		if (miscItems.remove(item)){
			syncLegacyMiscSlots();
		}
	}

	public ArrayList<Item> equippedItems(){
		ArrayList<Item> result = new ArrayList<>();
		if (weapon() != null)    result.add(weapon());
		if (armor() != null)     result.add(armor());
		result.addAll(equippedMiscs());
		if (secondWep() != null) result.add(secondWep());
		return result;
	}

	private ArrayList<Item> rawEquippedItems(){
		ArrayList<Item> result = new ArrayList<>();
		if (weapon != null)      result.add(weapon);
		if (armor != null)       result.add(armor);
		result.addAll(miscItems);
		if (secondWep != null)   result.add(secondWep);
		return result;
	}

	public void syncLegacyMiscSlots(){
		artifact = null;
		misc = null;
		ring = null;

		for (KindofMisc item : miscItems){
			if (artifact == null && item instanceof Artifact){
				artifact = (Artifact)item;
			}
			if (ring == null && item instanceof Ring){
				ring = (Ring)item;
			}
		}

		for (KindofMisc item : miscItems){
			if (item != artifact && item != ring){
				misc = item;
				break;
			}
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";
	private static final String MISC_ITEMS = "miscs";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( MISC_ITEMS, miscItems );
		bundle.put( SECOND_WEP, secondWep );
	}

	public static boolean bundleRestoring = false;
	
	public void restoreFromBundle( Bundle bundle ) {
		bundleRestoring = true;
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		miscItems.clear();
		if (bundle.contains(MISC_ITEMS)){
			Collection<Bundlable> restoredMiscs = bundle.getCollection(MISC_ITEMS);
			for (Bundlable item : restoredMiscs){
				if (item instanceof KindofMisc){
					miscItems.add((KindofMisc)item);
				}
			}
		} else {
			Artifact legacyArtifact = (Artifact) bundle.get(ARTIFACT);
			KindofMisc legacyMisc = (KindofMisc) bundle.get(MISC);
			Ring legacyRing = (Ring) bundle.get(RING);
			if (legacyArtifact != null) miscItems.add(legacyArtifact);
			if (legacyMisc != null)     miscItems.add(legacyMisc);
			if (legacyRing != null)     miscItems.add(legacyRing);
		}
		syncLegacyMiscSlots();
		for (KindofMisc item : miscItems){
			item.activate(owner);
		}

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);

		bundleRestoring = false;
	}

	public void clear(){
		backpack.clear();
		weapon = secondWep = null;
		armor = null;
		miscItems.clear();
		syncLegacyMiscSlots();
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		for (KindofMisc item : equippedMiscs()){
			if (item instanceof Artifact){
				//oblivion shard does not prevent artifact IDing
				item.identify();
				Badges.validateItemLevelAquired(item);
			} else if (item instanceof Ring){
				if (ShardOfOblivion.passiveIDDisabled()){
					((Ring)item).setIDReady();
				} else {
					item.identify();
					Badges.validateItemLevelAquired(item);
				}
			} else {
				item.identify();
				Badges.validateItemLevelAquired(item);
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ArrayList<Item> equipped = equippedItems();
		ScrollOfRemoveCurse.uncurse( owner, equipped.toArray(new Item[0]) );
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private ArrayList<Item> equipped = rawEquippedItems();
		private int backpackIndex = equipped.size();
		private Item lastItem = null;
		private boolean lastFromBackpack = false;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped.get(i) != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped.get(index++);
				if (item != null) {
					lastItem = item;
					lastFromBackpack = false;
					return item;
				}
			}
			
			lastItem = backpackIterator.next();
			lastFromBackpack = true;
			return lastItem;
		}

		@Override
		public void remove() {
			if (lastFromBackpack) {
				backpackIterator.remove();
			} else if (lastItem == weapon){
				weapon = null;
			} else if (lastItem == armor){
				armor = null;
			} else if (lastItem == secondWep){
				secondWep = null;
			} else if (lastItem instanceof KindofMisc){
				miscItems.remove(lastItem);
				syncLegacyMiscSlots();
			}
			lastItem = null;
		}
	}
}
