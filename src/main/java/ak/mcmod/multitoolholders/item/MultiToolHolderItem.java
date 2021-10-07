package ak.mcmod.multitoolholders.item;

import ak.mcmod.multitoolholders.IKeyEvent;
import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.inventory.ToolHolderInventory;
import com.google.common.collect.Multimap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.multitoolholders.Constants.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiToolHolderItem extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

  public static final byte OPEN_KEY = 0;
  public static final byte NEXT_KEY = 1;
  public static final byte PREV_KEY = 2;
  private final HolderType type;

  public MultiToolHolderItem(HolderType type) {
    super(new Item.Properties().tab(ItemGroup.TAB_TOOLS).stacksTo(1).defaultDurability(0)
            .addToolType(ToolType.AXE, -1).addToolType(ToolType.PICKAXE, -1)
            .addToolType(ToolType.SHOVEL, -1));
    this.type = type;
  }

  public static int getSlotNumFromItemStack(ItemStack itemStack) {
    if (!itemStack.hasTag()) {
      itemStack.setTag(new CompoundNBT());
    }
    if (!Objects.requireNonNull(itemStack.getTag()).contains(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      CompoundNBT nbtTagCompound = new CompoundNBT();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    CompoundNBT nbt = (CompoundNBT) itemStack.getTag().get(NBT_KEY_MTH);
    return Objects.requireNonNull(nbt).getInt(NBT_KEY_SLOT);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
                              ITooltipFlag flagIn) {
    ITextComponent toolName;
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    for (int i = 0; i < type.getSize(); i++) {
      if (!tools.getItem(i).isEmpty()) {
        toolName = tools.getItem(i).getHoverName();
        tooltip.add(toolName);
      }
    }
  }

  @Override
  public void inventoryTick(@Nonnull ItemStack itemStack, @Nonnull World world,
                            @Nonnull Entity entity, int slot, boolean isHeld) {
    if (entity instanceof PlayerEntity && isHeld && !world.isClientSide) {

      if (itemStack.hasTag()) {
        Objects.requireNonNull(itemStack.getTag()).remove(NBT_KEY_ENCHANT);
      }

      ItemStack nowItem = getActiveItemStack(itemStack);
      if (!nowItem.isEmpty()) {
        nowItem.getItem().inventoryTick(nowItem, world, entity, slot, true);
        setEnchantments(itemStack, nowItem);
      }
    }
  }

  @Override
  public void onCraftedBy(@Nonnull ItemStack itemStack, @Nonnull World worldIn,
                          @Nonnull PlayerEntity playerIn) {
    if (itemStack.getItem() instanceof MultiToolHolderItem) {
      itemStack.setTag(new CompoundNBT());
    }
  }

  @Override
  public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos blockPos,
                                   @Nonnull PlayerEntity player) {
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.onBlockStartBreak(blockPos, player);
    }
    return super.onBlockStartBreak(stack, blockPos, player);
  }

  @Override
  public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player,
                             @Nullable BlockState blockState) {
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.getHarvestLevel(tool, player, blockState);
    }
    return super.getHarvestLevel(stack, tool, player, blockState);
  }

  @Override
  public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull PlayerEntity player,
                                   @Nonnull Entity entity) {
    ToolHolderInventory toolHolder = getInventoryFromItemStack(stack);
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      attackTargetEntityWithTheItem(entity, player, itemStack);
      toolHolder.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return true;
    }
    return false;
  }

  @Override
  public ActionResultType useOn(ItemUseContext context) {
    PlayerEntity playerIn = context.getPlayer();
    ItemStack heldItem = context.getItemInHand();
    ToolHolderInventory toolHolder = getInventoryFromItemStack(heldItem);
    ItemStack itemStack = getActiveItemStack(heldItem);
    if (!itemStack.isEmpty() && Objects.nonNull(playerIn)) {
      BlockRayTraceResult blockRayTraceResult = new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
      ItemUseContext newContext = new ItemUseContext(playerIn, context.getHand(), blockRayTraceResult);
      ActionResultType ret = itemStack.getItem().useOn(newContext);
      toolHolder.writeToNBT(Objects.requireNonNull(heldItem.getTag()));
      return ret;
    }
    return super.useOn(context);
  }

  @Override
  public void releaseUsing(@Nonnull ItemStack stack, @Nonnull World worldIn,
                           @Nonnull LivingEntity entityLiving, int timeLeft) {
    ItemStack itemStackIn = entityLiving.getItemInHand(Hand.MAIN_HAND);
    ToolHolderInventory tools = getInventoryFromItemStack(itemStackIn);
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().releaseUsing(itemStack, worldIn, entityLiving, timeLeft);
      if (itemStack.getCount() <= 0) {
        destroyTheItem(entityLiving, itemStack, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
  }

  @Override
  public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull World worldIn,
                                   @Nonnull LivingEntity entityLiving) {
    ItemStack itemStackIn = entityLiving.getItemInHand(Hand.MAIN_HAND);
    ToolHolderInventory tools = getInventoryFromItemStack(itemStackIn);
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().finishUsingItem(itemStack, worldIn, entityLiving);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
    return stack;
  }

  @Override
  public ActionResult<ItemStack> use(World worldIn,
                                     PlayerEntity playerIn, Hand hand) {
    ItemStack itemStackIn = playerIn.getItemInHand(hand);
    ToolHolderInventory tools = getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getItem(activeSlot);
    if (!itemStack.isEmpty()) {
      playerIn.setItemInHand(hand, itemStack);
      ActionResult<ItemStack> actionResult = itemStack.getItem()
              .use(worldIn, playerIn, hand);
      tools.setItem(
              activeSlot, actionResult
                      .getObject());
      playerIn.setItemInHand(hand, itemStackIn);
      tools.writeToNBT(Objects.requireNonNull(itemStackIn.getTag()));
      return new ActionResult<>(actionResult.getResult(), itemStackIn);
    }
    return super.use(worldIn, playerIn, hand);
  }

  @Override
  public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity playerIn,
                                               LivingEntity target, Hand hand) {
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    ItemStack itemStack = getActiveItemStack(stack);
    ActionResultType ret = ActionResultType.SUCCESS;
    if (!itemStack.isEmpty()) {
      ret = itemStack.getItem()
              .interactLivingEntity(itemStack, playerIn,
                      target, hand);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
    return ret;
  }

  @Override
  public UseAction getUseAnimation(ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getUseAnimation();
    }
    return super.getUseAnimation(stack);
  }

  @Override
  public int getUseDuration(@Nonnull ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getUseDuration();
    }
    return super.getUseDuration(stack);
  }

  @Override
  public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getDestroySpeed(state);
    }
    return super.getDestroySpeed(stack, state);
  }

  @Override
  public boolean canHarvestBlock(ItemStack stack, BlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.isCorrectToolForDrops(state);
    }
    return super.canHarvestBlock(stack, state);
  }

  @Override
  public Set<ToolType> getToolTypes(ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getToolTypes();
    }
    return super.getToolTypes(stack);
  }

  @Override
  public boolean mineBlock(ItemStack stack, World worldIn,
                           BlockState state,
                           BlockPos pos, LivingEntity entityLiving) {
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty() && !worldIn.isClientSide) {
      boolean ret = nowItem.getItem()
              .mineBlock(nowItem, worldIn, state, pos,
                      entityLiving);
      if (nowItem.getCount() <= 0) {
        destroyTheItem(entityLiving, stack, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return ret;
    }
    return super.mineBlock(stack, worldIn, state, pos, entityLiving);
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target,
                           LivingEntity attacker) {
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty() && !attacker.getCommandSenderWorld().isClientSide) {
      boolean ret = nowItem.getItem().hurtEnemy(nowItem, target, attacker);
      if (nowItem.getCount() <= 0) {
        destroyTheItem(attacker, nowItem, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return ret;
    }
    return super.hurtEnemy(stack, target, attacker);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
          EquipmentSlotType slot, ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getAttributeModifiers(slot);
    }
    return super.getAttributeModifiers(slot, stack);
  }

  /**
   * 攻撃処理の丸コピ
   *
   * @param targetEntity 攻撃対象者
   * @param player       攻撃者
   * @param stack        ツールホルダー内のActiveItemStack
   */
  private static void attackTargetEntityWithTheItem(Entity targetEntity, PlayerEntity player,
                                                    ItemStack stack) {
    // 手持ちアイテムを退避
    ItemStack toolHolder = player.getMainHandItem();
    // ツールホルダー内のアイテムを手持ちに設定
    player.setItemInHand(Hand.MAIN_HAND, stack);
    player.attack(targetEntity);
    // 退避させたツールホルダーを設定
    player.setItemInHand(Hand.MAIN_HAND, toolHolder);
  }

  /**
   * 破壊処理メソッド
   *
   * @param livingEntity 破壊者
   * @param orig         ツールホルダー
   * @param hand         持ち手
   */
  private static void destroyTheItem(LivingEntity livingEntity, ItemStack orig, Hand hand) {
    ToolHolderInventory tools = getInventoryFromItemStack(orig);
    int slotNum = getSlotNumFromItemStack(orig);
    tools.setItem(slotNum, ItemStack.EMPTY);
    if (livingEntity instanceof PlayerEntity) {
      MinecraftForge.EVENT_BUS
              .post(new PlayerDestroyItemEvent((PlayerEntity) livingEntity, orig, hand));
    }
  }

  /**
   * エンチャント付与処理
   *
   * @param itemToEnchant エンチャントされるアイテム
   * @param itemEnchanted エンチャントされているアイテム
   */
  private static void setEnchantments(ItemStack itemToEnchant, ItemStack itemEnchanted) {
    String id;
    int lv;
    ListNBT list = itemEnchanted.getEnchantmentTags();
    if (!list.isEmpty()) {
      for (int i = 0; i < list.size(); ++i) {
        if (list.getCompound(i).getShort("lvl") > 0) {
          id = list.getCompound(i).getString("id");
          lv = list.getCompound(i).getShort("lvl");
          MultiToolHolders
                  .addEnchantmentToItem(itemToEnchant, ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(id)), lv);
        }
      }
    }
  }

  /**
   * ツールホルダーのNBTにスロット番号を設定する処理
   *
   * @param itemStack ツールホルダーアイテム
   * @param slotNum   新しいスロット番号
   */
  public static void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
    if (!itemStack.hasTag()) {
      itemStack.setTag(new CompoundNBT());
    }
    if (!Objects.requireNonNull(itemStack.getTag()).contains(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      CompoundNBT nbtTagCompound = new CompoundNBT();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    CompoundNBT nbt = (CompoundNBT) itemStack.getTag().get(NBT_KEY_MTH);
    Objects.requireNonNull(nbt).putInt(NBT_KEY_SLOT, slotNum);
  }

  /**
   * ツールホルダーのインベントリを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return インベントリツールホルダー
   */
  public static ToolHolderInventory getInventoryFromItemStack(ItemStack itemStack) {
    return new ToolHolderInventory(itemStack);
  }

  /**
   * ツールホルダーのActiveItemStackを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return ActiveItemStack
   */
  public static ItemStack getActiveItemStack(ItemStack itemStack) {
    int slot = getSlotNumFromItemStack(itemStack);
    return getInventoryFromItemStack(itemStack).getItem(slot);
  }

  @Override
  public void doKeyAction(ItemStack itemStack, PlayerEntity player, byte key) {
    if (key == OPEN_KEY) {
      if (!(player.containerMenu instanceof ToolHolderContainer)) {
        if (!player.level.isClientSide) {
          player.openMenu(new InterfaceToolHolder(type));
        }
      }
    } else if (key == NEXT_KEY) {
      int slot = getSlotNumFromItemStack(itemStack);
      setSlotNumToItemStack(itemStack, (slot + 1) % this.type.getSize());
    } else if (key == PREV_KEY) {
      int slot = getSlotNumFromItemStack(itemStack);
      setSlotNumToItemStack(itemStack, (this.type.getSize() + slot - 1) % this.type.getSize());
    }
  }

  public HolderType getType() {
    return type;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  public static class InterfaceToolHolder implements INamedContainerProvider {

    private final HolderType type;

    public InterfaceToolHolder(HolderType type) {
      this.type = type;
    }

    @Override
    public ITextComponent getDisplayName() {
      return new TranslationTextComponent("item."
              + MOD_ID
              + "."
              + ak.mcmod.multitoolholders.Constants.REG_NAME_ITEM_MULTI_TOOL_PREFIX
              + type.getSize());
    }

    @Nullable
    @Override
    public Container createMenu(int guiId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      ItemStack heldItem = playerEntity.getMainHandItem();
      int currentSlot = playerInventory.selected;
      IInventory holderInventory = getInventoryFromItemStack(heldItem);
      return new ToolHolderContainer(type, guiId, playerInventory, holderInventory, currentSlot);
    }
  }
}