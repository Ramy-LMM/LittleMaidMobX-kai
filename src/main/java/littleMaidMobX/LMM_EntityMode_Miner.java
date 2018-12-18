package littleMaidMobX;

import mmmlibx.lib.MMM_Helper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;

public class LMM_EntityMode_Miner extends LMM_EntityModeBase {
	int targetX;
	int targetY;
	int targetZ;
	float timeMined = 0; //Measured in ticks
	float mineTime; //Measured in ticks

	public static final int mmode_Miner = 0x0023;

	@Override
	public int priority()
	{
		return 3150;
	}

	public LMM_EntityMode_Miner(LMM_EntityLittleMaid pEntity)
	{
		super(pEntity);
	}

	@Override
	public void init() {
		LMM_TriggerSelect.appendTriggerItem(null, "Pickaxe", "");
		LMM_TriggerSelect.appendTriggerItem(null, "Ore", "");
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting)
	{
		// Miner:0x0023
		EntityAITasks[] ltasks = new EntityAITasks[2];
		ltasks[0] = new EntityAITasks(owner.aiProfiler);
		ltasks[1] = pDefaultTargeting;

		ltasks[1].addTask(4, owner.aiFindBlock);
		ltasks[0].addTask(22, owner.aiCollectItem);

		ltasks[1].addTask(32, owner.aiFindBlock);

		ltasks[0].addTask(52, new EntityAIWatchClosest(owner, EntityLivingBase.class, 10F));
		ltasks[0].addTask(51, new EntityAILookIdle(owner));

		owner.addMaidMode(ltasks, "Miner", mmode_Miner);
	}
	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		ItemStack litemstack = owner.maidInventory.getStackInSlot(0);
		if (litemstack != null)
		{
			if (litemstack.getItem() instanceof ItemPickaxe || LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Pickaxe", litemstack))
			{
				owner.setMaidMode("Miner");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode)
	{
		switch (pMode)
		{
			case mmode_Miner :
				owner.setBloodsuck(false);
				owner.aiAttack.setEnable(false);
				owner.aiShooting.setEnable(false);
				return true;
		}
		return false;
	}

	@Override
	public int getNextEquipItem(int pMode)
	{
		int li;
		ItemStack litemstack;

		switch (pMode) {
		case mmode_Miner :
			for (li = 0; li < owner.maidInventory.maxInventorySize; li++)
			{
				litemstack = owner.maidInventory.getStackInSlot(li);
				if (litemstack == null)
				{
					continue;
				}

				if (litemstack.getItem() instanceof ItemPickaxe || LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Pickaxe", litemstack))
				{
					return li;
				}
			}
			break;
		}
		return -1;
	}

	@Override
	public boolean checkItemStack(ItemStack pItemStack)
	{
		return true;
	}

	@Override
	public boolean isSearchBlock()
	{
		return true;
	}

	@Override
	public boolean shouldBlock(int pMode)
	{
		return owner.getCurrentEquippedItem() != null;
	}

	protected boolean isBlockMineable(int i, int j, int k)
	{
		Block theBlock = owner.worldObj.getBlock(i, j, k);
		ItemStack tool = owner.getCurrentEquippedItem();
		if (tool == null)
		{
			return false;
		}
		else
		{
			return (owner.getCurrentEquippedItem().getItem().canHarvestBlock(theBlock, tool));
		}
	}

	protected boolean shouldBlockBeMined(int x, int y, int z)
	{
		World worldObj = owner.worldObj;
		Block theBlock = worldObj.getBlock(x, y, z);
		return (isBlockMineable(x, y, z)
				&& (theBlock instanceof BlockOre || theBlock instanceof BlockRedstoneOre || LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Ore", new ItemStack(theBlock))));
	}

	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz)
	{
		if (owner.getHeldItem() == null)
		{
			return false;
		}
		if (owner.isFreedom() && owner.getHomePosition().getDistanceSquared(px, py, pz) > LMM_EntityModeBase.limitDistance_Freedom)
		{
			return false;
		}
		if (!owner.isFreedom() && owner.getMaidMasterEntity()!=null && owner.getMaidMasterEntity().getDistanceSq(px, py, pz) > LMM_EntityModeBase.limitDistance_Follow)
		{
			return false;
		}
		if (shouldBlockBeMined(px, py, pz) && canBlockBeSeen(px, py, pz, true, true, false) && isInvNotFull() && !owner.isMaidWait())
		{
			owner.worldObj.destroyBlockInWorldPartially(-1, targetX, targetY, targetZ, -1);
			targetX = px;
			targetY = py;
			targetZ = pz;
			timeMined = 0;
			mineTime = MMM_Helper.getMineTime(owner.worldObj, targetX, targetY, targetZ, owner.getCurrentEquippedItem());

			owner.aiWander.setEnable(false);
			if (owner.getNavigator().tryMoveToXYZ(px, py, pz, 1.0F))
			{
				owner.playSound(LMM_EnumSound.findTarget_N, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean executeBlock(int pMode)
	{
		if (owner.isFreedom() && owner.getHomePosition().getDistanceSquared(targetX, targetY, targetZ) > LMM_EntityModeBase.limitDistance_Freedom)
		{
			return false;
		}
		if (!owner.isFreedom() && owner.getMaidMasterEntity()!=null && owner.getMaidMasterEntity().getDistanceSq(targetX, targetY, targetZ) > LMM_EntityModeBase.limitDistance_Follow)
		{
			return false;
		}
		World worldObj = owner.worldObj;
		ItemStack item = owner.getCurrentEquippedItem();
		if (item == null) return false;

		ItemTool tool = (ItemTool) item.getItem();
		if (owner.getDistanceSq(targetX, targetY, targetZ) < 16D)
		{
			Block theBlock = worldObj.getBlock(targetX, targetY, targetZ);
			int completed = (int)Math.floor((timeMined/mineTime)*8);

			owner.getLookHelper().setLookPosition(targetX, targetY, targetZ, 10F, owner.getVerticalFaceSpeed());
			owner.setSwing(10, LMM_EnumSound.findTarget_B);
			//worldObj.playSoundEffect((double)targetX+0.5D, (double)targetY+0.5D, (double)targetZ+0.5D, theBlock.stepSound.soundName, 10000.0F, 0.8F + worldObj.rand.nextFloat() * 0.2F);
			worldObj.playSound((double)targetX+0.5D, (double)targetY+0.5D, (double)targetZ+0.5D, "dig.stone", 1.0f, (worldObj.rand.nextFloat() * 0.2F) + 0.95F, false);
			worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY, targetZ, completed);

			if(completed >= 8)
			{
				if (EnchantmentHelper.getSilkTouchModifier(owner))
				{
					worldObj.spawnEntityInWorld(new EntityItem(worldObj, targetX, targetY, targetZ, new ItemStack(theBlock)));
				}
				else
				{
					theBlock.dropBlockAsItem(worldObj, targetX, targetY, targetZ, worldObj.getBlockMetadata(targetX, targetY, targetZ), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item));
					theBlock.dropXpOnBlockBreak(worldObj, targetX, targetY, targetZ, theBlock.getExpDrop(worldObj, worldObj.getBlockMetadata(targetX, targetY, targetZ), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item)));
				}
				worldObj.setBlockToAir(targetX, targetY, targetZ);
				worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY, targetZ, -1);

				item.damageItem(1, owner);

				timeMined = 0.0f;
				owner.getNavigator().clearPathEntity();
				owner.aiWander.setEnable(true);
				return false;
			}
			else
			{
				String toolMaterial = tool.getToolMaterialName();
				timeMined += tool.func_150913_i().getEfficiencyOnProperMaterial();
			}
		}
		return true;
	}

	@Override
	public void updateAITick(int pMode)
	{
		if (pMode == mmode_Miner)
		{
			if (owner.getCurrentEquippedItem() == null || owner.getCurrentEquippedItem().stackSize <= 0)
			{
				owner.maidInventory.setInventoryCurrentSlotContents(null);
				owner.getNextEquipItem();
			}
		}
	}

	private boolean isInvNotFull()
	{
		if (owner.maidInventory.getFirstEmptyStack() == -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
