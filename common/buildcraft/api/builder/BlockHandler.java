package buildcraft.api.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * BlockHandlers are used to serialize blocks for saving/loading from
 * Blueprints.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlockHandler {

	private static final Map<Block, BlockHandler> handlers = new HashMap<Block, BlockHandler>();
	private final Block block;

	public static BlockHandler get(Block block) {
		BlockHandler handler = handlers.get(block);
		if (handler == null) {
			handler = new BlockHandler(block);
			registerHandler(block, handler);
		}
		return handler;
	}

	public static void registerHandler(Block block, BlockHandler handler) {
		handlers.put(block, handler);
	}

	public BlockHandler(Block block) {
		this.block = block;
	}

	/**
	 * By default we will ignore all blocks with Tile Entities.
	 *
	 * We will also skip any blocks that drop actual items like Ore blocks.
	 */
	public boolean canSaveBlockToSchematic(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		try {
			if (block.idDropped(meta, null, 0) != block.blockID) {
				return false;
			}
		} catch (NullPointerException ex) {
			return false;
		}
		return !block.hasTileEntity(meta);
	}

	/**
	 * It is assumed that Blueprints always face North on save.
	 *
	 * Store any info you need to reproduce the block in the data tag.
	 */
	public void saveToSchematic(World world, int x, int y, int z, NBTTagCompound blockData) {
		blockData.setByte("blockMeta", (byte) world.getBlockMetadata(x, y, z));
	}

	/**
	 * It is assumed that Blueprints always face North on save.
	 *
	 * Store any info you need to reproduce the block in the data tag.
	 */
	public void saveToSchematic(ItemStack stack, NBTTagCompound blockData) {
		if (stack.getHasSubtypes())
			blockData.setByte("blockMeta", (byte) stack.getItemDamage());
	}

	/**
	 * Provide a list of all the items that must be present to build this
	 * schematic.
	 *
	 * If you need axillary items like a painter or gate, list them as well.
	 * Items will be consumed in the readBlockFromSchematic() function below.
	 *
	 * This default implementation will only work for simple blocks without tile
	 * entities and will in fact break on Ore blocks as well. Which is why those
	 * blocks can't be saved by default.
	 */
	public List<ItemStack> getCostForSchematic(NBTTagCompound blockData) {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		cost.add(new ItemStack(block.idDropped(blockData.getByte("blockMeta"), BlueprintHelpers.RANDOM, 0), 1, block.damageDropped(blockData.getByte("blockMeta"))));
		return cost;
	}

	private boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null)
			return false;
		if (!stack1.isItemEqual(stack2))
			return false;
		if (!ItemStack.areItemStackTagsEqual(stack1, stack2))
			return false;
		return true;

	}

	/**
	 * Can the block be placed currently or is it waiting on some other block to
	 * be placed first?
	 */
	public boolean canPlaceNow(World world, int x, int y, int z, ForgeDirection blueprintOrientation, NBTTagCompound blockData) {
		return true;
	}

	/**
	 * This function handles the placement of the block in the world.
	 *
	 * The ForgeDirection parameter can be use to determine the orientation of
	 * the blueprint. Blueprints are always saved facing North. This function
	 * will have to rotate the block accordingly.
	 *
	 * The builder's inventory is passed in so you can consume the items you
	 * need. Use them as you see fit.
	 *
	 * If the function returns false, the block was not placed. You should not
	 * modify any ItemStack in the inventory until you have determined that
	 * everything you require is present.
	 */
	public boolean readBlockFromSchematic(World world, int x, int y, int z, ForgeDirection blueprintOrientation, NBTTagCompound blockData, IInventory builderInventory, EntityPlayer bcPlayer) {
		if (builderInventory != null) {
			List<ItemStack> requiredItems = getCostForSchematic(blockData);
			List<Integer> slotsToConsume = new ArrayList<Integer>();
			for (ItemStack cost : requiredItems) {
				boolean found = false;
				for (int slot = 0; slot < builderInventory.getSizeInventory(); slot++) {
					if (areItemsEqual(builderInventory.getStackInSlot(slot), cost)) {
						slotsToConsume.add(slot);
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
			for (Integer slot : slotsToConsume) {
				builderInventory.setInventorySlotContents(slot, BlueprintHelpers.consumeItem(builderInventory.getStackInSlot(slot)));
			}
		}
		return world.setBlock(x, y, z, block.blockID, blockData.getByte("blockMeta"), 3);
	}

	/**
	 * Checks if the block matches the schematic.
	 */
	public boolean doesBlockMatchSchematic(World world, int x, int y, int z, ForgeDirection blueprintOrientation, NBTTagCompound blockData) {
		if (block.blockID != world.getBlockId(x, y, z))
			return false;

		return !blockData.hasKey("blockMeta") || blockData.getByte("blockMeta") == world.getBlockMetadata(x, y, z);
	}
}
