package buildcraft.core;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.api.core.IIconProvider;

public class CoreIconProvider implements IIconProvider {

	public static int ENERGY 	= 0;
	
	public static int MAX 		= 1;

	private Icon[] _icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		return _icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		_icons = new Icon[MAX];
		
		_icons[ENERGY] = iconRegister.func_94245_a("buildcraft:icons/energy");
		
	}

}
