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
import java.util.BitSet;

public class RawBitmap implements LTLBitmap.BitmapAdapter {
    private BitSet bitset = new BitSet();

    private int size = 0;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; ++i) {
            sb.append(bitset.get(i) ? "1" : "0");
        }

        return sb.toString();
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        RawBitmap bm = new RawBitmap();
        bm.bitset = (BitSet) this.bitset.clone();
        bm.size = this.size;
        return bm;
    }

    @Override
    public boolean firstBit() {
        return bitset.get(0);
    }

    @Override
    public int cardinality() {
        int card = 0;
        for (int i = 0; i < size; ++i) {
            if (bitset.get(i)) {
                ++card;
            }
        }
        return card;
    }

    @Override
    public int last0() {
        for (int i = size - 1; i >= 0; --i) {
            if (bitset.get(i) == false) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int last1() {
        for (int i = size - 1; i >= 0; --i) {
            if (bitset.get(i) == true) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean get(int index) {
        if (index >= size || index < 0) {
            throw new InvalidParameterException();
        }
        return bitset.get(index);
    }

    @Override
    public void add(boolean bit) {
        if (bit) {
            bitset.set(size);
        } else {
            bitset.clear(size);
        }
        ++size;
    }

    @Override
    public void addMany(boolean bit, int count) {
        for (int i = 0; i < count; ++i) {
            add(bit);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getRealSize() {
        return size / 8 + ((size % 8) == 0 ? 0 : 1);
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        RawBitmap bm = (RawBitmap) clone();
        bm.bitset.flip(0, size);
        return bm;
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        RawBitmap left = (RawBitmap) clone();
        RawBitmap right = (RawBitmap) bm;
        left.bitset.and(right.bitset);
        left.size = Math.max(left.size, right.size);
        return left;
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        RawBitmap left = (RawBitmap) clone();
        RawBitmap right = (RawBitmap) bm;
        left.bitset.or(right.bitset);
        left.size = Math.max(left.size, right.size);
        return left;
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        RawBitmap left = (RawBitmap) clone();
        RawBitmap right = (RawBitmap) bm;
        left.bitset.xor(right.bitset);
        left.size = Math.max(left.size, right.size);
        return left;
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        RawBitmap bm = (RawBitmap) clone();
        bm.bitset = bm.bitset.get(1, bm.size);
        --bm.size;
        return bm;
    }

    @Override
    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    public LTLBitmap.BitmapIterator end() {
        return new Iterator(size);
    }

    class Iterator implements LTLBitmap.BitmapIterator {
        private int index;

        public Iterator() {
            this(0);
        }

        public Iterator(int index) {
            if (index > size || index < 0) {
                throw new InvalidParameterException();
            }
            this.index = index;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void moveForward(int offset) {
            if (offset < 0) {
                throw new InvalidParameterException();
            }
            if (index + offset > size) {
                throw new InvalidParameterException();
            }
            index += offset;
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            for (int i = index; i < size; ++i) {
                if (bitset.get(i) == false) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            int i = index;
            if (i == size) {
                --i;
            }
            for (; i < size; ++i) {
                if (bitset.get(i) == true) {
                    return new Iterator(i);
                }
            }
            return null;
        }

        @Override
        public boolean currentBit() {
            if (index == size) {
                throw new IndexOutOfBoundsException();
            }
            return bitset.get(index);
        }

        @Override
        public boolean isEnd() {
            return index == size;
        }
    }
}
