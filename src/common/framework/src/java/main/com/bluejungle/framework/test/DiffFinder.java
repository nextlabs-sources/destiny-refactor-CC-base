package com.bluejungle.framework.test;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/test/DiffFinder.java#1 $
 */

import java.util.HashMap;

/**
 * Compares two arrays of objects, producing a set of differences
 * described as a "change script."
 * Implements the diff algorithm by Hunt and McIlroy.
 */
public class DiffFinder {
    /**
     * Constructs a DiffFinder with default characteristics.
     * @param a - the LHS array of objects.
     * @param b - the RHS array of objects.
     */
    public DiffFinder(Object[] a, Object[] b ) {
        this( a, b, false, false );
    }
    /**
     * Constructs a DiffFinder that uses the search heuristic while searching
     * for diffs.
     * @param a - the LHS array of objects.
     * @param b - the RHS array of objects.
     * @param useHeuristic instructs the finder to use the heuristic while
     * searching for diffs.
     */
    public DiffFinder(Object[] a, Object[] b, boolean useHeuristic ) {
        this( a, b, useHeuristic, false );
    }
    /**
     * Constructs a DiffFinder that uses the search heuristic and
     * avoids the discards while searching for diffs.
     * @param a - the LHS array of objects.
     * @param b - the RHS array of objects.
     * @param useHeuristic instructs the finder to use the heuristic while
     * searching for diffs.
     * @param noDiscards indicates that there should be no discards while
     * searching for diffs. 
     */
    public DiffFinder(Object[] a, Object[] b, boolean useHeuristic, boolean noDiscards ) {
        this.useHeuristic = useHeuristic;
        this.noDiscards = noDiscards;
        final HashMap h = new HashMap();
        int[] equivs = new int[a.length];
        for (int i = 0; i < equivs.length; ++i) {
            Integer ir = (Integer) h.get(a[i]);
            if (ir == null) {
                h.put(a[i], new Integer(equivs[i] = equivCount++));
            } else {
                equivs[i] = ir.intValue();
            }
        }
        lhs = new InputData( equivs );
        equivs = new int[b.length];
        for (int i = 0; i < equivs.length; ++i) {
            Integer ir = (Integer) h.get(b[i]);
            if (ir == null) {
                h.put(b[i], new Integer(equivs[i] = equivCount++));
            } else {
                equivs[i] = ir.intValue();
            }
        }
        rhs = new InputData( equivs );
    }
    /**
     * Diffs the specified files and returns a "script"
     * that describes how to modify the LHS to produce the RHS.
     * @return a "script" that describes how to modify the LHS
     * to produce the RHS.
     */
    public ChangeRecord diffForward() {
        return calculateDifferences( LHS_TO_RHS );
    }
    /**
     * Diffs the specified files and returns a "script"
     * that describes how to modify the RHS to produce the LHS.
     * @return a "script" that describes how to modify the RHS
     * to produce the LHS.
     */
    public ChangeRecord diffReverse() {
        return calculateDifferences( RHS_TO_LHS );
    }
    
    private final boolean useHeuristic;
    private final boolean noDiscards;
    private final InputData lhs, rhs;
    private int equivCount = 1;
    private int[] xvec, yvec;
    private int[] fdiag;
    private int[] bdiag;
    private int fdiagoff, bdiagoff;
    private int cost;
    
    private int diag(int xoff, int xlim, int yoff, int ylim) {
        final int[] fd = fdiag;
        final int[] bd = bdiag;
        final int[] xv = xvec;
        final int[] yv = yvec;
        final int dmin = xoff - ylim;
        final int dmax = xlim - yoff;
        final int fmid = xoff - yoff;
        final int bmid = xlim - ylim;
        int fmin = fmid, fmax = fmid;
        int bmin = bmid, bmax = bmid;
        final boolean odd = (fmid - bmid & 1) != 0;
        fd[fdiagoff + fmid] = xoff;
        bd[bdiagoff + bmid] = xlim;
        
        for (int c = 1;; c++) {
            int d;
            boolean moreThan20 = false;
            
            if (fmin > dmin)
                fd[fdiagoff + --fmin - 1] = -1;
            else
                ++fmin;
            if (fmax < dmax)
                fd[fdiagoff + ++fmax + 1] = -1;
            else
                --fmax;
            for (d = fmax; d >= fmin; d -= 2) {
                int x, y, oldx, tLow = fd[fdiagoff + d - 1], tHigh = fd[fdiagoff
                                                                        + d + 1];
                
                if (tLow >= tHigh) {
                    x = tLow + 1;
                } else {
                    x = tHigh;
                }
                oldx = x;
                y = x - d;
                while (x < xlim && y < ylim && xv[x] == yv[y]) {
                    ++x;
                    ++y;
                }
                if (x - oldx > 20) {
                    moreThan20 = true;
                }
                fd[fdiagoff + d] = x;
                if (odd && bmin <= d && d <= bmax && bd[bdiagoff + d] <= fd[fdiagoff + d]) {
                    cost = 2 * c - 1;
                    return d;
                }
            }
            if (bmin > dmin) {
                bmin--;
                bd[bdiagoff + bmin - 1] = Integer.MAX_VALUE;
            } else {
                bmin++;
            }
            if (bmax < dmax) {
                bmax++;
                bd[bdiagoff + bmax + 1] = Integer.MAX_VALUE;
            } else {
                bmax--;
            }
            for (d = bmax; d >= bmin; d -= 2) {
                int x, y, oldx, tlo = bd[bdiagoff + d - 1], thi = bd[bdiagoff + d + 1];
                if ( tlo < thi ) {
                    x = tlo;
                } else {
                    x = thi - 1;
                }
                oldx = x;
                y = x - d;
                while (x > xoff && y > yoff && xv[x - 1] == yv[y - 1]) {
                    --x;
                    --y;
                }
                if (oldx - x > 20) {
                    moreThan20 = true;
                }
                bd[bdiagoff + d] = x;
                if (!odd && fmin <= d && d <= fmax && bd[bdiagoff + d] <= fd[fdiagoff + d]) {
                    cost = 2 * c;
                    return d;
                }
            }
            if (c > 200 && moreThan20 && useHeuristic) {
                int best = 0;
                int bestpos = -1;
                for (d = fmax; d >= fmin; d -= 2) {
                    int dd = d - fmid;
                    if ((fd[fdiagoff + d] - xoff) * 2 - dd > 12 * (c + (dd > 0 ? dd
                            : -dd))) {
                        if (fd[fdiagoff + d] * 2 - dd > best
                                && fd[fdiagoff + d] - xoff > 20
                                && fd[fdiagoff + d] - d - yoff > 20) {
                            int k;
                            int x = fd[fdiagoff + d];
                            for (k = 1; k <= 20; k++)
                                if (xvec[x - k] != yvec[x - d - k])
                                    break;
                                
                            if (k == 21) {
                                best = fd[fdiagoff + d] * 2 - dd;
                                bestpos = d;
                            }
                        }
                    }
                }
                if (best > 0) {
                    cost = 2 * c - 1;
                    return bestpos;
                }
                best = 0;
                for (d = bmax; d >= bmin; d -= 2) {
                    int dd = d - bmid;
                    if ((xlim - bd[bdiagoff + d]) * 2 + dd > 12 * (c + (dd > 0 ? dd
                            : -dd))) {
                        if ((xlim - bd[bdiagoff + d]) * 2 + dd > best
                                && xlim - bd[bdiagoff + d] > 20
                                && ylim - (bd[bdiagoff + d] - d) > 20) {
                            int k;
                            int x = bd[bdiagoff + d];
                            
                            for (k = 0; k < 20; k++) {
                                if (xvec[x + k] != yvec[x - d + k]) {
                                    break;
                                }
                            }
                            if (k == 20) {
                                best = (xlim - bd[bdiagoff + d]) * 2 + dd;
                                bestpos = d;
                            }
                        }
                    }
                }
                if (best > 0) {
                    cost = 2 * c - 1;
                    return bestpos;
                }
            }
        }
    }
    private void cmpSequence(int xoff, int xlim, int yoff, int ylim) {
        while (xoff < xlim && yoff < ylim && xvec[xoff] == yvec[yoff]) {
            ++xoff;
            ++yoff;
        }
        while (xlim > xoff && ylim > yoff
                && xvec[xlim - 1] == yvec[ylim - 1]) {
            --xlim;
            --ylim;
        }
        if (xoff == xlim)
            while (yoff < ylim)
                rhs.changed[1 + rhs.realindexes[yoff++]] = true;
        else if (yoff == ylim)
            while (xoff < xlim)
                lhs.changed[1 + lhs.realindexes[xoff++]] = true;
        else {
            int d = diag(xoff, xlim, yoff, ylim);
            int c = cost;
            int f = fdiag[fdiagoff + d];
            int b = bdiag[bdiagoff + d];
            
            if (c == 1) {
                throw new IllegalArgumentException("Empty subsequence");
            } else {
                cmpSequence( xoff, b, yoff, b - d );
                cmpSequence( b, xlim, b - d, ylim );
            }
        }
    }
    
    private void discardIrrelevantData() {
        lhs.discardIrrelevant( rhs, equivCount, noDiscards );
        rhs.discardIrrelevant( lhs, equivCount, noDiscards );
    }
    
    private boolean noShifting = false;
    
    private void shiftBoundaries() {
        if ( noShifting ) {
            return;
        }
        lhs.shiftBoundaries( rhs );
        rhs.shiftBoundaries( lhs );
    }
    
    private ChangeRecord calculateDifferences( final ScriptBuilder bld ) {
        discardIrrelevantData();
        xvec = lhs.undiscarded;
        yvec = rhs.undiscarded;
        
        int diags = lhs.remainingLines + rhs.remainingLines + 3;
        fdiag = new int[diags];
        fdiagoff = rhs.remainingLines + 1;
        bdiag = new int[diags];
        bdiagoff = rhs.remainingLines + 1;
        
        cmpSequence(0, lhs.remainingLines, 0,
                rhs.remainingLines);
        fdiag = null;
        bdiag = null;
        shiftBoundaries();
        return bld.build(lhs.changed,
                lhs.lineCount, rhs.changed,
                rhs.lineCount);
        
    }
    
    private static class InputData {
        private InputData(int[] equivs ) {
            lineCount = equivs.length;
            this.equivs = equivs;
            undiscarded = new int[lineCount];
            realindexes = new int[lineCount];
        }
        private void clear() {
            changed = new boolean[lineCount + 2];
        }
        private int[] equivCount( int equivCount ) {
            int[] countEquiv = new int[equivCount];
            for (int i = 0; i < lineCount; ++i) {
                ++countEquiv[equivs[i]];
            }
            return countEquiv;
        }
        private void discardIrrelevant(InputData f, int equivCount, boolean noDiscards ) {
            clear();
            final byte[] discarded = discardable(f.equivCount( equivCount ));
            filterDiscards(discarded);
            discard(discarded, noDiscards);
        }
        private byte[] discardable(final int[] counts) {
            final int end = lineCount;
            final byte[] discards = new byte[end];
            final int[] equivs = this.equivs;
            int many = 5;
            int tem = end / 64;
            while ((tem = tem >> 2) > 0) {
                many *= 2;
            }
            for (int i = 0; i < end; i++) {
                int nmatch;
                if (equivs[i] == 0) {
                    continue;
                }
                nmatch = counts[equivs[i]];
                if (nmatch == 0) {
                    discards[i] = 1;
                } else if (nmatch > many) {
                    discards[i] = 2;
                }
            }
            return discards;
        }
        private void filterDiscards(final byte[] discards) {
            final int end = lineCount;
            for (int i = 0; i < end; i++) {
                if (discards[i] == 2) {
                    discards[i] = 0;
                } else if (discards[i] != 0) {
                    int j;
                    int length;
                    int provisional = 0;
                    for (j = i; j < end; j++) {
                        if (discards[j] == 0) {
                            break;
                        } if (discards[j] == 2) {
                            ++provisional;
                        }
                    }
                    while (j > i && discards[j - 1] == 2) {
                        discards[--j] = 0;
                        --provisional;
                    }
                    length = j - i;
                    if (provisional * 4 > length) {
                        while (j > i) {
                            if (discards[--j] == 2) {
                                discards[j] = 0;
                            }
                        }
                    } else {
                        int consec;
                        int minimum = 1;
                        int tem = length / 4;
                        while ((tem = tem >> 2) > 0) {
                            minimum *= 2;
                        }
                        minimum++;
                        for (j = 0, consec = 0; j < length; j++) {
                            if (discards[i + j] != 2)
                                consec = 0;
                            else if (minimum == ++consec) {
                                j -= consec;
                            } else if (minimum < consec) {
                                discards[i + j] = 0;
                            }
                        }
                        for (j = 0, consec = 0; j < length; j++) {
                            if (j >= 8 && discards[i + j] == 1) {
                                break;
                            } if (discards[i + j] == 2) {
                                consec = 0;
                                discards[i + j] = 0;
                            } else if (discards[i + j] == 0) {
                                consec = 0;
                            } else {
                                consec++;
                            }
                            if (consec == 3) {
                                break;
                            }
                        }
                        i += length - 1;
                        for (j = 0, consec = 0; j < length; j++) {
                            if (j >= 8 && discards[i - j] == 1) {
                                break;
                            }
                            if (discards[i - j] == 2) {
                                consec = 0;
                                discards[i - j] = 0;
                            } else if (discards[i - j] == 0) {
                                consec = 0;
                            } else {
                                consec++;
                            }
                            if (consec == 3) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        private void discard(final byte[] discards, boolean noDiscards ) {
            final int end = lineCount;
            int j = 0;
            for (int i = 0; i < end; ++i)
                if (noDiscards || discards[i] == 0) {
                    undiscarded[j] = equivs[i];
                    realindexes[j++] = i;
                } else {
                    changed[1 + i] = true;
                }
            remainingLines = j;
        }
        private void shiftBoundaries(InputData f) {
            final boolean[] changedLHS = changed;
            final boolean[] changedRHS = f.changed;
            int i = 0;
            int j = 0;
            int maxI = lineCount;
            int precedingLHS = -1;
            int precedingRHS = -1;
            
            for (;;) {
                int start, end, startRHS;
                while (i < maxI && !changedLHS[1 + i]) {
                    while (changedRHS[1 + j++]) {
                        precedingRHS = j;
                    }
                    i++;
                }
                if (i == maxI) {
                    break;
                }
                start = i;
                startRHS = j;
                
                for (;;) {
                    while (i < maxI && changedLHS[1 + i]) {
                        i++;
                    }
                    end = i;
                    if ( end != maxI
                            && equivs[start] == equivs[end]
                                                       && !changedRHS[1 + j]
                                                                      && end != maxI
                                                                      && !((precedingLHS >= 0 && start == precedingLHS) || (precedingRHS >= 0 && startRHS == precedingRHS))) {
                        changedLHS[1 + end++] = true;
                        changedLHS[1 + start++] = false;
                        ++i;
                        ++j;
                    } else {
                        break;
                    }
                }
                precedingLHS = i;
                precedingRHS = j;
            }
        }
        private final int lineCount;
        private final int[] equivs;
        private final int[] undiscarded;
        private final int[] realindexes;
        private int remainingLines;
        private boolean[] changed;
    }
    public static class ChangeRecord {
        public final ChangeRecord next;
        public final int inserted;
        public final int deleted;
        public final int lineLHS;
        public final int lineRHS;
        public ChangeRecord(
                int lineLHS,
                int lineRHS,
                int deleted,
                int inserted,
                ChangeRecord next
        ) {
            this.lineLHS = lineLHS;
            this.lineRHS = lineRHS;
            this.inserted = inserted;
            this.deleted = deleted;
            this.next = next;
        }
    }
    private interface ScriptBuilder {
        public ChangeRecord build(boolean[] lhsChg, int lhsLen, boolean[] rhsChg, int rhsLen);
    }
    private static final ScriptBuilder LHS_TO_RHS = new ScriptBuilder() {
        public ChangeRecord build(final boolean[] lhsChg, int lhsLen, final boolean[] rhsChg, int rhsLen) {
            ChangeRecord script = null;
            int i0 = lhsLen, i1 = rhsLen;
            
            while (i0 >= 0 || i1 >= 0) {
                if (lhsChg[i0] || rhsChg[i1]) {
                    int line0 = i0, line1 = i1;
                    while (lhsChg[i0])
                        --i0;
                    while (rhsChg[i1])
                        --i1;
                    script = new ChangeRecord(i0, i1, line0 - i0, line1 - i1, script);
                }
                i0--;
                i1--;
            }
            
            return script;
        }
    };
    private static final ScriptBuilder RHS_TO_LHS = new ScriptBuilder() {
        public ChangeRecord build(final boolean[] lhsChg, int lhsLen, final boolean[] rhsChg, int rhsLen) {
            ChangeRecord script = null;
            int i0 = 0, i1 = 0;
            while (i0 < lhsLen || i1 < rhsLen) {
                if (lhsChg[1 + i0] || rhsChg[1 + i1]) {
                    int line0 = i0, line1 = i1;
                    while (lhsChg[1 + i0]) {
                        ++i0;
                    }
                    while (rhsChg[1 + i1]) {
                        ++i1;
                    }
                    script = new ChangeRecord(line0, line1, i0 - line0, i1 - line1, script);
                }
                i0++;
                i1++;
            }
            
            return script;
        }
    };
}
