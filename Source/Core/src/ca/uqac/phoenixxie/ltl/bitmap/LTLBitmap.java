/*
  LTL bitmap palette for BeepBeep
  Copyright (C) 2016-2020 Kun Xie and Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.phoenixxie.ltl.bitmap;

import java.security.InvalidParameterException;

public class LTLBitmap {
    private final Type type;
    private final BitmapAdapter bitmap;

    public LTLBitmap(Type type) {
        this.type = type;
        this.bitmap = createAdapter(type);
    }

    public LTLBitmap(Type type, String init) {
        this(type);
        add(init);
    }

    private LTLBitmap(Type type, BitmapAdapter bm) {
        this.type = type;
        this.bitmap = bm;
    }

    public static BitmapAdapter createAdapter(Type type) {
        switch (type) {
            case RAW:
                return new RawBitmap();
            case EWAH:
                return new EWAH64Bitmap();
            case EWAH32:
                return new EWAH32Bitmap();
            case ROARING:
                return new RoaringBitmap();
            case CONCISE:
                return new ConciseBitmap();
            case WAHCONCISE:
                return new ConciseBitmap(true);
        }
        throw new InvalidParameterException();
    }

    public void add(boolean bit) {
        bitmap.add(bit);
    }

    public void add(String in) {
        for (char c : in.toCharArray()) {
            if (c == '0') {
                add(false);
            } else if (c == '1') {
                add(true);
            }
        }
    }

    @Override
    public String toString() {
        return bitmap.toString();
    }

    public int sizeInBits() {
        return bitmap.size();
    }

    public int sizeInRealBytes() {
        return bitmap.getRealSize();
    }

    public int cardinality() {
        return bitmap.cardinality();
    }

    public LTLBitmap opNot() {
        return new LTLBitmap(type, bitmap.opNot());
    }

    public LTLBitmap opAnd(LTLBitmap bm) {
        return new LTLBitmap(type, bitmap.opAnd(bm.bitmap));
    }

    public LTLBitmap opOr(LTLBitmap bm) {
        return new LTLBitmap(type, bitmap.opOr(bm.bitmap));
    }

    public LTLBitmap opThen(LTLBitmap bm) {
        BitmapAdapter left = bitmap.opNot();
        return new LTLBitmap(type, left.opOr(bm.bitmap));
    }

    public LTLBitmap opNext() {
        return new LTLBitmap(type, bitmap.removeFirstBit());
    }

    public LTLBitmap opGlobal() {
        int last0 = bitmap.last0();
        if (last0 == -1) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(false, last0 + 1);
        newBm.addMany(true, bitmap.size() - last0 - 1);
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opFuture() {
        int last1 = bitmap.last1();
        if (last1 == -1) {
            return new LTLBitmap(type, bitmap.clone());
        }

        BitmapAdapter newBm = createAdapter(type);
        newBm.addMany(true, last1 + 1);
        newBm.addMany(false, bitmap.size() - last1 - 1);
        return new LTLBitmap(type, newBm);
    }

    public LTLBitmap opUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }

        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        if (left.size() > right.size()) {
            right.addMany(false, left.size() - right.size());
        } else if (left.size() < right.size()) {
            left.addMany(false, right.size() - left.size());
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
        BitmapAdapter answer = createAdapter(type);

        int pos = 0;
        BitmapIterator ita1 = ita, ita0 = ita;
        BitmapIterator itb1 = itb, itb0 = itb;
        while (!ita.isEnd() && !itb.isEnd()) {
            if (ita.index() >= ita1.index()) {
                ita1 = ita.find1();
            }
            if (ita1 == null) {
                ita = null;
            }
            if (itb.index() >= itb1.index()) {
                itb1 = itb.find1();
            }
            if (itb1 == null) {
                itb = null;
            }
            if (ita == null || itb == null) {
                break;
            }

            int near1 = Math.min(ita1.index(), itb1.index());
            if (near1 > pos) {
                int off = near1 - pos;
                answer.addMany(false, off);
                if (near1 == ita1.index()) {
                    ita = ita1;
                } else {
                    ita.moveForward(off);
                }
                if (near1 == itb1.index()) {
                    itb = itb1;
                } else {
                    itb.moveForward(off);
                }

                pos = near1;

                continue;
            }

            if (itb1.index() == pos) {
                if (itb0.index() <= itb1.index()) {
                    itb0 = itb1.find0();
                }
                if (itb0 == null) {
                    itb0 = right.end();
                }

                int off = itb0.index() - pos;
                answer.addMany(true, off);
                ita.moveForward(off);
                itb = itb0;
                pos = itb0.index();
                continue;
            }

            if (ita0.index() <= ita1.index()) {
                ita0 = ita1.find0();
            }
            if (ita0 == null) {
                ita0 = left.end();
            }

            if (ita0.index() >= itb1.index()) {
                int off = itb1.index() - pos + 1;
                answer.addMany(true, off);
                ita.moveForward(off);
                itb.moveForward(off);
                pos += off;
            } else {
                int off = ita0.index() - pos + 1;
                answer.addMany(false, off);
                ita.moveForward(off);
                itb.moveForward(off);
                pos += off;
            }
        }

        if (itb == null) {
            answer.addMany(false, left.size() - answer.size());
        } else if (ita == null) {
            pos = itb.index();
            while (!itb.isEnd()) {
                if (itb1.index() <= itb.index()) {
                    itb1 = itb.find1();
                }
                if (itb1 == null) {
                    answer.addMany(false, left.size() - answer.size());
                    break;
                }
                if (itb1.index() > pos) {
                    int off = itb1.index() - pos;
                    answer.addMany(false, off);
                    pos = itb1.index();
                    itb = itb1;
                    continue;
                }

                if (itb0.index() <= itb1.index()) {
                    itb0 = itb1.find0();
                }
                if (itb0 == null) {
                    answer.addMany(true, left.size() - answer.size());
                    break;
                }
                if (itb0.index() > pos) {
                    int off = itb0.index() - pos;
                    answer.addMany(true, off);
                    pos = itb0.index();
                    itb = itb0;
                }
            }
        }

        assert answer.size() == left.size();
        return new LTLBitmap(type, answer);
    }

    public LTLBitmap opWeakUntil(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }

        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        if (left.size() > right.size()) {
            right.addMany(false, left.size() - right.size());
        } else if (left.size() < right.size()) {
            left.addMany(false, right.size() - left.size());
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
        BitmapAdapter answer = createAdapter(type);

        int pos = 0;
        BitmapIterator ita1 = ita, ita0 = ita;
        BitmapIterator itb1 = itb, itb0 = itb;
        while (!ita.isEnd() && !itb.isEnd()) {
            if (ita1.index() <= ita.index()) {
                ita1 = ita.find1();
            }
            if (ita1 == null) {
                ita = null;
            }
            if (itb1.index() <= itb.index()) {
                itb1 = itb.find1();
            }
            if (itb1 == null) {
                itb = null;
            }
            if (ita == null || itb == null) {
                break;
            }

            int near1 = Math.min(ita1.index(), itb1.index());
            if (near1 > pos) {
                int off = near1 - pos;
                answer.addMany(false, off);
                if (near1 == ita1.index()) {
                    ita = ita1;
                } else {
                    ita.moveForward(off);
                }
                if (near1 == itb1.index()) {
                    itb = itb1;
                } else {
                    itb.moveForward(off);
                }
                pos = near1;
                continue;
            }

            if (itb1.index() == pos) {
                if (itb0.index() <= itb1.index()) {
                    itb0 = itb1.find0();
                }
                if (itb0 == null) {
                    itb0 = right.end();
                }

                int off = itb0.index() - pos;
                answer.addMany(true, off);
                ita.moveForward(off);
                itb = itb0;
                pos = itb0.index();
                continue;
            }

            if (ita0.index() <= ita1.index()) {
                ita0 = ita1.find0();
            }
            if (ita0 == null) {
                ita0 = left.end();
            }

            if (ita0.index() >= itb1.index()) {
                int off = itb1.index() - pos + 1;
                answer.addMany(true, off);
                ita.moveForward(off);
                itb.moveForward(off);
                pos += off;
            } else {
                int off = ita0.index() - pos + 1;
                answer.addMany(false, off);
                ita.moveForward(off);
                itb.moveForward(off);
                pos += off;
            }
        }

        if (itb == null) {
            if (ita == null) {
                answer.addMany(false, left.size() - answer.size());
            } else {
                int last0 = left.last0();
                if (last0 == -1 || last0 < ita.index()) {
                    answer.addMany(true, left.size() - answer.size());
                } else {
                    answer.addMany(false, last0 - ita.index() + 1);
                    answer.addMany(true, left.size() - answer.size());
                }
            }
        } else if (ita == null) {
            pos = itb.index();
            while (!itb.isEnd()) {
                if (itb1.index() <= itb.index()) {
                    itb1 = itb.find1();
                }
                if (itb1 == null) {
                    answer.addMany(false, left.size() - answer.size());
                    break;
                }
                if (itb1.index() > pos) {
                    int off = itb1.index() - pos;
                    answer.addMany(false, off);
                    pos = itb1.index();
                    itb = itb1;
                    continue;
                }

                if (itb0.index() <= itb1.index()) {
                    itb0 = itb1.find0();
                }
                if (itb0 == null) {
                    answer.addMany(true, left.size() - answer.size());
                    break;
                }
                if (itb0.index() > pos) {
                    int off = itb0.index() - pos;
                    answer.addMany(true, off);
                    pos = itb0.index();
                    itb = itb0;
                }
            }
        }

        assert answer.size() == left.size();
        return new LTLBitmap(type, answer);
    }

    public LTLBitmap opRelease(LTLBitmap rightBm) {
        if (type != rightBm.type) {
            throw new InvalidParameterException();
        }

        BitmapAdapter left = this.bitmap;
        BitmapAdapter right = rightBm.bitmap;
        if (left.size() > right.size()) {
            right.addMany(false, left.size() - right.size());
        } else if (left.size() < right.size()) {
            left.addMany(false, right.size() - left.size());
        }

        BitmapIterator ita = left.begin();
        BitmapIterator itb = right.begin();
        BitmapAdapter answer = createAdapter(type);

        int pos = 0;
        BitmapIterator ita1 = ita, ita0 = ita;
        BitmapIterator itb1 = itb, itb0 = itb;

        while (!ita.isEnd() && !itb.isEnd()) {
            int off;

            if (itb1.index() <= itb.index()) {
                itb1 = itb.find1();
            }
            if (itb1 == null) {
                itb = null;
                break;
            }

            if (itb1.index() > pos) {
                off = itb1.index() - pos;
                answer.addMany(false, off);
                ita.moveForward(off);
                itb = itb1;
                pos = itb1.index();
                continue;
            }

            if (ita1.index() <= ita.index()) {
                ita1 = ita.find1();
            }
            if (ita1 == null) {
                ita = null;
                break;
            }

            if (itb0.index() <= itb1.index()) {
                itb0 = itb1.find0();
            }
            if (itb0 == null) {
                itb0 = right.end();
            }

            if (ita1.index() >= itb0.index()) {
                off = itb0.index() - pos + 1;
                answer.addMany(false, off);
                ita.moveForward(off);
                itb.moveForward(off);
                pos += off;
                continue;
            }

            if (ita0.index() <= ita1.index()) {
                ita0 = ita1.find0();
            }
            if (ita0 == null) {
                ita0 = left.end();
            }

            int near0 = Math.min(ita0.index(), itb0.index());
            off = near0 - pos;
            answer.addMany(true, off);
            if (near0 == ita0.index()) {
                ita = ita0;
            } else {
                ita.moveForward(off);
            }
            if (near0 == itb0.index()) {
                itb = itb0;
            } else {
                itb.moveForward(off);
            }
            pos = near0;
        }

        if (ita == null && itb != null) {
            int last0 = right.last0();
            if (last0 == -1 || last0 < itb.index()) {
                answer.addMany(true, right.size() - answer.size());
            } else {
                answer.addMany(false, last0 - itb.index() + 1);
                answer.addMany(true, right.size() - answer.size());
            }
        } else {
            answer.addMany(false, right.size() - answer.size());
        }

        assert answer.size() == left.size();
        return new LTLBitmap(type, answer);
    }

    public enum Type {
        RAW("Raw"),
        CONCISE("Concise"),
        WAHCONCISE("WAH"),
        EWAH("EWAH(64bit)"),
        EWAH32("EWAH(32bit)"),
        ROARING("Roaring bitmap");

        private final String name;

        private Type(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }
    }

    public interface BitmapIterator {
        int index();

        void moveForward(int offset);

        BitmapIterator find0();

        BitmapIterator find1();

        boolean currentBit();

        boolean isEnd();
    }

    public interface BitmapAdapter extends Cloneable {
        void add(boolean bit);

        void addMany(boolean bit, int count);

        boolean get(int position);

        int size();

        int getRealSize();

        boolean firstBit();

        int cardinality();

        int last0();

        int last1();

        BitmapAdapter opNot();

        BitmapAdapter opAnd(BitmapAdapter bm);

        BitmapAdapter opOr(BitmapAdapter bm);

        BitmapAdapter opXor(BitmapAdapter bm);

        BitmapAdapter removeFirstBit();

        BitmapAdapter clone();

        BitmapIterator begin();

        BitmapIterator end();

        String toString();
    }
}
