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
import net.minecraftforge.common.util.Constants;
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
    public final int inventorySize;
    private final int guiId;

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
        if (!itemStack.getTagCompound().hasKey(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag(NBT_KEY_MTH, nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound) itemStack.getTagCompound().getTag(NBT_KEY_MTH);
        return nbt.getInteger(NBT_KEY_SLOT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        String ToolName;
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStack);
        for (int i = 0; i < inventorySize; i++) {
            if (tools.getStackInSlot(i) != ItemStack.EMPTY) {
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
                itemStack.getTagCompound().removeTag("ench");
            }

            ItemStack nowItem = getActiveItemStack(itemStack);
            if (nowItem != ItemStack.EMPTY) {
                nowItem.getItem().onUpdate(nowItem, world, entity, slot, true);
                this.setEnchantments(itemStack, nowItem);
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
        if (nowItem != ItemStack.EMPTY) {
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
        if (itemStack != ItemStack.EMPTY) {
            this.attackTargetEntityWithTheItem(entity, player, itemStack);
            return true;
        }
        return false;
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        InventoryToolHolder toolHolder = getInventoryFromItemStack(heldItem);
        int activeSlot = getSlotNumFromItemStack(heldItem);
        ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
        if (itemStack != ItemStack.EMPTY) {
            player.setHeldItem(hand, itemStack);
            EnumActionResult ret = itemStack.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
            player.setHeldItem(hand, heldItem);
            toolHolder.writeToNBT(heldItem.getTagCompound());
            return ret;
        }
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        InventoryToolHolder toolHolder = getInventoryFromItemStack(heldItem);
        int activeSlot = getSlotNumFromItemStack(heldItem);
        ItemStack itemStack = toolHolder.getStackInSlot(activeSlot);
        if (itemStack != ItemStack.EMPTY) {
            playerIn.setHeldItem(hand, itemStack);
            EnumActionResult ret = itemStack.getItem().onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            playerIn.setHeldItem(hand, heldItem);
            toolHolder.writeToNBT(heldItem.getTagCompound());
            return ret;
        }
        return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            itemStack.getItem().onPlayerStoppedUsing(itemStack, worldIn, entityLiving, timeLeft);
            if (itemStack.getCount() <= 0) {
                this.destroyTheItem(entityLiving, itemStack, EnumHand.MAIN_HAND);
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            itemStack.getItem().onItemUseFinish(itemStack, worldIn, entityLiving);
        }
        return stack;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        InventoryToolHolder tools = this.getInventoryFromItemStack(itemStackIn);
        int activeSlot = getSlotNumFromItemStack(itemStackIn);
        ItemStack itemStack = tools.getStackInSlot(activeSlot);
        if (itemStack != ItemStack.EMPTY) {
            playerIn.setHeldItem(hand, itemStack);
            ActionResult<ItemStack> actionResult = itemStack.getItem()
                    .onItemRightClick(worldIn, playerIn, hand);
            tools.setInventorySlotContents(
                    activeSlot, actionResult
                            .getResult());
            playerIn.setHeldItem(hand, itemStackIn);
            return new ActionResult<>(actionResult.getType(), itemStackIn);
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        ItemStack itemStack = getActiveItemStack(stack);
        return itemStack != ItemStack.EMPTY && itemStack.getItem()
                .itemInteractionForEntity(itemStack, playerIn,
                        target, hand);
    }

    @Override
    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            return itemStack.getItemUseAction();
        }
        return super.getItemUseAction(stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            return itemStack.getMaxItemUseDuration();
        }
        return super.getMaxItemUseDuration(stack);
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            return itemStack.getItem().getStrVsBlock(itemStack, state);
        }
        return super.getStrVsBlock(stack, state);
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            return itemStack.getItem().canHarvestBlock(state, itemStack);
        }
        return super.canHarvestBlock(state, stack);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        InventoryToolHolder tools = getInventoryFromItemStack(stack);
        int activeSlot = getSlotNumFromItemStack(stack);
        ItemStack nowItem = tools.getStackInSlot(activeSlot);
        if (nowItem != ItemStack.EMPTY && !worldIn.isRemote) {
            boolean ret = nowItem.getItem()
                    .onBlockDestroyed(nowItem, worldIn, state, pos,
                            entityLiving);
            if (nowItem.getCount() <= 0) {
                this.destroyTheItem(entityLiving, nowItem, EnumHand.MAIN_HAND);
            }
            tools.writeToNBT(stack.getTagCompound());
            return ret;
        }
        return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    @Nonnull
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack) {
        ItemStack itemStack = getActiveItemStack(stack);
        if (itemStack != ItemStack.EMPTY) {
            return itemStack.getAttributeModifiers(slot);
        }
        return super.getAttributeModifiers(slot, stack);
    }

    /**
     * 攻撃処理の丸コピ
     *
     * @param entityIn 攻撃対象者
     * @param player   攻撃者
     * @param stack    ツールホルダー内のActiveItemStack
     */
    private void attackTargetEntityWithTheItem(Entity entityIn, EntityPlayer player, ItemStack stack) {
        if (entityIn.canBeAttackedWithItem()) {
            if (!entityIn.hitByEntity(player)) {
                float playerATKValue = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float targetDEFValue;

                if (entityIn instanceof EntityLivingBase) {
                    targetDEFValue = EnchantmentHelper.getModifierForCreature(stack, ((EntityLivingBase) entityIn).getCreatureAttribute());
                } else {
                    targetDEFValue = EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                }

                float playerCooledATKValue = player.getCooledAttackStrength(0.5F);
                playerATKValue = playerATKValue * (0.2F + playerCooledATKValue * playerCooledATKValue * 0.8F);
                targetDEFValue = targetDEFValue * playerCooledATKValue;
                player.resetCooldown();

                if (playerATKValue > 0.0F || targetDEFValue > 0.0F) {
                    boolean isCooledATKOver = playerCooledATKValue > 0.9F;
                    boolean isSprintingCritical = false;
                    boolean isSword = false;
                    int knockBackModifier = EnchantmentHelper.getKnockbackModifier(player);

                    if (player.isSprinting() && isCooledATKOver) {
                        player.getEntityWorld().playSound(null,
                                player.posX, player.posY, player.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                                player.getSoundCategory(), 1.0F, 1.0F);
                        ++knockBackModifier;
                        isSprintingCritical = true;
                    }

                    boolean isJumpingCritical = isCooledATKOver && player.fallDistance > 0.0F
                            && !player.onGround
                            && !player.isOnLadder()
                            && !player.isInWater()
                            && !player.isPotionActive(MobEffects.BLINDNESS)
                            && !player.isRiding()
                            && entityIn instanceof EntityLivingBase
                            && !player.isSprinting();

                    if (isJumpingCritical) {
                        playerATKValue *= 1.5F;
                    }

                    playerATKValue = playerATKValue + targetDEFValue;
                    double walkedModified = (double) (player.distanceWalkedModified - player.prevDistanceWalkedModified);

                    if (isCooledATKOver && !isJumpingCritical && !isSprintingCritical
                            && player.onGround && walkedModified < (double) player.getAIMoveSpeed()) {
                        if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemSword) {
                            isSword = true;
                        }
                    }

                    float targetHealth = 0.0F;
                    boolean targetNotBurn = false;
                    int fireAspectModifier = EnchantmentHelper.getFireAspectModifier(player);

                    if (entityIn instanceof EntityLivingBase) {
                        targetHealth = ((EntityLivingBase) entityIn).getHealth();

                        if (fireAspectModifier > 0 && !entityIn.isBurning()) {
                            targetNotBurn = true;
                            entityIn.setFire(1);
                        }
                    }

                    double motionX = entityIn.motionX;
                    double motionY = entityIn.motionY;
                    double motionZ = entityIn.motionZ;
                    boolean retAttackEntityFrom = entityIn.attackEntityFrom(DamageSource.causePlayerDamage(player), playerATKValue);

                    if (retAttackEntityFrom) {
                        if (knockBackModifier > 0) {
                            if (entityIn instanceof EntityLivingBase) {
                                ((EntityLivingBase) entityIn).knockBack(
                                        player,
                                        (float) knockBackModifier * 0.5F,
                                        (double) MathHelper.sin(player.rotationYaw * 0.017453292F),
                                        (double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                            } else {
                                entityIn.addVelocity(
                                        (double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) knockBackModifier * 0.5F),
                                        0.1D,
                                        (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) knockBackModifier * 0.5F));
                            }

                            player.motionX *= 0.6D;
                            player.motionZ *= 0.6D;
                            player.setSprinting(false);
                        }

                        if (isSword) {
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
                            entityIn.motionX = motionX;
                            entityIn.motionY = motionY;
                            entityIn.motionZ = motionZ;
                        }

                        if (isJumpingCritical) {
                            player.getEntityWorld().playSound(null,
                                    player.posX, player.posY, player.posZ,
                                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(entityIn);
                        }

                        if (!isJumpingCritical && !isSword) {
                            if (isCooledATKOver) {
                                player.getEntityWorld().playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.getEntityWorld().playSound(null,
                                        player.posX, player.posY, player.posZ,
                                        SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (targetDEFValue > 0.0F) {
                            player.onEnchantmentCritical(entityIn);
                        }

                        if (!player.getEntityWorld().isRemote && entityIn instanceof EntityPlayer) {
                            EntityPlayer entityplayer = (EntityPlayer) entityIn;
                            ItemStack targetHandHeldItem = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                            if (stack != ItemStack.EMPTY
                                    && targetHandHeldItem != ItemStack.EMPTY
                                    && stack.getItem() instanceof ItemAxe
                                    && targetHandHeldItem.getItem() == Items.SHIELD) {
                                float efficiency = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(player) * 0.05F;

                                if (isSprintingCritical) {
                                    efficiency += 0.75F;
                                }

                                if (player.getRNG().nextFloat() < efficiency) {
                                    entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                                    player.getEntityWorld().setEntityState(entityplayer, (byte) 30);
                                }
                            }
                        }

                        if (playerATKValue >= 18.0F) {
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

                        if (stack != ItemStack.EMPTY && entity instanceof EntityLivingBase) {
                            stack.hitEntity((EntityLivingBase) entity, player);

                            if (stack.getCount() <= 0) {
                                destroyTheItem(player, stack, EnumHand.MAIN_HAND);
                            }
                        }

                        if (entityIn instanceof EntityLivingBase) {
                            float realDamage = targetHealth - ((EntityLivingBase) entityIn).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(realDamage * 10.0F));

                            if (fireAspectModifier > 0) {
                                entityIn.setFire(fireAspectModifier * 4);
                            }

                            if (player.getEntityWorld() instanceof WorldServer && realDamage > 2.0F) {
                                int k = (int) ((double) realDamage * 0.5D);
                                ((WorldServer) player.getEntityWorld()).spawnParticle(
                                        EnumParticleTypes.DAMAGE_INDICATOR,
                                        entityIn.posX, entityIn.posY + (double) (entityIn.height * 0.5F), entityIn.posZ,
                                        k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.3F);
                    } else {
                        player.getEntityWorld().playSound(null,
                                player.posX, player.posY, player.posZ,
                                SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                                player.getSoundCategory(), 1.0F, 1.0F);

                        if (targetNotBurn) {
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

    /**
     * ツールホルダーのNBTにスロット番号を設定する処理
     *
     * @param itemStack ツールホルダーアイテム
     * @param slotNum   新しいスロット番号
     */
    private void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey(NBT_KEY_MTH, Constants.NBT.TAG_COMPOUND)) {
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
    public void doKeyAction(@Nonnull ItemStack itemStack, EntityPlayer player, byte key) {
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