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

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.IntSet;

public class ConciseBitmap implements LTLBitmap.BitmapAdapter {
    private ConciseSet bitmap;
    private int size;

    public ConciseBitmap() {
        this(false);
    }

    public ConciseBitmap(boolean simWAH) {
        bitmap = new ConciseSet(simWAH);
        size = 0;
    }

    private ConciseBitmap(ConciseSet bm, int size) {
        this.bitmap = bm;
        this.size = size;
    }

    @Override
    public void add(boolean bit) {
        if (bit) {
            bitmap.add(size);
        }
        ++size;
    }

    @Override
    public void addMany(boolean bit, int count) {
        if (count <= 0) {
            return;
        }

        if (bit) {
            bitmap.addMany(size, count);
        }
        size += count;
    }

    @Override
    public boolean get(int position) {
        return bitmap.contains(position);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int getRealSize() {
        return bitmap.getRealSize();
    }

    @Override
    public String toString() {
        StringBuilder answer = new StringBuilder();
        IntSet.IntIterator i = bitmap.iterator();
        int lastpos = 0;
        while (i.hasNext()) {
            int pos = i.next();
            for (int j = lastpos; j < pos; ++j) {
                answer.append("0");
            }
            answer.append("1");
            lastpos = pos + 1;
        }
        for (int j = lastpos; j < size(); ++j) {
            answer.append("0");
        }
        return answer.toString();
    }

    @Override
    public boolean firstBit() {
        return get(0);
    }

    @Override
    public int cardinality() {
        return bitmap.size();
    }

    @Override
    public int last0() {
        if (size == 0) {
            return -1;
        }
        if (last1() != size - 1) {
            return size - 1;
        }
        return bitmap.last0();
    }

    @Override
    public int last1() {
        if (bitmap.isEmpty()) {
            return -1;
        }
        return bitmap.last();
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        ConciseBitmap bm = new ConciseBitmap(bitmap.complemented(), size);

        int bmSize = 0;
        if (bitmap.isEmpty()) {
            bmSize = 0;
        } else {
            bmSize = bitmap.last() + 1;
        }

        if (bmSize < size) {
            bm.bitmap.fill(bmSize, size - 1);
        }

        return bm;
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        return new ConciseBitmap(bitmap.intersection(((ConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
                );
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        return new ConciseBitmap(bitmap.union(((ConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
        );
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        return new ConciseBitmap(bitmap.symmetricDifference(((ConciseBitmap)bm).bitmap),
                Math.max(size, bm.size())
        );
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        return new ConciseBitmap(bitmap.shiftLeft1Bit(), size - 1);
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        return new ConciseBitmap(bitmap.clone(), size);
    }

    @Override
    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return new Iterator(null, size);
    }

    private class Iterator implements LTLBitmap.BitmapIterator {
        private ConciseSet.LTLIterator itor;
        private int index;
        private boolean isEnd;

        public Iterator() {
            itor = bitmap.getLTLIterator();
            index = 0;
            isEnd = false;
            if (size == 0) {
                isEnd = true;
            }
        }

        private Iterator(ConciseSet.LTLIterator it, int index) {
            this.itor = it;
            this.index = index;
            if (index == size) {
                this.isEnd = true;
            }
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public void moveForward(int offset) {
            index += offset;
            if (index > size) {
                throw new IndexOutOfBoundsException();
            }
            if (index == size) {
                isEnd = true;
                return;
            }

            if (bitmap.isEmpty() || index > bitmap.last()) {
                return;
            }

            itor.moveForward(offset);
            assert index == itor.index();
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (bitmap.isEmpty() || index > bitmap.last()) {
                return new Iterator(null, index);
            }

            ConciseSet.LTLIterator it = itor.find0();
            if (it == null) {
                if (size > bitmap.last() + 1) {
                    return new Iterator(null, bitmap.last() + 1);
                }
                return null;
            }

            return new Iterator(it, it.index());
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (bitmap.isEmpty() || index > bitmap.last()) {
                return null;
            }

            ConciseSet.LTLIterator it = itor.find1();
            if (it == null) {
                return null;
            }

            return new Iterator(it, it.index());
        }

        @Override
        public boolean currentBit() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (bitmap.isEmpty() || index > bitmap.last()) {
                return false;
            }

            return itor.currentBit();
        }

        @Override
        public boolean isEnd() {
            return isEnd;
        }
    }
}
