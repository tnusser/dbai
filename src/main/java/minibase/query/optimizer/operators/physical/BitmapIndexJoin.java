/*
 * @(#)BitmapIndexJoin.java   1.0   Jun 27, 2014
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.optimizer.operators.physical;

import minibase.catalog.DataOrder;
import minibase.query.optimizer.*;
import minibase.query.optimizer.operators.OperatorType;
import minibase.query.optimizer.util.StrongReference;
import minibase.query.schema.ColumnReference;
import minibase.query.schema.TableReference;

import java.util.List;

/**
 * Physical operator that implements the {@link minibase.query.optimizer.operators.logical.EquiJoin EquiJoin}
 * logical operator using the bit index join algorithm.
 * <p>
 * Minibase's query optimizer is based on the Cascades framework for query optimization and, additionally,
 * implements some of the improvements proposed by the Columbia database query optimizer.
 * <ul>
 * <li>Goetz Graefe: <strong>The Cascades Framework for Query Optimization</strong>. In
 * <em>IEEE Data(base) Engineering Bulletin</em>, 18(3), pp. 19-29, 1995.</li>
 * <li>Yongwen Xu: <strong>Efficiency in Columbia Database Query Optimizer</strong>,
 * <em>MSc Thesis, Portland State University</em>, 1998.</li>
 * </ul>
 * The Minibase query optimizer therefore descends from the EXODUS, Volcano, Cascades, and Columbia line of
 * query optimizers, which all use a rule-based, top-down approach to explore the space of possible query
 * execution plans, rather than a bottom-up approach based on dynamic programming.
 * </p>
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class BitmapIndexJoin extends AbstractPhysicalJoin {

    /**
     * Reference to table that is indexed.
     */
    private final TableReference table;

    /**
     * Creates a new {@code BitIndexJoin} physical operator. The join predicate is given in the form of two
     * sets of the same size. Each set contains a number of references to columns in the system catalog.
     * Columns are assumed to be pair-wise equal, i.e., the join predicate is
     * {@code leftColumns[1] = rightColumns[1] AND} ... {@code AND leftColumns[n] = rightColumns[n]}. If both
     * sets of column reference IDs are empty, a natural join or a cross-product is performed.
     *
     * @param leftColumns  left columns
     * @param rightColumns right columns
     * @param table        reference to indexed table
     */
    public BitmapIndexJoin(final List<ColumnReference> leftColumns, final List<ColumnReference> rightColumns,
                           final TableReference table) {
        super(OperatorType.BITMAPIDXJOIN, leftColumns, rightColumns);
        this.table = table;
    }

    /**
     * Constructs a new {@code BitIndexJoin} physical operator. This variant of the constructor is used by the
     * rule engine to create a template of a this operator. Therefore, all its fields are initialized to
     * {@code null}.
     */
    public BitmapIndexJoin() {
        super(OperatorType.BITMAPIDXJOIN);
        this.table = null;
    }

    /**
     * Returns the reference to the table that is indexed.
     *
     * @return reference to indexed table
     */
    public TableReference getTableReference() {
        return this.table;
    }

    @Override
    public PhysicalProperties getPhysicalProperties(final PhysicalProperties... inputProperties) {
        // The physical properties of the bit index join are the physical properties of the left input.
        return inputProperties[0];
    }

    @Override
    public Cost getLocalCost(final LogicalProperties localProperties,
                             final LogicalProperties... inputProperties) {
        final double leftCardinality = ((LogicalCollectionProperties) inputProperties[0]).getCardinality();
        final double outputCardinality = ((LogicalCollectionProperties) localProperties).getCardinality();
        // Compute CPU cost as the cost of reading a bit vector and then checking it. This cost estimation is
        // very conservative as it assumes that (1) one bit is read at a time and (2) the cost of checking one
        // bit is equal to the cost of evaluating a predicate.
        double cpuCost = leftCardinality * CostModel.CPU_READ.getCost();
        cpuCost += leftCardinality * CostModel.CPU_PRED.getCost();
        // Add the cost of projecting and copying the result.
        cpuCost += outputCardinality * CostModel.TOUCH_COPY.getCost();
        // Compute I/O cost as the cost required to read the accessed disk blocks of the bit vector.
        final double ioCost = leftCardinality / CostModel.BIT_INDEX_BF.getCost() * CostModel.IO.getCost();
        return new Cost(ioCost, cpuCost);
    }

    @Override
    public boolean satisfyRequiredProperties(final PhysicalProperties requiredProperties,
                                             final LogicalProperties inputLogicalProperties, final int inputNo,
                                             final StrongReference<PhysicalProperties> inputRequiredProperties) {
        this.assertInputNo(inputNo);
        if (inputNo == 1) {
            // No required physical properties for the right input.
            inputRequiredProperties.set(PhysicalProperties.anyPhysicalProperties());
            return true;
        }
        if (!DataOrder.ANY.equals(requiredProperties.getOrder())) {
            // If a specific data order is required, the keys need to be present in the schema of the left input.
            if (!((LogicalCollectionProperties) inputLogicalProperties).getSchema()
                    .containsKey(requiredProperties.getKey())) {
                inputRequiredProperties.set(null);
                return false;
            }
        }
        inputRequiredProperties.set(requiredProperties);
        return true;
    }
}
