package SupplySugarMachine;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SupplySugar_Block extends BlockContainer {
	public IIcon updownIcon;
	public IIcon sidedIcon;
	public IIcon markerIcon;

	public SupplySugar_Block(Material p_i45394_1_) {
		super(p_i45394_1_);
		setCreativeTab(CreativeTabs.tabTools);
        setBlockName("SupplySugarMachineBlock");/*システム名の設定*/
        //setBlockTextureName("freedommod:sugarsupplymachine_horizontal");/*ブロックのテクスチャの指定(複数指定の場合は消してください)*/
        setHardness(5.0F);/*硬さ*/
		this.setHarvestLevel("pickaxe", 1);/*回収するのに必要なツール*/
        setResistance(100000.0F);/*爆破耐性*/
        setStepSound(Block.soundTypeStone);/*ブロックの上を歩いた時の音*/
	/*setBlockUnbreakable();*//*ブロックを破壊不可に設定*/
	/*setTickRandomly(true);*//*ブロックのtick処理をランダムに。デフォルトfalse*/
	/*disableStats();*//*ブロックの統計情報を保存しない*/
        setLightOpacity(0);/*ブロックの透過係数。デフォルト０（不透過）*/
        setLightLevel(1.0F);/*明るさ 1.0F = 15*/
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);/*当たり判定*/
		// TODO 自動生成されたコンストラクター・スタブ
	}

	private void getChestPositionWithNBT(EntityPlayer player, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("author", player.getDisplayName());
		nbt.setString("title", "position");
		NBTTagList bookTag = new NBTTagList();
		bookTag.appendTag(new NBTTagString(x + "," + y + "," + z));
		nbt.setTag("pages", bookTag);
		ItemStack writtenBook = new ItemStack(Items.written_book, 1);
		writtenBook.setTagCompound(nbt);
		player.inventory.addItemStackToInventory(writtenBook);
		//EntityItem eItem = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, writtenBook);
		//player.worldObj.spawnEntityInWorld(eItem);
	}

	@Override
	public int getRenderType()
	{
		return 0;
		//return LMM_LittleMaidMobX.SupplySugar_RenderID;
	}

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
    public String getLocalizedName()
    {
        return "SupplySugarMachineBlock";
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		super.registerBlockIcons(iconRegister);
		updownIcon = iconRegister.registerIcon("supplysugar:supplysugarmachine_vertical");
		sidedIcon = iconRegister.registerIcon("supplysugar:supplysugarmachine_horizontal");
		markerIcon = iconRegister.registerIcon("supplysugar:supplysugarmachine_marker");
	}

	/*
	 * 面によって利用するアイコンを変更するメソッド.
	 * 引数のsideはブロックの上下東西南北(0~5の整数), metaはブロックのメタデータ.
	 * 上下東西南北を0~5で表すのはわかりづらいので, ここではForgeDirectionで定義されるEnum定数を利用している.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if ((side == ForgeDirection.EAST.ordinal()) || (side == ForgeDirection.NORTH.ordinal()) ||
			(side == ForgeDirection.SOUTH.ordinal()) || (side == ForgeDirection.WEST.ordinal())) {
			return sidedIcon;
		}
		return updownIcon;
	}

	@Override
	public void onBlockPlacedBy(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_, EntityLivingBase p_149689_5_, ItemStack p_149689_6_) {
		if (!p_149689_1_.isRemote) {
			//p_149689_1_.setTileEntity(p_149689_2_, p_149689_3_, p_149689_4_, tileentitysupply);
			TileEntitySupplySugar tile = (TileEntitySupplySugar)p_149689_1_.getTileEntity(p_149689_2_, p_149689_3_, p_149689_4_);
			//p_149689_1_.getPlayerEntityByName("aoyanagiyuu").addChatMessage(new ChatComponentText("tileentitysupply： " + tileentitysupply));
			if (tile == null) {
				tile = new TileEntitySupplySugar();
				//tileentitysupply.count = sugarNumber;
			}
			//p_149689_1_.getPlayerEntityByName("aoyanagiyuu").addChatMessage(new ChatComponentText("tile entity： "+tileentitysupply));
			if (p_149689_6_.hasTagCompound()) {
				NBTTagCompound nbt = p_149689_6_.getTagCompound();
				//tile.SugarNumber = nbt.getLong("sugar");
				tile.setSugarSize(nbt.getInteger("sugar"));
			}
			else {
				tile.setSugarSize(1);
			}
			//p_149689_1_.getPlayerEntityByName("aoyanagiyuu").addChatMessage(new ChatComponentText("設置ブロック 砂糖数："+sugarNumber));
		}
	}

    @Override
    public int quantityDropped(Random p_149745_1_)
    {
        return 0;
    }

    @Override
    public void harvestBlock(World world, EntityPlayer entityplayer, int x, int y, int z, int l) {
    	/*if (!world.isRemote) {
    		TileEntitySupply tileentitysupply = (TileEntitySupply)world.getTileEntity(x, y, z);
    		System.out.println("tileentitysupply: "+world.getTileEntity(x, y, z) +", "+x+", "+y+", "+z);
    		ItemStack items = new ItemStack(this, 1);
    		NBTTagCompound nbt = new NBTTagCompound();
    		//nbt.setLong("sugar", tileentitysupply.SugarNumber);
    		items.setTagCompound(nbt);
    		EntityItem eItem = new EntityItem(world, x, y, z, items);
    		eItem.dropItem(items.getItem(), 1);
    		//p_149749_1_.getPlayerEntityByName("aoyanagiyuu").addChatMessage(new ChatComponentText("壊した、お前が"));
    	}*/
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block p_149749_5_, int p_149749_6_) {
    	if (!world.isRemote) {
    		//System.out.println("breakBlock: "+world.getTileEntity(x, y, z) +", "+x+", "+y+", "+z);
    		TileEntitySupplySugar tile = (TileEntitySupplySugar)world.getTileEntity(x, y, z);
    		//System.out.println("tileentitysupply: "+world.getTileEntity(x, y, z) +", "+x+", "+y+", "+z);
    		ItemStack items = new ItemStack(this, 1);
    		NBTTagCompound nbt = new NBTTagCompound();
    		//nbt.setLong("sugar", tile.SugarNumber);
    		nbt.setInteger("sugar", tile.getSugarSize());
    		items.setTagCompound(nbt);
    		EntityItem eItem = new EntityItem(world, x, y, z, items);
    		//eItem.dropItem(items.getItem(), 1);
    		world.spawnEntityInWorld(eItem);
    		//p_149749_1_.getPlayerEntityByName("aoyanagiyuu").addChatMessage(new ChatComponentText("壊した、お前が"));
    	}
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float posX, float posY, float posZ) {
		//ブロックを右クリックした際の動作
		boolean isCorrectSugar = false;
		ItemStack item = player.inventory.getCurrentItem();
		TileEntity tile = world.getTileEntity(x, y, z);
		if (item != null) {
			if (item.getItem() == Items.book) {
				//IInventory inventory = ((BlockChest)block).func_149951_m(event.world, event.x, event.y, event.z);
				//IInventory inventory = (IInventory) tile;
				//player.addChatMessage(new ChatComponentText("sugar info: "+inventory));
				getChestPositionWithNBT(player, x, y, z);
				item.stackSize = item.stackSize - 1;
			}
			else if (item.getItem() == Items.name_tag) {
				((TileEntitySupplySugar)tile).CustomName = item.getDisplayName();
				item = null;
			}
			else if (item.getItem() == Items.written_book) {
				NBTTagCompound nbt = item.getTagCompound();
				String title = nbt.getString("title");
				if ((title != null) && (title.equals("position"))) {
					NBTTagList bookTag = (NBTTagList)nbt.getTag("pages");
					//player.addChatMessage(new ChatComponentText("bookTag: "+bookTag));
					boolean isWritten = false;
					for (int i = 0; i < bookTag.tagCount(); i++){
						String[] split = bookTag.getStringTagAt(i).split(",");
						if (x == Integer.parseInt(split[0]) &&
							y == Integer.parseInt(split[1]) &&
							z == Integer.parseInt(split[2])) {
							isWritten = true;
							break;
						}
					}
					if (!isWritten) {
						bookTag.appendTag(new NBTTagString(x + "," + y + "," + z));
						nbt.setTag("pages", bookTag);
						item.setTagCompound(nbt);
					}
				}
			}
			else {
    			isCorrectSugar = true;
			}
		}
		else {
			isCorrectSugar = true;
		}

		if (isCorrectSugar) {
			TileEntitySupplySugar tileentitysupply = (TileEntitySupplySugar)tile;
	    	//player.addChatMessage(new ChatComponentText("onBlockActivated： " + tileentitysupply));
	    	for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
	    		ItemStack itemStack = player.inventory.getStackInSlot(i);
	    		if ((itemStack != null) && (itemStack.getItem() == Items.sugar)) {
	    			tileentitysupply.addSugarSize(itemStack.stackSize);
	    			//itemStack.stackSize = -1;
	        		player.inventory.setInventorySlotContents(i, null);
	    		}
	    	}
	    	int size = tileentitysupply.getSugarSize();
	    	//System.out.println(tileentitysupply.getClass().getName());
			if (!world.isRemote) {
		    	player.addChatMessage(new ChatComponentText("砂糖の数： " + size));
			}
			if ((size > 0) && (tileentitysupply.IsOuputNoneSugarMessage)) {
				tileentitysupply.IsOuputNoneSugarMessage = false;
			}
			player.inventory.markDirty();
		}
    	return true;
    }

	public boolean hasTileEntity()
    {
        return true;
    }

	public boolean canProvidePower()
	{
		return true;
	}

	@Override
    public int isProvidingWeakPower(IBlockAccess p_149748_1_, int px, int py, int pz, int side)
    {
		TileEntity tile = p_149748_1_.getTileEntity(px, py, pz);
		if(tile instanceof TileEntitySupplySugar) {
			int count = ((TileEntitySupplySugar)tile).getSugarSize();
			int level = count / 320;
			return level;
		}
		return 0;
    }

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		// TODO 自動生成されたメソッド・スタブ
        return new TileEntitySupplySugar();
	}
}
