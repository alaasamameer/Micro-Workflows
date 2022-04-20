/*
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: crawl $'
 * '$Date: 2015-08-24 22:45:41 +0000 (Mon, 24 Aug 2015) $' 
 * '$Revision: 33631 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package test.org.kepler.authentication;

import org.kepler.authentication.AuthenticationManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AuthenticationManagerTest  extends TestCase
{
  
  public AuthenticationManagerTest(String name)
  {
    super(name);
  }


  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp()
  {
  }

  /**
   * Release any objects after tests are complete
   */
  public void tearDown()
  {
  }

  /**
   * Create a suite of tests to be run together
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new AuthenticationManagerTest("initialize"));
    suite.addTest(new AuthenticationManagerTest("getProxyTest"));
    return suite;
  }

  /**
   * Run an initial test that always passes to check that the test
   * harness is working.
   */
  public void initialize()
  {
    assertTrue(1 == 1);
  }
  
  /**
   * test the newLsid method.
   */
  public void getProxyTest()
  {
    try
    {
      AuthenticationManager manager = AuthenticationManager.getManager();
      //ProxyEntity entity = manager.getProxy("SEEK"); 
      //this is commented out because it opens a gui window and breaks the 
      //nightly build
     
    }
    catch(Exception e)
    {
      //e.printStackTrace();
      fail("unexpected exception: " + e.getMessage());
    }
  }
}