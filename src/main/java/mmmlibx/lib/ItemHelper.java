package mmmlibx.lib;

import littleMaidMobX.LMM_EntityLittleMaid;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class ItemHelper {
	public static boolean isCake(Item item){
		if (item == null) return false;
		return item == Items.cake;
	}

	public static boolean isCake(ItemStack pItemstack){
		if (pItemstack == null) return false;
		return isCake(pItemstack.getItem());
	}

	public static boolean isSugar(Item item){
		if (item == null) return false;
		return item == Items.sugar;
	}
	
	public static boolean isSugar(ItemStack pItemstack){
		if (pItemstack == null) return false;
		return isSugar(pItemstack.getItem());
	}

	public static boolean hasSugar(LMM_EntityLittleMaid maid){
		boolean flag = false;
		for(ItemStack stack: maid.maidInventory.mainInventory){
			if(stack == null) continue;
			if(isSugar(stack.getItem())){
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	public static int getFoodAmount(ItemStack pItemstack) {
		if (pItemstack == null) {
			return -1;
		}
		if (pItemstack.getItem() instanceof ItemFood) {
			return ((ItemFood) pItemstack.getItem()).func_150905_g(pItemstack);
		}
		return -1;
	}
	
	public static boolean isItemBurned(ItemStack pItemstack) {
		return ((pItemstack != null) &&
				TileEntityFurnace.getItemBurnTime(pItemstack) > 0);
	}

	public static boolean isItemSmelting(ItemStack pItemstack) {
		return ((pItemstack != null) && MMM_Helper.getSmeltingResult(pItemstack) != null);
	}

	public static boolean isItemExplord(ItemStack pItemstack) {
		if (pItemstack == null)
			return false;
		Item li = pItemstack.getItem();
		return (pItemstack != null && li instanceof ItemBlock && Block.getBlockFromItem(li).getMaterial() == Material.tnt);
	}
}
