package littleMaidMobX;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class LMM_ItemDismissalNotice extends Item
{
	public LMM_ItemDismissalNotice()
    {
        super();
        maxStackSize = 64;
		setCreativeTab(CreativeTabs.tabMisc);
    }
}
