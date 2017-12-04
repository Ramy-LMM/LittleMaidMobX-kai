package mmmlibx.lib;

import java.io.File;

import littleMaidMobX.LMM_OldZipTexturesLoader;

public class MMM_ProxyClient extends MMM_ProxyCommon
{
	public boolean isClient()
	{
		return true;
	}

	@Override
	public void addTextureToOldZipLoader(String name, File file) {
		LMM_OldZipTexturesLoader.keys.put(name, file);
	}
}
