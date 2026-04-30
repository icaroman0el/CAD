package cad.regulusxv.cad.block.entity;

import cad.regulusxv.cad.CAD;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CadCalibrationTableBlockEntity extends BlockEntity {
    public CadCalibrationTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(CAD.CAD_CALIBRATION_TABLE_BLOCK_ENTITY.get(), pos, blockState);
    }
}
