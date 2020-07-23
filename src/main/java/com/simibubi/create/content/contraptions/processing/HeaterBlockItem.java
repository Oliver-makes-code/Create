package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeaterBlockItem extends BlockItem {
	public HeaterBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		TileEntity te = context.getWorld()
			.getTileEntity(context.getPos());
		if (te instanceof MobSpawnerTileEntity) {
			AbstractSpawner spawner = ((MobSpawnerTileEntity) te).getSpawnerBaseLogic();
			List<WeightedSpawnerEntity> possibleSpawns = ObfuscationReflectionHelper
				.getPrivateValue(AbstractSpawner.class, spawner, "field_98285_e");
			if (possibleSpawns.isEmpty()) {
				possibleSpawns = new ArrayList<>();
				possibleSpawns.add(
					ObfuscationReflectionHelper.getPrivateValue(AbstractSpawner.class, spawner, "field_98282_f"));
			}
			for (WeightedSpawnerEntity e : possibleSpawns) {
				if (new ResourceLocation(e.getNbt().getString("id")).equals(EntityType.BLAZE.getRegistryName())) {
					ItemStack itemWithBlaze = withBlaze(context.getItem());
					context.getItem()
						.shrink(1);
					dropOrPlaceBack(context.getWorld(), context.getPlayer(), itemWithBlaze);
					return ActionResultType.SUCCESS;
				}
			}
		}
		return super.onItemUse(context);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack heldItem, PlayerEntity player, LivingEntity entity, Hand hand) {
		if (entity instanceof BlazeEntity) {
			ItemStack itemWithBlaze = withBlaze(heldItem);
			heldItem.shrink(1);
			dropOrPlaceBack(player.getEntityWorld(), player, itemWithBlaze);
			entity.remove();
			return true;
		}
		return super.itemInteractionForEntity(heldItem, player, entity, hand);
	}

	private static ItemStack withBlaze(ItemStack base) {
		ItemStack newItem = new ItemStack(base.getItem(), 1);
		CompoundNBT tag = new CompoundNBT();
		tag.putBoolean("has_blaze", true);
		newItem.setTag(tag);
		return newItem;
	}

	private static void dropOrPlaceBack(@Nullable World world, @Nullable PlayerEntity player, ItemStack item) {
		if (player == null)
			return;
		if (player instanceof FakePlayer || world == null) {
			player.dropItem(item, false, false);
		} else {
			player.inventory.placeItemBackInInventory(world, item);
		}
	}
}
