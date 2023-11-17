package slimeknights.tconstruct.smeltery.menu;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import lombok.Getter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;
import slimeknights.mantle.inventory.SmartItemHandlerSlot;
import slimeknights.mantle.util.sync.ValidZeroDataSlot;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.shared.inventory.TriggeringBaseContainerMenu;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.MixerAlloyTank;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class AlloyerContainerMenu extends TriggeringBaseContainerMenu<AlloyerBlockEntity> {
  public static final ResourceLocation TOOLTIP_FORMAT = TConstruct.getResource("alloyer");

  @Getter
  private boolean hasFuelSlot = false;
  public AlloyerContainerMenu(int id, @Nullable Inventory inv, @Nullable AlloyerBlockEntity alloyer) {
    super(TinkerSmeltery.alloyerContainer.get(), id, inv, alloyer);

    // create slots
    if (alloyer != null) {
      // refresh cache of neighboring tanks
      Level world = alloyer.getLevel();
      if (world != null && world.isClientSide) {
        MixerAlloyTank alloyTank = alloyer.getAlloyTank();
        for (Direction direction : Direction.values()) {
          if (direction != Direction.DOWN) {
            alloyTank.refresh(direction, true);
          }
        }
      }

      // add fuel slot if present
      BlockPos down = alloyer.getBlockPos().below();
      if (world != null && world.getBlockState(down).is(TinkerTags.Blocks.FUEL_TANKS)) {
        Optional<Storage<ItemVariant>> storage = Optional.ofNullable(ItemStorage.SIDED.find(world, down, null)).filter(view -> view instanceof SlottedStackStorage);
        if (storage.isPresent()) {
          hasFuelSlot = storage.filter(handler -> {
            this.addSlot(new SmartItemHandlerSlot((SlottedStackStorage) handler, 0, 151, 32));
            return true;
          }).isPresent();
        }
      }

      this.addInventorySlots();

      // syncing
      Consumer<DataSlot> referenceConsumer = this::addDataSlot;
      ValidZeroDataSlot.trackIntArray(referenceConsumer, alloyer.getFuelModule());
    }
  }

  public AlloyerContainerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, AlloyerBlockEntity.class));
  }
}
