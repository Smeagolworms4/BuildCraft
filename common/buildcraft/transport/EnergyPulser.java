package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;

public class EnergyPulser {

	private IPowerReceptor powerReceptor;

	private boolean isActive;
	private boolean singlePulse;
	private boolean hasPulsed;
	private int pulseCount;
	private int tick;
	public EnergyPulser(IPowerReceptor receptor) {
		powerReceptor = receptor;
	}

	public void update() {
		if (powerReceptor == null)
			return;

		if (isActive)
		{
			tick++;
			if (!singlePulse || !hasPulsed) {
				if (tick % 10 == 0 || !hasPulsed)
				{
					powerReceptor.getPowerProvider().receiveEnergy(Math.max(1 << (pulseCount-1),64), ForgeDirection.WEST);
					if (singlePulse) {
						hasPulsed = true;
					}
				}
			}
		}
	}

	public void enableSinglePulse(int count)	{
		singlePulse = true;
		isActive = true;
		pulseCount = count;
	}

	public void enablePulse(int count) {
		isActive = true;
		singlePulse = false;
		pulseCount = count;
	}

	public void disablePulse() {
		if (!isActive) {
			hasPulsed = false;
		}
		isActive = false;
		pulseCount = 0;
	}

	public boolean isActive() {
		return isActive;
	}

	private float getPulseSpeed() {
		return 0.1F;
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("SinglePulse", singlePulse);
		nbttagcompound.setBoolean("IsActive", isActive);
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		isActive = nbttagcompound.getBoolean("IsActive");
		singlePulse = nbttagcompound.getBoolean("SinglePulse");
	}
}
