package net.mehvahdjukaar.moyai;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class Moyai {
    public static final String MOD_ID = "moyai";

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static final Supplier<SoundEvent> MOYAI_BOOM_SOUND = RegHelper.registerSound(res("moyai_boom"),
            () -> new SoundEvent(res("record.moyai_boom")));
    public static final Supplier<SoundEvent> MOYAI_ROTATE = RegHelper.registerSound(res("moyai_rotate"),
            () -> new SoundEvent(res("block.moyai_rotate")));
    public static final Supplier<Block> MOYAI = RegHelper.registerBlock(res("moyai"), MoyaiBlock::new);
    public static final Supplier<BlockItem> MOYAI_ITEM = RegHelper.registerItem(res("moyai"), () ->
            new BlockItem(MOYAI.get(), (new Item.Properties()).rarity(Rarity.RARE).tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static final Supplier<GameEvent> MOYAI_BOOM_EVENT = RegHelper.register(res("moyai_boom"),
            () -> new GameEvent("moyai_boom", 16), Registry.GAME_EVENT);

    public static final boolean supplementaries = PlatformHelper.isModLoaded("supplementaries");

    public static void commonInit() {
        ModWorldgen.init();
    }

    public static void commonSetup() {
        Optional<Item> i = Registry.ITEM.getOptional(new ResourceLocation("supplementaries:soap"));
        i.ifPresent(item -> DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                BlockPos pos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                BlockState state = source.getLevel().getBlockState(pos);
                if (state.is(MOYAI.get())) {
                    if (MoyaiBlock.maybeEatSoap(stack, state, pos, source.getLevel(), null)) {
                        return stack;
                    }
                }
                return super.execute(source, stack);
            }
        }));
    }


    public static boolean onNotePlayed(LevelAccessor level, BlockPos pos, BlockState blockState) {
        if (blockState.getValue(NoteBlock.INSTRUMENT) == NoteBlockInstrument.BASEDRUM) {
            BlockState below = level.getBlockState(pos.below());
            if (below.getBlock() instanceof MoyaiBlock && level instanceof ServerLevel serverLevel) {
                level.gameEvent(MOYAI_BOOM_EVENT.get(), pos, new GameEvent.Context(null, blockState));

                int i = blockState.getValue(NoteBlock.NOTE);
                float f = (float) Math.pow(2.0D, (double) (i - 12) / 12.0D);
                level.playSound(null, pos, MOYAI_BOOM_SOUND.get(), SoundSource.RECORDS, 0.5F, f);
                //  serverLevel.sendParticles(ParticleTypes.NOTE, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.2D, (double) pos.getZ() + 0.5D, 1, 0.0D, 0.0D,0,(double) i / 24.0D);
                serverLevel.blockEvent(pos.below(), below.getBlock(), 0, i);
                return true;
            }
        }
        return false;
    }
}