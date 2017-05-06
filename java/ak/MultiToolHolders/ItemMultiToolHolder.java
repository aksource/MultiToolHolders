package ak.MultiToolHolders;

import ak.MultiToolHolders.inventory.ContainerToolHolder;
import ak.MultiToolHolders.inventory.InventoryToolHolder;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static ak.MultiToolHolders.Constants.*;

@Optional.InterfaceList(
        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
)
public class ItemMultiToolHolder extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

    public static final byte OPEN_KEY = 0;
    public static final byte NEXT_KEY = 1;
    public static final byte PREV_KEY = 2;
    private final EnumHolderType type;

    public ItemMultiToolHolder(EnumHolderType type) {
        super();
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.type = type;
    }

    public static int getSlotNumFromItemStack(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey(NBT_KEY_MTH)) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag(NBT_KEY_MTH, nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag(NBT_KEY_MTH);
        return nbt.getInteger(NBT_KEY_SLOT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nonnull EntityPlayer playerIn,
                               @Nonnull List<String> tooltip, boolean advanced) {
        String ToolName;
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        for (int i = 0; i < type.getSize(); i++) {
            if (tools != null && tools.getStackInSlot(i) != null) {
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
    public void onUpdate(@Nonnull ItemStack itemStack, @Nonnull World world, @Nonnull Entity entity, int slot, boolean isHeld) {
        if (entity instanceof EntityPlayer && isHeld && !world.isRemote) {

            if (itemStack.hasTagCompound()) {
                itemStack.getTagCompound().removeTag(NBT_KEY_ENCHANT);
            }

            ItemStack nowItem = getActiveItemStack(itemStack);
            if (nowItem != null) {
                nowItem.getItem().onUpdate(nowItem, world, entity, slot, true);
                this.setEnchantments(itemStack, nowItem);
            }
        }
    }

    @Override
    public void onCreated(@Nonnull ItemStack itemStack, @Nonnull World worldIn, @Nonnull EntityPlayer playerIn) {
        if (itemStack.getItem() instanceof ItemMultiToolHolder) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
    }

    @Override
    public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos blockPos, @Nonnull EntityPlayer player) {
        for (String toolClass : this.getToolClasses(stack)) {
            this.setHarvestLevel(toolClass, -1);
        }
        ItemStack nowItem = getActiveItemStack(stack);
        if (nowItem != null) {
            Set<String> toolClasses = nowItem.getItem().getToolClasses(nowItem);
            int harvestLevel;
            for (String toolClass : toolClasses) {
                harvestLevel = nowItem.getItem().getHarvestLevel(nowItem, toolClass);
                this.setHarvestLevel(toolClass, harvestLevel);
            }
            return nowItem.getItem().onBlockStartBreak(stack, blockPos, player);
        }
        return super.onBlockStartBreak(stack, blockPos, player);
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        InventoryToolHolder toolHolder = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
        if (itemStack != null) {
            this.attackTargetEntityWithTheItem(entity, player, itemStack);
            toolHolder.writeToNBT(stack.getTagCompound());
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull World world,
                                           @Nonnull BlockPos pos, @Nonnull EnumFacing side,
                                           float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        InventoryToolHolder toolHolder = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
        if (itemStack != null) {
            EnumActionResult ret = itemStack.getItem().onItemUseFirst(itemStack, player, world, pos, side, hitX, hitY, hitZ, hand);
            toolHolder.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn, @Nonnull World worldIn,
                                      @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        InventoryToolHolder toolHolder = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
        if (itemStack != null) {
            EnumActionResult ret = itemStack.getItem().onItemUse(itemStack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            toolHolder.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World worldIn,
                                     @Nonnull EntityLivingBase entityLiving, int timeLeft) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != null) {
            nowItem.getItem().onPlayerStoppedUsing(nowItem, worldIn, entityLiving, timeLeft);
            if (nowItem.stackSize <= 0) {
                this.destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
            }
            tools.writeToNBT(stack.getTagCompound());
        }
    }

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull EntityLivingBase entityLiving) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != null) {
            nowItem.getItem().onItemUseFinish(nowItem, worldIn, entityLiving);
            tools.writeToNBT(stack.getTagCompound());
        }
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, @Nonnull World worldIn,
                                                    @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand) {
        InventoryToolHolder tools = getInventoryFromItemStack(itemStackIn);
        int activeSlot = getSlotNumFromItemStack(itemStackIn);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != null) {
            ActionResult<ItemStack> actionResult = nowItem.getItem()
                    .onItemRightClick(nowItem, worldIn, playerIn, hand);
            tools.setInventorySlotContents(
                    activeSlot, actionResult
                            .getResult());
            tools.writeToNBT(itemStackIn.getTagCompound());
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn,
                                            @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != null) {
            boolean ret = nowItem.getItem()
                    .itemInteractionForEntity(nowItem, playerIn,
                            target, hand);
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getItemUseAction();
        } else
            return super.getItemUseAction(stack);
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getMaxItemUseDuration();
        } else
            return super.getMaxItemUseDuration(stack);
    }

    @Override
    public float getStrVsBlock(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getItem().getStrVsBlock(itemStack, state);
        } else
            return super.getStrVsBlock(stack, state);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, @Nonnull ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getItem().canHarvestBlock(state, itemStack);
        } else
            return super.canHarvestBlock(state, stack);
    }

    @Override
    public boolean onBlockDestroyed(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull IBlockState state,
                                    @Nonnull BlockPos pos, @Nonnull EntityLivingBase entityLiving) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != null && !worldIn.isRemote) {
            boolean ret = nowItem.getItem()
                    .onBlockDestroyed(nowItem, worldIn, state, pos,
                            entityLiving);
            if (nowItem.stackSize <= 0) {
                this.destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        } else
            return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    @Nonnull
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot,
                                                                     @Nonnull ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getAttributeModifiers(slot);
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean hitEntity(@Nonnull ItemStack stack, @Nonnull EntityLivingBase target, @Nonnull EntityLivingBase attacker) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(stack);
        int slotNum = getSlotNumFromItemStack(stack);
        if (tools != null && tools.getStackInSlot(slotNum) != null && !attacker.worldObj.isRemote) {
            ItemStack slotStack = tools.getStackInSlot(slotNum);
            boolean ret = slotStack.getItem().hitEntity(slotStack, target, attacker);
            if (slotStack.stackSize <= 0) {
                this.destroyTheItem(attacker, slotStack, EnumHand.MAIN_HAND);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.hitEntity(stack, target, attacker);
    }

    private void attackTargetEntityWithTheItem(Entity entityIn, EntityPlayer player, ItemStack stack) {
        if (entityIn.canBeAttackedWithItem()) {
            if (!entityIn.hitByEntity(player)) {
                float f = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float f1;

                if (entityIn instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.getModifierForCreature(stack, ((EntityLivingBase) entityIn).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                }

                float f2 = player.getCooledAttackStrength(0.5F);
                f = f * (0.2F + f2 * f2 * 0.8F);
                f1 = f1 * f2;
                player.resetCooldown();

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = f2 > 0.9F;
                    boolean flag1 = false;
                    boolean flag2 = false;
                    boolean flag3 = false;
                    int i = 0;
                    i = i + EnchantmentHelper.getKnockbackModifier(player);

                    if (player.isSprinting() && flag) {
                        player.worldObj.playSound(null,
                                player.posX, player.posY, player.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                        ++i;
                        flag1 = true;
                    }

                    flag2 = flag && player.fallDistance > 0.0F
                            && !player.onGround
                            && !player.isOnLadder()
                            && !player.isInWater()
                            && !player.isPotionActive(MobEffects.BLINDNESS)
                            && !player.isRiding()
                            && entityIn instanceof EntityLivingBase;
                    flag2 = flag2 && !player.isSprinting();

                    if (flag2) {
                        f *= 1.5F;
                    }

                    f = f + f1;
                    double d0 = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);

                    if (flag && !flag2 && !flag1 && player.onGround && d0 < (double) player.getAIMoveSpeed()) {
                        if (stack != null && stack.getItem() instanceof ItemSword) {
                            flag3 = true;
                        }
                    }

                    float f4 = 0.0F;
                    boolean flag4 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);

                    if (entityIn instanceof EntityLivingBase) {
                        f4 = ((EntityLivingBase) entityIn).getHealth();

                        if (j > 0 && !entityIn.isBurning()) {
                            flag4 = true;
                            entityIn.setFire(1);
                        }
                    }

                    double d1 = entityIn.motionX;
                    double d2 = entityIn.motionY;
                    double d3 = entityIn.motionZ;
                    boolean flag5 = entityIn.attackEntityFrom(DamageSource.causePlayerDamage(player), f);

                    if (flag5) {
                        if (i > 0) {
                            if (entityIn instanceof EntityLivingBase) {
                                ((EntityLivingBase) entityIn).knockBack(
                                        player,
                                        (float) i * 0.5F,
                                        (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
                                        (double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                            } else {
                                entityIn.addVelocity((double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) i * 0.5F), 0.1D, (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) i * 0.5F));
                            }

                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            player.setSprinting(false);
                        }

                        if (flag3) {
                            for (EntityLivingBase entitylivingbase : player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, entityIn.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))) {
                                if (entitylivingbase != player
                                        && entitylivingbase != entityIn
                                        && !player.isOnSameTeam(entitylivingbase)
                                        && player.getDistanceSqToEntity(entitylivingbase) < 9.0D) {
                                    entitylivingbase.knockBack(
                                            player,
                                            0.4F,
                                            (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
                                            (double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                                    entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(player), 1.0F);
                                }
                            }

                            player.worldObj.playSound(null,
                                    player.posX, player.posY, player.posZ,
                                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                            player.spawnSweepParticles();
                        }

                        if (entityIn instanceof EntityPlayerMP && entityIn.velocityChanged) {
                            ((EntityPlayerMP) entityIn).connection.sendPacket(new SPacketEntityVelocity(entityIn));
                            entityIn.velocityChanged = false;
                            entityIn.motionX = d1;
                            entityIn.motionY = d2;
                            entityIn.motionZ = d3;
                        }

                        if (flag2) {
                            player.worldObj.playSound(null,
                                    player.posX, player.posY, player.posZ,
                                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(entityIn);
                        }

                        if (!flag2 && !flag3) {
                            if (flag) {
                                player.worldObj.playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.worldObj.playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            player.onEnchantmentCritical(entityIn);
                        }

                        if (!player.worldObj.isRemote && entityIn instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entityIn;
                            ItemStack itemstack3 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

                            if (stack != null
                                    && itemstack3 != null
                                    && stack.getItem() instanceof ItemAxe
                                    && itemstack3.getItem() == Items.SHIELD) {
                                float f3 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(player) * 0.05F;

                                if (flag1) {
                                    f3 += 0.75F;
                                }

                                if (player.getRNG().nextFloat() < f3) {
                                    entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                                    player.worldObj.setEntityState(entityplayer, (byte) 30);
                                }
                            }
                        }

                        if (f >= 18.0F) {
                            player.addStat(AchievementList.OVERKILL);
                        }

                        player.setLastAttacker(entityIn);

                        if (entityIn instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) entityIn, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, entityIn);
                        Entity entity = entityIn;

                        if (entityIn instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) entityIn).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (stack != null && entity instanceof EntityLivingBase) {
                            stack.hitEntity((EntityLivingBase) entity, player);

                            if (stack.stackSize <= 0) {
//                                player.setHeldItem(EnumHand.MAIN_HAND, null);
                                destroyTheItem(player, stack, EnumHand.MAIN_HAND);
                            }
                        }

                        if (entityIn instanceof EntityLivingBase) {
                            float f5 = f4 - ((EntityLivingBase) entityIn).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (j > 0) {
                                entityIn.setFire(j * 4);
                            }

                            if (player.worldObj instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) player.worldObj).spawnParticle(
                                        EnumParticleTypes.DAMAGE_INDICATOR,
                                        entityIn.posX, entityIn.posY + (double) (entityIn.height * 0.5F), entityIn.posZ,
                                        k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        player.addExhaustion(0.3F);
                    } else {
                        player.worldObj.playSound(null,
                                player.posX, player.posY, player.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

                        if (flag4) {
                            entityIn.extinguish();
                        }
                    }
                }
            }
        }
    }

    /**
     * 破壊処理メソッド
     *
     * @param entityLivingBase 破壊者
     * @param orig             ツールホルダー
     * @param hand             持ち手
     */
    private void destroyTheItem(EntityLivingBase entityLivingBase, ItemStack orig, EnumHand hand) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(orig);
        int SlotNum = getSlotNumFromItemStack(orig);
        tools.setInventorySlotContents(SlotNum, null);
        if (entityLivingBase instanceof EntityPlayer)
            MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent((EntityPlayer) entityLivingBase, orig, hand));
    }

    private void setEnchantments(ItemStack itemToEnchant, ItemStack itemEnchanted) {
        int id;
        int lv;
        @Nullable
        NBTTagList list = itemEnchanted.getEnchantmentTagList();
        if (list != null) {
            for (int i = 0; i < list.tagCount(); ++i) {
                if (list.getCompoundTagAt(i).getShort("lvl") > 0) {
                    id = list.getCompoundTagAt(i).getShort("id");
                    lv = list.getCompoundTagAt(i).getShort("lvl");
                    MultiToolHolders.addEnchantmentToItem(itemToEnchant, Enchantment.getEnchantmentByID(id), lv);
                }
            }
        }
    }

    public void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey(NBT_KEY_MTH)) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag(NBT_KEY_MTH, nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag(NBT_KEY_MTH);
        nbt.setInteger(NBT_KEY_SLOT, slotNum);
    }

    public InventoryToolHolder getInventoryFromItemStack(ItemStack itemStack) {
        return new InventoryToolHolder(itemStack);
    }

    public ItemStack getActiveItemStack(ItemStack itemStack) {
        int slot = getSlotNumFromItemStack(itemStack);
        return getInventoryFromItemStack(itemStack).getStackInSlot(slot);
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