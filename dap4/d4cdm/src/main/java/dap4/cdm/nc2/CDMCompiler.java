/* Copyright 2012, UCAR/Unidata.
   See the LICENSE file for more information.
*/


package dap4.cdm.nc2;

import dap4.cdm.NodeMap;
import dap4.core.data.DSP;
import dap4.core.data.DataDataset;
import dap4.core.data.DataSort;
import dap4.core.dmr.DapDataset;
import dap4.core.util.DapException;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import java.util.Map;

/**
 * The goal for the CDM compiler is produce a NetcdfDataset
 * whose content comes from a DSP. In a sense it is the
 * inverse of CDMDSP.
 *
 * Compilation implies two translations/wraps.
 * 1. Create a set of CDMNodes corresponding to the
 * relevant nodes in the DMR.
 * 2. Create a set of CDM ucar.ma2.array objects that wrap the
 * DataDataset object.
 * Note that this compiler has nothing to do with D4DataCompiler.
 */

public class CDMCompiler
{
    static public boolean DEBUG = false;

    //////////////////////////////////////////////////
    // Constants

    //////////////////////////////////////////////////
    // Instance variables

    DapNetcdfFile ncfile = null;
    DSP dsp = null;
    DapDataset dmr = null;
    DataDataset d4root = null;
    Group cdmroot = null;
    NodeMap nodemap = null;
    Map<Variable, Array> arraymap = null;

    //////////////////////////////////////////////////
    //Constructor(s)

    /**
     * Constructor
     *
     * @param ncfile  the target NetcdfDataset (as yet empty)
     * @param dsp     the DSP to be wrapped
     */

    public CDMCompiler(DapNetcdfFile ncfile, DSP dsp)
            throws DapException
    {
        this.ncfile = ncfile;
        this.dsp = dsp;
        this.d4root = (DataDataset) dsp.getDataDataset();
        this.dmr = dsp.getDMR();
    }

    //////////////////////////////////////////////////
    // Accessors

    public NodeMap getNodeMap()
    {
        return this.nodemap;
    }

    public Map<Variable,Array> getArrayMap()
    {
        return this.arraymap;
    }

    public NetcdfFile getNetcdfFile()
    {
        return this.ncfile;
    }

    //////////////////////////////////////////////////
    // Compile DMR and Data into a NetcdfDataset

    /*Package access*/
    void
    compile()
            throws DapException
    {
        assert d4root.getSort() == DataSort.DATASET;
        compileDMR();
        compileData();
    }    

    //////////////////////////////////////////////////
    // Compile DMR->set of CDM nodes

    /**
     * Convert a DMR to equivalent CDM meta-databuffer
     * and populate a NetcdfDataset with it.
     *
     * @throws DapException
     */

    protected void
    compileDMR()
            throws DapException
    {
        // Convert the DMR to CDM metadata
        // and return a mapping from DapNode -> CDMNode
        this.nodemap = new DMRToCDM(this.ncfile, this.dsp).create();
    }

    //////////////////////////////////////////////////
    // Compile Data->set of CDM Array objects

    /**
     * Convert a DataDataset to equivalent CDM data (Array objects).
     * and populate a NetcdfDataset with it.
     *
     * @throws DapException
     */

    protected void
    compileData()
            throws DapException
    {
        // Convert the DMR to CDM metadata
        // and return a mapping from Variable -> Array
        this.arraymap = new DataToCDM(this.ncfile, this.dsp, this.nodemap).create();
    }

}