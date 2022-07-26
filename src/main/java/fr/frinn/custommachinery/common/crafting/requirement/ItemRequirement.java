package fr.frinn.custommachinery.common.crafting.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.frinn.custommachinery.api.codec.CodecLogger;
import fr.frinn.custommachinery.api.component.MachineComponentType;
import fr.frinn.custommachinery.api.crafting.CraftingResult;
import fr.frinn.custommachinery.api.crafting.ICraftingContext;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientRequirement;
import fr.frinn.custommachinery.api.requirement.IChanceableRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.api.requirement.RequirementType;
import fr.frinn.custommachinery.apiimpl.requirement.AbstractRequirement;
import fr.frinn.custommachinery.common.data.component.handler.ItemComponentHandler;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.integration.jei.wrapper.ItemIngredientWrapper;
import fr.frinn.custommachinery.common.util.Codecs;
import fr.frinn.custommachinery.common.util.ingredient.IIngredient;
import fr.frinn.custommachinery.common.util.ingredient.ItemTagIngredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class ItemRequirement extends AbstractRequirement<ItemComponentHandler> implements IChanceableRequirement<ItemComponentHandler>, IJEIIngredientRequirement<ItemStack> {

    public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(itemRequirementInstance ->
            itemRequirementInstance.group(
                    Codecs.REQUIREMENT_MODE_CODEC.fieldOf("mode").forGetter(AbstractRequirement::getMode),
                    IIngredient.ITEM.fieldOf("item").forGetter(requirement -> requirement.item),
                    Codec.INT.fieldOf("amount").forGetter(requirement -> requirement.amount),
                    CodecLogger.loggedOptional(Codecs.COMPOUND_NBT_CODEC,"nbt").forGetter(requirement -> Optional.ofNullable(requirement.nbt)),
                    CodecLogger.loggedOptional(Codec.doubleRange(0.0, 1.0),"chance", 1.0D).forGetter(requirement -> requirement.chance),
                    CodecLogger.loggedOptional(Codec.STRING,"slot", "").forGetter(requirement -> requirement.slot)
            ).apply(itemRequirementInstance, (mode, item, amount, nbt, chance, slot) -> {
                    ItemRequirement requirement = new ItemRequirement(mode, item, amount, nbt.orElse(null), slot);
                    requirement.setChance(chance);
                    return requirement;
            })
    );

    private final IIngredient<Item> item;
    private final int amount;
    @Nullable
    private final CompoundNBT nbt;
    private double chance = 1.0D;
    private final String slot;
    private final Lazy<ItemIngredientWrapper> wrapper;

    public ItemRequirement(RequirementIOMode mode, IIngredient<Item> item, int amount, @Nullable CompoundNBT nbt, String slot) {
        super(mode);
        if(mode == RequirementIOMode.OUTPUT && item instanceof ItemTagIngredient)
            throw new IllegalArgumentException("You can't use a Tag for an Output Item Requirement");
        this.item = item;
        this.amount = amount;
        this.nbt = nbt;
        this.slot = slot == null ? "" : slot;
        this.wrapper = Lazy.of(() -> new ItemIngredientWrapper(this.getMode(), this.item, this.amount, this.chance, false, this.nbt, this.slot));
    }

    @Override
    public RequirementType<ItemRequirement> getType() {
        return Registration.ITEM_REQUIREMENT.get();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public MachineComponentType getComponentType() {
        return Registration.ITEM_MACHINE_COMPONENT.get();
    }

    @Override
    public boolean test(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            return this.item.getAll().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum() >= amount;
        } else {
            if(this.item.getAll().get(0) != null)
                return component.getSpaceForItem(this.slot, this.item.getAll().get(0), this.nbt) >= amount;
            else throw new IllegalStateException("Can't use output item requirement with item tag");
        }
    }

    @Override
    public CraftingResult processStart(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.INPUT) {
            int maxExtract = this.item.getAll().stream().mapToInt(item -> component.getItemAmount(this.slot, item, this.nbt)).sum();
            if(maxExtract >= amount) {
                int toExtract = amount;
                for (Item item : this.item.getAll()) {
                    int canExtract = component.getItemAmount(this.slot, item, this.nbt);
                    if(canExtract > 0) {
                        canExtract = Math.min(canExtract, toExtract);
                        component.removeFromInputs(this.slot, item, canExtract, this.nbt);
                        toExtract -= canExtract;
                        if(toExtract == 0)
                            return CraftingResult.success();
                    }
                }
            }
            return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.input", this.item.toString(), amount, maxExtract));
        }
        return CraftingResult.pass();
    }

    @Override
    public CraftingResult processEnd(ItemComponentHandler component, ICraftingContext context) {
        int amount = (int)context.getModifiedValue(this.amount, this, null);
        if(getMode() == RequirementIOMode.OUTPUT) {
            if(this.item.getAll().get(0) != null) {
                Item item = this.item.getAll().get(0);
                int canInsert = component.getSpaceForItem(this.slot, item, this.nbt);
                if(canInsert >= amount) {
                    component.addToOutputs(this.slot, item, amount, this.nbt);
                    return CraftingResult.success();
                }
                return CraftingResult.error(new TranslationTextComponent("custommachinery.requirements.item.error.output", amount, new TranslationTextComponent(item.getTranslationKey())));
            } else throw new IllegalStateException("Can't use output item requirement with item tag");
        }
        return CraftingResult.pass();
    }

    @Override
    public void setChance(double chance) {
        this.chance = MathHelper.clamp(chance, 0.0, 1.0);
    }

    @Override
    public boolean shouldSkip(ItemComponentHandler component, Random rand, ICraftingContext context) {
        double chance = context.getModifiedValue(this.chance, this, "chance");
        return rand.nextDouble() > chance;
    }

    @Override
    public ItemIngredientWrapper getJEIIngredientWrapper() {
        return this.wrapper.get();
    }
}