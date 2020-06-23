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

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SynchronousProcessor;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap;
import ca.uqac.phoenixxie.ltl.bitmap.LTLBitmap.BitmapAdapter;
import java.util.Queue;

/**
 * Abstract processor implementing a binary bitmap connective or operator.
 */
public abstract class BinaryBitmapProcessor extends SynchronousProcessor
{
  protected LTLBitmap.Type type;
  
  public BinaryBitmapProcessor()
  {
    this(LTLBitmap.Type.RAW);
  }
  
  public BinaryBitmapProcessor(LTLBitmap.Type type)
  {
    super(2, 1);
    this.type = type;
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    BitmapAdapter ba1 = (BitmapAdapter) inputs[0];
    BitmapAdapter ba2 = (BitmapAdapter) inputs[1];
    outputs.add(new Object[] {processBitmap(ba1, ba2)});
    return false;
  }
  
  protected abstract BitmapAdapter processBitmap(BitmapAdapter bitmap1, BitmapAdapter bitmap2);

  @Override
  public Processor duplicate(boolean with_state)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
