package ak.multitoolholders;

import static ak.multitoolholders.Constants.NBT_KEY_ENCHANT;
import static ak.multitoolholders.Constants.NBT_KEY_MTH;
import static ak.multitoolholders.Constants.NBT_KEY_SLOT;

import ak.multitoolholders.inventory.ContainerToolHolder;
import ak.multitoolholders.inventory.InventoryToolHolder;
import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.network.NetworkHooks;

//@Optional.InterfaceList(
//        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
//                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
//)
public class ItemMultiToolHolder extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

  public static final byte OPEN_KEY = 0;
  public static final byte NEXT_KEY = 1;
  public static final byte PREV_KEY = 2;
  private final EnumHolderType type;

  public ItemMultiToolHolder(EnumHolderType type) {
    super(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).defaultMaxDamage(0)
        .addToolType(ToolType.AXE, -1).addToolType(ToolType.PICKAXE, -1)
        .addToolType(ToolType.SHOVEL, -1));
    this.type = type;
  }

  public static int getSlotNumFromItemStack(ItemStack itemStack) {
    if (!itemStack.hasTag()) {
      itemStack.setTag(new NBTTagCompound());
    }
    if (!itemStack.getTag().contains(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      NBTTagCompound nbtTagCompound = new NBTTagCompound();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    NBTTagCompound nbt = (NBTTagCompound) itemStack.getTag().get(NBT_KEY_MTH);
    return nbt.getInt(NBT_KEY_SLOT);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
      ITooltipFlag flagIn) {
    ITextComponent toolName;
    InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
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
    if (entity instanceof EntityPlayer && isHeld && !world.isRemote) {

      if (itemStack.hasTag()) {
        itemStack.getTag().remove(NBT_KEY_ENCHANT);
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
      @Nonnull EntityPlayer playerIn) {
    if (itemStack.getItem() instanceof ItemMultiToolHolder) {
      itemStack.setTag(new NBTTagCompound());
    }
  }

  @Override
  public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos blockPos,
      @Nonnull EntityPlayer player) {
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.getItem().onBlockStartBreak(stack, blockPos, player);
    }
    return super.onBlockStartBreak(stack, blockPos, player);
  }

  @Override
  public int getHarvestLevel(ItemStack stack, ToolType tool, @Nullable EntityPlayer player,
      @Nullable IBlockState blockState) {
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      return nowItem.getItem().getHarvestLevel(nowItem, tool, player, blockState);
    }
    return super.getHarvestLevel(stack, tool, player, blockState);
  }

  @Override
  public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player,
      @Nonnull Entity entity) {
    InventoryToolHolder toolHolder = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      this.attackTargetEntityWithTheItem(entity, player, itemStack);
      toolHolder.writeToNBT(stack.getTag());
      return true;
    }
    return false;
  }

  @Override
  @Nonnull
  public EnumActionResult onItemUse(ItemUseContext context) {
    EntityPlayer playerIn = context.getPlayer();
    ItemStack heldItem = context.getItem();
    InventoryToolHolder toolHolder = getInventoryFromItemStack(heldItem);
    int activeSlot = getSlotNumFromItemStack(heldItem);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      ItemUseContext newContext = new ItemUseContext(playerIn, itemStack,
          context.getPos(), context.getFace(), context.getHitX(), context.getHitY(),
          context.getHitZ());
      EnumActionResult ret = itemStack.getItem().onItemUse(newContext);
      toolHolder.writeToNBT(heldItem.getTag());
      return ret;
    }
    return super.onItemUse(context);
  }

  @Override
  public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World worldIn,
      @Nonnull EntityLivingBase entityLiving, int timeLeft) {
    ItemStack itemStackIn = entityLiving.getHeldItem(EnumHand.MAIN_HAND);
    InventoryToolHolder tools = this.getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onPlayerStoppedUsing(itemStack, worldIn, entityLiving, timeLeft);
      if (itemStack.getCount() <= 0) {
        this.destroyTheItem(entityLiving, itemStack, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(stack.getTag());
    }
  }

  @Nonnull
  @Override
  public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World worldIn,
      @Nonnull EntityLivingBase entityLiving) {
    ItemStack itemStackIn = entityLiving.getHeldItem(EnumHand.MAIN_HAND);
    InventoryToolHolder tools = this.getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onItemUseFinish(itemStack, worldIn, entityLiving);
      tools.writeToNBT(stack.getTag());
    }
    return stack;
  }

  @Override
  @Nonnull
  public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn,
      @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand) {
    ItemStack itemStackIn = playerIn.getHeldItem(hand);
    InventoryToolHolder tools = this.getInventoryFromItemStack(itemStackIn);
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
      tools.writeToNBT(itemStackIn.getTag());
      return new ActionResult<>(actionResult.getType(), itemStackIn);
    }
    return super.onItemRightClick(worldIn, playerIn, hand);
  }

  @Override
  public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn,
      @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
    InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    boolean ret = false;
    if (!itemStack.isEmpty()) {
      ret = itemStack.getItem()
          .itemInteractionForEntity(itemStack, playerIn,
              target, hand);
      tools.writeToNBT(stack.getTag());
    }
    return ret;
  }

  @Override
  @Nonnull
  public EnumAction getUseAction(@Nonnull ItemStack stack) {
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
  public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItem().getDestroySpeed(itemStack, state);
    }
    return super.getDestroySpeed(stack, state);
  }

  @Override
  public boolean canHarvestBlock(ItemStack stack, IBlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItem().canHarvestBlock(stack, state);
    }
    return super.canHarvestBlock(stack, state);
  }

  @Override
  public boolean onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World worldIn,
      @Nonnull IBlockState state,
      @Nonnull BlockPos pos, @Nonnull EntityLivingBase entityLiving) {
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !worldIn.isRemote) {
      boolean ret = nowItem.getItem()
          .onBlockDestroyed(nowItem, worldIn, state, pos,
              entityLiving);
      if (nowItem.getCount() <= 0) {
        this.destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(stack.getTag());
      return ret;
    }
    return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
  }

  @Override
  public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target,
      @Nonnull EntityLivingBase attacker) {
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !attacker.getEntityWorld().isRemote) {
      boolean ret = nowItem.getItem().hitEntity(nowItem, target, attacker);
      if (nowItem.getCount() <= 0) {
        this.destroyTheItem(attacker, nowItem, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(stack.getTag());
      return ret;
    }
    return super.hitEntity(stack, target, attacker);
  }

  @Override
  @Nonnull
  public Multimap<String, AttributeModifier> getAttributeModifiers(
      @Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
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
  private void attackTargetEntityWithTheItem(Entity targetEntity, EntityPlayer player,
      ItemStack stack) {
    // 手持ちアイテムを退避
    ItemStack toolHolder = player.getHeldItemMainhand();
    // ツールホルダー内のアイテムを手持ちに設定
    player.setHeldItem(EnumHand.MAIN_HAND, stack);
    player.attackTargetEntityWithCurrentItem(targetEntity);
    // 退避させたツールホルダーを設定
    player.setHeldItem(EnumHand.MAIN_HAND, toolHolder);
  }

  /**
   * 破壊処理メソッド
   *
   * @param entityLivingBase 破壊者
   * @param orig ツールホルダー
   * @param hand 持ち手
   */
  private void destroyTheItem(EntityLivingBase entityLivingBase, ItemStack orig, EnumHand hand) {
    InventoryToolHolder tools = this.getInventoryFromItemStack(orig);
    int slotNum = getSlotNumFromItemStack(orig);
    tools.setInventorySlotContents(slotNum, ItemStack.EMPTY);
    if (entityLivingBase instanceof EntityPlayer) {
      MinecraftForge.EVENT_BUS
          .post(new PlayerDestroyItemEvent((EntityPlayer) entityLivingBase, orig, hand));
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
    NBTTagList list = itemEnchanted.getEnchantmentTagList();
    if (!list.isEmpty()) {
      for (int i = 0; i < list.size(); ++i) {
        if (list.getCompound(i).getShort("lvl") > 0) {
          id = list.getCompound(i).getString("id");
          lv = list.getCompound(i).getShort("lvl");
          MultiToolHolders
              .addEnchantmentToItem(itemToEnchant, IRegistry.field_212628_q.func_212608_b(new ResourceLocation(id)), lv);
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
      itemStack.setTag(new NBTTagCompound());
    }
    if (!itemStack.getTag().contains(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      NBTTagCompound nbtTagCompound = new NBTTagCompound();
      itemStack.getTag().put(NBT_KEY_MTH, nbtTagCompound);
    }
    NBTTagCompound nbt = (NBTTagCompound) itemStack.getTag().get(NBT_KEY_MTH);
    nbt.putInt(NBT_KEY_SLOT, slotNum);
  }

  /**
   * ツールホルダーのインベントリを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return インベントリツールホルダー
   */
  @Nonnull
  public InventoryToolHolder getInventoryFromItemStack(@Nonnull ItemStack itemStack) {
    return new InventoryToolHolder(itemStack);
  }

  /**
   * ツールホルダーのActiveItemStackを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return ActiveItemStack
   */
  @Nonnull
  public ItemStack getActiveItemStack(@Nonnull ItemStack itemStack) {
    int slot = getSlotNumFromItemStack(itemStack);
    return getInventoryFromItemStack(itemStack).getStackInSlot(slot);
  }

  @Override
  public void doKeyAction(@Nonnull ItemStack itemStack, @Nonnull EntityPlayer player, byte key) {
    if (key == OPEN_KEY) {
      if (!(player.openContainer instanceof ContainerToolHolder)) {
        if (!player.world.isRemote) {
          NetworkHooks.openGui((EntityPlayerMP) player, new InterfaceToolHolder(type));
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

  public EnumHolderType getType() {
    return type;
  }

  //    @Optional.Method(modid = "CoFHCore")
//    @Override
  public boolean isUsable(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y,
      int z) {
    ItemStack nowItem = getInventoryFromItemStack(itemStack)
        .getStackInSlot(getSlotNumFromItemStack(itemStack));
    return CoopTE.isUsable(nowItem, entityLivingBase, x, y, z);
  }

  //    @Optional.Method(modid = "CoFHCore")
//    @Override
  public void toolUsed(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y,
      int z) {
    ItemStack nowItem = getInventoryFromItemStack(itemStack)
        .getStackInSlot(getSlotNumFromItemStack(itemStack));
    CoopTE.toolUsed(nowItem, entityLivingBase, x, y, z);
  }

  //    @Optional.Method(modid = "BuildCraftAPI|core")
//    @Override
  public boolean canWrench(EntityPlayer player, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(player.getHeldItemMainhand())
        .getStackInSlot(getSlotNumFromItemStack(player.getHeldItemMainhand()));
    return CoopBC.canWrench(nowItem, player, x, y, z);
  }

  //    @Optional.Method(modid = "BuildCraftAPI|core")
//    @Override
  public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(player.getHeldItemMainhand())
        .getStackInSlot(getSlotNumFromItemStack(player.getHeldItemMainhand()));
    CoopBC.wrenchUsed(nowItem, player, x, y, z);
  }

  public static class InterfaceToolHolder implements IInteractionObject {

    private EnumHolderType type;

    public InterfaceToolHolder(EnumHolderType type) {
      this.type = type;
    }

    @Override
    @Nonnull
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
      ItemStack heldItem = playerIn.getHeldItemMainhand();
      int currentSlot = playerInventory.currentItem;
      return new ContainerToolHolder(playerIn, heldItem, type, currentSlot);
    }

    @Override
    @Nonnull
    public String getGuiID() {
      return ak.multitoolholders.Constants.MOD_ID + ":toolholder" + type.getGuiId();
    }

    @Override
    @Nonnull
    public ITextComponent getName() {
      return new TextComponentTranslation(type.getContainerNameKey() + ".name");
    }

    @Override
    public boolean hasCustomName() {
      return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
      return null;
    }
  }
}