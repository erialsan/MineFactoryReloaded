package powercrystals.minefactoryreloaded.tile.base;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.setup.Machine;

public abstract class TileEntityFactoryTanked extends TileEntityFactoryPowered implements ITankContainerBucketable {

    public enum TankIO {
        FILL_ONLY,
        DRAIN_ONLY
    }

    private final TankIO tankIO;

    protected TileEntityFactoryTanked(Machine machine, TankIO tankIO) {

        super(machine);
        this.tankIO = tankIO;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {

        return tankIO == TankIO.DRAIN_ONLY ? 0 : fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {

        return drain(maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {

        return drain(resource, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {

        return tankIO == TankIO.FILL_ONLY;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {

        return tankIO == TankIO.DRAIN_ONLY;
    }

    @Override
    public boolean allowBucketFill(ItemStack stack) {

        return tankIO == TankIO.FILL_ONLY;
    }

    @Override
    public boolean allowBucketDrain(ItemStack stack) {

        return tankIO == TankIO.DRAIN_ONLY;
    }
}
