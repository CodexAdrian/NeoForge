/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.HandlerUtils;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

public class SimpleItemContext implements IItemContext {
    protected final IResourceHandlerModifiable<ItemResource> handler;
    protected final int index;

    public SimpleItemContext(IResourceHandlerModifiable<ItemResource> handler, int index) {
        this.handler = handler;
        this.index = index;
    }

    @Override
    public ItemResource getResource() {
        return handler.getResource(index);
    }

    @Override
    public int getAmount() {
        return handler.getAmount(index);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        int inserted = handler.insert(index, resource, amount, action);
        if (inserted < amount) {
            return insertOverflow(resource, amount - inserted, action);
        }
        return inserted;
    }

    public int insertOverflow(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtils.insertExcludingIndex(handler, index, resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return handler.extract(index, resource, amount, action);
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        int currentAmount = getAmount();
        if (amount >= currentAmount) {
            if (action.isExecuting()) {
                handler.set(index, resource, currentAmount);
            }
            return currentAmount;
        }
        int extracted = extract(getResource(), amount, action);
        if (extracted > 0) {
            return insertOverflow(resource, extracted, action);
        }
        return 0;
    }
}
