package powercrystals.minefactoryreloaded.item.base;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Multimap;

public class ItemFactoryTool extends ItemFactory {

    @Override
    public boolean isFull3D() {
        return true;
    }

    protected int getWeaponDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public Multimap getAttributeModifiers(ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(stack);
        int dmg = getWeaponDamage(stack);
        if (dmg != 0) {
            multimap.put(
                SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
                new AttributeModifier(field_111210_e, "Weapon modifier", dmg, 0));
        }
        return multimap;
    }

}
