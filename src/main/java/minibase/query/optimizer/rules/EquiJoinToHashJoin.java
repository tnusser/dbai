/*
 * @(#)EquiJoinToHashJoin.java   1.0   Jun 15, 20142
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.optimizer.rules;

import minibase.query.optimizer.Expression;
import minibase.query.optimizer.PhysicalProperties;
import minibase.query.optimizer.SearchContext;
import minibase.query.optimizer.operators.Operator;
import minibase.query.optimizer.operators.logical.EquiJoin;
import minibase.query.optimizer.operators.physical.HashJoin;
import minibase.query.schema.ColumnReference;

import java.util.List;

public class EquiJoinToHashJoin extends AbstractRule {

    public EquiJoinToHashJoin() {
        super(RuleType.EQUI_TO_HASH_JOIN,
                new Expression(new EquiJoin(), new Expression(new Leaf(0)), new Expression(new Leaf(1))),
                new Expression(new HashJoin(), new Expression(new Leaf(0)), new Expression(new Leaf(1))));
    }

    @Override
    public Promise getPromise(final Operator operator, final SearchContext context) {
        if (((EquiJoin) operator).getLeftColumns().size() == 0 ) {
            return Promise.NONE;
        } else if (((EquiJoin)operator).getRightColumns().size() == 0) {
            return Promise.NONE;
        }
        return Promise.HASH;
    }

    @Override
    public Expression nextSubstitute(final Expression before, final PhysicalProperties requiredProperties) {
        final EquiJoin equiJoin = (EquiJoin) before.getOperator();
        return new Expression(new HashJoin(equiJoin.getLeftColumns(), equiJoin.getRightColumns()), before.getInput(0),
                before.getInput(1));
    }
}
