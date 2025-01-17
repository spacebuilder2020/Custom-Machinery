package fr.frinn.custommachinery.common.integration.kubejs.requirements;

import fr.frinn.custommachinery.api.integration.kubejs.RecipeJSBuilder;
import fr.frinn.custommachinery.common.crafting.machine.MachineProcessor;
import fr.frinn.custommachinery.common.requirement.CommandRequirement;

public interface CommandRequirementJS extends RecipeJSBuilder {

    default RecipeJSBuilder runCommandOnStart(String command) {
        return this.runCommandOnStart(command, 2, false);
    }

    default RecipeJSBuilder runCommandOnStart(String command, int permissionLevel) {
        return this.runCommandOnStart(command, permissionLevel, false);
    }

    default RecipeJSBuilder runCommandOnStart(String command, boolean log) {
        return this.runCommandOnStart(command, 2, log);
    }

    default RecipeJSBuilder runCommandOnStart(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.STARTING, permissionLevel, log));
    }

    default RecipeJSBuilder runCommandEachTick(String command) {
        return this.runCommandEachTick(command, 2, true);
    }

    default RecipeJSBuilder runCommandEachTick(String command, int permissionLevel) {
        return this.runCommandEachTick(command, permissionLevel, true);
    }

    default RecipeJSBuilder runCommandEachTick(String command, boolean log) {
        return this.runCommandEachTick(command, 2, log);
    }

    default RecipeJSBuilder runCommandEachTick(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.CRAFTING_TICKABLE, permissionLevel, log));
    }

    default RecipeJSBuilder runCommandOnEnd(String command) {
        return this.runCommandOnEnd(command, 2, true);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, int permissionLevel) {
        return this.runCommandOnEnd(command, permissionLevel, true);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, boolean log) {
        return this.runCommandOnEnd(command, 2, log);
    }

    default RecipeJSBuilder runCommandOnEnd(String command, int permissionLevel, boolean log) {
        return this.addRequirement(new CommandRequirement(command, MachineProcessor.PHASE.ENDING, permissionLevel, log));
    }
}
