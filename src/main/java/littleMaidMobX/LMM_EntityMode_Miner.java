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

		//ltasks[0].addTask(1, owner.aiSwiming);
		//ltasks[0].addTask(2, owner.func_70907_r());
		//ltasks[0].addTask(3, owner.aiJumpTo);
		ltasks[1].addTask(4, owner.aiFindBlock);
		//ltasks[0].addTask(5, owner.aiPanic);

		//ltasks[0].addTask(10, owner.aiBeg);
		//ltasks[0].addTask(11, owner.aiBegMove);

		ltasks[0].addTask(22, owner.aiCollectItem);

		//ltasks[0].addTask(30, owner.aiTracer);
		//ltasks[0].addTask(31, owner.aiFollow);
		ltasks[1].addTask(32, owner.aiFindBlock);
		//ltasks[0].addTask(34, owner.aiWander);

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
		String ls = owner.getMaidMaster();
		return true;/*(pItemStack.getItem() == Items.sugar
				|| pItemStack.getItem() instanceof ItemPickaxe
				|| TriggerSelect.checkItem(ls, "Pickaxe", pItemStack)
				|| TriggerSelect.checkItem(ls, "Ore", pItemStack)
				|| TriggerSelect.checkItem(ls, "Pickup", pItemStack)
				);*/
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

			//owner.aiFindBlock.setEnable(false);
			owner.aiWander.setEnable(false);
			//owner.aiFollow.setEnable(false);
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
					theBlock.dropBlockAsItem(worldObj, targetX, targetY, targetZ, worldObj.getBlockMetadata(targetX, targetY, targetZ), 0);
					theBlock.dropXpOnBlockBreak(worldObj, targetX, targetY, targetZ, theBlock.getExpDrop(worldObj, worldObj.getBlockMetadata(targetX, targetY, targetZ), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item)));
				}
				worldObj.setBlockToAir(targetX, targetY, targetZ);
				worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY, targetZ, -1);

				item.damageItem(1, owner);

				timeMined = 0.0f;
				owner.getNavigator().clearPathEntity();
				//owner.aiFindBlock.setEnable(true);
				owner.aiWander.setEnable(true);
				//owner.aiFollow.setEnable(true);
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
		if (pMode == mmode_Miner)/* && owner.getNextEquipItem() && isInvNotFull())*/
		{
			/*boolean doMine = true;
			ItemStack tool = owner.getCurrentEquippedItem();
			ItemTool item = (ItemTool) tool.getItem();
			World worldObj = owner.worldObj;

			int ltx = targetX;
			int lty = targetY;
			int ltz = targetZ;

			int lxx = MathHelper.floor_double(owner.posX);
			int lyy = MathHelper.floor_double(owner.posY);
			int lzz = MathHelper.floor_double(owner.posZ);

			owner.maidAvatar.getValue();

			if (!shouldBlockBeMined(targetX, targetY, targetZ) || Math.abs(ltx-lxx)>2 || Math.abs(lty-lyy)>3 || Math.abs(ltz-lzz)>2 || !canBlockBeSeen(targetX, targetY, targetZ, true, true, false))
			{
				owner.aiFindBlock.setEnable(true);
				owner.aiWander.setEnable(true);
				doMine = false;
				ltx = lxx;
				lty = lyy;
				ltz = lzz;

				int lil[] = {lyy, lyy - 1, lyy + 1, lyy + 2, lyy + 3};

				for (int x = -1; x < 2; x++)
				{
					for (int z = -1; z < 2; z++)
					{
						for (int lyi : lil)
						{
							boolean isMineable = shouldBlockBeMined(lxx + x, lyi-1, lzz + z);
							if (isMineable && (item instanceof ItemPickaxe || TriggerSelect.checkItem(owner.getMaidMaster(), "Pickaxe", tool)) && canBlockBeSeen(lxx + x, lyi - 1, lzz + z, true, true, false))
							{
								doMine = isMineable;
								ltx = lxx + x;
								lty = lyi - 1;
								ltz = lzz + z;
							}
						}
					}
				}
			}

			if (doMine)
			{
				if (targetX == ltx && targetY == lty && targetZ == ltz)
				{
					Block theBlock = worldObj.getBlock(targetX, targetY, targetZ);
					int completed = (int)Math.floor((timeMined/mineTime)*8);

					owner.getLookHelper().setLookPosition(targetX, targetY, targetZ, 10F, owner.getVerticalFaceSpeed());
					owner.setSwing(10, EnumSound.installation);
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
							theBlock.dropBlockAsItem(worldObj, targetX, targetY, targetZ, worldObj.getBlockMetadata(targetX, targetY, targetZ), 0);
							theBlock.dropXpOnBlockBreak(worldObj, targetX, targetY, targetZ, theBlock.getExpDrop(worldObj, worldObj.getBlockMetadata(targetX, targetY, targetZ), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, tool)));
						}
						worldObj.setBlockToAir(targetX, targetY, targetZ);
						worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY, targetZ, -1);

						tool.damageItem(1, owner);

						timeMined = 0.0f;
						owner.getNavigator().clearPathEntity();
						owner.aiFindBlock.setEnable(true);
						owner.aiWander.setEnable(true);
					}
					else
					{
						String toolMaterial = item.getToolMaterialName();
						timeMined += item.func_150913_i().getEfficiencyOnProperMaterial();
					}
				}
				else
				{
					owner.worldObj.destroyBlockInWorldPartially(-1, targetX, targetY, targetZ, -1);

					targetX = ltx;
					targetY = lty;
					targetZ = ltz;

					timeMined = 0;
					mineTime = Helper.getMineTime(worldObj, targetX, targetY, targetZ, tool);

					owner.aiFindBlock.setEnable(false);
					owner.aiWander.setEnable(false);
				}*/

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
