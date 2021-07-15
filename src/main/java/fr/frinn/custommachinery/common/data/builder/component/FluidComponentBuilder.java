package fr.frinn.custommachinery.common.data.builder.component;

import com.google.common.collect.Lists;
import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponent;
import fr.frinn.custommachinery.api.components.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.api.components.builder.IComponentBuilderProperty;
import fr.frinn.custommachinery.api.components.builder.IMachineComponentBuilder;
import fr.frinn.custommachinery.common.data.builder.component.property.IntComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.builder.component.property.ModeComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.builder.component.property.StringComponentBuilderProperty;
import fr.frinn.custommachinery.common.data.component.FluidMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;

import java.util.ArrayList;
import java.util.List;

public class FluidComponentBuilder implements IMachineComponentBuilder<FluidMachineComponent> {

    private StringComponentBuilderProperty id = new StringComponentBuilderProperty("id", "");
    private IntComponentBuilderProperty capacity = new IntComponentBuilderProperty("capacity", 0);
    private IntComponentBuilderProperty maxInput = new IntComponentBuilderProperty("maxinput", 0);
    private IntComponentBuilderProperty maxOutput = new IntComponentBuilderProperty("maxoutput", 0);
    private ModeComponentBuilderProperty mode = new ModeComponentBuilderProperty("mode", ComponentIOMode.BOTH);
    private List<IComponentBuilderProperty<?>> properties = Lists.newArrayList(id, capacity, maxInput, maxOutput, mode);

    public FluidComponentBuilder fromComponent(IMachineComponent component) {
        if(component instanceof FluidMachineComponent) {
            FluidMachineComponent fluidComponent = (FluidMachineComponent)component;
            this.id.set(fluidComponent.getId());
            this.capacity.set(fluidComponent.getCapacity());
            this.maxInput.set(fluidComponent.getMaxInput());
            this.maxOutput.set(fluidComponent.getMaxOutput());
            this.mode.set(fluidComponent.getMode());
        }
        return this;
    }

    @Override
    public MachineComponentType<FluidMachineComponent> getType() {
        return Registration.FLUID_MACHINE_COMPONENT.get();
    }

    @Override
    public List<IComponentBuilderProperty<?>> getProperties() {
        return this.properties;
    }

    @Override
    public IMachineComponentTemplate<FluidMachineComponent> build() {
        return new FluidMachineComponent.Template(id.get(), capacity.get(), maxInput.get(), maxOutput.get(), new ArrayList<>(), false, mode.get());
    }
}
