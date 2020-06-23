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
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.Type;

/**
 * Bitmap implementation of logical disjunction.
 */
public class BitmapOr extends BinaryBitmapProcessor
{
  public BitmapOr(Type type)
  {
    super(type);
  }

  @Override
  protected BitmapAdapter processBitmap(BitmapAdapter left, BitmapAdapter right)
  {
    return left.opOr(right);
  }
}
