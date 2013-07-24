/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IAction;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.utils.EnumColor;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.IPipedItem;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.triggers.ActionPipeColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsDaizuli extends Pipe<PipeTransportItems> implements IPipeTransportItemsHook {

	private int standardIconIndex = PipeIconProvider.TYPE.PipeItemsDaizuli_Black.ordinal();
	private int solidIconIndex = PipeIconProvider.TYPE.PipeAllDaizuli_Solid.ordinal();
	@TileNetworkData
	private int color = EnumColor.WHITE.ordinal();
	private PipeLogicIron logic = new PipeLogicIron(this) {
		@Override
		protected boolean isValidFacing(ForgeDirection facing) {
			TileEntity tile = pipe.container.getTile(facing);
			if (tile instanceof TileGenericPipe) {
				Pipe otherPipe = ((TileGenericPipe) tile).pipe;
				if (otherPipe instanceof PipeItemsWood)
					return false;
				if (otherPipe.transport instanceof PipeTransportItems)
					return true;
				return false;
			}
			if (tile instanceof IInventory)
				return true;
			return false;
		}
	};

	public PipeItemsDaizuli(int itemID) {
		super(new PipeTransportItems(), itemID);

		transport.allowBouncing = true;
	}

	public EnumColor getColor() {
		return EnumColor.fromId(color);
	}

	public void setColor(EnumColor c) {
		if (color != c.ordinal()) {
			this.color = c.ordinal();
			container.scheduleRenderUpdate();
			container.markBlockForUpdate();
		}
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (player.isSneaking() && equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
			setColor(getColor().getNext());
			((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
			return true;
		}

		return logic.blockActivated(player);
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		logic.onNeighborBlockChange(blockId);
		super.onNeighborBlockChange(blockId);
	}

	@Override
	public void onBlockPlaced() {
		logic.onBlockPlaced();
		super.onBlockPlaced();
	}

	@Override
	public void initialize() {
		logic.initialize();
		super.initialize();
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container != null && container.getBlockMetadata() == direction.ordinal())
			return solidIconIndex;
		return standardIconIndex + color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public boolean canConnectRedstone() {
		return true;
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, EntityData data) {
		LinkedList<ForgeDirection> newMovements = new LinkedList<ForgeDirection>();
		EnumColor c = getColor();
		for (ForgeDirection dir : possibleOrientations) {
			if (data.color == c) {
				if (dir.ordinal() == container.getBlockMetadata())
					newMovements.add(dir);
			} else if (dir.ordinal() != container.getBlockMetadata()) {
				newMovements.add(dir);
			}
		}
		return newMovements;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		if (item.getSpeed() > Utils.pipeNormalSpeed) {
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed / 4.0F);
		}

		if (item.getSpeed() < Utils.pipeNormalSpeed) {
			item.setSpeed(Utils.pipeNormalSpeed);
		}
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);

		for (Map.Entry<IAction, Boolean> action : actions.entrySet()) {
			if (action.getKey() instanceof ActionPipeColor && action.getValue() != null && action.getValue()) {
				setColor(((ActionPipeColor) action.getKey()).color);
				break;
			}
		}
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> result = super.getActions();
		result.addAll(Arrays.asList(BuildCraftTransport.actionPipeColor));
		return result;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setByte("color", (byte) color);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		color = data.getByte("color");
	}
}
