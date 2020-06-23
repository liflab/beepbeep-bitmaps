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

/**
 * A "dummy" main class; it is only there to make the generated
 * JAR runnable from the command line. In such a case, it simply
 * displays a message indicating that the JAR is a library, not
 * intended to be used as a stand-alone program.
 */
public class Main
{
  /**
   * Build string to identify versions
   */
  protected static final String VERSION_STRING = Main.class.getPackage().getImplementationVersion();
  
  /**
   * Main method.
   * @param args Command line arguments
   */
  public static void main(String[] args)
  {
    System.out.println("LTL bitmap palette v" + VERSION_STRING + " - Trend computations for event streams");
    System.out.println("(C) 2016-2020 Laboratoire d'informatique formelle");
    System.out.println("This JAR file is a library that is not meant to be run from the");
    System.out.println("command line.");
    System.exit(0);
  }

  /**
   * Constructor. Should not be accessed.
   */
  private Main()
  {
    throw new IllegalAccessError("Main class");
  }
}