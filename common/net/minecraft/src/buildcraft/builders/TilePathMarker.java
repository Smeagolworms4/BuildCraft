
package net.minecraft.src.buildcraft.builders;

import java.util.LinkedList;
import java.util.TreeSet;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.EntityLaser;

public class TilePathMarker extends TileMarker {

	public EntityLaser lasers[] = new EntityLaser[2];

	public int x0, y0, z0, x1, y1, z1;
	public boolean loadLink0 = false, loadLink1 = false;
	public boolean tryingToConnect = false;

	public TilePathMarker links[] = new TilePathMarker[2];
	public static int searchSize = 64;	//TODO: this should be moved to default props
	
	//A list with the pathMarkers that aren't fully connected
	//It only contains markers within the loaded chunks
	private static LinkedList<TilePathMarker> availableMarkers = new LinkedList<TilePathMarker>();

	public TilePathMarker() {
		availableMarkers.add(this);
	}

	public boolean isFullyConnected() {
		return lasers[0] != null && lasers[1] != null;
	}

	public boolean isLinkedTo(TilePathMarker pathMarker) {
		return links[0] == pathMarker || links[1] == pathMarker;
	}

	public void connect(TilePathMarker marker, EntityLaser laser) {
		if (lasers[0] == null) {
			lasers[0] = laser;
			links[0] = marker;
		} else if (lasers[1] == null) {
			lasers[1] = laser;
			links[1] = marker;
		}

		if (isFullyConnected())
			availableMarkers.remove(this);
	}

	public void createLaserAndConnect(TilePathMarker pathMarker) {

		if (APIProxy.isClient(worldObj))
			return;

		EntityLaser laser = new EntityLaser(worldObj, new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5), new Position(pathMarker.xCoord + 0.5, pathMarker.yCoord + 0.5, pathMarker.zCoord + 0.5));
		laser.show();

		laser.setTexture(DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png");
		worldObj.spawnEntityInWorld(laser);

		connect(pathMarker, laser);
		pathMarker.connect(this, laser);
	}

	//Searches the availableMarkers list for the nearest available that is within searchSize
	private TilePathMarker findNearestAvailablePathMarker() {
		TilePathMarker nearestAvailable = null;
		double nearestDistance = 0, distance;	//The initialization of nearestDistance is only to make the compiler shut up

		for (TilePathMarker t : availableMarkers) {
			if (t.isFullyConnected()) {
				System.err.printf("Removing the non-available path markers isn't correct\n");
				continue;
			}

			if (t == this || t == this.links[0] || t == this.links[1])
				continue;

			distance = Math.sqrt(Math.pow(this.xCoord - t.xCoord, 2) + Math.pow(this.yCoord - t.yCoord, 2) + Math.pow(this.zCoord - t.zCoord, 2));

			if (distance > searchSize)
				continue;

			if (nearestAvailable == null || distance < nearestDistance) {
				nearestAvailable = t;
				nearestDistance = distance;
			}
		}

		return nearestAvailable;
	}

	@Override
	public void tryConnection() {
		if (isFullyConnected()) {
			return;
		}

		tryingToConnect = true;
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (tryingToConnect) {
			TilePathMarker nearestPathMarker = findNearestAvailablePathMarker();

			if (nearestPathMarker != null) {
				createLaserAndConnect(nearestPathMarker);
				tryingToConnect = false;
				worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			}
		}
	}

	public LinkedList<BlockIndex> getPath() {
		TreeSet<BlockIndex> visitedPaths = new TreeSet<BlockIndex>();
		LinkedList<BlockIndex> res = new LinkedList<BlockIndex>();

		TilePathMarker nextTile = this;

		while (nextTile != null) {
			BlockIndex b = new BlockIndex(nextTile.xCoord, nextTile.yCoord, nextTile.zCoord);

			visitedPaths.add(b);
			res.add(b);

			if (nextTile.links[0] != null && !visitedPaths.contains(new BlockIndex(nextTile.links[0].xCoord, nextTile.links[0].yCoord, nextTile.links[0].zCoord))) {
				nextTile = nextTile.links[0];
			} else if (nextTile.links[1] != null && !visitedPaths.contains(new BlockIndex(nextTile.links[1].xCoord, nextTile.links[1].yCoord, nextTile.links[1].zCoord))) {
				nextTile = nextTile.links[1];
			} else {
				nextTile = null;
			}
		}

		return res;

	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (lasers[0] != null) {
			links[0].unlink(this);
			lasers[0].setDead();
		}

		if (lasers[1] != null) {
			links[1].unlink(this);
			lasers[1].setDead();
		}

		lasers = new EntityLaser[2];
		links = new TilePathMarker[2];

		availableMarkers.remove(this);
		tryingToConnect = false;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (loadLink0) {
			TileEntity e0 = worldObj.getBlockTileEntity(x0, y0, z0);

			if (links[0] != e0 && links[1] != e0 && e0 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e0);
			}

			loadLink0 = false;
		}

		if (loadLink1) {
			TileEntity e1 = worldObj.getBlockTileEntity(x1, y1, z1);

			if (links[0] != e1 && links[1] != e1 && e1 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e1);
			}

			loadLink1 = false;
		}
	}

	private void unlink(TilePathMarker tile) {
		if (links[0] == tile) {
			lasers[0] = null;
			links[0] = null;
		}

		if (links[1] == tile) {
			lasers[1] = null;
			links[1] = null;
		}

		if (!isFullyConnected() && !availableMarkers.contains(this))
			availableMarkers.add(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("x0")) {
			x0 = nbttagcompound.getInteger("x0");
			y0 = nbttagcompound.getInteger("y0");
			z0 = nbttagcompound.getInteger("z0");

			loadLink0 = true;
		}

		if (nbttagcompound.hasKey("x1")) {
			x1 = nbttagcompound.getInteger("x1");
			y1 = nbttagcompound.getInteger("y1");
			z1 = nbttagcompound.getInteger("z1");

			loadLink1 = true;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (links[0] != null) {
			nbttagcompound.setInteger("x0", links[0].xCoord);
			nbttagcompound.setInteger("y0", links[0].yCoord);
			nbttagcompound.setInteger("z0", links[0].zCoord);
		}

		if (links[1] != null) {
			nbttagcompound.setInteger("x1", links[1].xCoord);
			nbttagcompound.setInteger("y1", links[1].yCoord);
			nbttagcompound.setInteger("z1", links[1].zCoord);
		}
	}

	@Override
	public void onChunkUnload() {
		availableMarkers.remove(this);
	}

	public static void clearAvailableMarkersList() {
		availableMarkers.clear();
	}
}
