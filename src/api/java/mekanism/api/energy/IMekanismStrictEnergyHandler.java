package mekanism.api.energy;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMekanismStrictEnergyHandler extends ISidedStrictEnergyHandler {

    /**
     * Used to check if an instance of {@link IMekanismStrictEnergyHandler} actually has the ability to handle energy.
     *
     * @return True if we are actually capable of handling energy.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismStrictEnergyHandler} without having gotten the object via the strict energy handler capability,
     * then you must call this method to make sure that it really is an inventory. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleEnergy() {
        return true;
    }

    /**
     * Returns the list of IEnergyContainers that this energy handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IEnergyContainers that this {@link IMekanismStrictEnergyHandler} contains for the given side. If there are no containers for the side or
     * {@link #canHandleEnergy()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all containers in the handler. Additionally, if {@link #canHandleEnergy()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<IEnergyContainer> getEnergyContainers(@Nullable Direction side);

    /**
     * Called when the contents of this energy handler change.
     */
    void onContentsChanged();

    /**
     * Returns the {@link IEnergyContainer} that has the given index from the list of containers on the given side.
     *
     * @param container The index of the container to retrieve.
     * @param side      The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IEnergyContainer} that has the given index from the list of containers on the given side.
     */
    @Nullable
    default IEnergyContainer getEnergyContainer(int container, @Nullable Direction side) {
        List<IEnergyContainer> containers = getEnergyContainers(side);
        return container >= 0 && container < containers.size() ? containers.get(container) : null;
    }

    @Override
    default int getEnergyContainerCount(@Nullable Direction side) {
        return getEnergyContainers(side).size();
    }

    @Override
    default double getEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0 : energyContainer.getEnergy();
    }

    @Override
    default void setEnergy(int container, double energy, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        if (energyContainer != null) {
            energyContainer.setEnergy(energy);
        }
    }

    @Override
    default double getMaxEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0 : energyContainer.getMaxEnergy();
    }

    @Override
    default double getNeededEnergy(int container, @Nullable Direction side) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0 : energyContainer.getNeeded();
    }

    @Override
    default double insertEnergy(int container, double amount, @Nullable Direction side, Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? amount : energyContainer.insert(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }

    @Override
    default double extractEnergy(int container, double amount, @Nullable Direction side, Action action) {
        IEnergyContainer energyContainer = getEnergyContainer(container, side);
        return energyContainer == null ? 0 : energyContainer.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }
}