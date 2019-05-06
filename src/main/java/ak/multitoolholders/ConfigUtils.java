package ak.multitoolholders;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig.Loading;
import org.apache.logging.log4j.LogManager;

/**
 * Created by A.K. on 2019/04/03.
 */
public class ConfigUtils {

  public static final Common COMMON;

  static final ForgeConfigSpec configSpec;

  static {
    Builder builder = new ForgeConfigSpec.Builder();
    COMMON = new Common(builder);
    configSpec = builder.build();
  }

  @SuppressWarnings("unused")
  @SubscribeEvent
  public static void configLoading(final Loading event) {
    LogManager
        .getLogger().debug(Constants.MOD_ID, "Loaded MultiToolHolders config file {}",
        event.getConfig().getFileName());
    COMMON.enableDisplayToolHolderInventory = COMMON.enableDisplayToolHolderInventoryValue.get();
    COMMON.toolHolderInvX = COMMON.toolHolderInvXValue.get();
    COMMON.toolHolderInvY = COMMON.toolHolderInvYValue.get();
    COMMON.toolNameSet = Sets.newHashSet(COMMON.toolStrArrayValue.get().split(","));
  }

  public static class Common {

    public int toolHolderInvX = 0;
    public int toolHolderInvY = 0;
    public Set<String> toolNameSet = new HashSet<>();
    public boolean enableDisplayToolHolderInventory = true;
    private BooleanValue enableDisplayToolHolderInventoryValue;
    private IntValue toolHolderInvXValue;
    private IntValue toolHolderInvYValue;
    private ConfigValue<String> toolStrArrayValue;

    Common(Builder builder) {
      builder.comment("Common settings")
          .push(Constants.MOD_ID);
      enableDisplayToolHolderInventoryValue = builder
          .comment("enable to display toolholder inventory in HUD")
          .define("enableDisplayToolHolderInventory", true);
      toolHolderInvXValue = builder.comment("ToolHolder Inventory x-position in HUD")
          .defineInRange("toolHolderInvX", 0, 0, Integer.MAX_VALUE);
      toolHolderInvYValue = builder.comment("ToolHolder Inventory y-position in HUD")
          .defineInRange("toolHolderInvY", 0, 0, Integer.MAX_VALUE);
      toolStrArrayValue = builder.define("toolStrArray", "");
      builder.pop();
    }
  }
}
