/* Copyright 2012, UCAR/Unidata.
   See the LICENSE file for more information. */

package dap4.core.util;

import java.util.Arrays;

public class Index
{
    static public final Index SCALAR = new Index(0);

    public int rank;
    public long[] indices; // allow direct access
    public long[] dimsizes; // allow direct access

    public Index(int rank)
    {
        this.rank = rank;
        this.dimsizes = new long[rank];
        indices = new long[rank];
        if(this.rank > 0) {
            Arrays.fill(indices, 0);
            Arrays.fill(dimsizes, 0);
        }
    }

    public Index(Index index)
    {
        this(index.getRank());
        if(this.rank > 0) {
            System.arraycopy(index.indices, 0, this.indices, 0, this.rank);
            System.arraycopy(index.dimsizes, 0, this.dimsizes, 0, this.rank);
        }
    }

    public Index(long[] indices, long[] dimsizes)
    {
        this(dimsizes.length);
        if(this.rank > 0) {
            System.arraycopy(indices, 0, this.indices, 0, this.rank);
            System.arraycopy(dimsizes, 0, this.dimsizes, 0, this.rank);
        }
    }

    public String
    toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for(int i = 0; i < this.rank; i++) {
            if(i > 0) buf.append(',');
            buf.append(indices[i]);
            buf.append('/');
            buf.append(dimsizes[i]);
        }
        buf.append("](");
        buf.append(this.index());
        buf.append(")");
        return buf.toString();
    }


    /**
     * Compute the linear index
     * from the current odometer indices.
     */
    public long
    index()
    {
        long offset = 0;
        for(int i = 0; i < this.indices.length; i++) {
            offset *= this.dimsizes[i];
            offset += this.indices[i];
        }
        return offset;
    }

    public int getRank()
    {
        return this.rank;
    }

    public long
    get(int i)
    {
        if(i < 0 || i >= this.rank)
            throw new IllegalArgumentException();
        return this.indices[i];
    }

    /**
     * Given an offset (single index) and a set of dimensions
     * compute the set of dimension indices that correspond
     * to the offset.
     */

    static public Index
    offsetToIndex(long offset, long[] dimsizes)
    {
        // offset = d3*(d2*(d1*(x1))+x2)+x3
        long[] indices = new long[dimsizes.length];
        for(int i = dimsizes.length - 1; i >= 0; i--) {
            indices[i] = offset % dimsizes[i];
            offset = (offset - indices[i]) / dimsizes[i];
        }
        return new Index(indices, dimsizes);
    }

}