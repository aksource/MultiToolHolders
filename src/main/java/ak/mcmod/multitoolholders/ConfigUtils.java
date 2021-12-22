package ak.mcmod.multitoolholders;

import com.google.common.collect.Sets;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.Set;

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

  @SubscribeEvent
  public static void configLoading(final ModConfigEvent.Loading event) {
    LogManager
            .getLogger().debug("Loaded MultiToolHolders config file {}",
                    event.getConfig().getFileName());
    COMMON.enableDisplayToolHolderInventory = COMMON.enableDisplayToolHolderInventoryValue.get();
    COMMON.toolHolderInvX = COMMON.toolHolderInvXValue.get();
    COMMON.toolHolderInvY = COMMON.toolHolderInvYValue.get();
    COMMON.toolNameSet = Sets.newHashSet(COMMON.toolStrArrayValue.get().split(","));
    COMMON.enableAutoChange = COMMON.enableAutoChangeValue.get();
  }

  public static class Common {

    public int toolHolderInvX = 0;
    public int toolHolderInvY = 0;
    public Set<String> toolNameSet = new HashSet<>();
    public boolean enableDisplayToolHolderInventory = true;
    public boolean enableAutoChange = true;
    private final BooleanValue enableDisplayToolHolderInventoryValue;
    private final IntValue toolHolderInvXValue;
    private final IntValue toolHolderInvYValue;
    private final ConfigValue<String> toolStrArrayValue;
    private final BooleanValue enableAutoChangeValue;

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
      enableAutoChangeValue = builder
              .comment("enable to change suitable tool automatically")
              .define("enableAutoChange", true);
      builder.pop();
    }
  }
}
