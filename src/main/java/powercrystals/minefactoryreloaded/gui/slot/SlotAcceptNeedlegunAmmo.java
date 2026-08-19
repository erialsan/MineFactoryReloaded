package powercrystals.minefactoryreloaded.gui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import powercrystals.minefactoryreloaded.api.INeedleAmmo;

public class SlotAcceptNeedlegunAmmo extends Slot {

    public SlotAcceptNeedlegunAmmo(IInventory inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null && stack.getItem() instanceof INeedleAmmo;
    }
}
