/*
 * @(#)EvaluatorBaseTest.java   1.0   Jan 13, 2014
 *
 * Copyright (c) 1996-1997 University of Wisconsin.
 * Copyright (c) 2006 Purdue University.
 * Copyright (c) 2013-2021 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package minibase.query.evaluator;

import org.junit.Before;

import minibase.BaseTest;
import minibase.access.file.File;
import minibase.access.file.HeapFile;
import minibase.catalog.DataType;
import minibase.query.schema.Schema;
import minibase.query.schema.SchemaBuilder;

import java.sql.Date;

/**
 * This base class contains code that is common to the test cases of the evaluator layer.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public class EvaluatorBaseTest extends BaseTest {

    /**
     * Array with sample sailor names.
     */
    protected static final String[] SNAMES = {"Daniel", "Andreas", "Dominik", "Jürgen", "Nadja", "Marcel",
            "Alexander", "Florian", "Johann", "Christoph", "Menna", "Michael", "Javid", "Lokesh", "Madhurima",
            "Shahana", "Emanuel", "Minh Huyen", "Mario", "Feeras", "Manuel", "Klaus", "Niklas", "Philip", "Sema",
            "Nayeem", "Tribhuwan", "Jens", "Leonard"};

    /**
     * Array with boat names
     */
    protected static final String[] BNAMES = {"Serenity", "Island Time", "Scout", "Pura Vida", "Seas the Day",
            "Shenanigans", "Black Pearl", "Cool Change", "Liberty", "Knot on Call"};

    /**
     * Array with boat colors
     */
    protected static final String[] BCOLORS = { "red", "yellow", "green", "blue", "white", "brown"};


    /**
     * Schema of the Sailors relation.
     */
    protected static final Schema S_SAILORS = new SchemaBuilder()
            .addField("sid", DataType.INT, DataType.INT.getSize())
            .addField("sname", DataType.CHAR, 50)
            .addField("rating", DataType.INT, DataType.INT.getSize())
            .addField("age", DataType.FLOAT, DataType.FLOAT.getSize())
            .build();

    /**
     * Schema of the Reserves relation.
     */
    protected static final Schema S_BOATS = new SchemaBuilder()
            .addField("bid", DataType.INT, DataType.INT.getSize())
            .addField("bname", DataType.CHAR, 20)
            .addField("color", DataType.CHAR, 10)
            .build();

    /**
     * Schema of the Reserves relation.
     */
    protected static final Schema S_RESERVES = new SchemaBuilder()
            .addField("sid", DataType.INT, DataType.INT.getSize())
            .addField("bid", DataType.INT, DataType.INT.getSize())
            .addField("day", DataType.DATE, DataType.DATE.getSize())
            .addField("rname", DataType.CHAR, 50)
            .build();

    /**
     * Returns the heap file containing the data of the Sailors relation.
     *
     * @param num number of tuples to generate
     * @return heap file of the Sailors relation
     */
    protected File createSailors(final int num) {
        final File sailors = HeapFile.createTemporary(this.getBufferManager());
        for (int i = 0; i < num; i++) {
            final byte[] tuple = EvaluatorBaseTest.S_SAILORS.newTuple();
            S_SAILORS.setAllFields(tuple,
                    i,
                    EvaluatorBaseTest.SNAMES[this.getRandom().nextInt(EvaluatorBaseTest.SNAMES.length)],
                    this.getRandom().nextInt(11),
                    this.getRandom().nextFloat() * 81.9f + 18.0f
            );
            sailors.insertRecord(tuple);
        }
        return sailors;
    }

    /**
     * Returns the heap file containing the data of the Reserves relation.
     *
     * @param num number of tuples to generate
     * @param numSailors number of sailors
     * @param numBoats number of boats
     * @return heap file of the Reserves relation
     */
    protected File createReserves(final int num, final int numSailors, final int numBoats) {
        final File reserves = HeapFile.createTemporary(this.getBufferManager());
        for (int i = 0; i < num; i++) {
            final byte[] tuple = EvaluatorBaseTest.S_RESERVES.newTuple();
            S_RESERVES.setAllFields(tuple,
                    this.getRandom().nextInt(numSailors),
                    this.getRandom().nextInt(numBoats),
                    new Date(
                            this.getRandom().nextInt(122),
                            this.getRandom().nextInt(12),
                            this.getRandom().nextInt(32)
                    ),
                    "Reservation" + i
            );
            reserves.insertRecord(tuple);
        }
        return reserves;
    }

    /**
     * Returns the heap file containing the data of the boat relation.
     * @param num number of tuples to generate
     *
     * @return heap file of the boat relation
     */
    protected File createBoats(final int num) {
        final File boats = HeapFile.createTemporary(this.getBufferManager());
        for (int i = 0; i < num; i++) {
            final byte[] tuple = EvaluatorBaseTest.S_BOATS.newTuple();
            S_BOATS.setAllFields(tuple,
                    i,
                    EvaluatorBaseTest.BNAMES[this.getRandom().nextInt(EvaluatorBaseTest.BNAMES.length)],
                    EvaluatorBaseTest.BCOLORS[this.getRandom().nextInt(EvaluatorBaseTest.BCOLORS.length)]
            );
            boats.insertRecord(tuple);
        }
        return boats;
    }


    /**
     * Test setup that creates a new database on disk and a heap file for the Sailors relations. It also
     * initializes the Sailors relation with random data.
     */
    @Before
    public void setUp() {
        this.initRandom();
    }

    /**
     * Evaluates an operator.
     *
     * @param operator the operator
     * @return number of tuples
     */
    protected int evaluate(final Operator operator) {
        int count = 0;
        try (TupleIterator it = operator.open()) {
            while (it.hasNext()) {
                count++;
            }
        }
        return count;
    }
}
