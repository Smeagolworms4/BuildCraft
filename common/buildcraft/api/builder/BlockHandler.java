package buildcraft.api.builder;

import buildcraft.core.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * BlockHandlers are used to serialize blocks for saving/loading from
 * Blueprints.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlockHandler {

	private static final BlockHandler DEFAULT_HANDLER = new BlockHandler();
	private static final Map<Integer, BlockHandler> handlers = new HashMap<Integer, BlockHandler>();

	public static BlockHandler getHandler(int blockId) {
		BlockHandler handler = handlers.get(blockId);
		if (handler == null) {
			return DEFAULT_HANDLER;
		}
		return handler;
	}

	public static BlockHandler getHandler(BlockSchematic schematic) {
		BlockHandler handler = null; // TODO: replace with mapping -> id code
		if (handler == null) {
			return DEFAULT_HANDLER;
		}
		return handler;
	}

	public static void registerHandler(int blockId, BlockHandler handler) {
		handlers.put(blockId, handler);
	}

	protected BlockHandler() {
	}

	/**
	 * By default we will ignore all blocks with Tile Entities.
	 *
	 * We will also skip any blocks that drop actual items like Ore blocks.
	 */
	public boolean canSaveBlockToSchematic(World world, int x, int y, int z) {
		if (world.isAirBlock(x, y, z)) {
			return false;
		}
		int blockId = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockId];
		if (block == null) {
			return false;
		}
		int meta = world.getBlockMetadata(x, y, z);
		try {
			if (block.idDropped(meta, null, 0) != blockId) {
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
	 * Tile Entities should store some NBT data in the BlockSchematic.blockData
	 * tag.
	 */
	public BlockSchematic saveBlockToSchematic(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockId];
		if (block == null) {
			return null;
		}
		BlockSchematic schematic = new BlockSchematic(block);
		schematic.blockMeta = world.getBlockMetadata(x, y, z);
		return schematic;
	}

	/**
	 * Provide a list of all the items that must be present to build this
	 * schematic.
	 *
	 * If you need axillary items like a painter or gate, list them as well.
	 * Items will be consumed in the consumeItems() callback below.
	 *
	 * This default implementation will only work for simple blocks without tile
	 * entities and will in fact break on Ore blocks as well. Which is why those
	 * blocks can't be saved by default.
	 */
	public List<ItemStack> getCostForSchematic(BlockSchematic schematic) {
		List<ItemStack> cost = new ArrayList<ItemStack>();
		Block block = null; // TODO: replace with mapping -> id code
		if (block != null) {
			cost.add(new ItemStack(block.idDropped(schematic.blockMeta, Utils.RANDOM, 0), 1, block.damageDropped(schematic.blockMeta)));
		}
		return cost;
	}

	/**
	 * Called when items are consumed for this block. The builder's inventory is
	 * passed in. Use them as you see fit.
	 *
	 * If the function returns false, the block is not placed. You should not
	 * modify any ItemStack until you have determined that everything you
	 * require is present.
	 */
	public boolean consumeItems(BlockSchematic schematic, IInventory builderInventory) {
		List<ItemStack> requiredItems = getCostForSchematic(schematic);
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
			builderInventory.setInventorySlotContents(slot, Utils.consumeItem(builderInventory.getStackInSlot(slot)));
		}
		return true;
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
	public boolean canPlaceNow(World world, int x, int y, int z, ForgeDirection blueprintOrientation, BlockSchematic schematic) {
		return true;
	}

	/**
	 * This function handles the placement of the block in the world.
	 *
	 * The ForgeDirection parameter can be use to determine the orientation of
	 * the blueprint. Blueprints are always saved facing North. This function
	 * will have to rotate the block accordingly.
	 */
	public boolean readBlockFromSchematic(World world, int x, int y, int z, ForgeDirection blueprintOrientation, BlockSchematic schematic, EntityPlayer bcPlayer) {
		if (schematic.blockId != 0) {
			return world.setBlock(x, y, z, schematic.blockId, schematic.blockMeta, 3);
		}
		return false;
	}

	/**
	 * Checks if the block matches the schematic.
	 */
	public boolean doesBlockMatchSchematic(World world, int x, int y, int z, ForgeDirection blueprintOrientation, BlockSchematic schematic) {
		int blockId = world.getBlockId(x, y, z);
		Block block = Block.blocksList[blockId];
		if (block == null) {
			return false;
		}
		if (!schematic.blockName.equals(block.getUnlocalizedName())) {
			return false;
		}
		return schematic.blockMeta == world.getBlockMetadata(x, y, z);
	}
}
