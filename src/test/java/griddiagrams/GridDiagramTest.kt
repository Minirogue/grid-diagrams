package griddiagrams

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class GridDiagramTest{

    @Test
    fun testLoadingOfUnknotFromResource(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("0_1")
        val cols = gridDiagram.savableGrid
        val xCols = cols[0]
        val oCols = cols[1]
        assertEquals(0,xCols[0])
        assertEquals(1,xCols[1])
        assertEquals(0,oCols[1])
        assertEquals(1,oCols[0])
    }

    @Test
    fun testLoadingOfTrefoilFromResource(){
        val gridDiagram = GridDiagram.getGridDiagramFromResource("3_1")
        val cols = gridDiagram.savableGrid
        val xCols = cols[0]
        val oCols = cols[1]
        assertArrayEquals(intArrayOf(1, 0, 4, 3, 2),xCols)
        assertArrayEquals(intArrayOf(4, 3, 2, 1, 0),oCols)
    }

}