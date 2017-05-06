package ak.multitoolholders;

import ak.multitoolholders.inventory.ContainerToolHolder;
import ak.multitoolholders.inventory.InventoryToolHolder;
import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.google.common.collect.Multimap;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

@Optional.InterfaceList(
        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
)
public class ItemMultiToolHolder extends Item implements IKeyEvent, IToolHammer, IToolWrench {

    public static final byte OPEN_KEY = 0;
    public static final byte NEXT_KEY = 1;
    public static final byte PREV_KEY = 2;
    private final EnumHolderType type;

    public ItemMultiToolHolder(EnumHolderType type) {
        super();
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.tabTools);
        this.type = type;
    }

    public static int getSlotNumFromItemStack(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey("multitoolholders")) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag("multitoolholders", nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag("multitoolholders");
        return nbt.getInteger("slot");
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltip, boolean advanced) {
        String toolName;
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        for (int i = 0; i < type.getSize(); i++) {
            if (tools != null && tools.getStackInSlot(i) != null) {
                toolName = tools.getStackInSlot(i).getDisplayName();
                tooltip.add(toolName);
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
                itemStack.getTagCompound().removeTag(Constants.NBT_KEY_ENCHANT);
            }

            InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
            int slotNum = getSlotNumFromItemStack(itemStack);

            if (tools != null && tools.getStackInSlot(slotNum) != null) {
                tools.getStackInSlot(slotNum).getItem()
                        .onUpdate(tools.getStackInSlot(slotNum), world, entity, slot, true);
                this.setEnchantments(itemStack, tools.getStackInSlot(slotNum));
            }
        }
    }

    @Override
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
        if (itemStack.getItem() instanceof ItemMultiToolHolder) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        for (String toolClass : this.getToolClasses(stack)) {
            this.setHarvestLevel(toolClass, -1);
        }
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack nowItem = tools.getStackInSlot(slotNum);
            Set<String> toolClasses = nowItem.getItem().getToolClasses(nowItem);
            int harvestLevel;
            for (String toolClass : toolClasses) {
                harvestLevel = nowItem.getItem().getHarvestLevel(nowItem, toolClass);
                this.setHarvestLevel(toolClass, harvestLevel);
            }
            return nowItem.getItem().onBlockStartBreak(stack, x, y, z, player);
        }
        return super.onBlockStartBreak(stack, x, y, z, player);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            this.attackTargetEntityWithTheItem(entity, player, tools.getStackInSlot(slotNum));
            tools.writeToNBT(stack.getTagCompound());
            return true;
        }
        return false;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
                                  float hitX, float hitY, float hitZ) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            boolean ret = slotStack.getItem().onItemUseFirst(slotStack, player, world, x, y, z, side, hitX, hitY, hitZ);
            if (slotStack.stackSize <= 0) {
                this.destroyTheItem(player, slotStack);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int posX,
                             int posY, int posZ, int side, float hitX, float hitY, float hitZ) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            boolean ret = slotStack.getItem().onItemUse(slotStack, player, world, posX, posY, posZ, side, hitX, hitY, hitZ);
            if (slotStack.stackSize <= 0) {
                this.destroyTheItem(player, slotStack);
            }
            tools.writeToNBT(itemStack.getTagCompound());
            return ret;
        }
        return false;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int timeLeft) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            slotStack.getItem().onPlayerStoppedUsing(slotStack, world, player, timeLeft);
            if (slotStack.stackSize <= 0) {
                this.destroyTheItem(player, slotStack);
            }
            tools.writeToNBT(itemStack.getTagCompound());
        }
    }

    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            slotStack.getItem().onEaten(slotStack, world, player);
            tools.writeToNBT(itemStack.getTagCompound());
        }
        return itemStack;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            tools.setInventorySlotContents(
                    slotNum,
                    tools.getStackInSlot(slotNum).getItem()
                            .onItemRightClick(tools.getStackInSlot(slotNum), world, player));
            tools.writeToNBT(itemStack.getTagCompound());
        }
        if (this.getItemUseAction(itemStack) != EnumAction.none)
            player.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
        return itemStack;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player,
                                            EntityLivingBase livingBase) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            ItemStack nowItem = tools.getStackInSlot(slotNum);
            boolean ret = nowItem.getItem().itemInteractionForEntity(nowItem, player, livingBase);
            tools.writeToNBT(itemStack.getTagCompound());
            return ret;
        }
        return super.itemInteractionForEntity(itemStack, player, livingBase);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemStack) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            return tools.getStackInSlot(slotNum).getItemUseAction();
        }
        return super.getItemUseAction(itemStack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemStack) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            return tools.getStackInSlot(slotNum).getMaxItemUseDuration();
        }
        return super.getMaxItemUseDuration(itemStack);
    }

    @Override
    public float getDigSpeed(ItemStack stack, Block block, int meta) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            return tools.getStackInSlot(slotNum).getItem()
                    .getDigSpeed(tools.getStackInSlot(slotNum), block, meta);
        }
        return super.getDigSpeed(stack, block, meta);
    }

    @Override
    public boolean canHarvestBlock(Block par1Block, ItemStack item) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(item);
        int slotNum = getSlotNumFromItemStack(item);
        if (tools != null && tools.getStackInSlot(slotNum) != null) {
            return tools.getStackInSlot(slotNum).getItem().canHarvestBlock(par1Block, item);
        }
        return super.canHarvestBlock(par1Block, item);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, Block block, int x, int y, int z,
                                    EntityLivingBase destroyer) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        int slotNum = getSlotNumFromItemStack(itemStack);
        if (tools != null && tools.getStackInSlot(slotNum) != null && !world.isRemote) {
            boolean ret = tools
                    .getStackInSlot(slotNum)
                    .getItem()
                    .onBlockDestroyed(tools.getStackInSlot(slotNum), world, block, x, y, z,
                            destroyer);
            if (tools.getStackInSlot(slotNum).stackSize <= 0) {
                this.destroyTheItem((EntityPlayer) destroyer, tools.getStackInSlot(slotNum));
            }
            tools.writeToNBT(itemStack.getTagCompound());
            return ret;
        }
        return super.onBlockDestroyed(itemStack, world, block, x, y, z, destroyer);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null && !attacker.worldObj.isRemote) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            boolean ret = slotStack.getItem().hitEntity(slotStack, target, attacker);
            if (slotStack.stackSize <= 0) {
                this.destroyTheItem((EntityPlayer) attacker, slotStack);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.hitEntity(stack, target, attacker);
    }

    private void attackTargetEntityWithTheItem(Entity target, EntityPlayer player, ItemStack stack) {
        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, target))) {
            return;
        }
        if (stack != null && stack.getItem().onLeftClickEntity(stack, player, target)) {
            return;
        }
        if (target.canAttackWithItem()) {
            if (!target.hitByEntity(player)) {
                float var2 = (float) this.getItemStrength(stack);

                if (player.isPotionActive(Potion.damageBoost)) {
                    var2 += 3 << player.getActivePotionEffect(Potion.damageBoost).getAmplifier();
                }

                if (player.isPotionActive(Potion.weakness)) {
                    var2 -= 2 << player.getActivePotionEffect(Potion.weakness).getAmplifier();
                }

                int var3 = 0;
                int var4 = 0;

                if (target instanceof EntityLivingBase) {
                    var4 = this.getEnchantmentModifierLiving(stack, player, (EntityLivingBase) target);
                    var3 += EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
                }

                if (player.isSprinting()) {
                    ++var3;
                }

                if (var2 > 0 || var4 > 0) {
                    boolean var5 = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder()
                            && !player.isInWater() && !player.isPotionActive(Potion.blindness)
                            && player.ridingEntity == null && target instanceof EntityLivingBase;

                    if (var5 && var2 > 0) {
                        var2 *= 1.5F;
                    }

                    var2 += var4;
                    boolean var6 = false;
                    int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);

                    if (target instanceof EntityLivingBase && var7 > 0 && !target.isBurning()) {
                        var6 = true;
                        target.setFire(1);
                    }

                    boolean var8 = target.attackEntityFrom(DamageSource.causePlayerDamage(player), var2);

                    if (var8) {
                        if (var3 > 0) {
                            target.addVelocity(
                                    (double) (-MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F)
                                            * (float) var3 * 0.5F), 0.1D,
                                    (double) (MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F)
                                            * (float) var3 * 0.5F));
                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            player.setSprinting(false);
                        }

                        if (var5) {
                            player.onCriticalHit(target);
                        }

                        if (var4 > 0) {
                            player.onEnchantmentCritical(target);
                        }

                        if (var2 >= 18) {
                            player.triggerAchievement(AchievementList.overkill);
                        }

                        player.setLastAttacker(target);

                        if (target instanceof EntityLivingBase) {
                            EnchantmentHelper.func_151384_a((EntityLivingBase) target, player);
                        }
                        EnchantmentHelper.func_151385_b(player, target);
                    }

                    if (stack != null && target instanceof EntityLivingBase) {
                        stack.hitEntity((EntityLivingBase) target, player);

                        if (stack.stackSize <= 0) {
                            this.destroyTheItem(player, stack);
                        }
                    }

                    if (target instanceof EntityLivingBase) {

                        player.addStat(StatList.damageDealtStat, Math.round(var2 * 10.0F));

                        if (var7 > 0 && var8) {
                            target.setFire(var7 * 4);
                        } else if (var6) {
                            target.extinguish();
                        }
                    }

                    player.addExhaustion(0.3F);
                }
            }
        }
    }

    private double getItemStrength(ItemStack item) {
        @SuppressWarnings("unchecked")
        Multimap<String, AttributeModifier> multimap = item.getAttributeModifiers();
        double d1 = 0;
        if (!multimap.isEmpty()) {
            for (Entry<String, AttributeModifier> entry : multimap.entries()) {
                AttributeModifier attributemodifier = entry.getValue();

                if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                    d1 = attributemodifier.getAmount();
                } else {
                    d1 = attributemodifier.getAmount() * 100.0D;
                }
            }
        }
        return d1;
    }

    private void destroyTheItem(EntityPlayer player, ItemStack orig) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(orig);
        int slotNum = getSlotNumFromItemStack(orig);
        tools.setInventorySlotContents(slotNum, null);
        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, orig));
    }

    private int getEnchantmentModifierLiving(ItemStack stack, EntityLivingBase attacker, EntityLivingBase enemy) {
        int calc = 0;
        if (stack != null) {
            NBTTagList nbttaglist = stack.getEnchantmentTagList();

            if (nbttaglist != null) {
                for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                    short short1 = nbttaglist.getCompoundTagAt(i).getShort("id");
                    short short2 = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                    if (Enchantment.enchantmentsList[short1] != null) {
                        calc += Enchantment.enchantmentsList[short1].func_152376_a(short2, enemy.getCreatureAttribute());
                    }
                }
            }
        }
        return calc > 0 ? 1 + attacker.worldObj.rand.nextInt(calc) : 0;
    }

    private void setEnchantments(ItemStack ToEnchant, ItemStack Enchanted) {
        int id;
        int lv;
        NBTTagList list = Enchanted.getEnchantmentTagList();
        if (list != null) {
            for (int i = 0; i < list.tagCount(); ++i) {
                if (list.getCompoundTagAt(i).getShort("lvl") > 0) {
                    id = list.getCompoundTagAt(i).getShort("id");
                    lv = list.getCompoundTagAt(i).getShort("lvl");
                    MultiToolHolders.addEnchantmentToItem(ToEnchant, Enchantment.enchantmentsList[id], lv);
                }
            }
        }
    }

    public void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey("multitoolholders")) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag("multitoolholders", nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag("multitoolholders");
        nbt.setInteger("slot", slotNum);
    }

    public InventoryToolHolder getInventoryFromItemStack(ItemStack itemStack) {
        return new InventoryToolHolder(itemStack);
    }

    @Override
    public void doKeyAction(@Nonnull ItemStack itemStack, @Nonnull EntityPlayer player, byte key) {
        if (key == OPEN_KEY) {
            if (player.openContainer == null || !(player.openContainer instanceof ContainerToolHolder)) {
                player.openGui(MultiToolHolders.instance, this.type.getGuiId(), player.worldObj, 0, 0, 0);
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

    @Optional.Method(modid = "CoFHCore")
    @Override
    public boolean isUsable(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y, int z) {
        ItemStack nowItem = getInventoryFromItemStack(itemStack).getStackInSlot(getSlotNumFromItemStack(itemStack));
        return CoopTE.isUsable(nowItem, entityLivingBase, x, y, z);
    }

    @Optional.Method(modid = "CoFHCore")
    @Override
    public void toolUsed(ItemStack itemStack, EntityLivingBase entityLivingBase, int x, int y, int z) {
        ItemStack nowItem = getInventoryFromItemStack(itemStack).getStackInSlot(getSlotNumFromItemStack(itemStack));
        CoopTE.toolUsed(nowItem, entityLivingBase, x, y, z);
    }

    @Optional.Method(modid = "BuildCraftAPI|core")
    @Override
    public boolean canWrench(EntityPlayer player, int x, int y, int z) {
        ItemStack nowItem = getInventoryFromItemStack(player.getCurrentEquippedItem()).getStackInSlot(getSlotNumFromItemStack(player.getCurrentEquippedItem()));
        return CoopBC.canWrench(nowItem, player, x, y, z);
    }

    @Optional.Method(modid = "BuildCraftAPI|core")
    @Override
    public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
        ItemStack nowItem = getInventoryFromItemStack(player.getCurrentEquippedItem()).getStackInSlot(getSlotNumFromItemStack(player.getCurrentEquippedItem()));
        CoopBC.wrenchUsed(nowItem, player, x, y, z);
    }
}