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

import com.googlecode.javaewah32.Buffer32;
import com.googlecode.javaewah32.EWAHCompressedBitmap32;
import com.googlecode.javaewah.IntIterator;
import com.googlecode.javaewah32.RunningLengthWord32;

public class EWAH32Bitmap implements LTLBitmap.BitmapAdapter {
    private EWAHCompressedBitmap32 bitmap;

    public EWAH32Bitmap() {
        bitmap = new EWAHCompressedBitmap32();
    }

    private EWAH32Bitmap(EWAHCompressedBitmap32 bm) {
        bitmap = bm;
    }

    @Override
    public void add(boolean bit) {
        if (bit) {
            bitmap.set(bitmap.sizeInBits());
        } else {
            bitmap.setSizeInBits(bitmap.sizeInBits() + 1, false);
        }
    }

    @Override
    public void addMany(boolean bit, int count) {
        if (count <= 0) {
            return;
        }
        int left = EWAHCompressedBitmap32.WORD_IN_BITS - (bitmap.sizeInBits() % EWAHCompressedBitmap32.WORD_IN_BITS);
        for (int i = 0; i < left && i < count; ++i) {
            add(bit);
        }
        count -= left;
        if (count <= 0) {
            return;
        }

        int fullwords = count / EWAHCompressedBitmap32.WORD_IN_BITS;
        left = count % EWAHCompressedBitmap32.WORD_IN_BITS;
        bitmap.addStreamOfEmptyWords(bit, fullwords);
        for (int i = 0; i < left; ++i) {
            add(bit);
        }
    }

    @Override
    public int size() {
        return bitmap.sizeInBits();
    }

    @Override
    public int getRealSize() {
        return bitmap.sizeInBytes();
    }

    @Override
    public boolean firstBit() {
        return bitmap.get(0);
    }

    @Override
    public int cardinality() {
        return bitmap.cardinality();
    }

    @Override
    public int last0() {
        return bitmap.findLast0();
    }

    @Override
    public int last1() {
        return bitmap.findLast1();
    }

    @Override
    public boolean get(int index) {
        if (index >= size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return bitmap.get(index);
    }

    @Override
    public LTLBitmap.BitmapAdapter opNot() {
        try {
            EWAHCompressedBitmap32 bm = bitmap.clone();
            bm.not();
            return new EWAH32Bitmap(bm);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LTLBitmap.BitmapAdapter opAnd(LTLBitmap.BitmapAdapter bm) {
        return new EWAH32Bitmap(bitmap.and(((EWAH32Bitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter opOr(LTLBitmap.BitmapAdapter bm) {
        return new EWAH32Bitmap(bitmap.or(((EWAH32Bitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter opXor(LTLBitmap.BitmapAdapter bm) {
        return new EWAH32Bitmap(bitmap.xor(((EWAH32Bitmap) bm).bitmap));
    }

    @Override
    public LTLBitmap.BitmapAdapter removeFirstBit() {
        return new EWAH32Bitmap(bitmap.removeFirstBit());
    }

    @Override
    public LTLBitmap.BitmapAdapter clone() {
        try {
            EWAHCompressedBitmap32 bm = bitmap.clone();
            return new EWAH32Bitmap(bm);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder answer = new StringBuilder();
        IntIterator i = bitmap.intIterator();
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
    public LTLBitmap.BitmapIterator begin() {
        return new Iterator();
    }

    @Override
    public LTLBitmap.BitmapIterator end() {
        return new Iterator('X');
    }

    public class Iterator implements LTLBitmap.BitmapIterator {
        private final Buffer32 buffer = bitmap.getBuffer();
        private final int sizeInWords = buffer.sizeInWords();

        private int index = 0;

        private boolean inClean = true;
        private int pointerInWords = 0;
        private int currMarkerStartPos = 0;
        private boolean isEnd = false;

        private int currLiteralWord;
        private int pointerInDirtyWord = 0;
        private int pointerDirtyWord = 0;
        @SuppressWarnings("unused")
		private int pointerInRunningLength = 0;

        private int currRunningLength;
        private boolean currRunningBit;
        private int currNumberOfDirtyWords;
        private int currMarkerMaxBits;
        private int currRunningLengthMaxBits;
        @SuppressWarnings("unused")
        private int currDirtyWordsMaxBits;

        public Iterator() {
            if (bitmap.sizeInBits() == 0) {
                isEnd = true;
                return;
            }

            updateMarkerInfo();
            updatePointerInThisMarker(0);
        }

        Iterator(char ignore) {
            isEnd = true;
            index = bitmap.sizeInBits();
        }

        private Iterator(int index, int pointerInWords, int currMarkerStartPos) {
            if (bitmap.sizeInBits() == 0) {
                isEnd = true;
                return;
            }

            this.index = index;
            this.pointerInWords = pointerInWords;
            this.currMarkerStartPos = currMarkerStartPos;

            updateMarkerInfo();
            updatePointerInThisMarker(this.index - currMarkerStartPos);
        }

        @Override
        public int index() {
            return index;
        }

        private void updateMarkerInfo() {
            currRunningLength = RunningLengthWord32.getRunningLength(this.buffer, pointerInWords);
            currRunningBit = RunningLengthWord32.getRunningBit(this.buffer, pointerInWords);
            currNumberOfDirtyWords = RunningLengthWord32.getNumberOfLiteralWords(this.buffer, pointerInWords);
            currMarkerMaxBits = EWAHCompressedBitmap32.WORD_IN_BITS * ((int) currRunningLength + currNumberOfDirtyWords);
            currRunningLengthMaxBits = EWAHCompressedBitmap32.WORD_IN_BITS * (int) currRunningLength;
            currDirtyWordsMaxBits = EWAHCompressedBitmap32.WORD_IN_BITS * currNumberOfDirtyWords;

            assert currRunningLength > 0 || currNumberOfDirtyWords > 0;

        }

        private void updatePointerInThisMarker(int pos) {
            if (pos >= currMarkerMaxBits) {
                throw new IndexOutOfBoundsException();
            }

            if (pos < currRunningLengthMaxBits) {
                inClean = true;
                pointerInRunningLength = pos;
            } else {
                pos -= currRunningLengthMaxBits;
                inClean = false;
                pointerDirtyWord = pos / EWAHCompressedBitmap32.WORD_IN_BITS;
                currLiteralWord = buffer.getWord(pointerInWords + 1 + pointerDirtyWord);
                pointerInDirtyWord = pos % EWAHCompressedBitmap32.WORD_IN_BITS;
            }
        }

        private void moveForwardOneMarker() {
            if (pointerInWords >= sizeInWords) {
                throw new IndexOutOfBoundsException();
            }

            pointerInWords += 1 + currNumberOfDirtyWords;
            currMarkerStartPos += currMarkerMaxBits;

            if (pointerInWords == sizeInWords) {
                isEnd = true;
                index = currMarkerStartPos;
                return;
            }

            updateMarkerInfo();
        }

        @Override
        public void moveForward(int offset) {
            if (offset <= 0) {
                return;
            }

            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset > bitmap.sizeInBits()) {
                throw new IndexOutOfBoundsException();
            }

            if (index + offset == bitmap.sizeInBits()) {
                index = bitmap.sizeInBits();
                isEnd = true;
                return;
            }

            index += offset;
            while (currMarkerStartPos + currMarkerMaxBits <= index) {
                moveForwardOneMarker();
            }
            updatePointerInThisMarker(index - currMarkerStartPos);
        }

        @Override
        public LTLBitmap.BitmapIterator find0() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            final int full = ~0;

            Iterator itor = new Iterator(index, pointerInWords, currMarkerStartPos);
            while (itor.pointerInWords < itor.sizeInWords) {
                if (itor.inClean) {
                    if (itor.currRunningBit == false) {
                        return itor;
                    }

                    // switch to dirty words
                    if (itor.currNumberOfDirtyWords > 0) {
                        itor.updatePointerInThisMarker(itor.currRunningLengthMaxBits);
                    }
                }

                int startbits = itor.currMarkerStartPos + itor.currRunningLengthMaxBits + itor.pointerDirtyWord * EWAHCompressedBitmap32.WORD_IN_BITS;
                for (int i = itor.pointerDirtyWord;
                     i < itor.currNumberOfDirtyWords;
                     ++i, itor.pointerInDirtyWord = 0, startbits += EWAHCompressedBitmap32.WORD_IN_BITS
                        ) {

                    int w = itor.buffer.getWord(itor.pointerInWords + 1 + i);
                    int leftbits = Math.min(EWAHCompressedBitmap32.WORD_IN_BITS, bitmap.sizeInBits() - startbits);
                    int mask = full;
                    if (leftbits != EWAHCompressedBitmap32.WORD_IN_BITS) {
                        mask = (1 << leftbits) - 1;
                    }
                    mask = (mask >>> itor.pointerInDirtyWord) << itor.pointerInDirtyWord;
                    if ((w & mask) == mask) {
                        continue;
                    }

                    mask = 1 << itor.pointerInDirtyWord;
                    for (int j = itor.pointerInDirtyWord; j < leftbits; ++j) {
                        if ((w & mask) == 0) {
                            itor.index = startbits + j;
                            itor.updatePointerInThisMarker(itor.index - itor.currMarkerStartPos);
                            return itor;
                        }
                        mask <<= 1L;
                    }
                }
                itor.moveForwardOneMarker();
                if (itor.isEnd) {
                    return null;
                }
                itor.index = itor.currMarkerStartPos;
                itor.updatePointerInThisMarker(0);
            }

            return null;
        }

        @Override
        public LTLBitmap.BitmapIterator find1() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            Iterator itor = new Iterator(index, pointerInWords, currMarkerStartPos);
            while (itor.pointerInWords < itor.sizeInWords) {
                if (itor.inClean) {
                    if (itor.currRunningBit == true) {
                        return itor;
                    }
                    // switch to dirty words
                    if (itor.currNumberOfDirtyWords > 0) {
                        itor.updatePointerInThisMarker(itor.currRunningLengthMaxBits);
                    }
                }

                int startbits = itor.currMarkerStartPos + itor.currRunningLengthMaxBits + itor.pointerDirtyWord * EWAHCompressedBitmap32.WORD_IN_BITS;
                for (int i = itor.pointerDirtyWord;
                     i < itor.currNumberOfDirtyWords;
                     ++i, itor.pointerInDirtyWord = 0, startbits += EWAHCompressedBitmap32.WORD_IN_BITS
                        ) {

                    int w = itor.buffer.getWord(itor.pointerInWords + 1 + i);
                    int leftbits = Math.min(EWAHCompressedBitmap32.WORD_IN_BITS, bitmap.sizeInBits() - startbits);

                    int mask = 0;
                    if (leftbits != EWAHCompressedBitmap32.WORD_IN_BITS) {
                        mask = ~((1 << leftbits) - 1);
                    }
                    if (itor.pointerInDirtyWord > 0) {
                        mask |= (1 << itor.pointerInDirtyWord) - 1;
                    }
                    if ((w | mask) == mask) {
                        continue;
                    }

                    mask = 1 << itor.pointerInDirtyWord;
                    for (int j = itor.pointerInDirtyWord; j < leftbits; ++j) {
                        if ((w & mask) == mask) {
                            itor.index = startbits + j;
                            itor.updatePointerInThisMarker(itor.index - itor.currMarkerStartPos);
                            return itor;
                        }
                        mask <<= 1;
                    }
                }
                itor.moveForwardOneMarker();
                if (itor.isEnd) {
                    return null;
                }
                itor.index = itor.currMarkerStartPos;
                itor.updatePointerInThisMarker(0);
            }

            return null;
        }

        @Override
        public boolean currentBit() {
            if (isEnd) {
                throw new IndexOutOfBoundsException();
            }

            if (inClean) {
                return currRunningBit;
            } else {
                int mask = 1 << pointerInDirtyWord;
                return ((mask & currLiteralWord) == mask);
            }
        }

        @Override
        public boolean isEnd() {
            return isEnd;
        }
    }
}
