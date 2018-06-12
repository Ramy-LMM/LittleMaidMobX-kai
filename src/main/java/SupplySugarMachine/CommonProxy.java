package SupplySugarMachine;

import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {
	public int getRenderID() {
		return -1;
	}

	public World getClientWorld() {
		return null;
	}

	public void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntitySupplySugar.class, "SupplySugarMachineTile");
	}

	/*public void initSupplySugarBlock() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySupplySugar.class, new SupplySugar_Render());
	}*/
}
