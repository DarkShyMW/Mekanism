package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tier.PipeTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockMechanicalPipe extends ItemBlockMekanism<BlockMechanicalPipe> {

    public ItemBlockMechanicalPipe(BlockMechanicalPipe block) {
        super(block, new Item.Properties());
    }

    @NotNull
    @Override
    public PipeTier getTier() {
        return Attribute.getTier(getBlock(), PipeTier.class);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
            tooltip.add(MekanismLang.FLUIDS.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.FORGE));
        } else {
            PipeTier tier = getTier();
            tooltip.add(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getPipeCapacity())));
            tooltip.add(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getPipePullAmount())));
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }
}