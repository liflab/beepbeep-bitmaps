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
 * Collects a stream of Boolean values into a bitmap. The processor outputs
 * the whole bitmap once it is notified that no further input event is
 * available (through {@link #onEndOfTrace(Queue)}.
 */
public class CollectBitmap extends SynchronousProcessor
{
  protected BitmapAdapter m_bitmap;
  
  protected LTLBitmap.Type m_type;
  
  public CollectBitmap(LTLBitmap.Type type)
  {
    super(1, 1);
    m_type = type;
    m_bitmap = LTLBitmap.createAdapter(type);
  }

  @Override
  protected boolean compute(Object[] inputs, Queue<Object[]> outputs)
  {
    boolean b = (Boolean) inputs[0];
    m_bitmap.add(b);
    return true;
  }

  @Override
  public Processor duplicate(boolean with_state)
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  protected boolean onEndOfTrace(Queue<Object[]> outputs)
  {
    outputs.add(new Object[] {m_bitmap});
    return true;
  }

}
