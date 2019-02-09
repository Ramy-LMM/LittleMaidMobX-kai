package littleMaidMobX;

import mmmlibx.lib.MMM_Helper;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MathHelper;

public class LMM_EntityMode_Lumberjack extends LMM_EntityModeBase {

	public static final int mmode_Lumberjack = 0x0025;

	private int targetX;
	private int targetY;
	private int targetZ;
	private int logNum;
	private float timeCut = 0; //Measured in ticks
	private float cutTime; //Measured in ticks
	private boolean isJump = false;
	private boolean isCutTree = false;
	private boolean isCutUnder = false;

	public LMM_EntityMode_Lumberjack(LMM_EntityLittleMaid pEntity) {
		super(pEntity);

		targetX = 0;
		targetY = 0;
		targetZ = 0;
		logNum = 0;
	}

	@Override
	public void init() {
		LMM_TriggerSelect.appendTriggerItem(null, "Axe", "");
	}

	@Override
	public int priority() {
		// TODO 自動生成されたメソッド・スタブ
		return 6400;
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting) {
		// TODO 自動生成されたメソッド・スタブ
		EntityAITasks[] ltasks = new EntityAITasks[2];
		//ltasks[0] = pDefaultMove;
		//ltasks[1] = pDefaultTargeting;
		ltasks[0] = new EntityAITasks(owner.aiProfiler);
		ltasks[1] = new EntityAITasks(owner.aiProfiler);

		ltasks[0].addTask(1, owner.aiSwiming);
		ltasks[0].addTask(2, owner.func_70907_r());
		ltasks[0].addTask(3, owner.aiJumpTo);
		ltasks[0].addTask(4, owner.aiFindBlock);
		ltasks[0].addTask(5, owner.aiPanic);
		ltasks[0].addTask(6, owner.aiBeg);
		ltasks[0].addTask(7, owner.aiBegMove);
		ltasks[0].addTask(8, owner.aiAvoidPlayer);
		ltasks[0].addTask(9, owner.aiCollectItem);
		ltasks[0].addTask(10, owner.aiFollow);
		ltasks[0].addTask(11, owner.aiWander);

		ltasks[0].addTask(52, new EntityAIWatchClosest(owner, EntityLivingBase.class, 10F));
		ltasks[0].addTask(51, new EntityAILookIdle(owner));

		owner.addMaidMode(ltasks, "Lumberjack", mmode_Lumberjack);
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		ItemStack litemstack = owner.maidInventory.getStackInSlot(0);
		if (litemstack != null)
		{
			if ((litemstack.getItem() instanceof ItemAxe) || (LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Axe", litemstack)))
			{
				owner.setMaidMode("Lumberjack");
				setSaplingOnHead();
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
			case mmode_Lumberjack :
				owner.setBloodsuck(false);
				owner.aiAttack.setEnable(false);
				owner.aiShooting.setEnable(false);
				setSaplingOnHead();
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
		case mmode_Lumberjack :
			for (li = 0; li < owner.maidInventory.maxInventorySize; li++)
			{
				litemstack = owner.maidInventory.getStackInSlot(li);
				if (litemstack == null)
				{
					continue;
				}

				if (litemstack.getItem() instanceof ItemAxe || LMM_TriggerSelect.checkItem(owner.getMaidMaster(), "Axe", litemstack))
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
	public boolean isSearchBlock() {
		return !owner.isMaidWait() && (owner.getCurrentEquippedItem() != null);
	}

	@Override
	public boolean shouldBlock(int pMode) {
		if ((pMode != mmode_Lumberjack)) return false;
		return (owner.getCurrentEquippedItem() != null);
	}

	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz) {
		if(pMode != mmode_Lumberjack) return false;

		if (owner.isFreedom()){
			if (owner.getHomePosition().getDistanceSquared(
					MathHelper.floor_double(owner.posX),
					MathHelper.floor_double(owner.posY),
					MathHelper.floor_double(owner.posZ)) > limitDistance_Freedom) {
				return false;
			}
		}
		else if (owner.getMaidMasterEntity() != null) {
			if (owner.getMaidMasterEntity().getDistanceSq(px, py, pz) > limitDistance_Follow) {
				return false;
			}
		}
		if (owner.isMaidWait()) {
			return false;
		}

		if(isCutTree) {
			return true;
		}

		// 原木の探索
		if (isLogBlock(px, py, pz)) {
			timeCut = 0;
			cutTime = MMM_Helper.getMineTime(owner.worldObj, px, py, pz, owner.getCurrentEquippedItem());
			targetX = px;
			targetY = py;
			targetZ = pz;
			isCutTree = true;
			logNum = 0;
			int num = 1;
			while(true) {
				if (!isLogBlock(px, py + num, pz)) {
					break;
				}
				num++;
			}

			if(num > 4) {
				isJump = true;
				targetY++;
			}

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
	public boolean executeBlock(int pMode, int px, int py, int pz) {
		if(pMode != mmode_Lumberjack) return false;

		Block theBlock = owner.worldObj.getBlock(targetX, targetY + logNum, targetZ);
		ItemStack item = owner.getCurrentEquippedItem();
		if (item == null) return false;

		if (owner.getDistanceSq(targetX, targetY, targetZ) < 16D) {
			int completed = (int)Math.floor((timeCut / cutTime) * 8);

			owner.getLookHelper().setLookPosition(targetX, targetY + logNum, targetZ, owner.getRotationYawHead(), owner.getVerticalFaceSpeed());
			owner.setSwing(5, LMM_EnumSound.findTarget_B);
			owner.worldObj.playSoundEffect((double)targetX + 0.5D, (double)targetY + logNum + 0.5D, (double)targetZ + 0.5D, theBlock.stepSound.soundName, 10000.0F, 0.8F + owner.worldObj.rand.nextFloat() * 0.2F);
			owner.worldObj.playSound((double)targetX + 0.5D, (double)targetY + logNum + 0.5D, (double)targetZ + 0.5D, Blocks.log.stepSound.getBreakSound(), 1.0f, (owner.worldObj.rand.nextFloat() * 0.2F) + 0.95F, false);
			owner.worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY + logNum, targetZ, completed);

			if(completed >= 8) {
				theBlock.dropBlockAsItem(owner.worldObj, targetX, targetY + logNum, targetZ, owner.worldObj.getBlockMetadata(targetX, targetY + logNum, targetZ), 0);
				owner.worldObj.setBlockToAir(targetX, targetY + logNum, targetZ);
				owner.worldObj.destroyBlockInWorldPartially(owner.getEntityId(), targetX, targetY + logNum, targetZ, -1);

				item.damageItem(1, owner);

				timeCut = 0.0f;
				owner.getNavigator().clearPathEntity();
				owner.aiWander.setEnable(true);

				logNum++;
				if (isLogBlock(targetX, targetY + logNum, targetZ)) {
					if((logNum == 2) && isJump) {
						owner.worldObj.setBlockToAir((int)owner.posX, (int)owner.posY + 1, (int)owner.posZ);
						owner.worldObj.setBlockToAir((int)owner.posX, (int)owner.posY + 2, (int)owner.posZ);
						owner.getNavigator().tryMoveToXYZ(targetX, targetY, targetZ, 1.0F);
						//owner.aiWander.setEnable(false);
					}
					isCutUnder = true;
				}
				else {
					if(isCutUnder) {
						logNum = 1;
						isCutUnder = false;
					}
					if (!isLogBlock(targetX, targetY - logNum, targetZ)) {
						isJump = false;
						isCutTree = false;
						logNum = 0;
						for (int li = 0; li < owner.maidInventory.maxInventorySize; li++)
						{
							ItemStack itemStack = owner.maidInventory.getStackInSlot(li);
							if (itemStack == null)
							{
								continue;
							}
							if(Item.getIdFromItem(itemStack.getItem()) == Block.getIdFromBlock(Blocks.sapling)) {
								itemStack.tryPlaceItemIntoWorld(owner.maidAvatar, owner.worldObj, targetX, targetY - 1, targetZ, 1, 0.5F, 1.0F, 0.5F);
								break;
							}
						}
						return false;
					}
				}
			}
			else {
				ItemTool tool = (ItemTool) item.getItem();
				String toolMaterial = tool.getToolMaterialName();
				timeCut += tool.func_150913_i().getEfficiencyOnProperMaterial();
			}
		}

		return true;
	}

	@Override
	public void onUpdate(int pMode) {
		super.onUpdate(pMode);

		if (pMode == mmode_Lumberjack) {

		}
	}

	@Override
	public void updateAITick(int pMode)
	{
		if (pMode == mmode_Lumberjack)
		{
			if (owner.getCurrentEquippedItem() == null || owner.getCurrentEquippedItem().stackSize <= 0)
			{
				owner.maidInventory.setInventoryCurrentSlotContents(null);
				owner.getNextEquipItem();
			}
		}
	}

	private boolean isLogBlock(int px, int py, int pz) {
		Block block = owner.worldObj.getBlock(px, py, pz);
		if ((block == Blocks.log) || (block == Blocks.log2)){
			return true;
		}
		return false;
	}

	private void setSaplingOnHead() {
		for (int li = 0; li < owner.maidInventory.maxInventorySize; li++)
		{
			ItemStack itemStack = owner.maidInventory.getStackInSlot(li);
			if (itemStack == null)
			{
				continue;
			}

			if (Item.getIdFromItem(itemStack.getItem()) == Block.getIdFromBlock(Blocks.sapling))
			{
				ItemStack lastItemStack = owner.maidInventory.getStackInSlot(owner.maidInventory.maxInventorySize - 1);
				owner.maidInventory.setInventorySlotContents(owner.maidInventory.maxInventorySize - 1, ItemStack.copyItemStack(itemStack));
				owner.maidInventory.setInventorySlotContents(li, ItemStack.copyItemStack(lastItemStack));
				break;
			}
		}
	}
}
