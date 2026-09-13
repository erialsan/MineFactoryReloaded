package powercrystals.minefactoryreloaded.tile.machine;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import cofh.core.util.fluid.FluidTankAdv;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.IFactoryRanchable;
import powercrystals.minefactoryreloaded.api.RanchedItem;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryTanked;

public class TileEntityRancher extends TileEntityFactoryTanked {

    public TileEntityRancher() {

        super(Machine.Rancher, TankIO.DRAIN_ONLY);
        setManageSolids(true);
        createEntityHAM(this);
        setCanRotate(true);
    }

    @Override
    protected boolean shouldPumpLiquid() {

        return true;
    }

    @Override
    public int getWorkMax() {

        return 1;
    }

    @Override
    public int getIdleTicksMax() {

        return 400;
    }

    @Override
    public boolean activateMachine() {

        boolean didDrop = false;

        var entities = getEntitiesInHarvestArea(EntityLivingBase.class);

        for (var e : entities) {
            if (MFRRegistry.getRanchables()
                .containsKey(e.getClass())) {
                IFactoryRanchable r = MFRRegistry.getRanchables()
                    .get(e.getClass());
                List<RanchedItem> drops = r.ranch(worldObj, e, this);
                if (drops != null) {
                    for (RanchedItem s : drops) {
                        if (s.hasFluid()) {
                            // whitelist fluids? multiple tanks?
                            fill((FluidStack) s.getResult(), true);
                            didDrop = true;
                            continue;
                        }

                        doDrop((ItemStack) s.getResult());
                        didDrop = true;
                    }
                    if (didDrop) {
                        setIdleTicks(20);
                        return true;
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
    protected FluidTankAdv[] createTanks() {

        return new FluidTankAdv[] { new FluidTankAdv(4 * BUCKET_VOLUME) };
    }

}
