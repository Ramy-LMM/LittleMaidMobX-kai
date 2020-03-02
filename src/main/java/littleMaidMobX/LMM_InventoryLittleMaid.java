package littleMaidMobX;

import java.util.Iterator;
import java.util.List;

import net.minecraft.util.ChatComponentText;
import net.minecraft.entity.player.EntityPlayer;
import mmmlibx.lib.MMM_Helper;
import mmmlibx.lib.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;

public class LMM_InventoryLittleMaid extends InventoryPlayer {

	/**
	 * 最大インベントリ数
	 */
	public static final int maxInventorySize = 20;
	/**
	 * オーナー
	 */
	public LMM_EntityLittleMaid entityLittleMaid;
	/**
	 * スロット変更チェック用
	 */
	public ItemStack prevItems[];

	public LMM_InventoryLittleMaid(LMM_EntityLittleMaid par1EntityLittleMaid) {
		super(par1EntityLittleMaid.maidAvatar);

		entityLittleMaid = par1EntityLittleMaid;
		mainInventory = new ItemStack[maxInventorySize];
		armorInventory = new ItemStack[4];
		prevItems = new ItemStack[getSizeInventory()];
	}

	@Override
	public void readFromNBT(NBTTagList par1nbtTagList) {
		mainInventory = new ItemStack[maxInventorySize];
		armorInventory = new ItemStack[4];

		for (int i = 0; i < par1nbtTagList.tagCount(); i++) {
			NBTTagCompound nbttagcompound = par1nbtTagList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 0xff;
			ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);

			if (itemstack == null) {
				continue;
			}

			if (j >= 0 && j < mainInventory.length) {
				mainInventory[j] = itemstack;
			}

			if (j >= 100 && j < armorInventory.length + 100) {
				armorInventory[j - 100] = itemstack;
			}
		}
	}

	@Override
	public NBTTagList writeToNBT(NBTTagList par1nbtTagList) {
		NBTTagList result = super.writeToNBT(par1nbtTagList);
		NBTTagList tagList = par1nbtTagList;
		if (par1nbtTagList == null) {
			tagList = new NBTTagList();
		}
		return result;
	}

	@Override
	public String getInventoryName() {
		return "InsideSkirt";
	}

	@Override
	public int getSizeInventory() {
		// 一応
		return mainInventory.length + armorInventory.length;
	}

	@Override
	public void openInventory() {
		entityLittleMaid.onGuiOpened();
	}

	@Override
	public void closeInventory() {
		entityLittleMaid.onGuiClosed();
	}

	@Override
	public void decrementAnimations() {
		for (int li = 0; li < this.mainInventory.length; ++li) {
			if (this.mainInventory[li] != null) {
				try {
					this.mainInventory[li].updateAnimation(this.player.worldObj,
							entityLittleMaid, li, this.currentItem == li);
				} catch (ClassCastException e) {
					this.mainInventory[li].updateAnimation(this.player.worldObj,
							entityLittleMaid.maidAvatar, li, this.currentItem == li);
				}
			}
		}
	}

	@Override
	public int getTotalArmorValue() {
		// 身に着けているアーマーの防御力の合算
		// 頭部以外
		//ItemStack lis = armorInventory[3];
		//armorInventory[3] = null;
		// int li = super.getTotalArmorValue() * 20 / 17;
		int li = super.getTotalArmorValue();
		// 兜分の補正
		for (int lj = 0; lj < armorInventory.length - 2; lj++) {
			if (armorInventory[lj] != null
					&& armorInventory[lj].getItem() instanceof ItemArmor) {
				li++;
			}
		}
		//armorInventory[3] = lis;
		//entityLittleMaid.mstatMasterEntity.addChatMessage(new ChatComponentText("armor: "+li));
		return li;
	}

	@Override
	public void damageArmor(float pDamage) {
		// 装備アーマーに対するダメージ
		pDamage = Math.max(pDamage/4, 1);

		for (int i = 0; i < armorInventory.length; i++) {
			if (armorInventory[i] != null && armorInventory[i].getItem() instanceof ItemArmor) {
				armorInventory[i].damageItem((int)pDamage, player);

				if (armorInventory[i].stackSize == 0) {
					armorInventory[i] = null;
				}
			}
		}
	}
/*
	@Override
	public int getDamageVsEntity(Entity entity) {
		return getDamageVsEntity(entity, currentItem);
	}

	public int getDamageVsEntity(Entity entity, int index) {
		if (index < 0 || index >= getSizeInventory()) return 1;
		ItemStack itemstack = getStackInSlot(index);
		if (itemstack != null) {
			if (itemstack.getItem() instanceof ItemAxe) {
				// アックスの攻撃力を補正
				return itemstack.getDamageVsEntity(entity) * 3 / 2 + 1;

			} else {
				return itemstack.getDamageVsEntity(entity);
			}
		} else {
			return 1;
		}
	}
*/
	public void dropAllItems(boolean detonator) {
		// インベントリをブチマケロ！
		Explosion lexp = null;
		if (detonator) {
			// Mobによる破壊の是非
			lexp = new Explosion(entityLittleMaid.worldObj, entityLittleMaid,
					entityLittleMaid.posX, entityLittleMaid.posY, entityLittleMaid.posZ, 3F);
			lexp.isFlaming = false;
			lexp.isSmoking = entityLittleMaid.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
		}

		//armorInventory[3] = null;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack it = getStackInSlot(i);
			if (it != null) {
				if (detonator && isItemExplord(i)) {
					Item j = it.getItem();
					for (int l = 0; l < it.stackSize; l++) {
						// 爆薬ぶちまけ
						((BlockTNT)Block.getBlockFromItem(j)).onBlockDestroyedByExplosion(
								entityLittleMaid.worldObj,
								MathHelper.floor_double(entityLittleMaid.posX)
								+ entityLittleMaid.getRNG().nextInt(7) - 3,
								MathHelper.floor_double(entityLittleMaid.posY)
								+ entityLittleMaid.getRNG().nextInt(7) - 3,
								MathHelper.floor_double(entityLittleMaid.posZ)
								+ entityLittleMaid.getRNG().nextInt(7) - 3, lexp);
					}
				} else {
					entityLittleMaid.entityDropItem(it, 0F);
				}
			}
			setInventorySlotContents(i, null);
		}
		if (detonator) {
			lexp.doExplosionA();
			lexp.doExplosionB(true);
		}
	}

	@Override
	public void dropAllItems() {
		dropAllItems(false);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (entityLittleMaid.isDead) {
			return false;
		}
		return entityplayer.getDistanceSqToEntity(entityLittleMaid) <= 64D;
	}

	@Override
	public ItemStack getCurrentItem() {
		if (currentItem >= 0 && currentItem < mainInventory.length) {
			return mainInventory[currentItem];
		} else {
			return null;
		}
	}

	@Override
	public boolean addItemStackToInventory(ItemStack par1ItemStack) {
		markDirty();
		return super.addItemStackToInventory(par1ItemStack);
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (stack != null && index >= maxInventorySize && index < getSizeInventory()) {
			int armorSlotIndex = index - maxInventorySize;
			for(int i = 0; i < 4; i++) {
				if (stack.getItem().isValidArmor(stack, i, entityLittleMaid)) {
					return true;
				}
			}
		} 
		else if (index >= 0 && index < getSizeInventory()) {
			return true;
		}
		return false;
	}

	public void setInventoryCurrentSlotContents(ItemStack itemstack) {
		if (currentItem > -1) {
			markDirty();
			setInventorySlotContents(currentItem, itemstack);
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack itemStack) {
		markDirty();
		if (isItemValidForSlot(index, itemStack)) {
			if (index >= maxInventorySize) {
				armorInventory[index - maxInventorySize] = itemStack;
			}
			else {
				mainInventory[index] = itemStack;
			}
		}
	}
	
	@Override
	public ItemStack getStackInSlot(int index) {
		ItemStack pItemStack = null;
		if ((index >= maxInventorySize) && (index < getSizeInventory())) {
			pItemStack = armorInventory[index - maxInventorySize];
		}
		else if (index > -1 && index < maxInventorySize) {
			pItemStack = mainInventory[index];
		}

		if ((pItemStack != null) && (pItemStack.stackSize <= 0)) {
			setInventorySlotContents(index, null);
			pItemStack = null;
		}

		return pItemStack;
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack target, returned = null;
		if ((target = getStackInSlot(index)) != null) {
			returned = target.splitStack(count);
			if (target.stackSize == 0) {
				setInventorySlotContents(index, null);
			}
			return returned;
		}
		return null;
	}

	public ItemStack removeStackFromSlot(int index) {
		markDirty();
		ItemStack aStack = null;
		if (index >= maxInventorySize) {
			aStack = ItemStack.copyItemStack(armorInventory[index - maxInventorySize]);
			armorInventory[index - maxInventorySize] = null;
		} 
		else if(index >= 0){
			aStack = ItemStack.copyItemStack(mainInventory[index]);
			mainInventory[index] = null;
		}
		return aStack;
	}
	
	public boolean isItemBurned(int index) {
		// 燃えるアイテムか?
		return index > -1 && ItemHelper.isItemBurned(getStackInSlot(index));
	}

	public boolean isItemSmelting(int index) {
		// 燃えるアイテムか?
		return ItemHelper.isItemSmelting(getStackInSlot(index));
	}

	public boolean isItemExplord(int index) {
		// 爆発物？
		return (index >= 0) && ItemHelper.isItemExplord(getStackInSlot(index));
	}
	
	/**
	 * 頭部の追加アイテムを返す。
	 */
	public ItemStack getHeadMount() {
		//return armorInventory[3];
		return mainInventory[mainInventory.length - 1];
	}

	protected int getInventorySlotContainItem(Item item) {
		// 指定されたアイテムIDの物を持っていれば返す
		for (int j = 0; j < mainInventory.length; j++) {
			if (mainInventory[j] != null && mainInventory[j].getItem() == item) {
				return j;
			}
		}

		return -1;
	}

	protected int getInventorySlotContainItem(Class itemClass) {
		// 指定されたアイテムクラスの物を持っていれば返す
		for (int j = 0; j < mainInventory.length; j++) {
			// if (mainInventory[j] != null &&
			// mainInventory[j].getItem().getClass().isAssignableFrom(itemClass))
			// {
			if (mainInventory[j] != null &&
				itemClass.isAssignableFrom(mainInventory[j].getItem().getClass())) {
				return j;
			}
		}

		return -1;
	}

	protected int getInventorySlotContainItemAndDamage(Item item, int damege) {
		// とダメージ値
		for (int i = 0; i < mainInventory.length; i++) {
			if (mainInventory[i] != null && mainInventory[i].getItem() == item
					&& mainInventory[i].getItemDamage() == damege) {
				return i;
			}
		}

		return -1;
	}

	protected ItemStack getInventorySlotContainItemStack(Item item) {
		// いらんかも？
		int j = getInventorySlotContainItem(item);
		return j > -1 ? mainInventory[j] : null;
	}

	protected ItemStack getInventorySlotContainItemStackAndDamege(Item item, int damege) {
		// いらんかも？
		int j = getInventorySlotContainItemAndDamage(item, damege);
		return j > -1 ? mainInventory[j] : null;
	}

	public int getInventorySlotContainItemFood() {
		// インベントリの最初の食料を返す
		for (int j = 0; j < mainInventory.length; j++) {
			ItemStack mi = mainInventory[j];
			if(ItemHelper.getFoodAmount(mi) > 0) {
				return j;
			}
		}
		return -1;
	}

	public int getSmeltingItem() {
		// 調理可能アイテムを返す
		for (int i = 0; i < mainInventory.length; i++) {
			if (isItemSmelting(i) && i != currentItem) {
				ItemStack mi = mainInventory[i];
				if (mi.getMaxDamage() > 0 && mi.getItemDamage() == 0) {
					// 修復レシピ対策
					continue;
				}
				// レシピ対応品
				return i;
			}
		}
		return -1;
	}

	public int getInventorySlotContainItemPotion(boolean flag, int potionID, boolean isUndead) {
		// インベントリの最初のポーションを返す
		// flag = true: 攻撃・デバフ系、 false: 回復・補助系
		// potionID: 要求ポーションのID
		for (int j = 0; j < mainInventory.length; j++) {
			if (mainInventory[j] != null
					&& mainInventory[j].getItem() instanceof ItemPotion) {
				ItemStack is = mainInventory[j];
				List list = ((ItemPotion) is.getItem()).getEffects(is);
				nextPotion: if (list != null) {
					PotionEffect potioneffect;
					for (Iterator iterator = list.iterator(); iterator.hasNext();) {
						potioneffect = (PotionEffect) iterator.next();
						if (potioneffect.getPotionID() == potionID) break;
						if (potioneffect.getPotionID() == Potion.heal.id) {
							if ((!flag && isUndead) || (flag && !isUndead)) {
								break nextPotion;
							}
						} else if (potioneffect.getPotionID() == Potion.harm.id) {
							if ((flag && isUndead) || (!flag && !isUndead)) {
								break nextPotion;
							}
						} else if (Potion.potionTypes[potioneffect.getPotionID()].isBadEffect() != flag) {
							break nextPotion;
						}
					}
					return j;
				}
			}
		}
		return -1;
	}

	public int getFirstEmptyStack() {
		for (int i = 0; i < mainInventory.length; i++) {
			if (mainInventory[i] == null) {
				return i;
			}
		}

		return -1;
	}

	// インベントリの転送関連
	public boolean isChanged(int pIndex) {
		// 変化があったかの判定
		ItemStack lis = getStackInSlot(pIndex);
		return !ItemStack.areItemStacksEqual(lis, prevItems[pIndex]);
		// return (lis == null || prevItems[pIndex] == null) ?
		// (prevItems[pIndex] != lis) : !ItemStack.areItemStacksEqual(lis,
		// prevItems[pIndex]);
		// return prevItems[pIndex] != getStackInSlot(pIndex);
	}

	public void setChanged(int pIndex) {
		prevItems[pIndex] = new ItemStack(Items.sugar);
	}

	public void resetChanged(int pIndex) {
		// 処理済みのチェック
		ItemStack lis = getStackInSlot(pIndex);
		prevItems[pIndex] = (lis == null ? null : lis.copy());
	}

	public void clearChanged() {
		// 強制リロード用、ダミーを登録して強制的に一周させる
		ItemStack lis = new ItemStack(Items.sugar);
		for (int li = 0; li < prevItems.length; li++) {
			prevItems[li] = lis;
		}
	}
}
