/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.ui;

import javax.swing.SwingUtilities;

import nl.esciencecenter.ptk.util.logging.PLogger;

public class UIGlobal {

    private static PLogger uiLogger;

    static {
        uiLogger = PLogger.getLogger(UIGlobal.class);
        uiLogger.debugPrintf(">>> UIGlobal.init() <<<\n");

        try {
            uiLogger = PLogger.getLogger("UIGlobal");

        } catch (Exception e) {
            uiLogger.logException(PLogger.FATAL, e, "Exception during initialization!");
        }
    }

    public static void init() {
    }

    public static void assertNotGuiThread(String msg) throws Error {
        assertGuiThread(false, msg);
    }

    public static void assertGuiThread(String msg) throws Error {
        assertGuiThread(true, msg);
    }

    public static void assertGuiThread(boolean mustBeGuiThread, String msg) throws Error {
        // still happens when trying to read/acces link targets of linknodes 
        if (mustBeGuiThread != UIGlobal.isGuiThread()) {
            uiLogger.infoPrintf("\n>>>\n    *** Swing GUI Event Assertion Error *** !!!\n>>>\n");
            throw new Error("Internal Error. Cannot perform this "
                    + (mustBeGuiThread ? "during" : "outside")
                    + "during the Swing GUI Event thread.\n" + msg);
        }
    }

    public static void swingInvokeLater(Runnable task) {
        SwingUtilities.invokeLater(task);
    }

    public static boolean isGuiThread() {
        return (SwingUtilities.isEventDispatchThread() == true);
    }

    public static boolean isApplet() {
        return false;
    }

}
