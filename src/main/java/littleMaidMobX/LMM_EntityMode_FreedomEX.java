package littleMaidMobX;

import static littleMaidMobX.LMM_Statics.*;

import java.util.ArrayList;
import java.util.List;

import mmmlibx.lib.MMM_Helper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import scala.util.Random;
import SupplySugarMachine.TileEntitySupplySugar;

public class LMM_EntityMode_FreedomEX extends LMM_EntityMode_Basic {
	public static final int mmode_FreedomEX = 0x3212;
	private boolean modeSearchChest = false;
	private int coolTime = 0;
	private int[][] chestPosition = null;
	private int chestPositionIndex = -1;
	private int[] startSearchPos = null;
	private List<Integer> searchedChestList = new ArrayList<Integer>();
	private boolean isMoving = false;
	private boolean isSugarSupplyMachine = false;
	private int[] prePos = null;
	private int freezeTime = 0;
	private final Random rand = new Random();
	private int sugarCount = 0;
	private int updateSugarCount = 30; //30tick(1.5s)
	private int attackTime = 0;
	private double[] attackPoint = null;
	private String attackPlayerName = null;

	public LMM_EntityMode_FreedomEX(LMM_EntityLittleMaid pEntity) {
		super(pEntity);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected void clearMy() {
		super.clearMy();
		owner.clearTilePos();
		owner.setTarget(null);
		owner.setWorking(false);
		modeSearchChest = false; // リセット
		owner.aiWander.setEnable(true);
		//searchedChestList.clear();
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting) {
		EntityAITasks[] Itasks = new EntityAITasks[2];
		Itasks[0] = pDefaultMove;
		Itasks[1] = pDefaultTargeting;
		//Itasks[0] = new EntityAITasks(owner.aiProfiler);
		//Itasks[1] = new EntityAITasks(owner.aiProfiler);
		Itasks[0].addTask(1, owner.aiAttack);
		Itasks[0].addTask(2, owner.aiSwiming);
		Itasks[0].addTask(3, owner.aiJumpTo);
		Itasks[1].addTask(1, new LMM_EntityAIHurtByTarget(owner, true));
		Itasks[1].addTask(2, new LMM_EntityAINearestAttackableTarget(owner, EntityPlayerMP.class, 0, true));
		owner.addMaidMode(Itasks, "FreedomEX", mmode_FreedomEX);
	}

	private boolean chechWrittenBookAtFirstInv(){
		ItemStack itemstack = owner.maidInventory.getStackInSlot(0);
		if (itemstack != null) {
			if (itemstack.getItem() == Items.written_book) {
				NBTTagCompound nbt = itemstack.getTagCompound();
				if (nbt != null) {
					String title = nbt.getString("title");
					if ((title != null) && title.equals("position")){
						NBTBase base = nbt.getTag("pages");
						if (base != null) {
							NBTTagList tagList = (NBTTagList)base;
							List<int[]> list = new ArrayList<int[]>();
							for (int i = 0; i < tagList.tagCount(); i++) {
								String[] split = tagList.getStringTagAt(i).split(",");
								if (split.length == 3) {
									int x = Integer.parseInt(split[0]);
									int y = Integer.parseInt(split[1]);
									int z = Integer.parseInt(split[2]);
									TileEntity tile = owner.worldObj.getTileEntity(x, y, z);
									if (tile != null) {
										list.add(new int[]{x, y, z});
									}
									else {
										tagList.removeTag(i);
									}
								}
							}
							chestPosition = new int[list.size()][3];
							for (int i = 0; i < list.size(); i++) {
								chestPosition[i] = list.get(i);
							}
							nbt.setTag("pages", tagList);
							itemstack.setTagCompound(nbt);

							chestPositionIndex = 0;
							prePos = new int[]{chestPosition[0][0], chestPosition[0][1], chestPosition[0][2]};
							//owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("position: "+chestPosition[0]+", "+chestPosition[1]+", "+chestPosition[2]));
							TileEntity tile = owner.worldObj.getTileEntity(chestPosition[0][0], chestPosition[0][1], chestPosition[0][2]);
							if (tile instanceof TileEntitySupplySugar) {
								isSugarSupplyMachine = true;
								modeSearchChest = false;
							}
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		boolean check = chechWrittenBookAtFirstInv();
		if (check) {
			owner.setMaidMode("FreedomEX");
			owner.aiWander.setEnable(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode) {
		switch (pMode) {
			case mmode_FreedomEX:
				//owner.setBloodsuck(true);
				owner.setFreedom(true);
				owner.aiWander.setEnable(true); // デフォルト false
				owner.aiJumpTo.setEnable(false);
				owner.aiFollow.setEnable(false);
				owner.aiAvoidPlayer.setEnable(false);
				sugarCount = countOfSugar();
				return true;
		}
		return false;
	}

	private int containsPosition(List<int[]> list, int[] checkPos) {
		for (int i = 0; i < list.size(); i++) {
			int[] pos = list.get(i);
			if ((pos[0] == pos[0]) && (pos[1] == pos[1]) && (pos[2] == pos[2])) {
				return i;
			}
		}
		return -1;
	}

	private void jumpToSupplySugarBlock() {
		//LMM_EntityAIJumpToMaster.javaから引用
		int i = MathHelper.floor_double(chestPosition[chestPositionIndex][0]) - 2;
		int j = MathHelper.floor_double(chestPosition[chestPositionIndex][2]) - 2;
		int k = MathHelper.floor_double(owner.boundingBox.minY);

		for (int l = 0; l <= 4; l++) {
			for (int i1 = 0; i1 <= 4; i1++) {
				if (l < 1 || i1 < 1 || l > 3 || i1 > 3) {
					double dd = owner.getDistanceSq(
							(double) (i + l) + 0.5D + MathHelper.sin(owner.rotationYaw * 0.01745329252F) * 2.0D,
							(double) k,
							(double) (j + i1) - MathHelper.cos(owner.rotationYaw * 0.01745329252F) * 2.0D);
					if (dd > 8D) {
						owner.setTarget(null);
						owner.setRevengeTarget(null);
						owner.getNavigator().clearPathEntity();
						owner.setLocationAndAngles(
								(float) (i + l) + 0.5F, k, (float) (j + i1) + 0.5F,
								owner.rotationYaw, owner.rotationPitch);
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz){
		/*if (modeSearchChest || isMoving) {
			owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("動いてる！modeSearchChest: "+modeSearchChest+", isMoving: "+isMoving));
			//clearMy();
			return false;
		}*/
		if (chestPosition == null) {
			chechWrittenBookAtFirstInv();
		}
		if (isSugarSupplyMachine && modeSearchChest) {
			int[] pos = {chestPosition[chestPositionIndex][0], chestPosition[chestPositionIndex][1], chestPosition[chestPositionIndex][2]};
			double distance = owner.getDistance(pos[0], pos[1], pos[2]);//.getDistanceTilePos();
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("distance: " + distance));
			if (distance < 3.0) {
				TileEntitySupplySugar tile = (TileEntitySupplySugar)owner.worldObj.getTileEntity(pos[0], pos[1], pos[2]);
				if (tile.getSugarSize() == 0) {
					//砂糖供給機が空だったら、次に距離の短い砂糖供給機へと向かう
					/*
					double minLength = owner.getDistanceSq(chestPosition[0][0], chestPosition[0][1], chestPosition[0][2]);
					int index = 0;
					for (int i = 1; i < chestPosition.length; i++) {
						if (searchedChestList.indexOf(i) == -1) {
							double length = owner.getDistanceSq(chestPosition[i][0], chestPosition[i][1], chestPosition[i][2]);
							if (length < minLength) {
								chestPositionIndex = i;
							}
						}
					}
					*/
					chestPositionIndex = searchNearestChest();
					//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("searchedChestList: " + searchedChestList.size()));
					owner.getNavigator().tryMoveToXYZ(chestPosition[chestPositionIndex][0], chestPosition[chestPositionIndex][1], chestPosition[chestPositionIndex][2], 1.0);
				}
				else {
					isMoving = false;
				}
				if (isOverStackSugar(5) || (owner.maidInventory.getFirstEmptyStack() == -1)) {
					return false;
				}
				else {
					return true;
				}
			}
			else {
				PathNavigate lpn = owner.getNavigator();
				boolean bpath = lpn.noPath();
				if ((modeSearchChest && isMoving) && bpath) {
					int x = chestPosition[chestPositionIndex][0];
					int y = chestPosition[chestPositionIndex][1];
					int z = chestPosition[chestPositionIndex][2];
					double aspect = 20.0 / distance;
					if (distance > 20.0) {
						x = (int) (owner.posX - (owner.posX - chestPosition[chestPositionIndex][0]) * aspect);
						y = (int) (owner.posY - (owner.posY - chestPosition[chestPositionIndex][1]) * aspect);
						z = (int) (owner.posZ - (owner.posZ - chestPosition[chestPositionIndex][2]) * aspect);
						//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("aspect: " + aspect));
					}

					//メイドさんが動けない場合、一定時間経過後にワープさせる
					boolean isJump = false;
					if ((prePos[0] == x) && (prePos[1] == y) && (prePos[2] == z)) {
						//long time = System.currentTimeMillis() - startTime;
						//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("Time: "+ freezeTime));
						if (freezeTime == 0) {
							isJump = true;
						}
					}
					else {
						prePos = new int[]{x, y, z};
						freezeTime = 1200; //一応60sec後。処理速度によってもっと遅くなったりするかも
						//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("distance: " + owner.getDistance(x, y, z)+", aspect: "+aspect));
						//TileEntity tile = owner.worldObj.getTileEntity(chestPosition[0], chestPosition[1], chestPosition[2]);
						//MMM_Helper.setPathToTile(owner, tile, false);
						bpath = owner.getNavigator().tryMoveToXYZ(x, y, z, 1.0);
						//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("パスを作りました。"));
					}

					if (isJump) {
						jumpToSupplySugarBlock();
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean checkItemStack(ItemStack pItemStack) {
		return true; // インベントリに空きがあればアイテムは拾いに行く。けど AICollectItem 内でも判定しているみたい
	}

	private void randomMove(int px, int py, int pz) {
		int x = rand.nextInt(4) + 3;
		int z = rand.nextInt(4) + 3;
		owner.getNavigator().tryMoveToXYZ(px + x, py, pz + z, 1.0);
	}


	private void outputNoneSugarMessage(TileEntitySupplySugar tile, int px, int py, int pz) {
		if (tile.getSugarSize() != 0) return;

		if (searchedChestList.indexOf(chestPositionIndex) == -1) {
			searchedChestList.add(chestPositionIndex);
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("探索済み追加: "+chestPositionIndex));
		}
		if(tile.IsOuputNoneSugarMessage == false) {
			//tile.renderMarker(px, py, pz);
			String text = "";
			if (tile.CustomName.isEmpty()) {
				text = "(" + px + ", " + py + ", " + pz + ")の砂糖供給機の中身が空です。";
			}
			else {
				text = tile.CustomName + "の砂糖供給機の中身が空です。";
			}
			owner.getMaidMasterEntity().addChatMessage(new ChatComponentText(text));
			tile.IsOuputNoneSugarMessage = true;
		}
	}

	private boolean getSugarAtSugarSupplyMachine (int px, int py, int pz) {
		TileEntitySupplySugar tile = (TileEntitySupplySugar)owner.worldObj.getTileEntity(px, py, pz);
		/*if (!(tile instanceof IInventory)) {
			return false;
		}*/
		if ((sugarCount >= 320) || (owner.maidInventory.getFirstEmptyStack() == -1))
		{
			clearMy();
			//owner.getNavigator().tryMoveToXYZ(startSearchPos[0], startSearchPos[1], startSearchPos[2], 1.0);	//検索を始めた場所に戻る
			randomMove(px, py, pz);	//ランダムに移動
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("お腹いっぱい"));
			return true;
		}
		boolean isInTheSkirt = false;
		//IInventory inventory = ((IInventory)tile);
		for (int i = 0; i < tile.getSizeInventory(); i++) {
			//ItemStack item = tile.getStackInSlot(i);
			ItemStack item = tile.decrStackSize(0, 0);
			if ((sugarCount < 320) && (item != null) && (item.getItem() == Items.sugar)) {
				if (((sugarCount + item.stackSize) >= 320)) {
					item.stackSize -= (sugarCount + item.stackSize) - 320;
					tile.setInventorySlotContents(i, item);
				}
				else {
					tile.setInventorySlotContents(i, null);
				}
				sugarCount += item.stackSize;
				owner.maidInventory.addItemStackToInventory(item);
				isInTheSkirt = true;
			}
			outputNoneSugarMessage(tile, px, py, pz);
		}

		if ((sugarCount >= 320) || (owner.maidInventory.getFirstEmptyStack() == -1)) {
			clearMy();
			//owner.getNavigator().tryMoveToXYZ(startSearchPos[0], startSearchPos[1], startSearchPos[2], 1.0);	//検索を始めた場所に戻る
			randomMove(px, py, pz);	//ランダムに移動
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("お腹いっぱい"));
			return true;
		}

		if (isInTheSkirt) {
			owner.playSound("random.pop");
			owner.setSwing(2, LMM_EnumSound.Null);
			return true;
		}

		//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("no sugar in chest."));

		return false;
	}

	private boolean chestAction(int px, int py, int pz) {
		TileEntity tile = owner.worldObj.getTileEntity(px, py, pz);
		if (tile instanceof TileEntitySupplySugar) {
			// ブロック系のチェスト
			// 使用直前に可視判定
			if (MMM_Helper.canBlockBeSeen(owner, px, py, pz, false, true, false)) {
				owner.setWorking(true);
				return getSugarAtSugarSupplyMachine(px, py, pz);
			} else {
				//owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("missing chest"));
				// 見失った
				clearMy();
			}
		} else {
			// 想定外のインベントリ
			clearMy();
		}
		return false;
	}

	@Override
	public void init() {
		;
	}

	private int searchNearestChest() {
		int index = 0;
		double minLength = Double.MAX_VALUE;
		for (int i = 0; i < chestPosition.length; i++) {
			if (searchedChestList.indexOf(i) == -1) {
				double length = owner.getDistanceSq(chestPosition[i][0], chestPosition[i][1], chestPosition[i][2]);
				if (length < minLength) {
					index = i;
				}
			}
		}
		return index;
	}

	@Override
	public boolean isSearchBlock() {
		if(chestPosition == null){
			chechWrittenBookAtFirstInv();
		}
		if (isSugarSupplyMachine) {
			if (isOneStackSugar()) {
				//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("お砂糖少ない: modeSearchChest: "+modeSearchChest+", isMoving: "+isMoving));
				if (!modeSearchChest || !isMoving) {
					//owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("isSearchBlock Sugar: "+Integer.toString(countOfSugar())+" "+chestPosition[0]+", "+chestPosition[1]+", "+chestPosition[2]));
					//owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("searchedChestList: "+searchedChestList.size()));
					owner.aiWander.setEnable(false);
					modeSearchChest = true; // 砂糖が1スタック未満だったらチェスト探索モード
					isMoving = true; //移動中
					startSearchPos = new int[]{(int)owner.posX, (int)owner.posY, (int)owner.posZ};

					chestPositionIndex = searchNearestChest();
					//owner.worldObj.getPlayerEntityByName(owner.getMaidMaster()).addChatMessage(new ChatComponentText("chestPositionIndex: "+chestPositionIndex));

					TileEntity tile = owner.worldObj.getTileEntity(chestPosition[chestPositionIndex][0], chestPosition[chestPositionIndex][1], chestPosition[chestPositionIndex][2]);
					boolean bpath = MMM_Helper.setPathToTile(owner, tile, false);
					//boolean bpath = owner.getNavigator().tryMoveToXYZ(chestPosition[0], chestPosition[1], chestPosition[2], 1.0);
					//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("お砂糖少ない: modeSearchChest: "+modeSearchChest+", isMoving: "+isMoving+", bpath: "+bpath));
					return bpath;
				}
			}
			else {
				modeSearchChest = false;
			}
			if (0 < coolTime) {
				return false; // クールタイム中は立ち止まる
			}
		}
		return true; // 自由行動へ
	}

	@Override
	public boolean shouldBlock(int pMode) {
		if (isSugarSupplyMachine) {
			if (!modeSearchChest || !isMoving) {
				//TileEntity tile = owner.worldObj.getTileEntity(chestPosition[0], chestPosition[1], chestPosition[2]);
				//boolean bpath = MMM_Helper.setPathToTile(owner, tile, false);
				//boolean bpath = owner.getNavigator().tryMoveToXYZ(chestPosition[0], chestPosition[1], chestPosition[2], 1.0);
				//isMoving = true;
				//modeSearchChest = true;
				PathNavigate lpn = owner.getNavigator();
				boolean bpath = lpn.noPath();
				//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("shouldBlock: "+bpath));
				return !bpath;
			}
		}
		return false;
	}

	@Override
	public boolean executeBlock(int pMode, int px, int py, int pz) {
		if (isSugarSupplyMachine) {
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("executeBlock SearchChest: "+modeSearchChest));
			if (modeSearchChest) {
				if (isOverStackSugar(5) || (owner.maidInventory.getFirstEmptyStack() == -1)) {
					//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("executeBlock:５個以上かインベントリいっぱい"));
					isMoving = false;
					modeSearchChest = false;
					owner.aiWander.setEnable(true);
					owner.setWorking(false);
					searchedChestList.clear();
				}
				else {
					//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("executeBlock:それ以外"));
					World w = owner.worldObj;
					boolean result = chestAction(chestPosition[chestPositionIndex][0], chestPosition[chestPositionIndex][1], chestPosition[chestPositionIndex][2]);
					//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("executeBlock: "+result));

					if (result) {
						coolTime = 10;
					}
					return result;
				}
			}
			else {
				if (isOverStackSugar(1)) {
					isMoving = false;
					modeSearchChest = false;
					owner.aiWander.setEnable(true);
					owner.setWorking(false);
					searchedChestList.clear();
				}
			}
		}
		return false;
	}

	@Override
	public boolean outrangeBlock(int pMode, int pX, int pY, int pZ) {
		if (isSugarSupplyMachine) {
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("outrangeBlock: modeSearchChest: "+modeSearchChest+", isMoving: "+isMoving+", wait: "+!owner.isMaidWaitEx()));
			if (modeSearchChest && !isMoving) {
				return true;
			}
			boolean result = false;
			if (!owner.isMaidWaitEx()) {
				result = owner.getNavigator().tryMoveToXYZ(pX, pY, pZ, 1.0);
			}
			return result;
		}
		return false;
	}

	@Override
	public int priority() {
		return 7101;
	}

	@Override
	public void showSpecial(LMM_RenderLittleMaid prenderlittlemaid, double px, double py, double pz) {
		if (owner.isContract()) {
			prenderlittlemaid.renderSugarCount(owner, sugarCount, px, py, pz, 64);
		}
	}

	@Override
	public boolean attackEntityAsMob(int pMode, Entity pEntity) {
		if ((pMode == mmode_FreedomEX) && (pEntity instanceof EntityPlayer)) {
			//float attackDamage = (float) MMM_Helper.getAttackVSEntity(owner.maidInventory.getStackInSlot(0));
			//pEntity.attackEntityFrom(DamageSource.causeMobDamage(owner), attackDamage);
			owner.maidAvatar.attackTargetEntityWithCurrentItem(pEntity);
			//owner.aiAttack.theMaid.attackEntityAsMob(pEntity);
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("attackEntityAsMob: "+owner.aiAttack.attackRange));
			//owner.setSwing(2, LMM_EnumSound.attack);
			return true;
		}
		return false;
	}

	@Override
	public boolean damageEntity(int pMode, DamageSource par1DamageSource, float par2) {
		if ((pMode == mmode_FreedomEX) && (attackTime <= 0) && (par1DamageSource.getEntity() instanceof EntityPlayer)) {
			owner.addAttackCount(1);
			attackPoint = new double[]{owner.posX, owner.posY, owner.posZ};
			attackTime = owner.getAttackTime();
			attackPlayerName = ((EntityPlayer) par1DamageSource.getEntity()).getDisplayName();
			PotionEffect moveSpeedEffect = new PotionEffect(Potion.moveSpeed.id, attackTime, 5);
			PotionEffect damageBoostEffect = new PotionEffect(Potion.damageBoost.id, attackTime, 999);
			PotionEffect waterBreathingEffect = new PotionEffect(Potion.waterBreathing.id, attackTime, 999);
			PotionEffect resistanceEffect = new PotionEffect(Potion.resistance.id, attackTime, 999);
			PotionEffect regenerationEffect = new PotionEffect(Potion.regeneration.id, attackTime, 999);
			PotionEffect fireResistanceEffect = new PotionEffect(Potion.fireResistance.id, attackTime, 0);
			//PotionEffect digSpeed = new PotionEffect(Potion.digSpeed.id, attackTime, 999);
			owner.addPotionEffect(moveSpeedEffect);
			owner.addPotionEffect(damageBoostEffect);
			owner.addPotionEffect(waterBreathingEffect);
			owner.addPotionEffect(resistanceEffect);
			owner.addPotionEffect(regenerationEffect);
			owner.addPotionEffect(fireResistanceEffect);
			//owner.addPotionEffect(digSpeed);
			owner.attackTime = attackTime + 20;
			//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("attackEntityFrom: "+attackTime+", "+par2+", "+owner.aiAttack.attackRange));
		}
		//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("attackEntityFrom: "+par1DamageSource.damageType+","+par2));
		if (attackTime >= 0) {
			par2 = 0;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int colorMultiplier(float pLight, float pPartialTicks) {
		if (attackTime > 0) {
			return 100 << 24 | 0x00df0f0f;
		}
		else {
			return 0;
		}
	}

	@Override
	public boolean isSearchEntity() {
		return true;
	}

	@Override
	public boolean checkEntity(int pMode, Entity pEntity) {
		if ((pMode == mmode_FreedomEX) && (attackTime > 0) && (pEntity instanceof EntityPlayer)) {
			/*
			ChunkCoordinates coord = ((EntityPlayer)pEntity).getBedLocation();
			double length = coord.getDistanceSquared((int)owner.posX, (int)owner.posY, (int)owner.posZ);

			Block block = owner.worldObj.getBlock(coord.posX, coord.posY, coord.posY);
			if((length < 9.0) && (block instanceof BlockBed)) {
				block.breakBlock(owner.worldObj, coord.posX, coord.posY, coord.posY, block, 0);
			}
			*/
			return true;
		}
		//owner.getMaidMasterEntity().addChatMessage(new ChatComponentText("checkEntity: "+pEntity));

		return false;
	}

	@Override
	public void onUpdate(int pMode) {
		super.onUpdate(pMode);
		if (0 < coolTime) {
			coolTime--;
		}
		if (0 < freezeTime) {
			freezeTime--;
		}
		if (0 < updateSugarCount) {
			updateSugarCount--;
			if (updateSugarCount == 0) {
				updateSugarCount = 30;
				sugarCount = countOfSugar();
			}
		}
		if (pMode == mmode_FreedomEX) {
			if (0 < attackTime) {
				attackTime--;
				owner.showParticleFX("reddust",
						(owner.posX + (double)(rand.nextFloat() * owner.width * 2.0F)) - (double)owner.width,
						owner.posY + 0.5D + (double)(rand.nextFloat() * owner.height),
						(owner.posZ + (double)(rand.nextFloat() * owner.width * 2.0F)) - (double)owner.width,
						1.2D, 0.4D, 0.4D);
				//owner.showParticleFX("reddust", 0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
				EntityPlayer player = owner.worldObj.getPlayerEntityByName(attackPlayerName);
				if (owner.getDistanceSq(player.posX, player.boundingBox.minY, player.posZ) > 20.0) {
					Vec3 vec = player.getLookVec();
					vec.xCoord *= 2.0;
					vec.zCoord *= 2.0;
					owner.setPosition(player.posX + vec.xCoord, player.posY, player.posZ + vec.zCoord);
				}
				if (attackTime % 200 == 0) {
					EntityTNTPrimed tnt = new EntityTNTPrimed(owner.worldObj);
					tnt.fuse = 40;
					Vec3 vec = player.getLookVec();
					vec.xCoord *= 1.5;
					vec.yCoord = 1.5;
					vec.zCoord *= 1.5;
					tnt.setPosition(player.posX + vec.xCoord, player.posY + vec.yCoord, player.posZ + vec.zCoord);
					owner.worldObj.spawnEntityInWorld(tnt);
				}
				if (attackTime == 0) {
					int count = owner.getAttackCount();
					for(int i = 0; i < count; i++) {
						int num = rand.nextInt(20);
						if (num == 1) {
							owner.setMaidFlags(false, dataWatch_Flags_remainsContract);
						}
					}
					//owner.getNavigator().tryMoveToXYZ(attackPoint[0], attackPoint[1], attackPoint[2], 1.0);
				}
			}
		}
	}

	private int countOfSugar(){
		int count = 0;
		for (int i = 0; i < LMM_InventoryLittleMaid.maxInventorySize; i++) {
			ItemStack item = owner.maidInventory.getStackInSlot(i);
			if (item != null && item.getItem() == Items.sugar) {
				count += item.stackSize;
			}
		}
		return count;
	}

	private boolean isOneStackSugar() {
		if (sugarCount < 64) {
			return true;
		}
		return false;
	}

	private boolean isOverStackSugar(int num) {
		if (sugarCount >= (64 * num)) {
			return true;
		}
		return false;
	}
/*
	private boolean isMaxInventory() {
		for (int i = 0; i < LMM_InventoryLittleMaid.maxInventorySize; i++) {
			ItemStack item = owner.maidInventory.getStackInSlot(i);
			if (item == null) {
				return false;
			}
		}
		return true;
	}
	*/
}