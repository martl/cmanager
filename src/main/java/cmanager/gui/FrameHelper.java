package cmanager.gui;

import java.util.ArrayList;
import javax.swing.JFrame;

public class FrameHelper {

    private static final ArrayList<JFrame> reactivationQueue = new ArrayList<>();

    private static synchronized void addToQueue(JFrame frame) {
        reactivationQueue.add(frame);
    }

    private static synchronized boolean isFirstInQueue(JFrame frame) {
        return reactivationQueue.get(reactivationQueue.size() - 1) == frame;
    }

    private static synchronized void removeFromQueue(JFrame frame) {
        reactivationQueue.remove(frame);
    }

    public static void showModalFrame(final JFrame newFrame, final JFrame owner) {
        addToQueue(owner);

        newFrame.setLocationRelativeTo(owner);
        owner.setVisible(false);
        owner.setEnabled(false);
        newFrame.setVisible(true);
        newFrame.toFront();

        Thread t =
                new Thread(
                        new Runnable() {
                            public void run() {
                                while (newFrame.isVisible() || !isFirstInQueue(owner)) {
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                    }
                                }

                                owner.setVisible(true);
                                owner.setEnabled(true);
                                owner.toFront();
                                removeFromQueue(owner);
                            }
                        });
        t.start();
    }
}
