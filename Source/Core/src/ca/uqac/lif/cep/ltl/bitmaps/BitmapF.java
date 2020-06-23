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
import static ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.createAdapter;

/**
 * Bitmap implementation of the LTL "future" operator. 
 */
public class BitmapF extends UnaryBitmapProcessor
{
  public BitmapF(Type type)
  {
    super(type);
  }

  @Override
  protected BitmapAdapter processBitmap(BitmapAdapter bitmap)
  {
    int last1 = bitmap.last1();
    if (last1 == -1) {
        return bitmap.clone();
    }
    BitmapAdapter newBm = createAdapter(type);
    newBm.addMany(true, last1 + 1);
    newBm.addMany(false, bitmap.size() - last1 - 1);
    return newBm;
  }
}
