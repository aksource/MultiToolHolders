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

import static ak.MultiToolHolders.MultiToolHolders.NBT_KEY_MTH;
import static ak.MultiToolHolders.MultiToolHolders.NBT_KEY_SLOT;

@Optional.InterfaceList(
        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
)
public class ItemMultiToolHolder extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

    public static final byte OPEN_KEY = 0;
    public static final byte NEXT_KEY = 1;
    public static final byte PREV_KEY = 2;
    public int inventorySize;
    private int guiId;

    public ItemMultiToolHolder(int slot, int guiId) {
        super();
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.TOOLS);
        this.inventorySize = slot;
        this.guiId = guiId;
        this.setRegistryName("itemmultitoolholder" + slot);
        this.setUnlocalizedName(MultiToolHolders.TextureDomain + "Holder" + slot);
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
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        String ToolName;
        InventoryToolHolder tools = this.getInventoryFromItemStack(par1ItemStack);
        for (int i = 0; i < inventorySize; i++) {
            if (tools != null && tools.getStackInSlot(i) != null) {
                ToolName = tools.getStackInSlot(i).getDisplayName();
                par3List.add(ToolName);
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
                itemStack.getTagCompound().removeTag("ench");
            }

            ItemStack nowItem = getActiveItemStack(itemStack);
            if (nowItem != null) {
                nowItem.getItem().onUpdate(nowItem, world, entity, slot, true);
                this.setEnchantments(itemStack, nowItem);
            }
        }
    }

    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (par1ItemStack.getItem() instanceof ItemMultiToolHolder) {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos blockPos, EntityPlayer player) {
        for (String toolClass : this.getToolClasses(stack)) {
            this.setHarvestLevel(toolClass, -1);
        }
        ItemStack nowItem = getActiveItemStack(stack);
        if (nowItem != null) {
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
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            this.attackTargetEntityWithTheItem(entity, player, itemStack);
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack itemStack = getActiveItemStack(player.getHeldItem(hand));
        if (itemStack != null) {
            return itemStack.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = getActiveItemStack(playerIn.getHeldItem(hand));
        if (itemStack != null && !worldIn.isRemote) {
            return itemStack.getItem().onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            itemStack.getItem().onPlayerStoppedUsing(itemStack, worldIn, entityLiving, timeLeft);
            if (itemStack.getCount() <= 0) {
                this.destroyTheItem(entityLiving, itemStack, EnumHand.MAIN_HAND);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            itemStack.getItem().onItemUseFinish(itemStack, worldIn, entityLiving);
        }
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStackIn);
        int SlotNum = getSlotNumFromItemStack(itemStackIn);
        ItemStack itemStack = getActiveItemStack(itemStackIn);
        if (itemStack != null) {
            ActionResult<ItemStack> actionResult = itemStack.getItem()
                    .onItemRightClick(worldIn, playerIn, hand);
            tools.setInventorySlotContents(
                    SlotNum, actionResult
                            .getResult());
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        ItemStack itemStack = getActiveItemStack(stack);
        return itemStack != null && itemStack.getItem()
                .itemInteractionForEntity(itemStack, playerIn,
                        target, hand);
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        ItemStack itemStack = getActiveItemStack(par1ItemStack);
        if (itemStack != null) {
            return itemStack.getItemUseAction();
        } else
            return super.getItemUseAction(par1ItemStack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        ItemStack itemStack = getActiveItemStack(par1ItemStack);
        if (itemStack != null) {
            return itemStack.getMaxItemUseDuration();
        } else
            return super.getMaxItemUseDuration(par1ItemStack);
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getItem().getStrVsBlock(itemStack, state);
        } else
            return super.getStrVsBlock(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getItem().canHarvestBlock(state, itemStack);
        } else
            return super.canHarvestBlock(state, stack);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        ItemStack nowItem = getActiveItemStack(stack);
        if (nowItem != null && !worldIn.isRemote) {
            boolean ret = nowItem.getItem()
                    .onBlockDestroyed(nowItem, worldIn, state, pos,
                            entityLiving);
            if (nowItem.getCount() <= 0) {
                this.destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        } else
            return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    @Nonnull
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != null) {
            return itemStack.getAttributeModifiers(slot);
        }
        return super.getAttributeModifiers(slot, stack);
    }

    private void attackTargetEntityWithTheItem(Entity entityIn, EntityPlayer player, ItemStack stack) {
//        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(player, entityIn)) return;
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
                        player.getEntityWorld().playSound(null,
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
                            for (EntityLivingBase entitylivingbase : player.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, entityIn.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D))) {
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

                            player.getEntityWorld().playSound(null,
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
                            player.getEntityWorld().playSound(null,
                                    player.posX, player.posY, player.posZ,
                                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(entityIn);
                        }

                        if (!flag2 && !flag3) {
                            if (flag) {
                                player.getEntityWorld().playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.getEntityWorld().playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F) {
                            player.onEnchantmentCritical(entityIn);
                        }

                        if (!player.getEntityWorld().isRemote && entityIn instanceof EntityPlayer) {
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
                                    player.getEntityWorld().setEntityState(entityplayer, (byte) 30);
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

                            if (stack.getCount() <= 0) {
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

                            if (player.getEntityWorld() instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) player.getEntityWorld()).spawnParticle(
                                        EnumParticleTypes.DAMAGE_INDICATOR,
                                        entityIn.posX, entityIn.posY + (double) (entityIn.height * 0.5F), entityIn.posZ,
                                        k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        player.addExhaustion(0.3F);
                    } else {
                        player.getEntityWorld().playSound(null,
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
    public void doKeyAction(ItemStack itemStack, EntityPlayer player, byte key) {
        if (key == OPEN_KEY) {
            if (player.openContainer == null || !(player.openContainer instanceof ContainerToolHolder)) {
                player.openGui(MultiToolHolders.instance, this.guiId, player.getEntityWorld(), 0, 0, 0);
            }
        } else if (key == NEXT_KEY) {
            int slot = getSlotNumFromItemStack(itemStack);
            this.setSlotNumToItemStack(itemStack, (slot + 1) % this.inventorySize);
        } else if (key == PREV_KEY) {
            int slot = getSlotNumFromItemStack(itemStack);
            this.setSlotNumToItemStack(itemStack, (this.inventorySize + slot - 1) % this.inventorySize);
        }
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