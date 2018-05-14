/**
 * Copyright (C) 2018 Laboratory of Experimental Biophysics
 * Ecole Polytechnique Federale de Lausanne (EPFL), Switzerland
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ch.epfl.leb.defcon.utils;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Tensor;

/**
 * Appends various operations to TensorFlow graphs.
 * 
 * @author Baptiste Ottino
 * @see <a href="https://www.tensorflow.org/api_docs/java/reference/org/tensorflow/Output">Output | TensorFlow</a>
 * @see <a href="https://www.tensorflow.org/api_docs/java/reference/org/tensorflow/Graph">Graph | TensorFlow</a> 
 */
public class GraphBuilder {
    
    /**
     * Builds an Output tensor with a single scalar value.
     * 
     * @param <T>
     * @param g The TensorFlow graph to modify.
     * @param name The full name of the operation.
     * @param value The value output by the operation.
     * @param type The output datatype.
     * @return Symbolic handle to the tensor produced by the appended operation.
     
     */
    public static <T> Output<T> constant(Graph g, String name, Object value, Class<T> type) {
        Tensor<T> t = Tensor.create(value, type);
        return g.opBuilder("Const", name)
                .setAttr("dtype", DataType.fromClass(type))
                .setAttr("value", t)
                .build()
                .output(0);
        
    }
    
    /**
     * Builds an Output tensor with a single scalar value.
     * 
     * @param g The TensorFlow graph to modify.
     * @param name The full name of the operation.
     * @param value The value output by the operation.
     * @return Symbolic handle to the tensor produced by the appended operation.
     */
    public static Output<Integer> constant(Graph g, String name, int value) {
        return GraphBuilder.constant(g, name, value, Integer.class);
    }

    /**
     * Adds a dimension to a TensorFlow Output tensor.
     * 
     * The dimension as added at the position "dim." (with numpy/tensorflow syntax: 0 for the
     * first position, -1 in the end)
     *
     * @param <T>
     * @param g The TensorFlow graph to modify.
     * @param name The full name of the operation.
     * @param input The input operation to expand.
     * @param dim
     * @return Symbolic handle to the tensor produced by the appended operation.
     */
    public static <T> Output<T> expandDims(Graph g, String name, Output<T> input, Output<Integer> dim) {
        return g.opBuilder("ExpandDims", name).addInput(input).addInput(dim).build().output(0);
    }
}
