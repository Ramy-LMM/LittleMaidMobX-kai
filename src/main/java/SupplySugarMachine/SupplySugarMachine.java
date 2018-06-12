package SupplySugarMachine;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(
		modid = "SupplySugarMachine",
		name = "SupplySugarMachine",
		version = "1.7.10_1.0"
		)
public class SupplySugarMachine {
	@Instance("FluidTankTutorial")
	public static SupplySugarMachine instance;

	public static SupplySugar_Block supplySugarBlock;
	public static int SupplySugar_RenderID;

	@SidedProxy(
			clientSide = "SupplySugarMachine.ClientProxy",
			serverSide = "SupplySugarMachine.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent evt)
	{
		//砂糖供給ブロックを追加
		supplySugarBlock = new SupplySugar_Block();
		GameRegistry.registerBlock(supplySugarBlock, SupplySugar_ItemBlock.class, "SupplySugarMachineBlock");
		GameRegistry.addRecipe(new ItemStack(supplySugarBlock, 1, 1), new Object[]{
				"RRR",
				"RSR",
				"RRR",
				'R', Blocks.cobblestone,
				'S', Items.sugar
		});
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		//レンダーを追加
		SupplySugar_RenderID = proxy.getRenderID();
		//RenderingRegistry.registerBlockHandler(new SupplySugar_Render());
		proxy.registerTileEntity();
	}
}
