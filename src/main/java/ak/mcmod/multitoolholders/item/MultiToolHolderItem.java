package ak.mcmod.multitoolholders.item;

import ak.mcmod.multitoolholders.IKeyEvent;
import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.inventory.ToolHolderContainer;
import ak.mcmod.multitoolholders.inventory.ToolHolderInventory;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

import static ak.mcmod.multitoolholders.Constants.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiToolHolderItem extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

  public static final byte OPEN_KEY = 0;
  public static final byte NEXT_KEY = 1;
  public static final byte PREV_KEY = 2;
  private final HolderType type;

  public MultiToolHolderItem(HolderType type) {
    super(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).stacksTo(1).defaultDurability(0));
    this.type = type;
  }

  public static int getSlotNumFromItemStack(ItemStack itemStack) {
    if (!itemStack.hasTag()) {
      itemStack.setTag(new CompoundTag());
    }
    if (!Objects.requireNonNull(itemStack.getTag()).contains(NBT_KEY_MTH, Tag.TAG_COMPOUND)) {
      var nbtTagCompound = new CompoundTag();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    var nbt = (CompoundTag) itemStack.getTag().get(NBT_KEY_MTH);
    return Objects.requireNonNull(nbt).getInt(NBT_KEY_SLOT);
  }

  /**
   * 攻撃処理の丸コピ
   *
   * @param targetEntity 攻撃対象者
   * @param player       攻撃者
   * @param stack        ツールホルダー内のActiveItemStack
   */
  private static void attackTargetEntityWithTheItem(Entity targetEntity, Player player,
                                                    ItemStack stack) {
    // 手持ちアイテムを退避
    var toolHolder = player.getMainHandItem();
    // ツールホルダー内のアイテムを手持ちに設定
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    player.attack(targetEntity);
    // 退避させたツールホルダーを設定
    player.setItemInHand(InteractionHand.MAIN_HAND, toolHolder);
  }

  /**
   * 破壊処理メソッド
   *
   * @param livingEntity 破壊者
   * @param orig         ツールホルダー
   * @param hand         持ち手
   */
  @SuppressWarnings("SameParameterValue")
  private static void destroyTheItem(LivingEntity livingEntity, ItemStack orig, InteractionHand hand) {
    var tools = getInventoryFromItemStack(orig);
    var slotNum = getSlotNumFromItemStack(orig);
    tools.setItem(slotNum, ItemStack.EMPTY);
    if (livingEntity instanceof Player) {
      MinecraftForge.EVENT_BUS
              .post(new PlayerDestroyItemEvent((Player) livingEntity, orig, hand));
    }
    tools.writeToNBT(Objects.requireNonNull(orig.getTag()));
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
    var list = itemEnchanted.getEnchantmentTags();
    if (!list.isEmpty()) {
      for (var i = 0; i < list.size(); ++i) {
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
      itemStack.setTag(new CompoundTag());
    }
    if (!Objects.requireNonNull(itemStack.getTag()).contains(NBT_KEY_MTH, Tag.TAG_COMPOUND)) {
      var nbtTagCompound = new CompoundTag();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    var nbt = (CompoundTag) itemStack.getTag().get(NBT_KEY_MTH);
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
    var slot = getSlotNumFromItemStack(itemStack);
    return getInventoryFromItemStack(itemStack).getItem(slot);
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip,
                              TooltipFlag flagIn) {
    var tools = getInventoryFromItemStack(stack);
    for (var i = 0; i < type.getSize(); i++) {
      if (!tools.getItem(i).isEmpty()) {
        tooltip.add(tools.getItem(i).getHoverName());
      }
    }
  }

  @Override
  public void inventoryTick(@Nonnull ItemStack itemStack, @Nonnull Level world,
                            @Nonnull Entity entity, int slot, boolean isHeld) {
    if (entity instanceof Player && isHeld && !world.isClientSide) {

      if (itemStack.hasTag()) {
        Objects.requireNonNull(itemStack.getTag()).remove(NBT_KEY_ENCHANT);
      }

      var nowItem = getActiveItemStack(itemStack);
      if (!nowItem.isEmpty()) {
        nowItem.getItem().inventoryTick(nowItem, world, entity, slot, true);
        setEnchantments(itemStack, nowItem);
      }
    }
  }

  @Override
  public void onCraftedBy(@Nonnull ItemStack itemStack, @Nonnull Level worldIn,
                          @Nonnull Player playerIn) {
    if (itemStack.getItem() instanceof MultiToolHolderItem) {
      itemStack.setTag(new CompoundTag());
    }
  }

  @Override
  public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos blockPos,
                                   @Nonnull Player player) {
    var nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.onBlockStartBreak(blockPos, player);
    }
    return super.onBlockStartBreak(stack, blockPos, player);
  }

//  @Override
//  public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable Player player,
//                             @Nullable BlockState blockState) {
//    var nowItem = getActiveItemStack(stack);
//    if (!nowItem.isEmpty()) {
//      return nowItem.getHarvestLevel(tool, player, blockState);
//    }
//    return super.getHarvestLevel(stack, tool, player, blockState);
//  }

  @Override
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    var nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.isCorrectToolForDrops(state);
    }
    return super.isCorrectToolForDrops(stack, state);
  }

  @Override
  public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull Player player,
                                   @Nonnull Entity entity) {
    var tools = getInventoryFromItemStack(stack);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    if (!nowItem.isEmpty()) {
      attackTargetEntityWithTheItem(entity, player, nowItem);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(player, nowItem, InteractionHand.MAIN_HAND);
      }
      return true;
    }
    return false;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    var playerIn = context.getPlayer();
    var heldItem = context.getItemInHand();
    var tools = getInventoryFromItemStack(heldItem);
    var nowItem = tools.getItem(getSlotNumFromItemStack(heldItem));
    if (!nowItem.isEmpty() && Objects.nonNull(playerIn)) {
      var blockRayTraceResult = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
      var newContext = new UseOnContext(playerIn, context.getHand(), blockRayTraceResult);
      var ret = nowItem.getItem().useOn(newContext);
      tools.writeToNBT(Objects.requireNonNull(heldItem.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(playerIn, nowItem, InteractionHand.MAIN_HAND);
      }
      return ret;
    }
    return super.useOn(context);
  }

  @Override
  public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level worldIn,
                           @Nonnull LivingEntity entityLiving, int timeLeft) {
    var itemStackIn = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
    var tools = getInventoryFromItemStack(itemStackIn);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    if (!nowItem.isEmpty()) {
      nowItem.getItem().releaseUsing(nowItem, worldIn, entityLiving, timeLeft);
      if (nowItem.getCount() <= 0) {
        destroyTheItem(entityLiving, nowItem, InteractionHand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
  }

  @Override
  public ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level worldIn,
                                   @Nonnull LivingEntity entityLiving) {
    var itemStackIn = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
    var tools = getInventoryFromItemStack(itemStackIn);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    if (!nowItem.isEmpty()) {
      nowItem.getItem().finishUsingItem(nowItem, worldIn, entityLiving);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(entityLiving, itemStackIn, InteractionHand.MAIN_HAND);
      }
    }
    return stack;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level worldIn,
                                                Player playerIn, InteractionHand hand) {
    var itemStackIn = playerIn.getItemInHand(hand);
    var tools = getInventoryFromItemStack(itemStackIn);
    var activeSlot = getSlotNumFromItemStack(itemStackIn);
    var itemStack = tools.getItem(activeSlot);
    if (!itemStack.isEmpty()) {
      playerIn.setItemInHand(hand, itemStack);
      InteractionResultHolder<ItemStack> actionResult = itemStack.getItem()
              .use(worldIn, playerIn, hand);
      tools.setItem(
              activeSlot, actionResult
                      .getObject());
      playerIn.setItemInHand(hand, itemStackIn);
      tools.writeToNBT(Objects.requireNonNull(itemStackIn.getTag()));
      if (itemStack.getCount() <= 0) {
        destroyTheItem(playerIn, itemStackIn, InteractionHand.MAIN_HAND);
      }
      return new InteractionResultHolder<>(actionResult.getResult(), itemStackIn);
    }
    return super.use(worldIn, playerIn, hand);
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn,
                                               LivingEntity target, InteractionHand hand) {
    var tools = getInventoryFromItemStack(stack);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    var ret = InteractionResult.SUCCESS;
    if (!nowItem.isEmpty()) {
      ret = nowItem.getItem()
              .interactLivingEntity(nowItem, playerIn,
                      target, hand);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(playerIn, stack, InteractionHand.MAIN_HAND);
      }
    }
    return ret;
  }

  @Override
  public UseAnim getUseAnimation(ItemStack stack) {
    var itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getUseAnimation();
    }
    return super.getUseAnimation(stack);
  }

  @Override
  public int getUseDuration(@Nonnull ItemStack stack) {
    var itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getUseDuration();
    }
    return super.getUseDuration(stack);
  }

  @Override
  public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
    var itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getDestroySpeed(state);
    }
    return super.getDestroySpeed(stack, state);
  }

//  @Override
//  public boolean canHarvestBlock(ItemStack stack, BlockState state) {
//    ItemStack itemStack = getActiveItemStack(stack);
//    if (!itemStack.isEmpty()) {
//      return itemStack.isCorrectToolForDrops(state);
//    }
//    return super.canHarvestBlock(stack, state);
//  }
//
//  @Override
//  public Set<ToolType> getToolTypes(ItemStack stack) {
//    ItemStack itemStack = getActiveItemStack(stack);
//    if (!itemStack.isEmpty()) {
//      return itemStack.getToolTypes();
//    }
//    return super.getToolTypes(stack);
//  }

  @Override
  public boolean mineBlock(ItemStack stack, Level worldIn,
                           BlockState state,
                           BlockPos pos, LivingEntity entityLiving) {
    var tools = getInventoryFromItemStack(stack);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    if (!nowItem.isEmpty() && !worldIn.isClientSide) {
      var ret = nowItem.getItem()
              .mineBlock(nowItem, worldIn, state, pos,
                      entityLiving);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(entityLiving, stack, InteractionHand.MAIN_HAND);
      }
      return ret;
    }
    return super.mineBlock(stack, worldIn, state, pos, entityLiving);
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target,
                           LivingEntity attacker) {
    var tools = getInventoryFromItemStack(stack);
    var nowItem = tools.getItem(getSlotNumFromItemStack(stack));
    if (!nowItem.isEmpty() && !attacker.getCommandSenderWorld().isClientSide) {
      var ret = nowItem.getItem().hurtEnemy(nowItem, target, attacker);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      if (nowItem.getCount() <= 0) {
        destroyTheItem(attacker, nowItem, InteractionHand.MAIN_HAND);
      }
      return ret;
    }
    return super.hurtEnemy(stack, target, attacker);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
          EquipmentSlot slot, ItemStack stack) {
    var itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getAttributeModifiers(slot);
    }
    return super.getAttributeModifiers(slot, stack);
  }

  @Override
  public void doKeyAction(ItemStack itemStack, Player player, byte key) {
    if (key == OPEN_KEY) {
      if (!(player.containerMenu instanceof ToolHolderContainer)) {
        if (!player.level.isClientSide) {
          player.openMenu(new InterfaceToolHolder(type));
        }
      }
    } else if (key == NEXT_KEY) {
      var slot = getSlotNumFromItemStack(itemStack);
      setSlotNumToItemStack(itemStack, (slot + 1) % this.type.getSize());
    } else if (key == PREV_KEY) {
      var slot = getSlotNumFromItemStack(itemStack);
      setSlotNumToItemStack(itemStack, (this.type.getSize() + slot - 1) % this.type.getSize());
    }
  }

  public HolderType getType() {
    return type;
  }

  @ParametersAreNonnullByDefault
  @MethodsReturnNonnullByDefault
  public record InterfaceToolHolder(HolderType type) implements MenuProvider {

    @Override
    public Component getDisplayName() {
      return Component.translatable("item."
              + MOD_ID
              + "."
              + ak.mcmod.multitoolholders.Constants.REG_NAME_ITEM_MULTI_TOOL_PREFIX
              + type.getSize());
    }

    @Override
    public AbstractContainerMenu createMenu(int guiId, Inventory playerInventory, Player playerEntity) {
      var heldItem = playerEntity.getMainHandItem();
      var currentSlot = playerInventory.selected;
      var holderInventory = getInventoryFromItemStack(heldItem);
      return new ToolHolderContainer(type, guiId, playerInventory, holderInventory, currentSlot);
    }
  }
}