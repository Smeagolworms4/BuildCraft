package buildcraft.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.util.Icon;

import buildcraft.core.DefaultProps;
import buildcraft.core.network.IClientState;
import buildcraft.transport.utils.ConnectionMatrix;
import buildcraft.transport.utils.FacadeMatrix;
import buildcraft.transport.utils.TextureMatrix;
import buildcraft.transport.utils.WireMatrix;

public class PipeRenderState implements IClientState {

	private boolean hasGate = false;
	private int gateTextureIndex = 0;

	public final ConnectionMatrix pipeConnectionMatrix = new ConnectionMatrix();
	public final TextureMatrix textureMatrix = new TextureMatrix();
	public final WireMatrix wireMatrix = new WireMatrix();

	public final FacadeMatrix facadeMatrix = new FacadeMatrix();

	private boolean dirty = false;

	/*
	 * This is a placeholder for the pipe renderer to set to a value that the BlockGenericPipe->TileGenericPipe will then return the the WorldRenderer
	 */
	public Icon currentTexture;

	public void setHasGate(boolean value) {
		if (hasGate != value) {
			hasGate = value;
			dirty = true;
		}
	}

	public boolean hasGate() {
		return hasGate;
	}

	public void setGateTexture(int value) {
		if (gateTextureIndex != value) {
			gateTextureIndex = value;
			dirty = true;
		}
	}

	public int getGateTextureIndex() {
		return gateTextureIndex;
	}

	public void clean() {
		dirty = false;
		pipeConnectionMatrix.clean();
		textureMatrix.clean();
		wireMatrix.clean();
		facadeMatrix.clean();
	}

	public boolean isDirty() {
		return dirty || pipeConnectionMatrix.isDirty() || textureMatrix.isDirty() || wireMatrix.isDirty() || facadeMatrix.isDirty();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeBoolean(hasGate);
		data.writeInt(gateTextureIndex);
		pipeConnectionMatrix.writeData(data);
		textureMatrix.writeData(data);
		wireMatrix.writeData(data);
		facadeMatrix.writeData(data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		hasGate = data.readBoolean();
		gateTextureIndex = data.readInt();
		pipeConnectionMatrix.readData(data);
		textureMatrix.readData(data);
		wireMatrix.readData(data);
		facadeMatrix.readData(data);
	}
}
