/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * A java aware white space detector.
 * 
 * @author Marc Mazas 2016
 */
class WhitespaceDetector implements IWhitespaceDetector {

  // MMa 10/2012 : renamed
  // MMa 02/2016 : some renamings

  /** {@inheritDoc} */
  @Override
  public boolean isWhitespace(final char aCh) {
    return Character.isWhitespace(aCh);
  }
}
