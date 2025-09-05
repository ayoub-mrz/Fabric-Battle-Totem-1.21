package net.ayoubmrz.battletotemmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {

    }
    private void shapedRecipe(RecipeExporter exporter, ItemConvertible output, RecipeCategory category,
                              String[] pattern, Object... inputs) {
        var builder = ShapedRecipeJsonBuilder.create(category, output);
        for (String line : pattern) builder.pattern(line);
        for (int i = 0; i < inputs.length; i += 2)
            builder.input((Character)inputs[i], (ItemConvertible)inputs[i+1]);
        builder.criterion(hasItem(output), conditionsFromItem(output)).offerTo(exporter);
    }
}
