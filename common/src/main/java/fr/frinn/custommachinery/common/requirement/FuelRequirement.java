package fr.frinn.custommachinery.common.requirement;

import fr.frinn.custommachinery.api.codec.NamedCodec;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FuelItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.FuelMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.impl.requirement.AbstractRequirement;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class FuelRequirement extends AbstractRequirement<FuelMachineComponent> implements ITickableRequirement<FuelMachineComponent>, IJEIIngredientRequirement<ItemStack> {

    public static final NamedCodec<FuelRequirement> CODEC = NamedCodec.record(fuelRequirementInstance ->
            fuelRequirementInstance.group(
                    NamedCodec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("amount", 1).forGetter(requirement -> requirement.amount)
            ).apply(fuelRequirementInstance, FuelRequirement::new), "Fuel requirement"
    );

    private final int amount;

    public FuelRequirement(int amount) {
        super(RequirementIOMode.INPUT);
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    @Override
    public RequirementType<FuelRequirement> getType() {
        return Registration.FUEL_REQUIREMENT.get();
    }

    @Override
    public boolean test(FuelMachineComponent component, ICraftingContext context) {
        return component.canStartRecipe(this.amount);
    }

    @Override
    public CraftingResult processStart(FuelMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processTick(FuelMachineComponent component, ICraftingContext context) {
        if(component.burn(this.amount))
            return CraftingResult.success();
        return CraftingResult.error(new TranslatableComponent("custommachinery.requirements.fuel.error"));
    }

    @Override
    public CraftingResult processEnd(FuelMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<FuelMachineComponent> getComponentType() {
        return Registration.FUEL_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IJEIIngredientWrapper<ItemStack>> getJEIIngredientWrappers(IMachineRecipe recipe) {
        return Collections.singletonList(new FuelItemIngredientWrapper(this.amount));
    }
}
