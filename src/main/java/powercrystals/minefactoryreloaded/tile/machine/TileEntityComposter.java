package powercrystals.minefactoryreloaded.tile.machine;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;

import cofh.core.util.fluid.FluidTankAdv;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryTanked;

public class TileEntityComposter extends TileEntityFactoryTanked {

    public TileEntityComposter() {
        super(Machine.Composter, TankIO.FILL_ONLY);
        setManageSolids(true);
        _tanks[0].setLock(FluidRegistry.getFluid("sewage"));
    }

    @Override
    protected boolean activateMachine() {
        if (drain(_tanks[0], 20, false) == 20) {
            if (!incrementWorkDone()) return false;

            if (getWorkDone() >= getWorkMax()) {
                doDrop(new ItemStack(MFRThings.fertilizerItem));
                setWorkDone(0);
            }
            drain(_tanks[0], 20, true);
            return true;
        }
        return false;
    }

    @Override
    public ForgeDirection getDropDirection() {
        return ForgeDirection.UP;
    }

    @Override
    public int getWorkMax() {
        return 100;
    }

    @Override
    public int getIdleTicksMax() {
        return 1;
    }

    @Override
    protected FluidTankAdv[] createTanks() {
        return new FluidTankAdv[] { new FluidTankAdv(4 * BUCKET_VOLUME) };
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

}
