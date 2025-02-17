package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.MultiFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.SingleFluidStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator.TaggedFluidStackIngredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidInputCache<RECIPE extends MekanismRecipe> extends NBTSensitiveInputCache<Fluid, FluidStack, FluidStack, FluidStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, FluidStackIngredient inputIngredient) {
        if (inputIngredient instanceof SingleFluidStackIngredient single) {
            addNbtInputCache(single.getInputRaw(), recipe);
        } else if (inputIngredient instanceof TaggedFluidStackIngredient tagged) {
            for (Fluid input : tagged.getRawInput()) {
                addInputCache(input, recipe);
            }
        } else if (inputIngredient instanceof MultiFluidStackIngredient multi) {
            return mapMultiInputs(recipe, multi);
        } else {
            //This should never really happen as we don't really allow for custom ingredients especially for networking,
            // but if it does add it as a fallback
            return true;
        }
        return false;
    }

    @Override
    protected Fluid createKey(FluidStack stack) {
        return stack.getFluid();
    }

    @Override
    protected FluidStack createNbtKey(FluidStack stack) {
        //Note: We can use FluidStacks directly as the Nbt key as they compare only on fluid and tag on equals and hashcode
        // and don't take the amount into account
        return stack;
    }

    @Override
    public boolean isEmpty(FluidStack input) {
        return input.isEmpty();
    }
}