package littleMaidMobX;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class LMM_EntityMode_Farmer extends LMM_EntityModeBase {

	public static final int mmode_Farmer = 0x0024;

	private int clearCount = 0;

	public LMM_EntityMode_Farmer(LMM_EntityLittleMaid pEntity) {
		super(pEntity);
		LMM_TriggerSelect.appendTriggerItem(null, "Hoe", "");
		LMM_TriggerSelect.appendTriggerItem(null, "Seeds", "");
	}

	@Override
	public int priority() {
		// TODO 自動生成されたメソッド・スタブ
		return 6300;
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove,
			EntityAITasks pDefaultTargeting) {
		// TODO 自動生成されたメソッド・スタブ
		EntityAITasks[] ltasks = new EntityAITasks[2];
		ltasks[0] = pDefaultMove;
		ltasks[1] = pDefaultTargeting;

		ltasks[0].addTask(0, owner.aiCollectItem);

		owner.addMaidMode(ltasks, "Farmer", mmode_Farmer);
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		// TODO 自動生成されたメソッド・スタブ
		ItemStack litemstack = owner.maidInventory.getStackInSlot(0);
		if (litemstack != null) {
			if (litemstack.getItem() instanceof ItemHoe)
			{
				owner.setMaidMode("Farmer");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode) {
		// TODO 自動生成されたメソッド・スタブ
		switch (pMode) {
		case mmode_Farmer :
			owner.setBloodsuck(false);
			owner.aiAttack.setEnable(false);
			owner.aiShooting.setEnable(false);
			return true;
		}

		return false;
	}

	@Override
	public int getNextEquipItem(int pMode) {
		int li;
		ItemStack litemstack;

		// モードに応じた識別判定、速度優先
		switch (pMode) {
			case mmode_Farmer :
				for (li = 0; li < owner.maidInventory.maxInventorySize; li++) {
					litemstack = owner.maidInventory.getStackInSlot(li);
					if (litemstack == null) continue;

					// クワ
					if (litemstack.getItem() instanceof ItemHoe) {
						return li;
					}
				}
			break;
		}

		return -1;
	}

	@Override
	public boolean checkItemStack(ItemStack pItemStack) {
		if(pItemStack==null) return false;
		return (pItemStack.getItem() instanceof ItemSeeds || LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Seeds", pItemStack));
	}

	@Override
	public boolean isSearchBlock() {
		return !owner.isMaidWait()&&(owner.getCurrentEquippedItem()!=null);
	}

	@Override
	public boolean shouldBlock(int pMode) {
		return owner.getCurrentEquippedItem() != null;
	}

	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz) {
		if(owner.isFreedom()){
			if(owner.getHomePosition().getDistanceSquared(
					MathHelper.floor_double(owner.posX),
					MathHelper.floor_double(owner.posY),
					MathHelper.floor_double(owner.posZ)) > limitDistance_Freedom){
				return false;
			}
		}
		else if(owner.getMaidMasterEntity() != null) {
			if(owner.getMaidMasterEntity().getDistanceSq(px,py,pz)>limitDistance_Follow){
				return false;
			}
		}
		if(!canMoveThrough(owner, 0.9D, px + 0.5D, py + 1.9D, pz + 0.5D, py==MathHelper.floor_double(owner.posY-1D), true, false)) return false;
		if(isUnfarmedLand(px,py,pz)) return true;
		if(isFarmedLand(px,py,pz)){
			/*耕地が見つかっても、
			 * ①周りに未耕作の地域がある場合はtrueを返さない
			 * ②種を持っていない場合もfalse
			 */

			int p = LMM_EntityModeBase.Water_Radius * 3;
			for(int az=-p;az<=p;az++){
				for(int ax=-p;ax<=p;ax++){
					if(isUnfarmedLand(px+ax,py,pz+az)) return false;
				}
			}

			if(getHadSeedIndex()==-1)
				return false;
			return true;
		}
		if(isCropGrown(px,py,pz)) return true;
		return false;
	}

	@Override
	public boolean executeBlock(int pMode, int px, int py, int pz) {
//		if(owner.worldObj.isRemote) return false;
		ItemStack curStack = owner.getCurrentEquippedItem();

		boolean haveNothing = !(curStack.getItem() instanceof ItemHoe);

		if (!haveNothing && isUnfarmedLand(px,py,pz) &&
				curStack.tryPlaceItemIntoWorld(owner.maidAvatar, owner.worldObj, px, py, pz, 1, 0.5F, 1.0F, 0.5F)) {
			owner.setSwing(10, LMM_EnumSound.Null/*, false*/);
			//owner.playLittleMaidSound(EnumSound.farmer_farm, false);

			/*
			if (owner.maidAvatar.capabilities.isCreativeMode) {
				lis.stackSize = li;
			}
			*/
			if (curStack.stackSize <= 0) {
				owner.maidInventory.setInventoryCurrentSlotContents(null);
				owner.getNextEquipItem();
			}
//			owner.getNavigator().clearPathEntity();
		}
		if(isFarmedLand(px,py,pz)){
			//種を持っている
			int index = getHadSeedIndex();
			if(index!=-1){
				ItemStack stack = owner.maidInventory.getStackInSlot(index);
				int li = stack.stackSize;
				stack.tryPlaceItemIntoWorld(owner.maidAvatar, owner.worldObj, px, py, pz, 1, 0.5F, 1.0F, 0.5F);
				//owner.playLittleMaidSound(EnumSound.farmer_plant, false);
				if (owner.maidAvatar.capabilities.isCreativeMode) {
					stack.stackSize = li;
				}
				owner.setSwing(10, LMM_EnumSound.Null/*, false*/);
				if(stack.stackSize<=0){
					owner.maidInventory.setInventorySlotContents(index, null);
				}
			}
		}
		if(isCropGrown(px,py,pz))
		{
			// 収穫
			/*BlockPos pos = new BlockPos(px,py,pz);
			owner.worldObj.destroyBlock(pos, true);*/
			World worldObj = owner.worldObj;
			Block theBlock = worldObj.getBlock(px,py,pz);
			ItemStack tool = owner.getCurrentEquippedItem();

			theBlock.dropBlockAsItem(worldObj, px, py, pz, worldObj.getBlockMetadata(px, py, pz), 0);
			theBlock.dropXpOnBlockBreak(worldObj, px, py, pz, theBlock.getExpDrop(worldObj, worldObj.getBlockMetadata(px, py, pz), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, tool)));
			worldObj.setBlockToAir(px, py, pz);
			owner.setSwing(10, LMM_EnumSound.Null/*, false*/);
			//owner.playLittleMaidSound(LMM_EnumSound.farmer_harvest, false);
			//owner.addMaidExperience(4f);
			executeBlock(pMode,px,py-1,pz);
//			return true;
		}
		return false;
	}

	@Override
	public void onUpdate(int pMode) {
		// TODO 自動生成されたメソッド・スタブ
		if(pMode==mmode_Farmer&&++clearCount>=300&&owner.getNavigator().noPath()){
			try{
				if(!owner.isWorking()){
					if(owner.aiCollectItem.shouldExecute()) owner.aiCollectItem.updateTask();
				}
			}catch(NullPointerException e){}
			clearCount = 0;
		}
	}

	@Override
	public void updateAITick(int pMode) {
		if (pMode == mmode_Farmer && owner.getNextEquipItem()) {
			if(owner.getAIMoveSpeed()>0.5F) owner.setAIMoveSpeed(0.5F);
			if(owner.maidInventory.getFirstEmptyStack()==-1)
			{
				owner.setMaidMode("FarmPorter");
			}
		}
	}

	private int getHadSeedIndex() {
		for (int i = 0; i < owner.maidInventory.maxInventorySize; i++) {
			ItemStack itemStack = owner.maidInventory.getStackInSlot(i);
			if (itemStack == null) continue;

			Item item = itemStack.getItem();
			if ((item instanceof ItemSeeds) ||
				(item instanceof ItemSeedFood)) {
				return i;
			}
		}
		return -1;
	}

	private boolean isUnfarmedLand(int x, int y, int z){
		//耕されておらず、直上が空気ブロック
		//近くに水があるときにとりあえず耕す用
		Block b = owner.worldObj.getBlock(x,y,z);
		return (Block.isEqualTo(b, Blocks.dirt)||Block.isEqualTo(b, Blocks.grass))&&
				owner.worldObj.isAirBlock(x,y+1,z) && isBlockWatered(x, y, z);
	}

	private boolean isFarmedLand(int x, int y, int z){
		//耕されていて、直上が空気ブロック
		Block b = owner.worldObj.getBlock(x,y,z);
		if(b instanceof BlockFarmland){
			return owner.worldObj.isAirBlock(x,y+1,z);
		}
		return false;
	}

	private boolean isCropGrown(int x, int y, int z){
		Block b = owner.worldObj.getBlock(x,y,z);
		if(b instanceof BlockCrops){
			int age = (Integer) owner.worldObj.getBlockMetadata(x, y, z)/* + MathHelper.getRandomIntegerInRange(owner.worldObj.rand, 2, 5)*/;
			if(age==7) return true;
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean isBlockWatered(int x, int y, int z)
	{
		for (int l = x - 4; l <= x + 4; ++l)
        {
            for (int i1 = y; i1 <= y + 1; ++i1)
            {
                for (int j1 = z - 4; j1 <= z + 4; ++j1)
                {
                    if (owner.worldObj.getBlock(l, i1, j1).getMaterial() == Material.water)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
	}

	/**
	 * 基本的にcanBlockBeSeenに同じ。違いは足元基準で「通れるか」を判断するもの
	 */
	public static boolean canMoveThrough(Entity pEntity, double fixHeight, double pX, double pY, double pZ, boolean toTop, boolean do1, boolean do2)
	{
		Block lblock = pEntity.worldObj.getBlock(MathHelper.floor_double(pX), MathHelper.floor_double(pY), MathHelper.floor_double(pZ));
		if (lblock == null) {
			return false;
		}
//		lblock.setBlockBoundsBasedOnState(pEntity.worldObj, new BlockPos(pX, pY, pZ));

		Vec3 vec3do = Vec3.createVectorHelper(pEntity.posX, pEntity.posY+fixHeight, pEntity.posZ);
		Vec3 vec3dt = Vec3.createVectorHelper(pX, pY, pZ);
		MovingObjectPosition movingobjectposition = pEntity.worldObj.func_147447_a(vec3do, vec3dt, do1, do2, false);

		if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectType.BLOCK) {
			if (movingobjectposition.blockX == (int)pX &&
					movingobjectposition.blockY == (int)pY &&
					movingobjectposition.blockZ == (int)pZ) {
				return true;
			}
			return false;
		}
		return true;
	}
}
