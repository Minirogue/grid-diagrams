package griddiagrams.markovchain

import griddiagrams.GridDiagram
import org.junit.Test
import java.lang.IllegalArgumentException
import java.lang.Integer.max

class GridMoveTest{

    @Test(expected = IllegalArgumentException::class)
    fun invalidMoveTypeThrowsException(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, max(max(max(GridDiagram.MOVETYPE_COMMUTATION, GridDiagram.MOVETYPE_DESTABILIZATION),
                GridDiagram.MOVETYPE_NONE), GridDiagram.MOVETYPE_STABILIZATION)+1, 1, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCommutationIndexThrowsExceptionNegative(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_COMMUTATION, -1, 0)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidDestabilizationIndexThrowsExceptionNegative(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_DESTABILIZATION, -1, 0)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidStabilizationIndexThrowsExceptionNegative(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_STABILIZATION, -1, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCommutationIndexThrowsExceptionLarge(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_COMMUTATION, 4*gridDiagram.size, 0)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidDestabilizationIndexThrowsExceptionLarge(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_DESTABILIZATION, 4*gridDiagram.size, 0)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidStabilizationIndexThrowsExceptionLarge(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_STABILIZATION, 4*gridDiagram.size, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStabilizationGridLineThrowsExceptionNegative(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_STABILIZATION, 0, -1)
    }
    @Test(expected = IllegalArgumentException::class)
    fun invalidStabilizationGridLineThrowsExceptionLarge(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        GridMove(gridDiagram, GridDiagram.MOVETYPE_STABILIZATION, 0, gridDiagram.size+1)
    }




}