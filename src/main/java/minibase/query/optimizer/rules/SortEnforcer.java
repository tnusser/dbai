/*
 * @(#)SortEnforcer.java   1.0   Jun 14, 2014
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.optimizer.rules;

import minibase.catalog.DataOrder;
import minibase.query.optimizer.Expression;
import minibase.query.optimizer.PhysicalProperties;
import minibase.query.optimizer.SearchContext;
import minibase.query.optimizer.operators.Operator;
import minibase.query.optimizer.operators.physical.Sort;

/**
 * Enforcer rule that inserts the {@code QSort} physical enforcer, i.e., Expression(...) ->
 * Expression(QSort(...)).
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
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni.kn&gt;
 * @version 1.0
 */
public class SortEnforcer extends AbstractRule {

    /**
     * Creates a new enforcer rule that inserts the {@code QSort} physical operator.
     */
    public SortEnforcer() {
        super(RuleType.SORT_ENFORCER,
                // original pattern
                new Expression(new Leaf(0)),
                // substitute pattern
                new Expression(new Sort(), new Expression(new Leaf(0))));
    }

    @Override
    public Promise getPromise(final Operator opertator, final SearchContext context) {
        // Get the properties required by the current optimization context.
        final PhysicalProperties requiredProperties = context.getRequiredProperties();
        if (DataOrder.ANY.equals(requiredProperties.getOrder())) {
            // If the required data order can be anything, sorting does not make sense.
            return Promise.NONE;
        } else {
            return Promise.SORT;
        }
    }

    @Override
    public Expression nextSubstitute(final Expression before, final PhysicalProperties requiredProperties) {
        return new Expression(new Sort(), before);
    }
}
