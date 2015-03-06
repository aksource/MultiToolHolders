package ak.MultiToolHolders;

import ak.MultiToolHolders.inventory.ContainerToolHolder;
import ak.MultiToolHolders.inventory.InventoryToolHolder;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

@Optional.InterfaceList(
        {@Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore"),
                @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")}
)
public class ItemMultiToolHolder extends Item implements IKeyEvent/*, IToolHammer, IToolWrench*/ {

	public int inventorySize;
    private int guiId;
    public static final byte OPEN_KEY = 0;
    public static final byte NEXT_KEY = 1;
    public static final byte PREV_KEY = 2;

	public ItemMultiToolHolder(int slot, int guiId) {
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.tabTools);
		this.inventorySize = slot;
        this.guiId = guiId;
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
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		ItemStack itemStack = getActiveItemStack(stack);
		if (itemStack != null) {
			this.attackTargetEntityWithTheItem(entity, player, itemStack);
			return true;
		}
		return false;
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos blockPos, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		ItemStack itemStack = getActiveItemStack(stack);
		if (itemStack != null) {
			boolean ret = itemStack.getItem()
					.onItemUseFirst(itemStack, player, world, blockPos, side, hitX, hitY, hitZ);
			if (itemStack.stackSize <= 0) {
				this.destroyTheItem(player, itemStack);
			}
			return ret;
		}
		return false;
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos blockPos, EnumFacing par7, float par8, float par9, float par10) {
		ItemStack itemStack = getActiveItemStack(par1ItemStack);
		if (itemStack != null && !par3World.isRemote) {
			boolean ret = itemStack.getItem()
					.onItemUse(itemStack, par2EntityPlayer, par3World, blockPos, par7, par8, par9, par10);
			if (itemStack.stackSize <= 0) {
				this.destroyTheItem(par2EntityPlayer, itemStack);
			}
			return ret;
		}
		return false;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4) {
		ItemStack itemStack = getActiveItemStack(par1ItemStack);
		if (itemStack != null) {
			itemStack.getItem().onPlayerStoppedUsing(itemStack, par2World, par3EntityPlayer, par4);
			if (itemStack.stackSize <= 0)
			{
				this.destroyTheItem(par3EntityPlayer, itemStack);
			}
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		ItemStack itemStack = getActiveItemStack(par1ItemStack);
		if (itemStack != null) {
			itemStack.getItem().onItemUseFinish(itemStack, par2World, par3EntityPlayer);
		}
		return par1ItemStack;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        InventoryToolHolder tools = this.getInventoryFromItemStack(par1ItemStack);
        int SlotNum = getSlotNumFromItemStack(par1ItemStack);
		ItemStack itemStack = getActiveItemStack(par1ItemStack);
		if (itemStack != null) {
			tools.setInventorySlotContents(
                    SlotNum,
                    itemStack.getItem()
                            .onItemRightClick(itemStack, par2World, par3EntityPlayer));
		}
		if (this.getItemUseAction(par1ItemStack) != EnumAction.NONE)
			par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
		return par1ItemStack;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer,
			EntityLivingBase par3EntityLivingBase) {
		ItemStack itemStack = getActiveItemStack(par1ItemStack);
        return itemStack != null && itemStack.getItem()
                .itemInteractionForEntity(itemStack, par2EntityPlayer,
                        par3EntityLivingBase);
	}

	@Override
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
	public float getDigSpeed(ItemStack stack, IBlockState state) {
		ItemStack itemStack = getActiveItemStack(stack);
		if (itemStack != null) {
			return itemStack.getItem().getDigSpeed(itemStack, state);
		} else
			return super.getDigSpeed(stack, state);
	}

	@Override
	public boolean canHarvestBlock(Block par1Block, ItemStack item)
	{
        ItemStack itemStack = getActiveItemStack(item);
		if (itemStack != null) {
			return itemStack.getItem().canHarvestBlock(par1Block, itemStack);
		} else
			return super.canHarvestBlock(par1Block, item);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemStack, World world, Block block, BlockPos blockPos,
			EntityLivingBase par7EntityLiving) {
		InventoryToolHolder tools = getInventoryFromItemStack(itemStack);
		ItemStack nowItem = getActiveItemStack(itemStack);
		if (nowItem != null && !world.isRemote) {
			boolean ret =nowItem.getItem()
					.onBlockDestroyed(nowItem, world, block, blockPos,
                            par7EntityLiving);
			if (nowItem.stackSize <= 0) {
				this.destroyTheItem((EntityPlayer) par7EntityLiving, nowItem);
			}
			tools.writeToNBT(itemStack.getTagCompound());
			return ret;
		} else
			return super.onBlockDestroyed(itemStack, world, block, blockPos, par7EntityLiving);
	}

	private void attackTargetEntityWithTheItem(Entity par1Entity, EntityPlayer player, ItemStack stack)
	{
		if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, par1Entity))) {
			return;
		}
		if (stack != null && stack.getItem().onLeftClickEntity(stack, player, par1Entity)) {
			return;
		}
		if (par1Entity.canAttackWithItem()) {
			if (!par1Entity.hitByEntity(player)) {
				float var2 = (float) this.getItemStrength(stack);

				if (player.isPotionActive(Potion.damageBoost)) {
					var2 += 3 << player.getActivePotionEffect(Potion.damageBoost).getAmplifier();
				}

				if (player.isPotionActive(Potion.weakness)) {
					var2 -= 2 << player.getActivePotionEffect(Potion.weakness).getAmplifier();
				}

				int var3 = 0;
				int var4 = 0;

				if (par1Entity instanceof EntityLivingBase) {
					var4 = this.getEnchantmentModifierLiving(stack, player, (EntityLivingBase) par1Entity);
					var3 += EnchantmentHelper.getEnchantmentLevel(Enchantment.field_180313_o/*knockback*/.effectId, stack);
				}

				if (player.isSprinting()) {
					++var3;
				}

				if (var2 > 0 || var4 > 0) {
					boolean var5 = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder()
							&& !player.isInWater() && !player.isPotionActive(Potion.blindness)
							&& player.ridingEntity == null && par1Entity instanceof EntityLivingBase;

					if (var5 && var2 > 0) {
						var2 *= 1.5F;
					}

					var2 += var4;
					boolean var6 = false;
					int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);

					if (par1Entity instanceof EntityLivingBase && var7 > 0 && !par1Entity.isBurning()) {
						var6 = true;
						par1Entity.setFire(1);
					}

					boolean var8 = par1Entity.attackEntityFrom(DamageSource.causePlayerDamage(player), var2);

					if (var8) {
						if (var3 > 0) {
							par1Entity.addVelocity(
									(double) (-MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F)
											* (float) var3 * 0.5F), 0.1D,
									(double) (MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F)
											* (float) var3 * 0.5F));
							player.motionX *= 0.6D;
							player.motionZ *= 0.6D;
							player.setSprinting(false);
						}

						if (var5) {
							player.onCriticalHit(par1Entity);
						}

						if (var4 > 0) {
							player.onEnchantmentCritical(par1Entity);
						}

						if (var2 >= 18) {
							player.triggerAchievement(AchievementList.overkill);
						}

						player.setLastAttacker(par1Entity);

						if (par1Entity instanceof EntityLivingBase) {
							EnchantmentHelper.func_151384_a((EntityLivingBase) par1Entity, player);
						}
					}

					if (stack != null && par1Entity instanceof EntityLivingBase) {
						stack.hitEntity((EntityLivingBase) par1Entity, player);

						if (stack.stackSize <= 0) {
							this.destroyTheItem(player, stack);
						}
					}

					if (par1Entity instanceof EntityLivingBase) {

						player.addStat(StatList.damageDealtStat, Math.round(var2 * 10.0F));

						if (var7 > 0 && var8) {
							par1Entity.setFire(var7 * 4);
						} else if (var6) {
							par1Entity.extinguish();
						}
					}

					player.addExhaustion(0.3F);
				}
			}
		}
	}

	private double getItemStrength(ItemStack item)
	{
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

	private void destroyTheItem(EntityPlayer player, ItemStack orig)
	{
        InventoryToolHolder tools = this.getInventoryFromItemStack(orig);
        int SlotNum = getSlotNumFromItemStack(orig);
		tools.setInventorySlotContents(SlotNum, null);
		MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, orig));
	}

	private int getEnchantmentModifierLiving(ItemStack stack, EntityLivingBase attacker, EntityLivingBase enemy)
	{
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

	private void setEnchantments(ItemStack ToEnchant, ItemStack Enchanted)
	{
		int EnchNum;
		int EnchLv;
		NBTTagList list = Enchanted.getEnchantmentTagList();
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				if (list.getCompoundTagAt(i).getShort("lvl") > 0) {
					EnchNum = list.getCompoundTagAt(i).getShort("id");
					EnchLv = list.getCompoundTagAt(i).getShort("lvl");
                    MultiToolHolders.addEnchantmentToItem(ToEnchant, Enchantment.enchantmentsList[EnchNum], EnchLv);
				}
			}
		}
	}

    public static int getSlotNumFromItemStack(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey("multitoolholders")) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag("multitoolholders", nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound)itemStack.getTagCompound().getTag("multitoolholders");
        return nbt.getInteger("slot");
    }

    public void setSlotNumToItemStack(ItemStack itemStack, int slotNum) {
        if (!itemStack.hasTagCompound()) itemStack.setTagCompound(new NBTTagCompound());
        if (!itemStack.getTagCompound().hasKey("multitoolholders")) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            itemStack.getTagCompound().setTag("multitoolholders", nbtTagCompound);
        }
        NBTTagCompound nbt = (NBTTagCompound)itemStack.getTagCompound().getTag("multitoolholders");
        nbt.setInteger("slot", slotNum);
    }

    public InventoryToolHolder getInventoryFromItemStack(ItemStack itemStack) {
        return  new InventoryToolHolder(itemStack);
    }

	public ItemStack getActiveItemStack(ItemStack itemStack) {
		int slot = getSlotNumFromItemStack(itemStack);
		return getInventoryFromItemStack(itemStack).getStackInSlot(slot);
	}
    @Override
    public void doKeyAction(ItemStack itemStack, EntityPlayer player, byte key) {
        if (key == OPEN_KEY) {
            if (player.openContainer == null || !(player.openContainer instanceof ContainerToolHolder)) {
                player.openGui(MultiToolHolders.instance, this.guiId, player.worldObj, 0, 0, 0);
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
        ItemStack nowItem = getInventoryFromItemStack(player.getCurrentEquippedItem()).getStackInSlot(getSlotNumFromItemStack(player.getCurrentEquippedItem()));
        return CoopBC.canWrench(nowItem, player, x, y, z);
    }

    @Optional.Method(modid = "BuildCraftAPI|core")
//    @Override
    public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
        ItemStack nowItem = getInventoryFromItemStack(player.getCurrentEquippedItem()).getStackInSlot(getSlotNumFromItemStack(player.getCurrentEquippedItem()));
        CoopBC.wrenchUsed(nowItem, player, x, y, z);
    }
}