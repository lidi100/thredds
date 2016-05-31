/* Copyright 2012-2016, UCAR/Unidata.
   See the LICENSE file for more information.
*/

package dap4.cdm;

import dap4.cdm.CDMDSP;
import dap4.cdm.CDMDataAtomic;
import dap4.cdm.CDMDataDataset;
import dap4.core.data.*;
import dap4.core.dmr.*;
import ucar.ma2.Array;

public class CDMDataFactory implements DapDataFactory
{
    //////////////////////////////////////////////////
    // Constants

    //////////////////////////////////////////////////
    // Constructor(s)

    public CDMDataFactory()
    {
    }

    //////////////////////////////////////////////////
    // DapDataFactory API

    @Override
    public DataDataset
    newDataset(DSP dsp, DapDataset template)
            throws DataException
    {
        return new CDMDataDataset((CDMDSP) dsp, template);
    }

    @Override
    public DataAtomic
    newAtomicVariable(DSP dsp, DapAtomicVariable template, Object source)
            throws DataException
    {
        return new CDMDataAtomic(dsp, template, (Array)source);
    }

    @Override
    public DataSequence
    newSequence(DSP dsp, DapSequence template, DataCompoundArray parent, Object source)
            throws DataException
    {
        return new CDMDataSequence(dsp, template, (CDMDataCompoundArray)parent, (Integer)source);
    }

    @Override
    public DataRecord
    newRecord(DSP dsp, DapSequence template, DataSequence parent, Object source)
            throws DataException
    {
        return new CDMDataRecord((CDMDSP) dsp, template, (CDMDataSequence)parent, (Integer)source);
    }

    @Override
    public DataStructure
    newStructure(DSP dsp, DapStructure dap, DataCompoundArray parent, Object source)
            throws DataException
    {
        return new CDMDataStructure((CDMDSP) dsp, dap, (CDMDataCompoundArray)parent, (Integer)source);
    }

    @Override
    public DataCompoundArray
    newCompoundArray(DSP dsp, DapVariable dapvar)
            throws DataException
    {
        return new CDMDataCompoundArray((CDMDSP) dsp, dapvar);
    }

}
