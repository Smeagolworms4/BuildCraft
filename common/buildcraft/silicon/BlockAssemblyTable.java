package buildcraft.silicon;

import java.util.ArrayList;

import buildcraft.mod_BuildCraftSilicon;
import buildcraft.core.CoreProxy;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiIds;
import buildcraft.core.Utils;
import buildcraft.factory.TileAssemblyTable;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;


public class BlockAssemblyTable extends BlockContainer {

	public BlockAssemblyTable(int i) {
		super(i, Material.iron);
		// TODO Auto-generated constructor stub

		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(0.5F);

	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		if (!CoreProxy.isClient(world))
			entityplayer.openGui(mod_BuildCraftSilicon.instance, GuiIds.ASSEMBLY_TABLE, world, i, j, k);
		return true;
	}
	
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);
		super.onBlockRemoval(world, i, j, k);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (i == 1) {
			return 16 * 6 + 12;
		} else if (i == 0) {
			return 16 * 2 + 15;
		} else {
			return 16 * 6 + 11;
		}

	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileAssemblyTable();
	}
	
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
