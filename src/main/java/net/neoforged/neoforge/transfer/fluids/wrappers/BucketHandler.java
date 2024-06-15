/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids.wrappers;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidConstants;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.Objects;

public class BucketHandler implements ISingleResourceHandler<FluidResource> {
    private final IItemContext context;

    public BucketHandler(IItemContext context) {
        this.context = context;
    }

    @Override
    public FluidResource getResource() {
        Item item = context.getResource().getItem();
        if (item instanceof BucketItem bucket) {
            return bucket.content.getDefaultResource();
        } else if (item instanceof MilkBucketItem && NeoForgeMod.MILK.isBound()) {
            return NeoForgeMod.MILK.get().getDefaultResource();
        }
        return FluidResource.BLANK;
    }

    @Override
    public int getAmount() {
        return FluidConstants.BUCKET;
    }

    @Override
    public int getLimit(FluidResource resource) {
        return FluidConstants.BUCKET;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return !resource.getFilledBucket().isBlank();
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    private ItemResource getFilled(FluidResource resource) {
        return resource.getFilledBucket();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidConstants.BUCKET || resource.isBlank() || !getResource().isBlank()) return 0;

        int exchanged = context.exchange(getFilled(resource), amount / FluidConstants.BUCKET, action);
        return exchanged * FluidConstants.BUCKET;
    }

    private ItemResource getEmpty() {
        return Items.BUCKET.getDefaultResource();
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidConstants.BUCKET || resource.isBlank() || !Objects.equals(resource, getResource())) return 0;

        int exchanged = context.exchange(getEmpty(), amount / FluidConstants.BUCKET, action);
        return exchanged * FluidConstants.BUCKET;
    }
}
