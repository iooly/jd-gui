/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import groovy.transform.CompileStatic
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Token

import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

@CompileStatic
abstract class HyperlinkPage extends TextPage {
    protected static final Cursor defaultCursor = Cursor.defaultCursor
    protected static final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

    protected TreeMap<Integer, HyperlinkData> hyperlinks = new TreeMap<>()

    HyperlinkPage() {
        def listener = new MouseAdapter() {
            int lastX = -1
            int lastY = -1

            void mouseClicked(MouseEvent e) {
                if (e.clickCount > 0) {
                    int offset = textArea.viewToModel(new Point(e.x, e.y))
                    if (offset != -1) {
                        def entry = hyperlinks.floorEntry(offset)
                        if (entry) {
                            def entryData = entry.value
                            if (entryData && (offset < entryData.endPosition) && (offset >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                                openHyperlink(e.getXOnScreen(), e.getYOnScreen(), entryData)
                            }
                        }
                    }
                }
            }

            void mouseMoved(MouseEvent e) {
                if ((e.x != lastX) || (e.y != lastY)) {
                    lastX = e.x
                    lastY = e.y

                    int offset = textArea.viewToModel(new Point(e.x, e.y))
                    if (offset != -1) {
                        def entry = hyperlinks.floorEntry(offset)
                        if (entry) {
                            def entryData = entry.value
                            if (entryData && (offset < entryData.endPosition) && (offset >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                                textArea.cursor = handCursor
                                return
                            }
                        }
                    }

                    textArea.cursor = defaultCursor
                }
            }

            void mouseWheelMoved(MouseWheelEvent e) {
                mouseMoved(e)
                textArea.parent.dispatchEvent(e)
            }
        }

        textArea.addMouseListener(listener)
        textArea.addMouseMotionListener(listener)
        textArea.addMouseWheelListener(listener)
    }

    protected RSyntaxTextArea newRSyntaxTextArea() { new HyperlinkSyntaxTextArea() }

    void addHyperlink(HyperlinkData hyperlinkData) {
        hyperlinks.put(hyperlinkData.startPosition, hyperlinkData)
    }

    void clearHyperlinks() {
        hyperlinks.clear()
    }

    protected abstract boolean isHyperlinkEnabled(HyperlinkData hyperlinkData)

    protected abstract void openHyperlink(int x, int y, HyperlinkData hyperlinkData)

    static class HyperlinkData {
        int startPosition
        int endPosition

        HyperlinkData(int startPosition, int endPosition) {
            this.startPosition = startPosition
            this.endPosition = endPosition
        }
    }

    class HyperlinkSyntaxTextArea extends RSyntaxTextArea {
        boolean getUnderlineForToken(Token t) {
            def entry = hyperlinks.floorEntry(t.offset)
            if (entry) {
                def entryData = entry.value
                if (entryData && (t.offset < entryData.endPosition) && (t.offset >= entryData.startPosition) && isHyperlinkEnabled(entryData)) {
                    return true
                }
            }
            return super.getUnderlineForToken(t)
        }
    }
}
