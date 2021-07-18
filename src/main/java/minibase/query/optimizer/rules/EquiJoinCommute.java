/*
 * @(#)EquiJoinCommute.java   1.0   Jan 30, 2017
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
import minibase.query.optimizer.operators.logical.EquiJoin;

public class EquiJoinCommute extends AbstractRule {

    public EquiJoinCommute() {
        super(RuleType.EQUI_JOIN_COMMUTE,
                1 << RuleType.EQUI_JOIN_LTOR.ordinal()
                        | 1 << RuleType.EQUI_JOIN_RTOL.ordinal()
                        | 1 << RuleType.EQUI_JOIN_COMMUTE.ordinal(),
                new Expression(new EquiJoin(), new Expression(new Leaf(0)), new Expression(new Leaf(1))),
                new Expression(new EquiJoin(), new Expression(new Leaf(1)), new Expression(new Leaf(0)))
        );
    }

    @Override
    public Expression nextSubstitute(final Expression before, final PhysicalProperties requiredProperties) {
        final EquiJoin equi = (EquiJoin) before.getOperator();
        return new Expression(new EquiJoin(equi.getRightColumns(), equi.getLeftColumns()),
                before.getInput(1), before.getInput(0));
    }
}
