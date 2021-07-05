/* QueryExpressionParserTokenManager.java */
/* Generated By:JJTree&JavaCC: Do not edit this line. QueryExpressionParserTokenManager.java */
package minibase.query.optimizer.parser;

/**
 * Token Manager.
 */
public class QueryExpressionParserTokenManager implements QueryExpressionParserConstants {

    /**
     * Debug output.
     */
    public java.io.PrintStream debugStream = System.out;

    /**
     * Set debug output.
     */
    public void setDebugStream(java.io.PrintStream ds) {
        debugStream = ds;
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
            case 0:
                if ((active0 & 0x3ffffffe0L) != 0L) {
                    jjmatchedKind = 49;
                    return 31;
                }
                if ((active0 & 0x200000000000L) != 0L)
                    return 1;
                return -1;
            case 1:
                if ((active0 & 0x2ffffffc0L) != 0L) {
                    if (jjmatchedPos != 1) {
                        jjmatchedKind = 49;
                        jjmatchedPos = 1;
                    }
                    return 31;
                }
                if ((active0 & 0x100000020L) != 0L)
                    return 31;
                return -1;
            case 2:
                if ((active0 & 0x1ffc000L) != 0L) {
                    if (jjmatchedPos != 2) {
                        jjmatchedKind = 49;
                        jjmatchedPos = 2;
                    }
                    return 4;
                }
                if ((active0 & 0x1d4000500L) != 0L)
                    return 31;
                if ((active0 & 0x22a003ac0L) != 0L) {
                    if (jjmatchedPos != 2) {
                        jjmatchedKind = 49;
                        jjmatchedPos = 2;
                    }
                    return 31;
                }
                return -1;
            case 3:
                if ((active0 & 0x1ffd000L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 3;
                    return 4;
                }
                if ((active0 & 0x88002bc0L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 3;
                    return 31;
                }
                if ((active0 & 0x222000000L) != 0L)
                    return 31;
                return -1;
            case 4:
                if ((active0 & 0x17e8000L) != 0L)
                    return 4;
                if ((active0 & 0x80002bc0L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 4;
                    return 31;
                }
                if ((active0 & 0x815000L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 4;
                    return 4;
                }
                if ((active0 & 0x8000000L) != 0L)
                    return 31;
                return -1;
            case 5:
                if ((active0 & 0x14000L) != 0L)
                    return 4;
                if ((active0 & 0x803800L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 5;
                    return 4;
                }
                if ((active0 & 0xc0L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 5;
                    return 31;
                }
                if ((active0 & 0x80000300L) != 0L)
                    return 31;
                return -1;
            case 6:
                if ((active0 & 0x800000L) != 0L)
                    return 4;
                if ((active0 & 0x40L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 6;
                    return 31;
                }
                if ((active0 & 0x3800L) != 0L) {
                    jjmatchedKind = 49;
                    jjmatchedPos = 6;
                    return 4;
                }
                if ((active0 & 0x80L) != 0L)
                    return 31;
                return -1;
            default:
                return -1;
        }
    }

    private final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
            case 40:
                return jjStopAtPos(0, 40);
            case 41:
                return jjStopAtPos(0, 41);
            case 42:
                return jjStopAtPos(0, 44);
            case 44:
                return jjStopAtPos(0, 42);
            case 46:
                return jjStartNfaWithStates_0(0, 45, 1);
            case 59:
                return jjStopAtPos(0, 43);
            case 60:
                jjmatchedKind = 38;
                return jjMoveStringLiteralDfa1_0(0x8800000000L);
            case 61:
                return jjStopAtPos(0, 34);
            case 62:
                jjmatchedKind = 36;
                return jjMoveStringLiteralDfa1_0(0x2000000000L);
            case 65:
            case 97:
                return jjMoveStringLiteralDfa1_0(0x102001020L);
            case 68:
            case 100:
                return jjMoveStringLiteralDfa1_0(0x220000040L);
            case 69:
            case 101:
                return jjMoveStringLiteralDfa1_0(0x200L);
            case 70:
            case 102:
                return jjMoveStringLiteralDfa1_0(0x8000000L);
            case 71:
            case 103:
                return jjMoveStringLiteralDfa1_0(0x2400L);
            case 73:
            case 105:
                return jjMoveStringLiteralDfa1_0(0x4000000L);
            case 79:
            case 111:
                return jjMoveStringLiteralDfa1_0(0x1ffc800L);
            case 80:
            case 112:
                return jjMoveStringLiteralDfa1_0(0x80L);
            case 83:
            case 115:
                return jjMoveStringLiteralDfa1_0(0xd0000100L);
            default:
                return jjMoveNfa_0(5, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (curChar) {
            case 61:
                if ((active0 & 0x2000000000L) != 0L)
                    return jjStopAtPos(1, 37);
                else if ((active0 & 0x8000000000L) != 0L)
                    return jjStopAtPos(1, 39);
                break;
            case 62:
                if ((active0 & 0x800000000L) != 0L)
                    return jjStopAtPos(1, 35);
                break;
            case 65:
            case 97:
                return jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
            case 69:
            case 101:
                return jjMoveStringLiteralDfa2_0(active0, 0x240000500L);
            case 71:
            case 103:
                return jjMoveStringLiteralDfa2_0(active0, 0x1000L);
            case 73:
            case 105:
                return jjMoveStringLiteralDfa2_0(active0, 0x40L);
            case 76:
            case 108:
                return jjMoveStringLiteralDfa2_0(active0, 0x8000000L);
            case 78:
            case 110:
                return jjMoveStringLiteralDfa2_0(active0, 0x4000000L);
            case 80:
            case 112:
                return jjMoveStringLiteralDfa2_0(active0, 0x1ffc000L);
            case 81:
            case 113:
                return jjMoveStringLiteralDfa2_0(active0, 0x200L);
            case 82:
            case 114:
                return jjMoveStringLiteralDfa2_0(active0, 0x2880L);
            case 83:
            case 115:
                if ((active0 & 0x20L) != 0L) {
                    jjmatchedKind = 5;
                    jjmatchedPos = 1;
                }
                return jjMoveStringLiteralDfa2_0(active0, 0x100000000L);
            case 84:
            case 116:
                return jjMoveStringLiteralDfa2_0(active0, 0x92000000L);
            default:
                break;
        }
        return jjStartNfa_0(0, active0);
    }

    private int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(0, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (curChar) {
            case 95:
                return jjMoveStringLiteralDfa3_0(active0, 0x1ffc000L);
            case 67:
            case 99:
                if ((active0 & 0x100000000L) != 0L)
                    return jjStartNfaWithStates_0(2, 32, 31);
                break;
            case 68:
            case 100:
                return jjMoveStringLiteralDfa3_0(active0, 0x800L);
            case 71:
            case 103:
                return jjMoveStringLiteralDfa3_0(active0, 0x1000L);
            case 74:
            case 106:
                return jjMoveStringLiteralDfa3_0(active0, 0x200L);
            case 76:
            case 108:
                if ((active0 & 0x40000000L) != 0L) {
                    jjmatchedKind = 30;
                    jjmatchedPos = 2;
                }
                return jjMoveStringLiteralDfa3_0(active0, 0x100L);
            case 79:
            case 111:
                return jjMoveStringLiteralDfa3_0(active0, 0x8002080L);
            case 82:
            case 114:
                if ((active0 & 0x10000000L) != 0L) {
                    jjmatchedKind = 28;
                    jjmatchedPos = 2;
                }
                return jjMoveStringLiteralDfa3_0(active0, 0x80000000L);
            case 83:
            case 115:
                return jjMoveStringLiteralDfa3_0(active0, 0x200000040L);
            case 84:
            case 116:
                if ((active0 & 0x400L) != 0L)
                    return jjStartNfaWithStates_0(2, 10, 31);
                else if ((active0 & 0x4000000L) != 0L)
                    return jjStartNfaWithStates_0(2, 26, 31);
                return jjMoveStringLiteralDfa3_0(active0, 0x22000000L);
            default:
                break;
        }
        return jjStartNfa_0(1, active0);
    }

    private int jjMoveStringLiteralDfa3_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(1, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
        switch (curChar) {
            case 95:
                return jjMoveStringLiteralDfa4_0(active0, 0x1000L);
            case 65:
            case 97:
                return jjMoveStringLiteralDfa4_0(active0, 0x8004000L);
            case 67:
            case 99:
                if ((active0 & 0x200000000L) != 0L)
                    return jjStartNfaWithStates_0(3, 33, 31);
                break;
            case 69:
            case 101:
                if ((active0 & 0x20000000L) != 0L)
                    return jjStartNfaWithStates_0(3, 29, 31);
                return jjMoveStringLiteralDfa4_0(active0, 0x20900L);
            case 71:
            case 103:
                return jjMoveStringLiteralDfa4_0(active0, 0x300000L);
            case 73:
            case 105:
                return jjMoveStringLiteralDfa4_0(active0, 0x81000000L);
            case 74:
            case 106:
                return jjMoveStringLiteralDfa4_0(active0, 0x80L);
            case 76:
            case 108:
                return jjMoveStringLiteralDfa4_0(active0, 0x8c0000L);
            case 78:
            case 110:
                return jjMoveStringLiteralDfa4_0(active0, 0x410000L);
            case 79:
            case 111:
                return jjMoveStringLiteralDfa4_0(active0, 0x8200L);
            case 82:
            case 114:
                if ((active0 & 0x2000000L) != 0L)
                    return jjStartNfaWithStates_0(3, 25, 31);
                break;
            case 84:
            case 116:
                return jjMoveStringLiteralDfa4_0(active0, 0x40L);
            case 85:
            case 117:
                return jjMoveStringLiteralDfa4_0(active0, 0x2000L);
            default:
                break;
        }
        return jjStartNfa_0(2, active0);
    }

    private int jjMoveStringLiteralDfa4_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(2, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(3, active0);
            return 4;
        }
        switch (curChar) {
            case 67:
            case 99:
                return jjMoveStringLiteralDfa5_0(active0, 0x100L);
            case 69:
            case 101:
                if ((active0 & 0x80000L) != 0L)
                    return jjStartNfaWithStates_0(4, 19, 4);
                else if ((active0 & 0x200000L) != 0L)
                    return jjStartNfaWithStates_0(4, 21, 4);
                else if ((active0 & 0x400000L) != 0L)
                    return jjStartNfaWithStates_0(4, 22, 4);
                return jjMoveStringLiteralDfa5_0(active0, 0x80L);
            case 73:
            case 105:
                return jjMoveStringLiteralDfa5_0(active0, 0x800240L);
            case 76:
            case 108:
                return jjMoveStringLiteralDfa5_0(active0, 0x1000L);
            case 78:
            case 110:
                if ((active0 & 0x1000000L) != 0L)
                    return jjStartNfaWithStates_0(4, 24, 4);
                return jjMoveStringLiteralDfa5_0(active0, 0x80004000L);
            case 79:
            case 111:
                return jjMoveStringLiteralDfa5_0(active0, 0x10000L);
            case 80:
            case 112:
                return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
            case 81:
            case 113:
                if ((active0 & 0x20000L) != 0L)
                    return jjStartNfaWithStates_0(4, 17, 4);
                break;
            case 82:
            case 114:
                if ((active0 & 0x8000L) != 0L)
                    return jjStartNfaWithStates_0(4, 15, 4);
                return jjMoveStringLiteralDfa5_0(active0, 0x800L);
            case 84:
            case 116:
                if ((active0 & 0x40000L) != 0L)
                    return jjStartNfaWithStates_0(4, 18, 4);
                else if ((active0 & 0x100000L) != 0L)
                    return jjStartNfaWithStates_0(4, 20, 4);
                else if ((active0 & 0x8000000L) != 0L)
                    return jjStartNfaWithStates_0(4, 27, 31);
                break;
            default:
                break;
        }
        return jjStartNfa_0(3, active0);
    }

    private int jjMoveStringLiteralDfa5_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(3, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(4, active0);
            return 5;
        }
        switch (curChar) {
            case 95:
                return jjMoveStringLiteralDfa6_0(active0, 0x2800L);
            case 67:
            case 99:
                return jjMoveStringLiteralDfa6_0(active0, 0x80L);
            case 68:
            case 100:
                if ((active0 & 0x4000L) != 0L)
                    return jjStartNfaWithStates_0(5, 14, 4);
                break;
            case 71:
            case 103:
                if ((active0 & 0x80000000L) != 0L)
                    return jjStartNfaWithStates_0(5, 31, 31);
                break;
            case 73:
            case 105:
                return jjMoveStringLiteralDfa6_0(active0, 0x1000L);
            case 75:
            case 107:
                return jjMoveStringLiteralDfa6_0(active0, 0x800000L);
            case 78:
            case 110:
                if ((active0 & 0x200L) != 0L)
                    return jjStartNfaWithStates_0(5, 9, 31);
                return jjMoveStringLiteralDfa6_0(active0, 0x40L);
            case 84:
            case 116:
                if ((active0 & 0x100L) != 0L)
                    return jjStartNfaWithStates_0(5, 8, 31);
                else if ((active0 & 0x10000L) != 0L)
                    return jjStartNfaWithStates_0(5, 16, 4);
                break;
            default:
                break;
        }
        return jjStartNfa_0(4, active0);
    }

    private int jjMoveStringLiteralDfa6_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(4, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(5, active0);
            return 6;
        }
        switch (curChar) {
            case 66:
            case 98:
                return jjMoveStringLiteralDfa7_0(active0, 0x2800L);
            case 67:
            case 99:
                return jjMoveStringLiteralDfa7_0(active0, 0x40L);
            case 69:
            case 101:
                if ((active0 & 0x800000L) != 0L)
                    return jjStartNfaWithStates_0(6, 23, 4);
                break;
            case 83:
            case 115:
                return jjMoveStringLiteralDfa7_0(active0, 0x1000L);
            case 84:
            case 116:
                if ((active0 & 0x80L) != 0L)
                    return jjStartNfaWithStates_0(6, 7, 31);
                break;
            default:
                break;
        }
        return jjStartNfa_0(5, active0);
    }

    private int jjMoveStringLiteralDfa7_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(5, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(6, active0);
            return 7;
        }
        switch (curChar) {
            case 84:
            case 116:
                if ((active0 & 0x40L) != 0L)
                    return jjStartNfaWithStates_0(7, 6, 31);
                else if ((active0 & 0x1000L) != 0L)
                    return jjStartNfaWithStates_0(7, 12, 4);
                break;
            case 89:
            case 121:
                if ((active0 & 0x800L) != 0L)
                    return jjStartNfaWithStates_0(7, 11, 4);
                else if ((active0 & 0x2000L) != 0L)
                    return jjStartNfaWithStates_0(7, 13, 4);
                break;
            default:
                break;
        }
        return jjStartNfa_0(6, active0);
    }

    private int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            return pos + 1;
        }
        return jjMoveNfa_0(state, pos + 1);
    }

    static final long[] jjbitVec0 = {
            0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };

    private int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 31;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (; ; ) {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64) {
                long l = 1L << curChar;
                do {
                    switch (jjstateSet[--i]) {
                        case 5:
                            if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 46)
                                    kind = 46;
                                {
                                    jjCheckNAddStates(0, 3);
                                }
                            } else if (curChar == 47) {
                                jjAddStates(4, 5);
                            } else if (curChar == 45) {
                                jjCheckNAddStates(6, 9);
                            } else if (curChar == 34) {
                                jjCheckNAddTwoStates(6, 7);
                            } else if (curChar == 46) {
                                jjCheckNAddTwoStates(1, 2);
                            }
                            break;
                        case 31:
                        case 4:
                            if ((0x3ff001000000000L & l) == 0L)
                                break;
                            if (kind > 49)
                                kind = 49;
                        {
                            jjCheckNAdd(4);
                        }
                        break;
                        case 1:
                            if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 48)
                                    kind = 48;
                                {
                                    jjCheckNAdd(2);
                                }
                            } else if (curChar == 45) {
                                jjCheckNAdd(2);
                            }
                            break;
                        case 0:
                            if (curChar == 46) {
                                jjCheckNAddTwoStates(1, 2);
                            }
                            break;
                        case 2:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 48)
                                kind = 48;
                        {
                            jjCheckNAdd(2);
                        }
                        break;
                        case 6:
                            if ((0xfffffffbffffffffL & l) != 0L) {
                                jjCheckNAddTwoStates(6, 7);
                            }
                            break;
                        case 7:
                            if (curChar == 34 && kind > 52)
                                kind = 52;
                            break;
                        case 8:
                            if (curChar == 45) {
                                jjCheckNAddStates(6, 9);
                            }
                            break;
                        case 9:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 46)
                                kind = 46;
                        {
                            jjCheckNAdd(9);
                        }
                        break;
                        case 10:
                            if (curChar == 45) {
                                jjCheckNAdd(11);
                            }
                            break;
                        case 11:
                            if ((0x3ff000000000000L & l) != 0L) {
                                jjCheckNAddTwoStates(11, 0);
                            }
                            break;
                        case 12:
                            if ((0x3ff000000000000L & l) == 0L)
                                break;
                            if (kind > 46)
                                kind = 46;
                        {
                            jjCheckNAddStates(0, 3);
                        }
                        break;
                        case 13:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 14;
                            break;
                        case 14:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 15;
                            break;
                        case 15:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 16;
                            break;
                        case 16:
                            if (curChar == 45)
                                jjstateSet[jjnewStateCnt++] = 17;
                            break;
                        case 17:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 18;
                            break;
                        case 18:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 19;
                            break;
                        case 19:
                            if (curChar == 45)
                                jjstateSet[jjnewStateCnt++] = 20;
                            break;
                        case 20:
                            if ((0x3ff000000000000L & l) != 0L)
                                jjstateSet[jjnewStateCnt++] = 21;
                            break;
                        case 21:
                            if ((0x3ff000000000000L & l) != 0L && kind > 53)
                                kind = 53;
                            break;
                        case 22:
                            if (curChar == 47) {
                                jjAddStates(4, 5);
                            }
                            break;
                        case 23:
                            if (curChar != 47)
                                break;
                            if (kind > 54)
                                kind = 54;
                        {
                            jjCheckNAdd(24);
                        }
                        break;
                        case 24:
                            if ((0xffffffffffffdbffL & l) == 0L)
                                break;
                            if (kind > 54)
                                kind = 54;
                        {
                            jjCheckNAdd(24);
                        }
                        break;
                        case 25:
                            if (curChar == 42) {
                                jjCheckNAddTwoStates(26, 27);
                            }
                            break;
                        case 26:
                            if ((0xfffffbffffffffffL & l) != 0L) {
                                jjCheckNAddTwoStates(26, 27);
                            }
                            break;
                        case 27:
                            if (curChar == 42) {
                                jjCheckNAddStates(10, 12);
                            }
                            break;
                        case 28:
                            if ((0xffff7bffffffffffL & l) != 0L) {
                                jjCheckNAddTwoStates(29, 27);
                            }
                            break;
                        case 29:
                            if ((0xfffffbffffffffffL & l) != 0L) {
                                jjCheckNAddTwoStates(29, 27);
                            }
                            break;
                        case 30:
                            if (curChar == 47 && kind > 55)
                                kind = 55;
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                        case 5:
                        case 3:
                            if ((0x7fffffe07fffffeL & l) == 0L)
                                break;
                            if (kind > 49)
                                kind = 49;
                        {
                            jjCheckNAddTwoStates(3, 4);
                        }
                        break;
                        case 31:
                            if ((0x7fffffe87fffffeL & l) != 0L) {
                                if (kind > 49)
                                    kind = 49;
                                {
                                    jjCheckNAdd(4);
                                }
                            }
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                if (kind > 49)
                                    kind = 49;
                                {
                                    jjCheckNAddTwoStates(3, 4);
                                }
                            }
                            break;
                        case 4:
                            if ((0x7fffffe87fffffeL & l) == 0L)
                                break;
                            if (kind > 49)
                                kind = 49;
                        {
                            jjCheckNAdd(4);
                        }
                        break;
                        case 6: {
                            jjAddStates(13, 14);
                        }
                        break;
                        case 24:
                            if (kind > 54)
                                kind = 54;
                            jjstateSet[jjnewStateCnt++] = 24;
                            break;
                        case 26: {
                            jjCheckNAddTwoStates(26, 27);
                        }
                        break;
                        case 28:
                        case 29: {
                            jjCheckNAddTwoStates(29, 27);
                        }
                        break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                        case 6:
                            if ((jjbitVec0[i2] & l2) != 0L) {
                                jjAddStates(13, 14);
                            }
                            break;
                        case 24:
                            if ((jjbitVec0[i2] & l2) == 0L)
                                break;
                            if (kind > 54)
                                kind = 54;
                            jjstateSet[jjnewStateCnt++] = 24;
                            break;
                        case 26:
                            if ((jjbitVec0[i2] & l2) != 0L) {
                                jjCheckNAddTwoStates(26, 27);
                            }
                            break;
                        case 28:
                        case 29:
                            if ((jjbitVec0[i2] & l2) != 0L) {
                                jjCheckNAddTwoStates(29, 27);
                            }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 31 - (jjnewStateCnt = startsAt)))
                return curPos;
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }

    static final int[] jjnextStates = {
            9, 11, 0, 13, 23, 25, 9, 10, 11, 0, 27, 28, 30, 6, 7,
    };

    /**
     * Token literal values.
     */
    public static final String[] jjstrLiteralImages = {
            "", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, "\75", "\74\76", "\76", "\76\75", "\74",
            "\74\75", "\50", "\51", "\54", "\73", "\52", "\56", null, null, null, null, null, null,
            null, null, null, null,};

    protected Token jjFillToken() {
        final Token t;
        final String curTokenImage;
        final int beginLine;
        final int endLine;
        final int beginColumn;
        final int endColumn;
        String im = jjstrLiteralImages[jjmatchedKind];
        curTokenImage = (im == null) ? input_stream.GetImage() : im;
        beginLine = input_stream.getBeginLine();
        beginColumn = input_stream.getBeginColumn();
        endLine = input_stream.getEndLine();
        endColumn = input_stream.getEndColumn();
        t = Token.newToken(jjmatchedKind, curTokenImage);

        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;

        return t;
    }

    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    /**
     * Get the next Token.
     */
    public Token getNextToken() {
        Token specialToken = null;
        Token matchedToken;
        int curPos = 0;

        EOFLoop:
        for (; ; ) {
            try {
                curChar = input_stream.BeginToken();
            } catch (Exception e) {
                jjmatchedKind = 0;
                jjmatchedPos = -1;
                matchedToken = jjFillToken();
                matchedToken.specialToken = specialToken;
                return matchedToken;
            }

            try {
                input_stream.backup(0);
                while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
                    curChar = input_stream.BeginToken();
            } catch (java.io.IOException e1) {
                continue EOFLoop;
            }
            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos)
                    input_stream.backup(curPos - jjmatchedPos - 1);
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    matchedToken.specialToken = specialToken;
                    return matchedToken;
                } else {
                    if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                        matchedToken = jjFillToken();
                        if (specialToken == null)
                            specialToken = matchedToken;
                        else {
                            matchedToken.specialToken = specialToken;
                            specialToken = (specialToken.next = matchedToken);
                        }
                    }
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            String error_after = null;
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else
                    error_column++;
            }
            if (!EOFSeen) {
                input_stream.backup(1);
                error_after = curPos <= 1 ? "" : input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
        }
    }

    private void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }

    private void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    private void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

    /**
     * Constructor.
     */
    public QueryExpressionParserTokenManager(SimpleCharStream stream) {

        if (SimpleCharStream.staticFlag)
            throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");

        input_stream = stream;
    }

    /**
     * Constructor.
     */
    public QueryExpressionParserTokenManager(SimpleCharStream stream, int lexState) {
        ReInit(stream);
        SwitchTo(lexState);
    }

    /**
     * Reinitialise parser.
     */
    public void ReInit(SimpleCharStream stream) {

        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }

    private void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 31; i-- > 0; )
            jjrounds[i] = 0x80000000;
    }

    /**
     * Reinitialise parser.
     */
    public void ReInit(SimpleCharStream stream, int lexState) {

        ReInit(stream);
        SwitchTo(lexState);
    }

    /**
     * Switch to specified lex state.
     */
    public void SwitchTo(int lexState) {
        if (lexState >= 1 || lexState < 0)
            throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
        else
            curLexState = lexState;
    }

    /**
     * Lexer state names.
     */
    public static final String[] lexStateNames = {
            "DEFAULT",
    };
    static final long[] jjtoToken = {
            0x337fffffffffe1L,
    };
    static final long[] jjtoSkip = {
            0xc000000000001eL,
    };
    static final long[] jjtoSpecial = {
            0xc0000000000000L,
    };
    protected SimpleCharStream input_stream;

    private final int[] jjrounds = new int[31];
    private final int[] jjstateSet = new int[2 * 31];


    protected int curChar;
}
