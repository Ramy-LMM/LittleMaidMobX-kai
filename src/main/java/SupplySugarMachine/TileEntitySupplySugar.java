package SupplySugarMachine;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySupplySugar extends TileEntity implements ISidedInventory {
	private static int stackLimit = 2000000000;
	public int SugarNumber = 0;
	//private ItemStack Sugar = new ItemStack(Items.sugar, 1);
	public boolean IsOuputNoneSugarMessage = false;
	public String CustomName = "";

	@Override
	public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.SugarNumber = nbt.getInteger("sugar");
        String name = nbt.getString("name");
        if (!name.isEmpty()) {
        	this.CustomName = name;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
    	super.writeToNBT(nbt);
    	nbt.setInteger("sugar", this.SugarNumber);
    	if (!this.CustomName.isEmpty()) {
    		nbt.setString("name",this.CustomName);
    	}
    }

    /*
     * パケットの送信・受信処理。
     * カスタムパケットは使わず、バニラのパケット送受信処理を使用。
     */
    @Override
	public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
	}

	@Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.func_148857_g();
		this.readFromNBT(nbt);
    }

	@SideOnly(Side.CLIENT)
    public int getMetadata()
    {
    	return this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    }

	public void markDirty()
	{
		List<EntityPlayer> list = this.worldObj.playerEntities;
		for (EntityPlayer player : list) {
			if ((player instanceof EntityPlayerMP)) {
				((EntityPlayerMP)player).playerNetServerHandler.sendPacket(getDescriptionPacket());
			}
		}
	}

	public int getSugarSize() {
		return this.SugarNumber;
	}

	public void addSugarSize(int size) {
		int tmp_size = this.SugarNumber + size;
		if (tmp_size > this.stackLimit) {
			this.SugarNumber = this.stackLimit;
		}
		else {
			this.SugarNumber = tmp_size;
		}
		markDirty();
	}

	public void setSugarSize(int size) {
		this.SugarNumber = size;
	}

	@Override
	public int getSizeInventory() {
		// TODO 自動生成されたメソッド・スタブ
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int p_70301_1_) {
		/*
		this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("getStackInSlot: "+p_70301_1_));
		// TODO 自動生成されたメソッド・スタブ
		int num = 0;
		if(this.SugarNumber > 64) {
			num = 64;
			//this.Sugar.stackSize -= 64;
		}
		else {
			num = (int)this.SugarNumber;
			//this.Sugar.stackSize = 0;
		}

		if (num == 0) {
			return null;
		}
		else {
			return new ItemStack(Items.sugar, num);
		}
		*/
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int dec) {
		// TODO 自動生成されたメソッド・スタブ
		if ((this.SugarNumber == 0) || (slot == 1)) return null;
		int num = 0;
		if(this.SugarNumber > 64) {
			num = 64;
			this.SugarNumber -= 64;
		}
		else {
			num = this.SugarNumber;
			this.SugarNumber = 0;
		}

		markDirty();
		if (num == 0) {
			return null;
		}
		else {
			return new ItemStack(Items.sugar, num);
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		// TODO 自動生成されたメソッド・スタブ
		//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("getStackInSlotOnClosing: "+slot));
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack setStack) {
		// TODO 自動生成されたメソッド・スタブ
		//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("setInventorySlotContents: "+slot));
		if((setStack != null) && (setStack.getItem() == Items.sugar)){
			//this.SugarNumber += setStack.stackSize;
			this.addSugarSize(setStack.stackSize);
		}
	}

	@Override
	public String getInventoryName() {
		// TODO 自動生成されたメソッド・スタブ
		return "SugarSupplyMachine";
	}

	@Override
	public boolean hasCustomInventoryName() {
		// TODO 自動生成されたメソッド・スタブ
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO 自動生成されたメソッド・スタブ
		return this.stackLimit;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		// TODO 自動生成されたメソッド・スタブ
		//return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
		return true;
	}

	@Override
	public void openInventory() {
		// TODO 自動生成されたメソッド・スタブ
		//closeInventory();
	}

	@Override
	public void closeInventory() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@SideOnly(Side.SERVER)
	public void renderMarker(int px, int py, int pz) {
		//LMM_LittleMaidMobX.proxy.render.renderTileEntityAt(this, px, py, pz, 1.0F);
		//SupplySugar_Render render = new SupplySugar_Render();
		//render.renderTileEntityAt(this, px, py, pz, 1.0F);
	}

	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2) {
		// TODO 自動生成されたメソッド・スタブ
		if ((par2 != null) && (par2.getItem() == Items.sugar) && (par2.stackSize > 0)) {
			//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("isItemValidForSlot: "+par1+", "+par2.stackSize));
			par2.stackSize -= 1;
			this.addSugarSize(1);
			return true;
		}
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		// TODO 自動生成されたメソッド・スタブ
		//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("getAccessibleSlotsFromSide: "+p_94128_1_));
		return new int[] { 0, 1, 2 };
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack var2, int side) {
		// TODO 自動生成されたメソッド・スタブ
		//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("入れる: "+slot+", "+side));
		/*
		*/
		if ((var2 != null) && (var2.getItem() == Items.sugar)) {
			return true;
		}
		return false;
		//return this.isItemValidForSlot(slot, var2);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack var2, int side) {
		// TODO 自動生成されたメソッド・スタブ
		//this.worldObj.getPlayerEntityByName("aoyanagiYuu").addChatMessage(new ChatComponentText("出す？:"+slot+", "+side));
		return false;
	}
}
