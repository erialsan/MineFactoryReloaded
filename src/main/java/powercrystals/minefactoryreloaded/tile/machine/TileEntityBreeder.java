package powercrystals.minefactoryreloaded.tile.machine;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityBreeder extends TileEntityFactoryPowered {

    public TileEntityBreeder() {
        super(Machine.Breeder);
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
    protected boolean activateMachine() {
        var entities = getEntitiesInHarvestArea(EntityAnimal.class);

        if (entities.size() > MFRConfig.breederShutdownThreshold.getInt()) {
            setIdleTicks(getIdleTicksMax());
            return false;
        }
        ArrayList<Integer> doors = new ArrayList<>();

        for (int i = getSizeInventory(); i-- > 0;) {
            ItemStack item = _inventory[i];
            if (item != null) {
                if (item.getItem()
                    .equals(Items.wooden_door)) {
                    doors.add(i);
                }
                if (entities.size() == 0) continue;
                Iterator<EntityAnimal> iter = entities.iterator();
                while (iter.hasNext()) {
                    EntityAnimal a = iter.next();

                    if (!a.isInLove() && a.getGrowingAge() == 0) {
                        if (a.isBreedingItem(_inventory[i])) {
                            a.func_146082_f(null);
                            decrStackSize(i, 1);
                            iter.remove();
                            return true;
                        }
                    } else iter.remove();
                }
            }
        }

        if (doors.size() > 0) {
            var villagers = getEntitiesInHarvestArea(EntityVillager.class);

            if (villagers.size() > MFRConfig.breederShutdownThreshold.getInt()) {
                setIdleTicks(getIdleTicksMax());
                return false;
            }
            if (villagers.size() != 0) for (int i : doors) {
                ItemStack item = _inventory[i];
                if (item != null) {
                    if (villagers.size() == 0) break;
                    Iterator<EntityVillager> iter = villagers.iterator();
                    while (iter.hasNext()) {
                        EntityVillager v = iter.next();
                        if (v.getGrowingAge() == 0 && !v.isMating()) {
                            for (Object o : v.tasks.taskEntries) {
                                if (o instanceof EntityAIVillagerMate mate) {
                                    mate.startExecuting();
                                    decrStackSize(i, 1);
                                    iter.remove();
                                    return true;
                                }
                            }
                        } else iter.remove();
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
}
