package SupplySugarMachine;

import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerTileEntity()
	{
		//ClientRegistry.registerTileEntity(TileEntitySupplySugar.class, "SupplySugarMachineTile", new SupplySugar_Render());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySupplySugar.class, new SupplySugar_Render());
	}

	@Override
	public int getRenderID()
	{
		return RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public World getClientWorld() {

		return FMLClientHandler.instance().getClient().theWorld;
	}
}
