package ak.multitoolholders.item;

import ak.multitoolholders.IKeyEvent;
import ak.multitoolholders.MultiToolHolders;
import ak.multitoolholders.inventory.ToolHolderContainer;
import ak.multitoolholders.inventory.ToolHolderInventory;
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

import static ak.multitoolholders.Constants.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiToolHolderItem extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

  public static final byte OPEN_KEY = 0;
  public static final byte NEXT_KEY = 1;
  public static final byte PREV_KEY = 2;
  private final HolderType type;

  public MultiToolHolderItem(HolderType type) {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0)
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
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
      ITooltipFlag flagIn) {
    ITextComponent toolName;
    ToolHolderInventory tools = this.getInventoryFromItemStack(stack);
    for (int i = 0; i < type.getSize(); i++) {
      if (!tools.getStackInSlot(i).isEmpty()) {
        toolName = tools.getStackInSlot(i).getDisplayName();
        tooltip.add(toolName);
      }
    }
  }

  @Override
  public void inventoryTick(@Nonnull ItemStack itemStack, @Nonnull World world,
      @Nonnull Entity entity, int slot, boolean isHeld) {
    if (entity instanceof PlayerEntity && isHeld && !world.isRemote) {

      if (itemStack.hasTag()) {
        Objects.requireNonNull(itemStack.getTag()).remove(NBT_KEY_ENCHANT);
      }

      ItemStack nowItem = getActiveItemStack(itemStack);
      if (!nowItem.isEmpty()) {
        nowItem.getItem().inventoryTick(nowItem, world, entity, slot, true);
        this.setEnchantments(itemStack, nowItem);
      }
    }
  }

  @Override
  public void onCreated(@Nonnull ItemStack itemStack, @Nonnull World worldIn,
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
      return nowItem.getItem().onBlockStartBreak(stack, blockPos, player);
    }
    return super.onBlockStartBreak(stack, blockPos, player);
  }

  @Override
  public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable PlayerEntity player,
      @Nullable BlockState blockState) {
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.getItem().getHarvestLevel(nowItem, tool, player, blockState);
    }
    return super.getHarvestLevel(stack, tool, player, blockState);
  }

  @Override
  public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull PlayerEntity player,
      @Nonnull Entity entity) {
    ToolHolderInventory toolHolder = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      this.attackTargetEntityWithTheItem(entity, player, itemStack);
      toolHolder.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return true;
    }
    return false;
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context) {
    PlayerEntity playerIn = context.getPlayer();
    ItemStack heldItem = context.getItem();
    ToolHolderInventory toolHolder = getInventoryFromItemStack(heldItem);
    int activeSlot = getSlotNumFromItemStack(heldItem);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty() && Objects.nonNull(playerIn)) {
      BlockRayTraceResult blockRayTraceResult = new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside());
      ItemUseContext newContext = new ItemUseContext(playerIn, context.getHand(), blockRayTraceResult);
      ActionResultType ret = itemStack.getItem().onItemUse(newContext);
      toolHolder.writeToNBT(Objects.requireNonNull(heldItem.getTag()));
      return ret;
    }
    return super.onItemUse(context);
  }

  @Override
  public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World worldIn,
      @Nonnull LivingEntity entityLiving, int timeLeft) {
    ItemStack itemStackIn = entityLiving.getHeldItem(Hand.MAIN_HAND);
    ToolHolderInventory tools = this.getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onPlayerStoppedUsing(itemStack, worldIn, entityLiving, timeLeft);
      if (itemStack.getCount() <= 0) {
        this.destroyTheItem(entityLiving, itemStack, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
  }

  @Override
  public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World worldIn,
      @Nonnull LivingEntity entityLiving) {
    ItemStack itemStackIn = entityLiving.getHeldItem(Hand.MAIN_HAND);
    ToolHolderInventory tools = this.getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onItemUseFinish(itemStack, worldIn, entityLiving);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
    return stack;
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn,
      PlayerEntity playerIn, Hand hand) {
    ItemStack itemStackIn = playerIn.getHeldItem(hand);
    ToolHolderInventory tools = this.getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      playerIn.setHeldItem(hand, itemStack);
      ActionResult<ItemStack> actionResult = itemStack.getItem()
          .onItemRightClick(worldIn, playerIn, hand);
      tools.setInventorySlotContents(
          activeSlot, actionResult
              .getResult());
      playerIn.setHeldItem(hand, itemStackIn);
      tools.writeToNBT(Objects.requireNonNull(itemStackIn.getTag()));
      return new ActionResult<>(actionResult.getType(), itemStackIn);
    }
    return super.onItemRightClick(worldIn, playerIn, hand);
  }

  @Override
  public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn,
      LivingEntity target, Hand hand) {
    ToolHolderInventory tools = this.getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    ActionResultType ret = ActionResultType.SUCCESS;
    if (!itemStack.isEmpty()) {
      ret = itemStack.getItem()
          .itemInteractionForEntity(itemStack, playerIn,
              target, hand);
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
    }
    return ret;
  }

  @Override
  public UseAction getUseAction(ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getUseAction();
    }
    return super.getUseAction(stack);
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
      return itemStack.getItem().getDestroySpeed(itemStack, state);
    }
    return super.getDestroySpeed(stack, state);
  }

  @Override
  public boolean canHarvestBlock(ItemStack stack, BlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItem().canHarvestBlock(stack, state);
    }
    return super.canHarvestBlock(stack, state);
  }

  @Override
  public boolean onBlockDestroyed(ItemStack stack, World worldIn,
      BlockState state,
      BlockPos pos, LivingEntity entityLiving) {
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !worldIn.isRemote) {
      boolean ret = nowItem.getItem()
          .onBlockDestroyed(nowItem, worldIn, state, pos,
              entityLiving);
      if (nowItem.getCount() <= 0) {
        this.destroyTheItem(entityLiving, stack, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return ret;
    }
    return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
  }

  @Override
  public boolean hitEntity(ItemStack stack, LivingEntity target,
      LivingEntity attacker) {
    ToolHolderInventory tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !attacker.getEntityWorld().isRemote) {
      boolean ret = nowItem.getItem().hitEntity(nowItem, target, attacker);
      if (nowItem.getCount() <= 0) {
        this.destroyTheItem(attacker, nowItem, Hand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTag()));
      return ret;
    }
    return super.hitEntity(stack, target, attacker);
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
   * @param player 攻撃者
   * @param stack ツールホルダー内のActiveItemStack
   */
  private void attackTargetEntityWithTheItem(Entity targetEntity, PlayerEntity player,
      ItemStack stack) {
    // 手持ちアイテムを退避
    ItemStack toolHolder = player.getHeldItemMainhand();
    // ツールホルダー内のアイテムを手持ちに設定
    player.setHeldItem(Hand.MAIN_HAND, stack);
    player.attackTargetEntityWithCurrentItem(targetEntity);
    // 退避させたツールホルダーを設定
    player.setHeldItem(Hand.MAIN_HAND, toolHolder);
  }

  /**
   * 破壊処理メソッド
   *
   * @param livingEntity 破壊者
   * @param orig ツールホルダー
   * @param hand 持ち手
   */
  private void destroyTheItem(LivingEntity livingEntity, ItemStack orig, Hand hand) {
    ToolHolderInventory tools = this.getInventoryFromItemStack(orig);
    int slotNum = getSlotNumFromItemStack(orig);
    tools.setInventorySlotContents(slotNum, ItemStack.EMPTY);
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
  private void setEnchantments(ItemStack itemToEnchant, ItemStack itemEnchanted) {
    String id;
    int lv;
    ListNBT list = itemEnchanted.getEnchantmentTagList();
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
   * @param slotNum 新しいスロット番号
   */
  private void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
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
  public ToolHolderInventory getInventoryFromItemStack(ItemStack itemStack) {
    return new ToolHolderInventory(itemStack);
  }

  /**
   * ツールホルダーのActiveItemStackを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return ActiveItemStack
   */
  public ItemStack getActiveItemStack(ItemStack itemStack) {
    int slot = getSlotNumFromItemStack(itemStack);
    return getInventoryFromItemStack(itemStack).getStackInSlot(slot);
  }

  @Override
  public void doKeyAction(ItemStack itemStack, PlayerEntity player, byte key) {
    if (key == OPEN_KEY) {
      if (!(player.openContainer instanceof ToolHolderContainer)) {
        if (!player.world.isRemote) {
//          NetworkHooks.openGui((ServerPlayerEntity) player, new InterfaceToolHolder(type));
          player.openContainer(new InterfaceToolHolder(type));
        }
      }
    } else if (key == NEXT_KEY) {
      int slot = getSlotNumFromItemStack(itemStack);
      this.setSlotNumToItemStack(itemStack, (slot + 1) % this.type.getSize());
    } else if (key == PREV_KEY) {
      int slot = getSlotNumFromItemStack(itemStack);
      this.setSlotNumToItemStack(itemStack, (this.type.getSize() + slot - 1) % this.type.getSize());
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
              + ak.multitoolholders.Constants.REG_NAME_ITEM_MULTI_TOOL_PREFIX
              + type.getSize());
    }

    @Nullable
    @Override
    public Container createMenu(int guiId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      ItemStack heldItem = playerEntity.getHeldItemMainhand();
      int currentSlot = playerInventory.currentItem;
      IInventory holderInventory = ((MultiToolHolderItem) heldItem.getItem())
              .getInventoryFromItemStack(heldItem);
      return new ToolHolderContainer(type, guiId, playerInventory, holderInventory, currentSlot);
    }
  }
}