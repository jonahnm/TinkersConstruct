package slimeknights.tconstruct.library.recipe.melting;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.List;

/**
 * Extension of melting recipe to boost results of ores
 */
public class OreMeltingRecipe extends MeltingRecipe {
  @Getter
  private final OreRateType oreType;
  public OreMeltingRecipe(ResourceLocation id, String group, Ingredient input, FluidStack output, int temperature, int time, List<FluidStack> byproducts, OreRateType oreType) {
    super(id, group, input, output, temperature, time, byproducts);
    this.oreType = oreType;
  }

  @Override
  public FluidStack getOutput(IMeltingContainer inv) {
    FluidStack output = getOutput();
    return inv.getOreRate().applyOreBoost(oreType, output);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerSmeltery.oreMeltingSerializer.get();
  }

  public static class Serializer extends MeltingRecipe.AbstractSerializer<OreMeltingRecipe> {
    @Override
    protected OreMeltingRecipe createFromJson(ResourceLocation id, String group, Ingredient input, FluidStack output, int temperature, int time, List<FluidStack> byproducts, JsonObject json) {
      OreRateType rate = OreRateType.parse(json, "rate");
      // multiply byproducts by the config amount, config is loaded, and this prevents running it twice (once on read from network)
      byproducts = byproducts.stream().map(fluid -> Config.COMMON.foundryByproductRate.applyOreBoost(rate, fluid)).toList();
      return new OreMeltingRecipe(id, group, input, output, temperature, time, byproducts, rate);
    }

    @Override
    protected OreMeltingRecipe createFromNetwork(ResourceLocation id, String group, Ingredient input, FluidStack output, int temperature, int time, List<FluidStack> byproducts, FriendlyByteBuf buffer) {
      OreRateType rate = buffer.readEnum(OreRateType.class);
      return new OreMeltingRecipe(id, group, input, output, temperature, time, byproducts, rate);
    }

    @Override
    protected void toNetworkSafe(FriendlyByteBuf buffer, OreMeltingRecipe recipe) {
      super.toNetworkSafe(buffer, recipe);
      buffer.writeEnum(recipe.oreType);
    }
  }
}
