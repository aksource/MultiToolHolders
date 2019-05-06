package ak.multitoolholders.recipe;

import static ak.multitoolholders.MultiToolHolders.itemMultiToolHolder3;
import static ak.multitoolholders.MultiToolHolders.itemMultiToolHolder5;
import static ak.multitoolholders.MultiToolHolders.itemMultiToolHolder7;
import static ak.multitoolholders.MultiToolHolders.itemMultiToolHolder9;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapedRecipe.Serializer;

/**
 * Created by A.K. on 2019/04/27.
 */
public class RecipeHandler {


  public static void addRecipe(final Consumer<IRecipe> registry) {
    ItemStack[] toolHolders = new ItemStack[]{new ItemStack(itemMultiToolHolder3),
        new ItemStack(itemMultiToolHolder5), new ItemStack(itemMultiToolHolder7),
        new ItemStack(itemMultiToolHolder9)};
    ItemStack[] holderMaterials = new ItemStack[]{new ItemStack(Items.IRON_INGOT),
        new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GOLD_INGOT),
        new ItemStack(Items.DIAMOND)};
    ShapedRecipe.Serializer serializer = new Serializer();
    for (int i = 0; i < toolHolders.length; i++) {
      JsonObject root = new JsonObject();
      Map<String, Object> toJsonMap = new HashMap<>();
      toJsonMap.put("type", "crafting_shaped");
      root.addProperty("type", "crafting_shaped");
      toJsonMap.put("pattern", Lists.newArrayList("AAA", "ABA", "CCC"));
      JsonArray innerArray = new JsonArray();
      innerArray.add(new JsonPrimitive("AAA"));
      innerArray.add(new JsonPrimitive("ABA"));
      innerArray.add(new JsonPrimitive("CCC"));
      root.add("pattern", innerArray);
      Map<String, Object> keyMap = new HashMap<>();
      JsonObject key = new JsonObject();
      JsonObject item = new JsonObject();
      keyMap.put("A", holderMaterials[i].getItem().getRegistryName().toString());
      item.addProperty("item", holderMaterials[i].getItem().getRegistryName().toString());
      key.add("A", item);
      keyMap.put("B", Blocks.CHEST.getRegistryName().toString());
      item = new JsonObject();
      item.addProperty("item", Blocks.CHEST.getRegistryName().toString());
      key.add("B", item);
      keyMap.put("C", Blocks.TRIPWIRE_HOOK.getRegistryName().toString());
      item = new JsonObject();
      item.addProperty("item", Blocks.TRIPWIRE_HOOK.getRegistryName().toString());
      key.add("C", item);
      toJsonMap.put("key", keyMap);
      root.add("key", key);
      Map<String, Object> resultMap = new HashMap<>();
      JsonObject result = new JsonObject();
      resultMap.put("item", toolHolders[i].getItem().getRegistryName().toString());
      result.addProperty("item", toolHolders[i].getItem().getRegistryName().toString());
      toJsonMap.put("result", resultMap);
      root.add("result", result);
      registry.accept(serializer.read(toolHolders[i].getItem().getRegistryName(),
          root));
    }
  }
}
