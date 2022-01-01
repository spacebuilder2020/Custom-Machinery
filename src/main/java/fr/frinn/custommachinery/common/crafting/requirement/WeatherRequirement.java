package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfo;
import fr.frinn.custommachinery.api.integration.jei.IDisplayInfoRequirement;
import fr.frinn.custommachinery.api.requirement.ITickableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.WeatherMachineComponent;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.util.Codecs;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;

public class WeatherRequirement extends AbstractRequirement<WeatherMachineComponent> implements ITickableRequirement<WeatherMachineComponent>, IDisplayInfoRequirement {

    public static final Codec<WeatherRequirement> CODEC = RecordCodecBuilder.create(weatherRequirementInstance ->
            weatherRequirementInstance.group(
                    Codecs.WEATHER_TYPE_CODEC.fieldOf("weather").forGetter(requirement -> requirement.weather),
                    CodecLogger.loggedOptional(Codec.BOOL,"onmachine", true).forGetter(requirement -> requirement.onMachine),
                    CodecLogger.loggedOptional(Codec.BOOL,"jei", true).forGetter(requirement -> requirement.jeiVisible)
            ).apply(weatherRequirementInstance, (weather, onMachine, jei) -> {
                    WeatherRequirement requirement = new WeatherRequirement(weather, onMachine);
                    requirement.setJeiVisible(jei);
                    return requirement;
            })
    );

    private final WeatherMachineComponent.WeatherType weather;
    private final boolean onMachine;
    private boolean jeiVisible = true;

    public WeatherRequirement(WeatherMachineComponent.WeatherType weather, boolean onMachine) {
        super(RequirementIOMode.INPUT);
        this.weather = weather;
        this.onMachine = onMachine;
    }

    @Override
    public RequirementType<WeatherRequirement> getType() {
        return Registration.WEATHER_REQUIREMENT.get();
    }

    @Override
    public boolean test(WeatherMachineComponent component, ICraftingContext context) {
        return component.hasWeather(this.weather, this.onMachine);
    }

    @Override
    public CraftingResult processStart(WeatherMachineComponent component, ICraftingContext context) {
        if(component.hasWeather(this.weather, this.onMachine))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.weather.error", this.weather));
    }

    @Override
    public CraftingResult processEnd(WeatherMachineComponent component, ICraftingContext context) {
        return CraftingResult.pass();
    }

    @Override
    public MachineComponentType<WeatherMachineComponent> getComponentType() {
        return Registration.WEATHER_MACHINE_COMPONENT.get();
    }

    @Override
    public CraftingResult processTick(WeatherMachineComponent component, ICraftingContext context) {
        if(component.hasWeather(this.weather, this.onMachine))
            return CraftingResult.success();
        return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.weather.error", this.weather));
    }

    @Override
    public void setJeiVisible(boolean jeiVisible) {
        this.jeiVisible = jeiVisible;
    }

    @Override
    public void getDisplayInfo(IDisplayInfo info) {
        info.addTooltip(new TranslationTextComponent("custommachinery.requirements.weather.info", this.weather.getText()));
        if(this.onMachine)
            info.addTooltip(new TranslationTextComponent("custommachinery.requirements.weather.info.sky"));
        info.setVisible(this.jeiVisible);
        info.setItemIcon(Items.SUNFLOWER);
    }
}