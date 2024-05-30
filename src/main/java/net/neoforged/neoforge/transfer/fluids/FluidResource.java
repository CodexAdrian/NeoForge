/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;

/**
 * Immutable combination of a {@link Fluid} and data components.
 * Similar to a {@link FluidStack}, but immutable and without amount information.
 */
public final class FluidResource implements IResource, DataComponentHolder {
    /**
     * Codec for a fluid resource.
     * Same format as {@link FluidStack#fixedAmountCodec}.
     * Does <b>not</b> accept blank resources.
     */
    public static final Codec<FluidResource> CODEC = FluidStack.fixedAmountCodec(1)
            .xmap(FluidResource::of, r -> r.toStack(1));
    /**
     * Codec for a fluid resource. Same format as {@link #CODEC}, and also accepts blank resources.
     */
    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(FluidResource.BLANK), r -> r.isBlank() ? Optional.of(FluidResource.BLANK) : Optional.of(r));
    /**
     * Codec for a fluid resource and an amount. Does <b>not</b> accept empty stacks.
     */
    public static final Codec<ResourceStack<FluidResource>> WITH_AMOUNT_CODEC = FluidStack.CODEC
            .xmap(FluidStack::immutable, FluidStack::of);
    /**
     * Codec for a fluid resource and an amount. Accepts empty stacks.
     */
    public static final Codec<ResourceStack<FluidResource>> OPTIONAL_WITH_AMOUNT_CODEC = FluidStack.OPTIONAL_CODEC
            .xmap(FluidStack::immutable, FluidStack::of);
    /**
     * Stream codec for a fluid resource. Accepts blank resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> OPTIONAL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidResource::getFluidHolder,
            DataComponentPatch.STREAM_CODEC,
            FluidResource::getComponentsPatch,
            FluidResource::of);

    public static final FluidResource BLANK = new FluidResource(FluidStack.EMPTY);
    public static final ResourceStack<FluidResource> EMPTY_STACK = new ResourceStack<>(FluidResource.BLANK, 0);

    public static FluidResource of(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? BLANK : new FluidResource(fluidStack.copyWithAmount(1));
    }

    public static FluidResource of(Fluid fluid) {
        return fluid == Fluids.EMPTY ? BLANK : new FluidResource(new FluidStack(fluid, 1));
    }

    public static FluidResource of(Holder<Fluid> fluid, DataComponentPatch patch) {
        return fluid.value() == Fluids.EMPTY ? BLANK : new FluidResource(new FluidStack(fluid, 1, patch));
    }

    /**
     * We wrap a fluid stack which must never be exposed and/or modified.
     */
    private final FluidStack innerStack;

    private FluidResource(FluidStack innerStack) {
        this.innerStack = innerStack;
    }

    @Override
    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    public FluidResource applyPatch(DataComponentPatch patch) {
        FluidStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return new FluidResource(stack);
    }

    public <D> FluidResource set(DataComponentType<D> type, D data) {
        FluidStack stack = innerStack.copy();
        stack.set(type, data);
        return new FluidResource(stack);
    }

    public FluidResource remove(DataComponentType<?> type) {
        FluidStack stack = innerStack.copy();
        stack.remove(type);
        return new FluidResource(stack);
    }

    public Fluid getFluid() {
        return innerStack.getFluid();
    }

    public Holder<Fluid> getFluidHolder() {
        return innerStack.getFluidHolder();
    }

    public FluidType getFluidType() {
        return innerStack.getFluidType();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, innerStack);
    }

    public FluidStack toStack(int amount) {
        return this.innerStack.copyWithAmount(amount);
    }

    public boolean is(TagKey<Fluid> tag) {
        return innerStack.is(tag);
    }

    public boolean is(Fluid fluid) {
        return innerStack.is(fluid);
    }

    public boolean is(Predicate<Holder<Fluid>> predicate) {
        return innerStack.is(predicate);
    }

    public boolean is(Holder<Fluid> holder) {
        return innerStack.is(holder);
    }

    public boolean is(HolderSet<Fluid> holders) {
        return innerStack.is(holders);
    }

    public boolean is(FluidType fluidType) {
        return innerStack.is(fluidType);
    }

    public static boolean isSameFluid(FluidResource first, FluidResource other) {
        return first.is(other.getFluid());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof FluidResource v && FluidStack.isSameFluidSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }
}