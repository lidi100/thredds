/////////////////////////////////////////////////////////////////////////////
// This file is part of the "Java-DAP" project, a Java implementation
// of the OPeNDAP Data Access Protocol.
//
// Copyright (c) 2007 OPeNDAP, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// You can contact OPeNDAP, Inc. at PO Box 112, Saunderstown, RI. 02874-0112.
/////////////////////////////////////////////////////////////////////////////

package opendap.dap;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;

import opendap.dap.Server.InvalidParameterException;

/**
 * This class is used to hold arrays of other OPeNDAP data. The elements of the
 * array can be simple or compound data types. There is no limit on the
 * number of dimensions an array can have, or on the size of each dimension.
 * <p/>
 * If desired, the user can give each dimension of an array a name. You can,
 * for example, have a 360x180 array of temperatures, covering the whole
 * globe with one-degree squares. In this case, you could name the first
 * dimension "Longitude" and the second dimension "Latitude". This can
 * help prevent a great deal of confusion.
 * <p/>
 * The <code>DArray</code> is used as part of the <code>DGrid</code> class,
 * where the dimension names are crucial to its structure. The dimension names
 * correspond to "Map" vectors, holding the actual values for that column of
 * the array.
 * <p/>
 * Each array dimension carries with it its own projection information. The
 * projection inforamtion takes the form of three integers: the start, stop,
 * and stride values. This is clearest with an example. Consider a
 * one-dimensional array 10 elements long. If the start value of the
 * dimension constraint is 3, then the constrained array appears to be seven
 * elements long. If the stop value is changed to 7, then the array appears
 * to be five elements long. If the stride is changed to two, the array will
 * appear to be 3 elements long. Array constraints are written as
 * <code>[start:stride:stop]</code>.
 * <p/>
 * <code><pre>
 * A = [1 2 3 4 5 6 7 8 9 10]
 * A[3::] = [4 5 6 7 8 9 10]
 * A[3::7] = [4 5 6 7 8]
 * A[3:2:7] = [4 6 8]
 * A[0:3:9] = [1 4 7 10]
 * </pre></code>
 * <p/>
 * NB: OPeNDAP uses zero-based indexing.
 *
 * @author jehamby
 * @version $Revision: 19676 $
 * @see DGrid
 * @see DVector
 * @see BaseType
 */
public class DArray extends DVector implements Cloneable {
    /**
     * A Vector of DArrayDimension information (i.e. the shape)
     */
    private Vector<DArrayDimension> dimVector;

    /**
     * Constructs a new <code>DArray</code>.
     */
    public DArray() {
        this(null);
    }

    /**
     * Constructs a new <code>DArray</code> with name <code>n</code>.
     *
     * @param n the name of the variable.
     */
    public DArray(String n) {
        super(n);
        dimVector = new Vector<DArrayDimension>();
    }

    /**
     * Returns a clone of this <code>DArray</code>.  A deep copy is performed
     * on all data inside the variable.
     *
     * @return a clone of this <code>DArray</code>.
     */
    public Object clone() {
        DArray a = (DArray) super.clone();
        a.dimVector = new Vector<DArrayDimension>();
        for (int i = 0; i < dimVector.size(); i++) {
            DArrayDimension d =  dimVector.elementAt(i);
            a.dimVector.addElement((DArrayDimension)d.clone());
        }
        return a;
    }

    /**
     * Returns the OPeNDAP type name of the class instance as a <code>String</code>.
     *
     * @return the OPeNDAP type name of the class instance as a <code>String</code>.
     */
    public String getTypeName() {
        return "Array";
    }

    /**
     * Checks for internal consistency.  For <code>DArray</code>, verify that
     * the dimension vector is not empty.
     *
     * @param all for complex constructor types, this flag indicates whether to
     *            check the semantics of the member variables, too.
     * @throws BadSemanticsException if semantics are bad, explains why.
     * @see BaseType#checkSemantics(boolean)
     */
    public void checkSemantics(boolean all)
            throws BadSemanticsException {
        super.checkSemantics(all);

        if (dimVector.isEmpty())
            throw new BadSemanticsException("An array variable must have dimensions");
    }


    /**
     * Write the variable's declaration in a C-style syntax. This
     * function is used to create textual representation of the Data
     * Descriptor Structure (DDS).  See <em>The OPeNDAP User Manual</em> for
     * information about this structure.
     *
     * @param os         The <code>PrintWriter</code> on which to print the
     *                   declaration.
     * @param space      Each line of the declaration will begin with the
     *                   characters in this string.  Usually used for leading spaces.
     * @param print_semi a boolean value indicating whether to print a
     *                   semicolon at the end of the declaration.
     * @see BaseType#printDecl(PrintWriter, String, boolean)
     */
    public void printDecl(PrintWriter os, String space, boolean print_semi,
                          boolean constrained) {

        // BEWARE! Since printDecl()is (multiplely) overloaded in BaseType and
        // all of the different signatures of printDecl() in BaseType lead to
        // one signature, we must be careful to override that SAME signature
        // here. That way all calls to printDecl() for this object lead to
        // this implementation.

        //os.println("DArray.printDecl()");

        getPrimitiveVector().printDecl(os, space, false, constrained);
        for (Enumeration e = dimVector.elements(); e.hasMoreElements();) {
            DArrayDimension d = (DArrayDimension) e.nextElement();
            os.print("[");
            if (d.getName() != null)
                os.print(d.getName() + " = ");
            os.print(d.getSize() + "]");
        }
        if (print_semi)
            os.println(";");
    }

    /**
     * Prints the value of the variable, with its declaration.  This
     * function is primarily intended for debugging OPeNDAP applications and
     * text-based clients such as geturl.
     *
     * @param pw           the <code>PrintWriter</code> on which to print the value.
     * @param space        this value is passed to the <code>printDecl</code> method,
     *                     and controls the leading spaces of the output.
     * @param print_decl_p a boolean value controlling whether the
     *                     variable declaration is printed as well as the value.
     * @see BaseType#printVal(PrintWriter, String, boolean)
     */
    public void printVal(PrintWriter pw, String space, boolean print_decl_p) {
        // print the declaration if print decl is true.
        // for each dimension,
        //   for each element,
        //     print the array given its shape, number of dimensions.
        // Add the `;'

        if (print_decl_p) {
            printDecl(pw, space, false);
            pw.print(" = ");
        }

        int dims = numDimensions();
        int shape[] = new int[dims];
        int i = 0;
        for (Enumeration e = dimVector.elements(); e.hasMoreElements();) {
            DArrayDimension d = (DArrayDimension) e.nextElement();
            shape[i++] = d.getSize();
        }

        printArray(pw, 0, dims, shape, 0);

        if (print_decl_p)
            pw.println(";");

        pw.flush();
    }

    /**
     * Print an array. This is a private member function.
     *
     * @param os     is the stream used for writing
     * @param index  is the index of VEC to start printing
     * @param dims   is the number of dimensions in the array
     * @param shape  holds the size of the dimensions of the array.
     * @param offset holds the current offset into the dimension array.
     * @return the number of elements written.
     */
    private int printArray(PrintWriter os, int index, int dims, int shape[],
                           int offset) {
        if (dims == 1) {
            os.print("{");
            for (int i = 0; i < shape[offset] - 1; i++) {
                getPrimitiveVector().printSingleVal(os, index++);
                os.print(", ");
            }
            getPrimitiveVector().printSingleVal(os, index++);
            os.print("}");
            return index;
        } else {
            os.print("{");
            for (int i = 0; i < shape[offset] - 1; i++) {
                index = printArray(os, index, dims - 1, shape, offset + 1);
                os.print(",");
            }
            index = printArray(os, index, dims - 1, shape, offset + 1);
            os.print("}");
            return index;
        }
    }

    /**
     * Given a size and a name, this function adds a dimension to the
     * array.  For example, if the <code>DArray</code> is already 10 elements
     * long, calling <code>appendDim</code> with a size of 5 will transform the
     * array into a 10x5 matrix.  Calling it again with a size of 2 will
     * create a 10x5x2 array, and so on.
     *
     * @param size the size of the desired new dimension.
     * @param name the name of the new dimension.
     */
    public void appendDim(int size, String name) {
        DArrayDimension newDim = new DArrayDimension(size, name);
        dimVector.addElement(newDim);
    }

    /**
     * Given a size and a name, this function adds a dimension to the
     * array.  For example, if the <code>DArray</code> is already 10 elements
     * long, calling <code>appendDim</code> with a size of 5 will transform the
     * array into a 10x5 matrix.  Calling it again with a size of 2 will
     * create a 10x5x2 array, and so on.
     *
     * @param size the size of the desired new dimension.
     * @param name the name of the new dimension.
     */
    public void appendDim(int size, String name, boolean decodeName) {
        DArrayDimension newDim = new DArrayDimension(size, name, decodeName);
        dimVector.addElement(newDim);
    }

    /**
     * Add a dimension to the array.  Same as <code>appendDim(size, null)</code>.
     *
     * @param size the size of the desired new dimension.
     * @see DArray#appendDim(int, String)
     */
    public void appendDim(int size) {
        appendDim(size, null);
    }

    /**
     * Returns an <code>Enumeration</code> of <code>DArrayDimension</code>s
     * in this array.
     *
     * @return an <code>Enumeration</code> of <code>DArrayDimension</code>s
     *         in this array.
     */
    public final Enumeration getDimensions() {
        return dimVector.elements();
    }

    /**
     * Returns the number of dimensions in this array.
     *
     * @return the number of dimensions in this array.
     */
    public final int numDimensions() {
        return dimVector.size();
    }


    /**
     * Use this method to "squeeze" out all of the array dimensions whose
     * size is equal to 1.
     * <br>
     * Many queries that contstrain Arrays return an Array that has dimensions
     * whose size has been reduced to 1. In effect that the dimension no longer
     * really exists, except as a notational convention for tracking the
     * hyperslab that the array represents. Since many clients have difficulty
     * handling n-dimensional arrays this method was added to allow the client
     * to easily "squeeze" the "extra" dimensions out of the array.
     *
     *
     */
    public void squeeze(){

        if(dimVector.size()==1)
            return;


        Vector<DArrayDimension> squeezeCandidates = new Vector<DArrayDimension>();

        for (DArrayDimension dim: dimVector){
            if(dim.getSize()==1)
                squeezeCandidates.add(dim);
        }

        if(squeezeCandidates.size()==dimVector.size())
            squeezeCandidates.remove(squeezeCandidates.size()-1);


        //System.out.println("DArray.squeeze(): Removing "+
        //        squeezeCandidates.size()+" dimensions of size 1.");


        dimVector.removeAll(squeezeCandidates);

    }

    /**
     * Returns the <code>DArrayDimension</code> object for
     * the dimension requested. It makes sure that the dimension requested
     * exists.
     */
    public DArrayDimension getDimension(int dimension) throws InvalidParameterException {

        // QC the passed dimension
        if (dimension < dimVector.size())
            return  dimVector.get(dimension);
        else
            throw new InvalidParameterException("DArray.getDimension(): Bad dimension request: dimension > # of dimensions");
    }

    /**
     * Returns the <code>DArrayDimension</code> object for
     * the first dimension.
     */
    public DArrayDimension getFirstDimension() {
        return dimVector.get(0);
    }


    /**
     *
     * @param pw
     * @param pad
     * @param constrained
     * @opendap.ddx.experimental
     */
    public void printXML(PrintWriter pw, String pad, boolean constrained) {

        pw.print(pad + "<Array");
        if (getName() != null) {
            pw.print(" name=\"" +
                    opendap.dap.XMLparser.DDSXMLParser.normalizeToXML(getClearName()) + "\"");
        }
        pw.println(">");


        printXMLcore(pw, pad, constrained);

        pw.println(pad + "</Array>");


    }


    /**
     *
     * @param pw
     * @param pad
     * @param constrained
     * @opendap.ddx.experimental
     */
    public void printAsMapXML(PrintWriter pw, String pad, boolean constrained) {

        pw.print(pad + "<Map");
        if (getName() != null) {
            pw.print(" name=\"" +
                    opendap.dap.XMLparser.DDSXMLParser.normalizeToXML(getClearName()) + "\"");
        }
        pw.println(">");

        printXMLcore(pw, pad, constrained);

        pw.println(pad + "</Map>");

    }


    /**
     *
     * @param pw
     * @param pad
     * @param constrained
     * @opendap.ddx.experimental
     */
    private void printXMLcore(PrintWriter pw, String pad, boolean constrained) {

        Enumeration e = getAttributeNames();
        while (e.hasMoreElements()) {
            String aName = (String) e.nextElement();

            Attribute a = getAttribute(aName);
            if(a!=null)
                a.printXML(pw, pad + "\t", constrained);

        }


        BaseType bt = null;

        PrimitiveVector pv = getPrimitiveVector();
        // *** Nathan, can we get rid of this 'if?' 05/15/03 jhrg
        if (pv instanceof BaseTypePrimitiveVector) {
            bt = pv.getTemplate();
        } else {
            bt = pv.getTemplate();
        }

        String nameCache = bt.getName();

        bt.setName(null);

        bt.printXML(pw, pad + "\t", constrained);

        bt.setName(nameCache);

        Enumeration dae = getDimensions();
        while (dae.hasMoreElements()) {
            DArrayDimension dad = (DArrayDimension) dae.nextElement();
            int size = dad.getSize();
            String name = dad.getName();
            if (name == null) {
                pw.println(pad + "\t" + "<dimension size=\"" + size + "\"/>");
            } else {
                pw.println(pad + "\t" + "<dimension name=\"" +
                        opendap.dap.XMLparser.DDSXMLParser.normalizeToXML(name) +
                        "\" size=\"" + size + "\"/>");
            }
        }
    }
}

