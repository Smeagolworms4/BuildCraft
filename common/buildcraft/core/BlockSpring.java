package buildcraft.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class BlockSpring extends Block {

	public static final Random rand = new Random();

	public enum EnumSpring {

		WATER(5, -1, Block.waterStill.blockID),
		OIL(6000, 32, 0); // Set in BuildCraftEnergy
		public static final EnumSpring[] VALUES = values();
		public final int tickRate, chance;
		public int liquidBlockId;

		private EnumSpring(int tickRate, int chance, int liquidBlockId) {
			this.tickRate = tickRate;
			this.chance = chance;
			this.liquidBlockId = liquidBlockId;
		}

		public static EnumSpring fromMeta(int meta) {
			if (meta < 0 || meta >= VALUES.length) {
				return WATER;
			}
			return VALUES[meta];
		}
	}

	public BlockSpring(int id) {
		super(id, Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		disableStats();
		setTickRandomly(true);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public void getSubBlocks(int id, CreativeTabs tab, List list) {
		for (EnumSpring type : EnumSpring.VALUES) {
			list.add(new ItemStack(this, 1, type.ordinal()));
		}
	}
	
	@Override
    public int damageDropped(int meta) {
        return meta;
    }

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		assertSpring(world, x, y, z);
	}

//	@Override
//	public void onNeighborBlockChange(World world, int x, int y, int z, int blockid) {
//		assertSpring(world, x, y, z);
//	}
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		world.scheduleBlockUpdate(x, y, z, blockID, EnumSpring.fromMeta(meta).tickRate);
	}

	private void assertSpring(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		EnumSpring spring = EnumSpring.fromMeta(meta);
		world.scheduleBlockUpdate(x, y, z, blockID, spring.tickRate);
		if (!world.isAirBlock(x, y + 1, z)) {
			return;
		}
		if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
			return;
		}
		world.setBlock(x, y + 1, z, spring.liquidBlockId);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon("bedrock");
	}
}
