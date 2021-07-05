/*
 * @(#)Distinct.java   1.0   Jun 28, 2014
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.optimizer.operators.logical;

import minibase.catalog.TableStatistics;
import minibase.query.optimizer.LogicalCollectionProperties;
import minibase.query.optimizer.LogicalProperties;
import minibase.query.optimizer.operators.OperatorType;
import minibase.query.schema.Schema;

/**
 * The {@code Distinct} logical operator removes duplicates from its input.
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
 */
public class Distinct extends AbstractLogicalOperator {

    /**
     * Creates a new {@code Distinct} logical operator.
     */
    public Distinct() {
        super(OperatorType.DISTINCT);
    }

    @Override
    public LogicalProperties getLogicalProperties(final LogicalProperties... inputProperties) {
        final LogicalCollectionProperties collectionProperties = (LogicalCollectionProperties) inputProperties[0];
        final Schema schema = this.getSchema(collectionProperties.getSchema());
        final double uniqueCardinality = collectionProperties.getUniqueCardinality();
        return new LogicalCollectionProperties(schema,
                new TableStatistics(uniqueCardinality, uniqueCardinality, collectionProperties.getWidth()),
                collectionProperties.getColumnStatistics(), false);
    }

    @Override
    public Schema getSchema(final Schema... inputSchemas) {
        return inputSchemas[0];
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
