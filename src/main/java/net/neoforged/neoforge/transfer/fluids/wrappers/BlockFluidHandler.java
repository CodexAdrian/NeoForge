package net.neoforged.neoforge.transfer.fluids.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidConstants;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class BlockFluidHandler implements ISingleResourceHandler<FluidResource> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable protected final Player player;
    protected final Level level;
    protected final BlockPos blockPos;

    public BlockFluidHandler(@Nullable Player player, Level level, BlockPos blockPos) {
        this.player = player;
        this.level = level;
        this.blockPos = blockPos;
    }

    public BlockFluidHandler(Level level, BlockPos blockPos) {
        this(null, level, blockPos);
    }

    @Override
    public FluidResource getResource() {
        FluidState fluidState = level.getFluidState(blockPos);
        return fluidState.getType().getDefaultResource();
    }

    @Override
    public int getAmount() {
        return getResource().isBlank() ? 0 : FluidConstants.BUCKET;
    }

    @Override
    public int getLimit(FluidResource ignored) {
        return FluidConstants.BUCKET;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return true;
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidConstants.BUCKET) return 0;
        BlockState state = level.getBlockState(blockPos);
        if (action.isExecuting()) {
            if (state.getBlock() instanceof LiquidBlockContainer container && container.canPlaceLiquid(null, level, blockPos, state, resource.getFluid())) {
                container.placeLiquid(level, blockPos, state, resource.getFluid().defaultFluidState());
            } else if (state.canBeReplaced(resource.getFluid())) {
                level.destroyBlock(blockPos, true);
                level.setBlock(blockPos, state, Block.UPDATE_ALL_IMMEDIATE);
            } else {
                return 0;
            }
        }
        return FluidConstants.BUCKET;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(blockPos);
        FluidState fluidState = level.getFluidState(blockPos);
        if (amount < FluidConstants.BUCKET || resource.isBlank() || !resource.equals(fluidState.getType().getDefaultResource())) return 0;
        if (!state.getFluidState().isEmpty()) {
            if (!(state.getBlock() instanceof BucketPickup pickupHandler)) return 0;
            if (action.isSimulating()) {
                return FluidConstants.BUCKET;
            }
            ItemStack stack = pickupHandler.pickupBlock(player, level, blockPos, state);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof BucketItem bucket && !resource.equals(bucket.content.getDefaultResource())) {
                    LOGGER.error("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                            BuiltInRegistries.FLUID.getKey(fluidState.getType()), blockPos, level.dimension().location(), BuiltInRegistries.FLUID.getKey(bucket.content));
                    return 0;
                }
                return FluidConstants.BUCKET;
            }
        }
        return 0;
    }
}