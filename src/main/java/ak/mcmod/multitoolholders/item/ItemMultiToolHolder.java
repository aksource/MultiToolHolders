package ak.mcmod.multitoolholders.item;

import ak.mcmod.multitoolholders.CoopBC;
import ak.mcmod.multitoolholders.CoopTE;
import ak.mcmod.multitoolholders.IKeyEvent;
import ak.mcmod.multitoolholders.MultiToolHolders;
import ak.mcmod.multitoolholders.inventory.ContainerToolHolder;
import ak.mcmod.multitoolholders.inventory.InventoryToolHolder;
import com.google.common.collect.Multimap;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ak.mcmod.multitoolholders.Constants.*;

@Optional.InterfaceList(
        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemMultiToolHolder extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

  public static final byte OPEN_KEY = 0;
  public static final byte NEXT_KEY = 1;
  public static final byte PREV_KEY = 2;
  private final HolderType type;

  public ItemMultiToolHolder(HolderType type) {
    super();
    this.setHasSubtypes(true);
    this.setMaxStackSize(1);
    this.setMaxDamage(0);
    this.setCreativeTab(CreativeTabs.TOOLS);
    this.type = type;
  }

  public static int getSlotNumFromItemStack(ItemStack itemStack) {
    if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
    if (!Objects.requireNonNull(itemStack.getTagCompound()).hasKey(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      NBTTagCompound nbtTagCompound = new NBTTagCompound();
      itemStack.getTagCompound().setTag(NBT_KEY_MTH, nbtTagCompound);
    }
    NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag(NBT_KEY_MTH);
    return nbt.getInteger(NBT_KEY_SLOT);
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    String ToolName;
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    for (int i = 0; i < type.getSize(); i++) {
      if (!tools.getStackInSlot(i).isEmpty()) {
        ToolName = tools.getStackInSlot(i).getDisplayName();
        tooltip.add(ToolName);
      }
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean isFull3D() {
    //Toolか剣しか普通は使わないので、falseの時はないと判断。
    return true;
  }

  @Override
  public void onUpdate(ItemStack itemStack, World world, Entity entity, int slot, boolean isHeld) {
    if (entity instanceof EntityPlayer && isHeld && !world.isRemote) {

      if (itemStack.hasTagCompound()) {
        Objects.requireNonNull(itemStack.getTagCompound()).removeTag(NBT_KEY_ENCHANT);
      }

      ItemStack nowItem = getActiveItemStack(itemStack);
      if (!nowItem.isEmpty()) {
        nowItem.getItem().onUpdate(nowItem, world, entity, slot, true);
        setEnchantments(itemStack, nowItem);
      }
    }
  }

  @Override
  public void onCreated(ItemStack itemStack, World worldIn, EntityPlayer playerIn) {
    if (itemStack.getItem() instanceof ItemMultiToolHolder) {
      itemStack.setTagCompound(new NBTTagCompound());
    }
  }

  @Override
  public boolean onBlockStartBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
    for (String toolClass : this.getToolClasses(stack)) {
      this.setHarvestLevel(toolClass, -1);
    }
    ItemStack nowItem = getActiveItemStack(stack);
    if (!nowItem.isEmpty()) {
      Set<String> toolClasses = nowItem.getItem().getToolClasses(nowItem);
      IBlockState blockState = player.getEntityWorld().getBlockState(blockPos);
      int harvestLevel;
      for (String toolClass : toolClasses) {
        harvestLevel = nowItem.getItem().getHarvestLevel(nowItem, toolClass, player, blockState);
        this.setHarvestLevel(toolClass, harvestLevel);
      }
      return nowItem.getItem().onBlockStartBreak(stack, blockPos, player);
    }
    return super.onBlockStartBreak(stack, blockPos, player);
  }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
    InventoryToolHolder toolHolder = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      attackTargetEntityWithTheItem(entity, player, itemStack);
      toolHolder.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
      return true;
    }
    return false;
  }

  @Override
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                         EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack heldItem = player.getHeldItem(hand);
    InventoryToolHolder toolHolder = getInventoryFromItemStack(heldItem);
    int activeSlot = getSlotNumFromItemStack(heldItem);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      player.setHeldItem(hand, itemStack);
      EnumActionResult ret = itemStack.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
      player.setHeldItem(hand, heldItem);
      toolHolder.writeToNBT(Objects.requireNonNull(heldItem.getTagCompound()));
      return ret;
    }
    return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
  }

  @Override
  public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    ItemStack heldItem = playerIn.getHeldItem(hand);
    InventoryToolHolder toolHolder = getInventoryFromItemStack(heldItem);
    int activeSlot = getSlotNumFromItemStack(heldItem);
    ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      playerIn.setHeldItem(hand, itemStack);
      EnumActionResult ret = itemStack.getItem().onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
      playerIn.setHeldItem(hand, heldItem);
      toolHolder.writeToNBT(Objects.requireNonNull(heldItem.getTagCompound()));
      return ret;
    }
    return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
  }

  @Override
  public void onPlayerStoppedUsing(ItemStack stack, World worldIn,
                                   EntityLivingBase entityLiving, int timeLeft) {
    ItemStack itemStackIn = entityLiving.getHeldItem(EnumHand.MAIN_HAND);
    InventoryToolHolder tools = getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onPlayerStoppedUsing(itemStack, worldIn, entityLiving, timeLeft);
      if (itemStack.getCount() <= 0) {
        destroyTheItem(entityLiving, itemStack, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
    }
  }

  @Override
  public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
    ItemStack itemStackIn = entityLiving.getHeldItem(EnumHand.MAIN_HAND);
    InventoryToolHolder tools = getInventoryFromItemStack(itemStackIn);
    int activeSlot = getSlotNumFromItemStack(itemStackIn);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    if (!itemStack.isEmpty()) {
      itemStack.getItem().onItemUseFinish(itemStack, worldIn, entityLiving);
      tools.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
    }
    return stack;
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
    ItemStack itemStackIn = playerIn.getHeldItem(hand);
    InventoryToolHolder tools = getInventoryFromItemStack(itemStackIn);
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
      tools.writeToNBT(Objects.requireNonNull(itemStackIn.getTagCompound()));
      return new ActionResult<>(actionResult.getType(), itemStackIn);
    }
    return super.onItemRightClick(worldIn, playerIn, hand);
  }

  @Override
  public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn,
                                          EntityLivingBase target, EnumHand hand) {
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack itemStack = tools.getStackInSlot(activeSlot);
    boolean ret = false;
    if (!itemStack.isEmpty()) {
      ret = itemStack.getItem()
              .itemInteractionForEntity(itemStack, playerIn,
                      target, hand);
      tools.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
    }
    return ret;
  }

  @Override
  public EnumAction getItemUseAction(ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItemUseAction();
    }
    return super.getItemUseAction(stack);
  }

  @Override
  public int getMaxItemUseDuration(ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getMaxItemUseDuration();
    }
    return super.getMaxItemUseDuration(stack);
  }

  @Override
  public float getDestroySpeed(ItemStack stack, IBlockState state) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItem().getDestroySpeed(itemStack, state);
    }
    return super.getDestroySpeed(stack, state);
  }

  @Override
  public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
    ItemStack itemStack = getActiveItemStack(stack);
    if (!itemStack.isEmpty()) {
      return itemStack.getItem().canHarvestBlock(state, itemStack);
    }
    return super.canHarvestBlock(state, stack);
  }

  @Override
  public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state,
                                  BlockPos pos, EntityLivingBase entityLiving) {
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !worldIn.isRemote) {
      boolean ret = nowItem.getItem()
              .onBlockDestroyed(nowItem, worldIn, state, pos,
                      entityLiving);
      if (nowItem.getCount() <= 0) {
        destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
      return ret;
    }
    return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
  }

  @Override
  public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
    InventoryToolHolder tools = getInventoryFromItemStack(stack);
    int activeSlot = getSlotNumFromItemStack(stack);
    ItemStack nowItem = tools.getStackInSlot(activeSlot);
    if (!nowItem.isEmpty() && !attacker.getEntityWorld().isRemote) {
      boolean ret = nowItem.getItem().hitEntity(nowItem, target, attacker);
      if (nowItem.getCount() <= 0) {
        destroyTheItem(attacker, nowItem, EnumHand.MAIN_HAND);
      }
      tools.writeToNBT(Objects.requireNonNull(stack.getTagCompound()));
      return ret;
    }
    return super.hitEntity(stack, target, attacker);
  }

  @Override
  public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
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
  private static void attackTargetEntityWithTheItem(Entity targetEntity, EntityPlayer player, ItemStack stack) {
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
   * @param orig             ツールホルダー
   * @param hand             持ち手
   */
  private static void destroyTheItem(EntityLivingBase entityLivingBase, ItemStack orig, EnumHand hand) {
    InventoryToolHolder tools = getInventoryFromItemStack(orig);
    int slotNum = getSlotNumFromItemStack(orig);
    tools.setInventorySlotContents(slotNum, ItemStack.EMPTY);
    if (entityLivingBase instanceof EntityPlayer)
      MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent((EntityPlayer) entityLivingBase, orig, hand));
  }

  /**
   * エンチャント付与処理
   *
   * @param itemToEnchant エンチャントされるアイテム
   * @param itemEnchanted エンチャントされているアイテム
   */
  private static void setEnchantments(ItemStack itemToEnchant, ItemStack itemEnchanted) {
    int id;
    int lv;
    NBTTagList list = itemEnchanted.getEnchantmentTagList();
    if (!list.isEmpty()) {
      for (int i = 0; i < list.tagCount(); ++i) {
        if (list.getCompoundTagAt(i).getShort("lvl") > 0) {
          id = list.getCompoundTagAt(i).getShort("id");
          lv = list.getCompoundTagAt(i).getShort("lvl");
          MultiToolHolders.addEnchantmentToItem(itemToEnchant, Enchantment.getEnchantmentByID(id), lv);
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
    if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
    if (!Objects.requireNonNull(itemStack.getTagCompound()).hasKey(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
      NBTTagCompound nbtTagCompound = new NBTTagCompound();
      itemStack.getTagCompound().setTag(NBT_KEY_MTH, nbtTagCompound);
    }
    NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag(NBT_KEY_MTH);
    nbt.setInteger(NBT_KEY_SLOT, slotNum);
  }

  /**
   * ツールホルダーのインベントリを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return インベントリツールホルダー
   */
  public static InventoryToolHolder getInventoryFromItemStack(ItemStack itemStack) {
    return new InventoryToolHolder(itemStack);
  }

  /**
   * ツールホルダーのActiveItemStackを取得
   *
   * @param itemStack ツールホルダーアイテム
   * @return ActiveItemStack
   */
  public static ItemStack getActiveItemStack(ItemStack itemStack) {
    int slot = getSlotNumFromItemStack(itemStack);
    return getInventoryFromItemStack(itemStack).getStackInSlot(slot);
  }

  @Override
  public void doKeyAction(ItemStack itemStack, EntityPlayer player, byte key) {
    if (key == OPEN_KEY) {
      if (!(player.openContainer instanceof ContainerToolHolder)) {
        player.openGui(MultiToolHolders.instance, this.type.getGuiId(), player.getEntityWorld(), 0, 0, 0);
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

  @Optional.Method(modid = "CoFHCore")
//    @Override
  public boolean isUsable(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(itemStack).getStackInSlot(getSlotNumFromItemStack(itemStack));
    return CoopTE.isUsable(nowItem, entityLivingBase, x, y, z);
  }

  @Optional.Method(modid = "CoFHCore")
//    @Override
  public void toolUsed(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(itemStack).getStackInSlot(getSlotNumFromItemStack(itemStack));
    CoopTE.toolUsed(nowItem, entityLivingBase, x, y, z);
  }

  @Optional.Method(modid = "BuildCraftAPI|core")
//    @Override
  public boolean canWrench(EntityPlayer player, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(player.getHeldItemMainhand()).getStackInSlot(getSlotNumFromItemStack(player.getHeldItemMainhand()));
    return CoopBC.canWrench(nowItem, player, x, y, z);
  }

  @Optional.Method(modid = "BuildCraftAPI|core")
//    @Override
  public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
    ItemStack nowItem = getInventoryFromItemStack(player.getHeldItemMainhand()).getStackInSlot(getSlotNumFromItemStack(player.getHeldItemMainhand()));
    CoopBC.wrenchUsed(nowItem, player, x, y, z);
  }
}