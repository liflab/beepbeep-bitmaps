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
package ca.uqac.lif.cep.ltl.bitmaps;

import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.BitmapAdapter;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.BitmapIterator;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.Type;

import static ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.createAdapter;

/**
 * Bitmap implementation of the LTL "until" operator. 
 */
public class BitmapU extends BinaryBitmapProcessor
{
  public BitmapU(Type type)
  {
    super(type);
  }

  @Override
  protected BitmapAdapter processBitmap(BitmapAdapter left, BitmapAdapter right)
  {
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
    return answer;
  }
}
