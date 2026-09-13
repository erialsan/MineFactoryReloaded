package powercrystals.minefactoryreloaded.tile.machine;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import powercrystals.minefactoryreloaded.api.ISyringe;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityVet extends TileEntityFactoryPowered {

    public TileEntityVet() {
        super(Machine.Vet);
        createEntityHAM(this);
        setManageSolids(true);
        setCanRotate(true);
    }

    @Override
    public int getWorkMax() {
        return 1;
    }

    @Override
    public int getIdleTicksMax() {
        return 200;
    }

    @Override
    public boolean activateMachine() {
        var entities = getEntitiesInHarvestArea(EntityLivingBase.class);
        for (var e : entities) {
            if (e instanceof EntityPlayer || e instanceof EntityMob) {
                continue;
            }

            for (int i = 0; i < getSizeInventory(); i++) {
                ItemStack s = getStackInSlot(i);
                if (s != null && s.getItem() instanceof ISyringe syringe) {
                    if (syringe.canInject(worldObj, e, s)) {
                        if (syringe.inject(worldObj, e, s)) {
                            setInventorySlotContents(i, syringe.getEmptySyringe(s));
                            return true;
                        }
                    }
                }
            }
        }
        setIdleTicks(getIdleTicksMax());
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack s, int side) {
        if (s != null && s.getItem() instanceof ISyringe syringe) {
            return !syringe.isEmpty(s);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
        ItemStack s = getStackInSlot(slot);
        if (s != null && s.getItem() instanceof ISyringe syringe) {
            return syringe.isEmpty(s);
        }
        return true;
    }
}
