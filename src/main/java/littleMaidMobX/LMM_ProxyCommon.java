package littleMaidMobX;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import network.W_Message;
import SupplySugarMachine.SupplySugar_Render;
import SupplySugarMachine.TileEntitySupplySugar;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class LMM_ProxyCommon
{
	public void init() {}
	public void postInit() {}
	public void onItemPickup(EntityPlayer lmm_EntityLittleMaidAvatar,Entity entity, int i) {}
	public void onCriticalHit(EntityPlayer pAvatar, Entity par1Entity) {}
	public void onEnchantmentCritical(EntityPlayer pAvatar, Entity par1Entity) {}
	public void clientCustomPayload(W_Message var2) {}
	public EntityPlayer getClientPlayer(){ return null; }
	public void loadSounds(){}

	public SupplySugar_Render render = new SupplySugar_Render();

	public boolean isSinglePlayer()
	{
		return MinecraftServer.getServer().isSinglePlayer();
	}

	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntitySupplySugar.class, "SupplySugarMachineTile");
	}

	public void initSupplySugarBlock() {
		ClientRegistry.registerTileEntity(TileEntitySupplySugar.class, "SupplySugarMachineBlock", render);
	}
}
