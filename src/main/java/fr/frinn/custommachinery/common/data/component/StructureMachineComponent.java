package fr.frinn.custommachinery.common.data.component;

import fr.frinn.custommachinery.api.components.ComponentIOMode;
import fr.frinn.custommachinery.api.components.IMachineComponentManager;
import fr.frinn.custommachinery.api.components.MachineComponentType;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.BlockStructure;
import net.minecraft.state.properties.BlockStateProperties;

public class StructureMachineComponent extends AbstractMachineComponent {

    public StructureMachineComponent(IMachineComponentManager manager) {
        super(manager, ComponentIOMode.NONE);
    }

    @Override
    public MachineComponentType<StructureMachineComponent> getType() {
        return Registration.STRUCTURE_MACHINE_COMPONENT.get();
    }

    public boolean checkStructure(BlockStructure pattern) {
        if(getManager().getTile().getWorld() == null)
            return false;
        return pattern.match(getManager().getTile().getWorld(), getManager().getTile().getPos(), getManager().getTile().getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
    }
}
