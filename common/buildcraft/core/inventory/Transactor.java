package buildcraft.core.inventory;

import buildcraft.api.core.Orientations;
import buildcraft.api.inventory.ISpecialInventory;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraftforge.common.ISidedInventory;

public abstract class Transactor {

	public ItemStack add(ItemStack stack, Orientations orientation, boolean doAdd) {
		ItemStack added = stack.copy();
		added.stackSize = inject(stack, orientation, doAdd);	
		return added;
	}
	
	public abstract int inject(ItemStack stack, Orientations orientation, boolean doAdd);
	
	public static Transactor getTransactorFor(Object object) {
		
		if(object instanceof ISpecialInventory)
			return new TransactorSpecial((ISpecialInventory)object);
		
		else if(object instanceof ISidedInventory)
			return new TransactorSided((ISidedInventory)object);
		
		else if(object instanceof IInventory)
			return new TransactorSimple((IInventory)object);
		
		return null;
	}
}
